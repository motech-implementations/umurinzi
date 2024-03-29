package org.motechproject.umurinzi.helper;

import static org.motechproject.umurinzi.constants.UmurinziConstants.SIMPLE_DATE_FORMATTER;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.motechproject.umurinzi.domain.Config;
import org.motechproject.umurinzi.domain.Subject;
import org.motechproject.umurinzi.dto.ZetesSubjectDto;
import org.motechproject.umurinzi.exception.IvrException;
import org.motechproject.umurinzi.exception.UmurinziReportException;
import org.motechproject.umurinzi.mapper.ZetesMapper;
import org.motechproject.umurinzi.service.ConfigService;
import org.motechproject.umurinzi.service.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ZetesHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(ZetesHelper.class);

  private static final ZetesMapper MAPPER = ZetesMapper.INSTANCE;

  private static final String SQL_QUERY = "SELECT subject.\"SUBJECT_ID\", subject.\"PHONE\", subject.\"CITY_FIRST_VACCINATED\", "
      + "to_date(prime.\"DATE_DOSING\", 'dd-MON-yy') AS \"PRIME_VAC_DATE\", "
      + "to_date(boost.\"DATE_DOSING\", 'dd-MON-yy') AS \"BOOST_VAC_DATE\" "
      + "FROM \"DEMO_UMURINZI\" subject "
      + "LEFT JOIN \"DOSING_UMURINZI\" prime ON subject.\"SUBJECT_ID\" = prime.\"SUBJECT_ID\" AND prime.\"VISIT_PURPOSE\" = 'DOSE 1 VISIT' "
      + "LEFT JOIN \"DOSING_UMURINZI\" boost ON subject.\"SUBJECT_ID\" = boost.\"SUBJECT_ID\" AND boost.\"VISIT_PURPOSE\" = 'DOSE 1 & 2 VISIT' "
      + "GROUP BY subject.\"SUBJECT_ID\", \"PHONE\", \"CITY_FIRST_VACCINATED\", \"PRIME_VAC_DATE\", \"BOOST_VAC_DATE\", subject.\"REFRESH_DATE\", prime.\"REFRESH_DATE\", boost.\"REFRESH_DATE\" ";

  private static final String TRANSFER_SQL_QUERY = "SELECT transfer.\"SUBJECT_ID\", transfer.\"SUBJECT_ID_EBL3010\" "
      + "FROM \"UMURINZI_TRANSFER_TO_EBL3010\" transfer";

  private static final String SUBJECT_ID_COLUMN = "SUBJECT_ID";
  private static final String PHONE_COLUMN = "PHONE";
  private static final String CITY_FIRST_VACCINATED = "CITY_FIRST_VACCINATED";
  private static final String PRIME_VAC_DATE_COLUMN = "PRIME_VAC_DATE";
  private static final String BOOST_VAC_DATE_COLUMN = "BOOST_VAC_DATE";
  private static final String SUBJECT_TRANSFER_ID_COLUMN = "SUBJECT_ID_EBL3010";

  private static final String WELCOME_MESSAGE = "welcome-message";
  private static final String TRANSFER_MESSAGE = "transfer-message";

  private static final int MAX_NUMBER_OF_ATTEMPTS = 3;
  private static final int MAX_SUBJECTS_IN_CALL = 50;

  @Autowired
  private SubjectService subjectService;

  @Autowired
  private ConfigService configService;

  @Autowired
  private IvrCallHelper ivrCallHelper;

  public void fetchZetesData() {
    Config config = configService.getConfig();

    try {
      if (StringUtils.isNotBlank(config.getLastZetesImportDate())) {
        LocalDate startDate = SIMPLE_DATE_FORMATTER
            .parseLocalDate(config.getLastZetesImportDate());
        fetchZetesData(startDate);
      } else {
        fetchZetesData(null);
      }

      LOGGER.debug("Updating Zetes import last date");
      config = configService.getConfig();
      config.setLastZetesImportDate(LocalDate.now().toString(SIMPLE_DATE_FORMATTER));
      configService.updateConfig(config);
      LOGGER.debug("Zetes last import date updated");
    } catch (UmurinziReportException e) {
      LOGGER.error("Error occurred while importing data from Zetes", e);
    }
  }

  public void fetchZetesData(LocalDate lastUpdate) {
    LOGGER.info("Fetching data from Zetes");
    List<ZetesSubjectDto> zetesSubjects = fetchSubjectsFromZetes(lastUpdate);
    LOGGER.info("Fetched data of {} subjects from Zetes", zetesSubjects.size());

    List<String> ivrIds = new ArrayList<>();
    Subject subject;

    for (ZetesSubjectDto zetesSubject : zetesSubjects) {
      try {
        subject = createOrUpdateSubject(zetesSubject);

        if (subject != null && StringUtils.isNotBlank(subject.getIvrId())) {
          ivrIds.add(subject.getIvrId());
        }

        if (ivrIds.size() > MAX_SUBJECTS_IN_CALL) {
          tryToSendWelcomeMessage(ivrIds);
          ivrIds.clear();
        }
      } catch (Exception e) {
        LOGGER.error(e.getMessage(), e);
      }
    }

    tryToSendWelcomeMessage(ivrIds);
  }

  public void transferSubjects() {
    Config config = configService.getConfig();

    try {
      if (StringUtils.isNotBlank(config.getLastSubjectTransferDate())) {
        LocalDate startDate = SIMPLE_DATE_FORMATTER
            .parseLocalDate(config.getLastSubjectTransferDate());
        transferSubjects(startDate);
      } else {
        transferSubjects(null);
      }

      config = configService.getConfig();
      config.setLastSubjectTransferDate(LocalDate.now().toString(SIMPLE_DATE_FORMATTER));
      configService.updateConfig(config);
    } catch (UmurinziReportException e) {
      LOGGER.error("Error occurred while importing data from Zetes", e);
    }
  }

  public void transferSubjects(LocalDate lastTransfer) {
    Connection sqlConnection = null;

    try {
      sqlConnection = getSqlConnection();
      StringBuilder query = new StringBuilder(TRANSFER_SQL_QUERY);

      if (lastTransfer != null) {
        query.append("WHERE transfer.\"DATE_TRANSFER_TO_EBL3010\" >= '")
            .append(lastTransfer.toString(SIMPLE_DATE_FORMATTER))
            .append("'");
      }

      ResultSet resultSet = sqlConnection.createStatement().executeQuery(query.toString());

      while (resultSet.next()) {
        String subjectId = resultSet.getString(SUBJECT_ID_COLUMN);
        String subjectTransferId = resultSet.getString(SUBJECT_TRANSFER_ID_COLUMN);

        transferSubject(subjectId, subjectTransferId);
      }
    } catch (SQLException e) {
      throw new UmurinziReportException("Error occurred while fetching data from Zetes", e);
    } finally {
      if (sqlConnection != null) {
        try {
          sqlConnection.close();
        } catch (SQLException e) {
          LOGGER.error("Error while closing SQL connection", e);
        }
      }
    }
  }

  private List<ZetesSubjectDto> fetchSubjectsFromZetes(LocalDate lastUpdateDate) {
    Connection sqlConnection = null;

    try {
      sqlConnection = getSqlConnection();
      StringBuilder query = new StringBuilder(SQL_QUERY);

      if (lastUpdateDate != null) {
        query.append("HAVING GREATEST(to_date(subject.\"REFRESH_DATE\", 'dd-MON-yy'), COALESCE(to_date(prime.\"REFRESH_DATE\", 'dd-MON-yy'), '1900-01-01'), "
            + "COALESCE(to_date(boost.\"REFRESH_DATE\", 'dd-MON-yy'), '1900-01-01')) >= '")
            .append(lastUpdateDate.toString(SIMPLE_DATE_FORMATTER))
            .append("'");
      }

      ResultSet resultSet = sqlConnection.createStatement().executeQuery(query.toString());

      return getSubjectsFromResultSet(resultSet);
    } catch (SQLException e) {
      throw new UmurinziReportException("Error occurred while fetching data from Zetes", e);
    } finally {
      if (sqlConnection != null) {
        try {
          sqlConnection.close();
        } catch (SQLException e) {
          LOGGER.error("Error while closing SQL connection", e);
        }
      }
    }
  }

  private Connection getSqlConnection() throws SQLException {
    Config config = configService.getConfig();
    String databaseUrl = config.getZetesDbUrl();
    String username = config.getZetesDbUsername();
    String password = config.getZetesDbPassword();

    loadSQLDriverClass(config.getZetesDbDriver());

    return DriverManager.getConnection(databaseUrl, username, password);
  }

  private List<ZetesSubjectDto> getSubjectsFromResultSet(ResultSet resultSet) throws SQLException {
    List<ZetesSubjectDto> subjects = new ArrayList<>();

    while (resultSet.next()) {
      ZetesSubjectDto subject = new ZetesSubjectDto();

      subject.setSubjectId(resultSet.getString(SUBJECT_ID_COLUMN));
      subject.setPhoneNumber(resultSet.getString(PHONE_COLUMN));
      subject.setVaccinationSite(resultSet.getString(CITY_FIRST_VACCINATED));
      subject.setPrimeVaccinationDate(resultSet.getDate(PRIME_VAC_DATE_COLUMN));
      subject.setBoostVaccinationDate(resultSet.getDate(BOOST_VAC_DATE_COLUMN));

      subjects.add(subject);
    }

    return subjects;
  }

  private void loadSQLDriverClass(String driver) {
    try {
      Class.forName(driver);
    } catch (ClassNotFoundException e) {
      LOGGER.warn(driver + " class not found.", e);
    }
  }

  private Subject createOrUpdateSubject(ZetesSubjectDto zetesSubject) {
    Subject subject = subjectService.findSubjectBySubjectId(zetesSubject.getSubjectId());

    if (subject != null) {
      MAPPER.updateFromDto(zetesSubject, subject);

      LOGGER.debug("Updating subject with id: {}", subject.getSubjectId());
      subjectService.update(subject);
      LOGGER.debug("Subject with id: {} updated", subject.getSubjectId());

      return null;
    }

    subject = MAPPER.fromDto(zetesSubject);

    tryToCreateSubject(subject);

    return subject;
  }

  private void tryToSendWelcomeMessage(List<String> ivrIds) {
    boolean messageSent = false;
    int numberOfAttempts = 1;

    while (!messageSent && numberOfAttempts < MAX_NUMBER_OF_ATTEMPTS) {
      messageSent = sendWelcomeMessage(ivrIds);
      numberOfAttempts++;
    }
  }

  private boolean sendWelcomeMessage(List<String> ivrIds) {
    Config config = configService.getConfig();

    try {
      if (config.getEnableWelcomeMessage()) {
        LOGGER.debug("Sending welcome message");
        ivrCallHelper.sendCallsInBulk(WELCOME_MESSAGE, ivrIds);
        LOGGER.debug("Welcome message sent");
      }

      return true;
    } catch (Exception e) {
      LOGGER.warn("Error occurred when sending welcome message", e);
    }

    return false;
  }

  private void tryToCreateSubject(Subject subject) {
    boolean subjectCreated = false;
    int numberOfAttempts = 0;

    while (!subjectCreated && numberOfAttempts < MAX_NUMBER_OF_ATTEMPTS) {
      subjectCreated = createSubject(subject);
      numberOfAttempts++;
    }
  }

  private boolean createSubject(Subject subject) {
    try {
      subjectService.create(subject);

      return true;
    } catch (IvrException e) {
      LOGGER.warn("Could not create subscriber for subject with id: {}", subject.getSubjectId());
    }

    return false;
  }

  private void transferSubject(String subjectId, String transferSubjectId) {
    Subject subject = subjectService.findSubjectBySubjectId(subjectId);

    if (subject == null) {
      LOGGER.error("Cannot transfer Subject with Transfer Id: " + transferSubjectId
       + ", because Subject with Id: " + subjectId + " not found.");
    } else if (StringUtils.isBlank(subject.getTransferSubjectId())) {
      subject.setTransferSubjectId(transferSubjectId);

      subjectService.update(subject);

      Config config = configService.getConfig();

      if (config.getEnableTransferMessage()) {
        try {
          ivrCallHelper.initiateIvrCall(TRANSFER_MESSAGE, subject);
        } catch (Exception e) {
          LOGGER.error(e.getMessage(), e);
        }
      }
    } else {
      subject.setTransferSubjectId(transferSubjectId);
      subjectService.update(subject);

      LOGGER.warn("Transfer Id updated for Subject with Id: " + subjectId + ", new Transfer Id: "
          + transferSubjectId + " old Transfer Id: " + subject.getTransferSubjectId());
    }
  }
}
