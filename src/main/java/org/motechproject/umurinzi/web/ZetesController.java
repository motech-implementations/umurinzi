package org.motechproject.umurinzi.web;

import static org.motechproject.umurinzi.constants.UmurinziConstants.HAS_MANAGE_MODULE_ROLE;
import static org.motechproject.umurinzi.constants.UmurinziConstants.SIMPLE_DATE_FORMATTER;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.motechproject.umurinzi.helper.ZetesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class ZetesController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ZetesController.class);

  @Autowired
  private ZetesHelper zetesHelper;

  @RequestMapping(value = "/fetchZetesData", method = RequestMethod.POST)
  @PreAuthorize(HAS_MANAGE_MODULE_ROLE)
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<String> fetchZetesData(@RequestBody String startDate) {
    try {
      LocalDate date = null;

      if (StringUtils.isNotBlank(startDate)) {
        date = LocalDate.parse(startDate, SIMPLE_DATE_FORMATTER);
      }

      zetesHelper.fetchZetesData(date);

      LOGGER.info("Zetes date transfer started by custom request from date: {}", date == null ? null : date.toString(SIMPLE_DATE_FORMATTER));
    } catch (IllegalArgumentException e) {
      LOGGER.error("Invalid date format", e);
      return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      LOGGER.error("Fatal error raised during creating reports", e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return new ResponseEntity<>(HttpStatus.OK);
  }

  @RequestMapping(value = "/transferZetesSubjects", method = RequestMethod.POST)
  @PreAuthorize(HAS_MANAGE_MODULE_ROLE)
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<String> transferZetesSubjects(@RequestBody String startDate) {
    try {
      LocalDate date = null;

      if (StringUtils.isNotBlank(startDate)) {
        date = LocalDate.parse(startDate, SIMPLE_DATE_FORMATTER);
      }

      zetesHelper.transferSubjects(date);

      LOGGER.info("Zetes subject transfer started by custom request from date: {}", date == null ? null : date.toString(SIMPLE_DATE_FORMATTER));
    } catch (IllegalArgumentException e) {
      LOGGER.error("Invalid date format", e);
      return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      LOGGER.error("Fatal error raised during creating reports", e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return new ResponseEntity<>(HttpStatus.OK);
  }
}
