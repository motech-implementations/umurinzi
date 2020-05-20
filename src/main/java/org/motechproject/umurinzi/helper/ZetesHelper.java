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
import org.motechproject.umurinzi.exception.UmurinziInitiateCallException;
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

  private static final String SUBJECT_ID_COLUMN = "SUBJECT_ID";
  private static final String PHONE_COLUMN = "PHONE";
  private static final String CITY_FIRST_VACCINATED = "CITY_FIRST_VACCINATED";
  private static final String PRIME_VAC_DATE_COLUMN = "PRIME_VAC_DATE";
  private static final String BOOST_VAC_DATE_COLUMN = "BOOST_VAC_DATE";

  private static final String WELCOME_MESSAGE = "welcome-message";

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

      config = configService.getConfig();
      config.setLastZetesImportDate(LocalDate.now().toString(SIMPLE_DATE_FORMATTER));
      configService.updateConfig(config);
    } catch (UmurinziReportException e) {
      LOGGER.error("Error occurred while importing data from Zetes", e);
    }
  }

  public void fetchZetesData(LocalDate lastUpdate) {
    List<ZetesSubjectDto> zetesSubjects = fetchSubjectsFromZetes(lastUpdate);

    for (ZetesSubjectDto zetesSubject : zetesSubjects) {
      createOrUpdateSubject(zetesSubject);
    }
  }

  private List<ZetesSubjectDto> fetchSubjectsFromZetes(LocalDate lastUpdateDate) {
    Config config = configService.getConfig();
    String databaseUrl = config.getZetesDbUrl();
    String username = config.getZetesDbUsername();
    String password = config.getZetesDbPassword();

    loadSQLDriverClass(config.getZetesDbDriver());

    Connection sqlConnection = null;

    try {
      sqlConnection = DriverManager.getConnection(databaseUrl, username, password);
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

  private void createOrUpdateSubject(ZetesSubjectDto zetesSubject) {
    Subject subject = subjectService.findSubjectBySubjectId(zetesSubject.getSubjectId());

    if (subject != null) {
      MAPPER.updateFromDto(zetesSubject, subject);

      subjectService.update(subject);
    } else {
      subject = MAPPER.fromDto(zetesSubject);

      subjectService.create(subject);

      try {
        ivrCallHelper.initiateIvrCall(WELCOME_MESSAGE, subject);
      } catch (UmurinziInitiateCallException e) {
        LOGGER.error(e.getMessage(), e);
      }
    }
  }
}
