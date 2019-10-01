package org.motechproject.umurinzi.web;

import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.motechproject.commons.date.model.Time;
import org.motechproject.commons.date.util.DateUtil;
import org.motechproject.umurinzi.domain.Config;
import org.motechproject.umurinzi.domain.enums.VisitType;
import org.motechproject.umurinzi.scheduler.UmurinziScheduler;
import org.motechproject.umurinzi.service.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class ConfigController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigController.class);

    @Autowired
    @Qualifier("configService")
    private ConfigService configService;

    @Autowired
    private UmurinziScheduler umurinziScheduler;

    @RequestMapping(value = "/umurinzi-config", method = RequestMethod.GET)
    @ResponseBody
    public Config getConfig() {
        return configService.getConfig();
    }

    @RequestMapping(value = "/umurinzi-config", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Config updateConfig(@RequestBody Config config) {
        configService.updateConfig(config);

        umurinziScheduler.unscheduleDailyReportJob();
        scheduleJobs();

        return configService.getConfig();
    }

    @PreAuthorize("hasRole('manageUmurinzi')")
    @RequestMapping(value = "/availableVisits", method = RequestMethod.GET)
    @ResponseBody
    public List<String> getAvailableVisits() {
        List<String> availableValues = new ArrayList<>();
        for (VisitType visitType : VisitType.values()) {
            if (!VisitType.PRIME_VACCINATION_DAY.equals(visitType)
                && !VisitType.BOOST_VACCINATION_DAY.equals(visitType)) {
                availableValues.add(visitType.getDisplayValue());
            }
        }
        return availableValues;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public String handleException(Exception e) {
        LOGGER.error("Error while updating configs", e);
        return e.getMessage();
    }

    private void scheduleJobs() {
        if (configService.getConfig().getEnableReportJob()) {
            DateTime reportStartDate = DateUtil.newDateTime(LocalDate.now(),
                Time.parseTime(configService.getConfig().getReportCalculationStartTime(), ":"));
            if (reportStartDate.isBeforeNow()) {
                reportStartDate = reportStartDate.plusDays(1);
            }
            umurinziScheduler.scheduleDailyReportJob(reportStartDate);
        }
    }
}
