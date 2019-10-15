package org.motechproject.umurinzi.web;

import static org.motechproject.umurinzi.constants.UmurinziConstants.HAS_MANAGE_MODULE_ROLE;

import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.motechproject.server.config.SettingsFacade;
import org.motechproject.umurinzi.helper.IvrCallHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class UmurinziController {

    private static final String UI_CONFIG = "custom-ui.js";

    @Autowired
    @Qualifier("umurinziSettings")
    private SettingsFacade settingsFacade;

    @Autowired
    private IvrCallHelper ivrCallHelper;

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/mds-databrowser-config", method = RequestMethod.GET)
    @ResponseBody
    public String getCustomUISettings() throws IOException {
        return IOUtils.toString(settingsFacade.getRawConfig(UI_CONFIG));
    }

    @PreAuthorize(HAS_MANAGE_MODULE_ROLE)
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/sendIvrCall", method = RequestMethod.GET)
    public void sendIvrCall(@RequestParam String subjectId, @RequestParam String messageKey) {
        ivrCallHelper.initiateIvrCall(messageKey, subjectId);
    }
}
