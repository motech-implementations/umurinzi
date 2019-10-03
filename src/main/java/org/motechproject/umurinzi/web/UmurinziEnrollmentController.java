package org.motechproject.umurinzi.web;

import static org.motechproject.umurinzi.constants.UmurinziConstants.AVAILABLE_LOOKUPS_FOR_SUBJECT_ENROLLMENTS;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.motechproject.mds.dto.LookupDto;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.mds.util.Order;
import org.motechproject.umurinzi.constants.UmurinziConstants;
import org.motechproject.umurinzi.domain.Enrollment;
import org.motechproject.umurinzi.domain.SubjectEnrollments;
import org.motechproject.umurinzi.exception.UmurinziEnrollmentException;
import org.motechproject.umurinzi.exception.UmurinziException;
import org.motechproject.umurinzi.exception.UmurinziLookupException;
import org.motechproject.umurinzi.repository.EnrollmentDataService;
import org.motechproject.umurinzi.service.LookupService;
import org.motechproject.umurinzi.service.UmurinziEnrollmentService;
import org.motechproject.umurinzi.web.domain.GridSettings;
import org.motechproject.umurinzi.web.domain.Records;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@PreAuthorize(UmurinziConstants.HAS_ENROLLMENTS_TAB_ROLE)
public class UmurinziEnrollmentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UmurinziEnrollmentController.class);

    @Autowired
    private UmurinziEnrollmentService umurinziEnrollmentService;

    @Autowired
    private EnrollmentDataService enrollmentDataService;

    @Autowired
    private LookupService lookupService;

    @RequestMapping(value = "/getEnrollments", method = RequestMethod.POST)
    @ResponseBody
    public Records<?> getEnrollments(GridSettings settings) {
        Order order = null;
        if (!settings.getSortColumn().isEmpty()) {
            order = new Order(settings.getSortColumn(), settings.getSortDirection());
        }

        QueryParams queryParams = new QueryParams(settings.getPage(), settings.getRows(), order);

        try {
            return lookupService.getEntities(SubjectEnrollments.class, settings.getLookup(), settings.getFields(), queryParams);
        } catch (UmurinziLookupException e) {
            LOGGER.debug(e.getMessage(), e);
            return null;
        }
    }

    @RequestMapping(value = "/getLookupsForEnrollments", method = RequestMethod.GET)
    @ResponseBody
    public List<LookupDto> getLookupsForEnrollments() {
        List<LookupDto> ret = new ArrayList<>();
        List<LookupDto> availableLookups;

        try {
            availableLookups = lookupService.getAvailableLookups(SubjectEnrollments.class.getName());
        } catch (UmurinziLookupException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }

        for (LookupDto lookupDto : availableLookups) {
            if (AVAILABLE_LOOKUPS_FOR_SUBJECT_ENROLLMENTS.contains(lookupDto.getLookupName())) {
                ret.add(lookupDto);
            }
        }
        return ret;
    }

    @PreAuthorize(UmurinziConstants.HAS_MANAGE_ENROLLMENTS_ROLE)
    @RequestMapping(value = "/checkAdvancedPermissions", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> checkAdvancedPermissions() {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize(UmurinziConstants.HAS_MANAGE_ENROLLMENTS_ROLE)
    @RequestMapping(value = "/getEnrollmentAdvanced/{subjectId}", method = RequestMethod.POST)
    @ResponseBody
    public Records<?> getEnrollmentAdvanced(@PathVariable String subjectId, GridSettings settings) {
        Order order = null;
        if (!settings.getSortColumn().isEmpty()) {
            order = new Order(settings.getSortColumn(), settings.getSortDirection());
        }

        QueryParams queryParams = new QueryParams(null, null, order);

        long recordCount;
        int rowCount;

        recordCount = enrollmentDataService.countFindBySubjectId(subjectId);
        rowCount = (int) Math.ceil(recordCount / (double) settings.getRows());

        List<Enrollment> enrollments = enrollmentDataService.findBySubjectId(subjectId, queryParams);

        return new Records<>(settings.getPage(), rowCount, (int) recordCount, enrollments);
    }

    @RequestMapping(value = "/enrollSubject", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> enrollSubject(@RequestBody String subjectId) {
        if (StringUtils.isBlank(subjectId)) {
            return new ResponseEntity<>("Participant id cannot be empty", HttpStatus.BAD_REQUEST);
        }

        try {
            umurinziEnrollmentService.enrollSubject(subjectId);
        } catch (UmurinziEnrollmentException e) {
            LOGGER.debug(e.getMessage(), e);
            return new ResponseEntity<>(getMessageFromException(e), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/unenrollSubject", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> unenrollSubject(@RequestBody String subjectId) {
        if (StringUtils.isBlank(subjectId)) {
            return new ResponseEntity<>("Participant id cannot be empty", HttpStatus.BAD_REQUEST);
        }

        try {
            umurinziEnrollmentService.unenrollSubject(subjectId);
        } catch (UmurinziEnrollmentException e) {
            LOGGER.debug(e.getMessage(), e);
            return new ResponseEntity<>(getMessageFromException(e), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize(UmurinziConstants.HAS_MANAGE_ENROLLMENTS_ROLE)
    @RequestMapping(value = "/enrollCampaign/{subjectId}/{campaignName}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> enrollCampaign(@PathVariable String subjectId, @PathVariable String campaignName) {

        if (StringUtils.isBlank(subjectId)) {
            return new ResponseEntity<>("Participant id cannot be empty", HttpStatus.BAD_REQUEST);
        }

        if (StringUtils.isBlank(campaignName)) {
            return new ResponseEntity<>("Campaign name cannot be empty", HttpStatus.BAD_REQUEST);
        }

        try {
            umurinziEnrollmentService.enrollSubjectToCampaign(subjectId, campaignName);
        } catch (UmurinziEnrollmentException e) {
            LOGGER.debug(e.getMessage(), e);
            return new ResponseEntity<>(getMessageFromException(e), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize(UmurinziConstants.HAS_MANAGE_ENROLLMENTS_ROLE)
    @RequestMapping(value = "/unenrollCampaign/{subjectId}/{campaignName}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> unenrollCampaign(@PathVariable String subjectId, @PathVariable String campaignName) {

        if (StringUtils.isBlank(subjectId)) {
            return new ResponseEntity<>("Participant id cannot be empty", HttpStatus.BAD_REQUEST);
        }

        if (StringUtils.isBlank(campaignName)) {
            return new ResponseEntity<>("Campaign name cannot be empty", HttpStatus.BAD_REQUEST);
        }

        try {
            umurinziEnrollmentService.unenrollSubject(subjectId, campaignName);
        } catch (UmurinziEnrollmentException e) {
            LOGGER.debug(e.getMessage(), e);
            return new ResponseEntity<>(getMessageFromException(e), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private String getMessageFromException(UmurinziException e) {
        return e.getMessage();
    }
}
