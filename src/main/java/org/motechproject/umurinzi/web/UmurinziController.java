package org.motechproject.umurinzi.web;

import static org.motechproject.umurinzi.constants.UmurinziConstants.HAS_MANAGE_MODULE_ROLE;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.motechproject.mds.ex.csv.CsvImportException;
import org.motechproject.server.config.SettingsFacade;
import org.motechproject.umurinzi.domain.Subject;
import org.motechproject.umurinzi.helper.IvrCallHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.supercsv.io.CsvMapReader;
import org.supercsv.prefs.CsvPreference;

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

    @PreAuthorize(HAS_MANAGE_MODULE_ROLE)
    @RequestMapping(value = "/sendCallsInBulk/json", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void sendIvrCalls(@RequestParam String messageKey, @RequestBody List<String> participantIds) {
        ivrCallHelper.sendCallsToParticipants(messageKey, participantIds);
    }

    @PreAuthorize(HAS_MANAGE_MODULE_ROLE)
    @RequestMapping(value = "/sendCallsInBulk/csvFile", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void sendIvrCalls(@RequestParam String messageKey, @RequestParam MultipartFile csvFile) {
        try {
            try (InputStream in = csvFile.getInputStream()) {
                Reader reader = new InputStreamReader(in);
                sendIvrCallsFromCsv(messageKey, reader);
            }
        } catch (IOException e) {
            throw new CsvImportException("Unable to open uploaded file", e);
        }
    }

    @PreAuthorize(HAS_MANAGE_MODULE_ROLE)
    @RequestMapping(value = "/sendCallsInBulk/csv", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void sendIvrCalls(@RequestParam String messageKey, @RequestBody String csvFile) {
        Reader reader = new InputStreamReader(new ByteArrayInputStream(csvFile.getBytes()));
        sendIvrCallsFromCsv(messageKey, reader);
    }

    private void sendIvrCallsFromCsv(String messageKey, Reader reader) {
        try (CsvMapReader csvMapReader = new CsvMapReader(reader, CsvPreference.STANDARD_PREFERENCE)) {
            final String [] headers = csvMapReader.getHeader(true);

            Map<String, String> row;
            List<String> participantIds = new ArrayList<>();

            while ((row = csvMapReader.read(headers)) != null) {
                String participantId = row.get(Subject.SUBJECT_ID_FIELD_NAME);

                if (StringUtils.isBlank(participantId)) {
                    participantId = row.get(Subject.SUBJECT_ID_FIELD_DISPLAY_NAME);
                }

                if (StringUtils.isNotBlank(participantId)) {
                    participantIds.add(participantId);
                }
            }

            ivrCallHelper.sendCallsToParticipants(messageKey, participantIds);
        } catch (IOException e) {
            throw new CsvImportException("IO Error when reading CSV", e);
        }
    }

}
