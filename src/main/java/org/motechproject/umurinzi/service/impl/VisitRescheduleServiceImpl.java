package org.motechproject.umurinzi.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.joda.time.LocalDate;
import org.motechproject.commons.api.Range;
import org.motechproject.umurinzi.domain.Subject;
import org.motechproject.umurinzi.domain.Visit;
import org.motechproject.umurinzi.domain.VisitScheduleOffset;
import org.motechproject.umurinzi.domain.enums.VisitType;
import org.motechproject.umurinzi.dto.VisitRescheduleDto;
import org.motechproject.umurinzi.repository.VisitDataService;
import org.motechproject.umurinzi.service.ConfigService;
import org.motechproject.umurinzi.service.UmurinziEnrollmentService;
import org.motechproject.umurinzi.service.LookupService;
import org.motechproject.umurinzi.service.SubjectService;
import org.motechproject.umurinzi.service.VisitRescheduleService;
import org.motechproject.umurinzi.service.VisitScheduleOffsetService;
import org.motechproject.umurinzi.util.QueryParamsBuilder;
import org.motechproject.umurinzi.web.domain.GridSettings;
import org.motechproject.umurinzi.web.domain.Records;
import org.motechproject.mds.query.QueryParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("visitRescheduleService")
public class VisitRescheduleServiceImpl implements VisitRescheduleService {

    @Autowired
    private LookupService lookupService;

    @Autowired
    private VisitDataService visitDataService;

    @Autowired
    private VisitScheduleOffsetService visitScheduleOffsetService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private UmurinziEnrollmentService umurinziEnrollmentService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Records<VisitRescheduleDto> getVisitsRecords(GridSettings settings) throws IOException {
        QueryParams queryParams = QueryParamsBuilder.buildQueryParams(settings, getFields(settings.getFields()));
        Records<Visit> detailsRecords = lookupService.getEntities(Visit.class, settings.getLookup(), settings.getFields(), queryParams);

        List<VisitRescheduleDto> dtos = new ArrayList<>();

        for (Visit visit : detailsRecords.getRows()) {

            LocalDate vaccinationDate = getVaccinationDate(visit);

            Boolean notVaccinated = true;
            Range<LocalDate> dateRange = null;

            if (vaccinationDate != null) {
                dateRange = calculateEarliestAndLatestDate(visit.getType(), vaccinationDate);
                notVaccinated = false;
            }

            dtos.add(new VisitRescheduleDto(visit, dateRange,  notVaccinated));
        }

        return new Records<>(detailsRecords.getPage(), detailsRecords.getTotal(), detailsRecords.getRecords(), dtos);
    }

    @Override
    public VisitRescheduleDto saveVisitReschedule(VisitRescheduleDto visitRescheduleDto, Boolean ignoreLimitation) {
        Visit visit = visitDataService.findById(visitRescheduleDto.getVisitId());

        if (visit == null) {
            throw new IllegalArgumentException("Cannot reschedule, because details for Visit not found");
        }

        validateDates(visitRescheduleDto, visit);

        return new VisitRescheduleDto(updateVisitDetailsWithDto(visit, visitRescheduleDto));
    }

    private Visit updateVisitDetailsWithDto(Visit visit, VisitRescheduleDto dto) {
        boolean rollbackCompleted = dto.getActualDate() == null && visit.getDate() != null;

        visit.setIgnoreDateLimitation(dto.getIgnoreDateLimitation());
        visit.setDateProjected(dto.getPlannedDate());
        visit.setDate(dto.getActualDate());

        Subject subject = visit.getSubject();

        if (VisitType.BOOST_VACCINATION_DAY.equals(dto.getVisitType())
            && !Objects.equals(dto.getActualDate(), subject.getBoosterVaccinationDate())) {
          subject.setBoosterVaccinationDate(dto.getActualDate());

          visitDataService.update(visit);
          subjectService.update(subject);

          return visit;
        } else if (dto.getActualDate() != null) {
            umurinziEnrollmentService.completeCampaign(visit);
        } else if (dto.getPlannedDate() != null) {
            umurinziEnrollmentService.createEnrollmentOrReenrollCampaign(visit, rollbackCompleted);
        }

        return visitDataService.update(visit);
    }

    private void validateDates(VisitRescheduleDto dto, Visit visit) {
        if (dto.getActualDate() != null) {
            validateActualDate(dto);
        } else {
            validatePlannedDate(dto, visit);
        }
    }

    private void validateActualDate(VisitRescheduleDto dto) {
        if (dto.getActualDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Actual Date cannot be in the future.");
        }
    }

    private void validatePlannedDate(VisitRescheduleDto dto, Visit visit) {
        LocalDate plannedDate = dto.getPlannedDate();

        //If plannedDate isn't actually updated don't need to validate
        if (!dto.getIgnoreDateLimitation() && !plannedDate.equals(visit.getDateProjected())) {
            Range<LocalDate> dateRange = calculateEarliestAndLatestDate(visit);

            if (dateRange != null) {
                LocalDate earliestDate = dateRange.getMin();
                LocalDate latestDate = dateRange.getMax();

                if (plannedDate.isBefore(earliestDate)) {
                    throw new IllegalArgumentException(
                        String.format("The date should be after %s but is %s", earliestDate, plannedDate));
                }

                if (latestDate != null &&  plannedDate.isAfter(latestDate)) {
                    throw new IllegalArgumentException(
                        String.format("The date should be before %s but is %s", latestDate, plannedDate));
                }
            }
        }
    }

    private Map<String, Object> getFields(String json) throws IOException {
        if (json == null) {
            return null;
        } else {
            return objectMapper.readValue(json, new TypeReference<LinkedHashMap>() {
            });  //NO CHECKSTYLE WhitespaceAround
        }
    }

    private Range<LocalDate> calculateEarliestAndLatestDate(Visit visit) {
        LocalDate vaccinationDate = getVaccinationDate(visit);

        if (vaccinationDate == null) {
            return null;
        }

        return calculateEarliestAndLatestDate(visit.getType(), vaccinationDate);
    }

    private Range<LocalDate> calculateEarliestAndLatestDate(VisitType visitType, LocalDate vaccinationDate) {
        LocalDate minDate = LocalDate.now();
        LocalDate maxDate = null;

        Map<VisitType, VisitScheduleOffset> visitTypeOffset = visitScheduleOffsetService.getAllAsMap();

        if (visitTypeOffset == null) {
            return new Range<>(minDate, maxDate);
        }

        VisitScheduleOffset offset = visitTypeOffset.get(visitType);

        if (offset == null) {
            return new Range<>(minDate, maxDate);
        }

        if (offset.getEarliestDateOffset() != null) {
            LocalDate date = vaccinationDate.plusDays(offset.getEarliestDateOffset());

            if (date.isAfter(minDate)) {
                minDate = date;
            }
        }

        if (offset.getLatestDateOffset() != null) {
            maxDate = vaccinationDate.plusDays(offset.getLatestDateOffset());
        }

        return new Range<>(minDate, maxDate);
    }

    private LocalDate getVaccinationDate(Visit visit) {
        return visit.getSubject().getPrimerVaccinationDate();
    }
}
