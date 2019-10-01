package org.motechproject.umurinzi.web;

import static org.motechproject.umurinzi.constants.UmurinziConstants.REPORTS_TAB_PERMISSION;
import static org.motechproject.umurinzi.constants.UmurinziConstants.SUBJECTS_TAB_PERMISSION;
import static org.motechproject.umurinzi.constants.UmurinziConstants.UNSCHEDULED_VISITS_TAB_PERMISSION;
import static org.motechproject.umurinzi.constants.UmurinziConstants.VISIT_RESCHEDULE_TAB_PERMISSION;

import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TabAccessController {

    @RequestMapping(value = "/available/umurinziTabs", method = RequestMethod.GET)
    @ResponseBody
    public List<String> getAvailableTabs() {
        List<String> availableTabs = new ArrayList<>();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth.getAuthorities().contains(new SimpleGrantedAuthority(SUBJECTS_TAB_PERMISSION))) {
            availableTabs.add("subjects");
        }

        if (auth.getAuthorities().contains(new SimpleGrantedAuthority(VISIT_RESCHEDULE_TAB_PERMISSION))) {
            availableTabs.add("enrollment");
        }

        if (auth.getAuthorities().contains(new SimpleGrantedAuthority(REPORTS_TAB_PERMISSION))) {
            availableTabs.add("reports");
        }

        if (auth.getAuthorities().contains(new SimpleGrantedAuthority(UNSCHEDULED_VISITS_TAB_PERMISSION))) {
            availableTabs.add("unscheduledVisit");
        }

        return availableTabs;
    }

}
