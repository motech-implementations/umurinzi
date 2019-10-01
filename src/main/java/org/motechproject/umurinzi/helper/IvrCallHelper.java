package org.motechproject.umurinzi.helper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.motechproject.umurinzi.constants.UmurinziConstants;
import org.motechproject.umurinzi.domain.Config;
import org.motechproject.umurinzi.domain.Subject;
import org.motechproject.umurinzi.domain.VotoLanguage;
import org.motechproject.umurinzi.domain.VotoMessage;
import org.motechproject.umurinzi.domain.enums.Language;
import org.motechproject.umurinzi.exception.UmurinziInitiateCallException;
import org.motechproject.umurinzi.repository.VotoLanguageDataService;
import org.motechproject.umurinzi.repository.VotoMessageDataService;
import org.motechproject.umurinzi.service.ConfigService;
import org.motechproject.umurinzi.service.SubjectService;
import org.motechproject.ivr.service.OutboundCallService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IvrCallHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(IvrCallHelper.class);

    @Autowired
    private ConfigService configService;

    @Autowired
    private VotoMessageDataService votoMessageDataService;

    @Autowired
    private VotoLanguageDataService votoLanguageDataService;

    @Autowired
    private SubjectService subjectService;

    private OutboundCallService outboundCallService;

    public void initiateIvrCall(String messageKey, String externalId) {
        Config config = configService.getConfig();

        Subject subject = getSubject(externalId);

        if (config.getSendIvrCalls() != null && config.getSendIvrCalls()
            && subject.getLanguage() != null && StringUtils.isNotBlank(subject.getPhoneNumber())) {

            String votoLanguageId = getVotoLanguageId(subject.getLanguage(), externalId);
            String votoMessageId = getVotoMessageId(messageKey, externalId);

            JsonObject subscriber = new JsonObject();
            subscriber.addProperty(UmurinziConstants.PHONE, subject.getPhoneNumber());
            subscriber.addProperty(UmurinziConstants.LANGUAGE, votoLanguageId);

            JsonArray subscriberArray = new JsonArray();
            subscriberArray.add(subscriber);

            Gson gson = new GsonBuilder().serializeNulls().create();
            String subscribers = gson.toJson(subscriberArray);

            Map<String, String> callParams = new HashMap<>();
            callParams.put(UmurinziConstants.API_KEY, config.getApiKey());
            callParams.put(UmurinziConstants.MESSAGE_ID, votoMessageId);
            callParams.put(UmurinziConstants.STATUS_CALLBACK_URL, config.getStatusCallbackUrl());
            callParams.put(UmurinziConstants.SUBSCRIBERS, subscribers);
            callParams.put(
                UmurinziConstants.SEND_SMS_IF_VOICE_FAILS, config.getSendSmsIfVoiceFails() ? "1" : "0");
            callParams.put(
                UmurinziConstants.DETECT_VOICEMAIL, config.getDetectVoiceMail() ? "1" : "0");
            callParams.put(UmurinziConstants.RETRY_ATTEMPTS_SHORT, config.getRetryAttempts().toString());
            callParams.put(UmurinziConstants.RETRY_DELAY_SHORT, config.getRetryDelay().toString());
            callParams.put(UmurinziConstants.RETRY_ATTEMPTS_LONG, UmurinziConstants.RETRY_ATTEMPTS_LONG_DEFAULT);
            callParams.put(UmurinziConstants.SUBJECT_ID, externalId);
            callParams.put(UmurinziConstants.SUBJECT_PHONE_NUMBER, subject.getPhoneNumber());

            LOGGER.info("Initiating call: {}", callParams.toString());

            outboundCallService.initiateCall(config.getIvrSettingsName(), callParams);
        }
    }

    private Subject getSubject(String subjectId) {
        Subject subject = subjectService.findSubjectBySubjectId(subjectId);

        if (subject == null) {
            throw new UmurinziInitiateCallException("Cannot initiate call, because Provider with id: %s not found", subjectId);
        }

        return subject;
    }

    private String getVotoLanguageId(Language language, String subjectId) {
        VotoLanguage votoLanguage = votoLanguageDataService.findByLanguage(language);

        if (votoLanguage == null) {
            throw new UmurinziInitiateCallException("Cannot initiate call for Provider with id: %s, because Voto Language for language: %s not found",
                    subjectId, language.toString());
        }

        return votoLanguage.getVotoId();
    }

    private String getVotoMessageId(String messageKey, String subjectId) {
        VotoMessage votoMessage = votoMessageDataService.findByMessageKey(messageKey);

        if (votoMessage == null) {
            throw new UmurinziInitiateCallException("Cannot initiate call for Provider with id: %s, because Voto Message with key: %s not found",
                    subjectId, messageKey);
        }

        return votoMessage.getVotoIvrId();
    }

    @Autowired
    public void setOutboundCallService(OutboundCallService outboundCallService) {
        this.outboundCallService = outboundCallService;
    }
}
