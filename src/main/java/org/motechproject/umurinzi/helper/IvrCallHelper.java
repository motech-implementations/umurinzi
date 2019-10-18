package org.motechproject.umurinzi.helper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.motechproject.ivr.service.OutboundCallService;
import org.motechproject.umurinzi.constants.UmurinziConstants;
import org.motechproject.umurinzi.domain.Config;
import org.motechproject.umurinzi.domain.Subject;
import org.motechproject.umurinzi.domain.VotoMessage;
import org.motechproject.umurinzi.exception.UmurinziInitiateCallException;
import org.motechproject.umurinzi.repository.VotoMessageDataService;
import org.motechproject.umurinzi.service.ConfigService;
import org.motechproject.umurinzi.service.SubjectService;
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
    private SubjectService subjectService;

    private OutboundCallService outboundCallService;

    public void initiateIvrCall(String messageKey, String externalId) {
        Config config = configService.getConfig();

        Subject subject = getSubject(externalId);

        if (config.getSendIvrCalls() != null && config.getSendIvrCalls()
            && StringUtils.isNotBlank(config.getIvrLanguageId())
            && StringUtils.isNotBlank(subject.getPhoneNumber())) {

            boolean hasHelpLine = StringUtils.isNotBlank(subject.getHelpLine());
            String votoMessageId = getVotoMessageId(messageKey, hasHelpLine, externalId);

            JsonObject subscriberData = new JsonObject();
            subscriberData.addProperty(UmurinziConstants.PREFERRED_LANGUAGE, config.getIvrLanguageId());
            subscriberData.addProperty(UmurinziConstants.RECEIVE_VOICE, "1");
            subscriberData.addProperty(UmurinziConstants.RECEIVE_SMS, "1");

            JsonObject subscriberProperties = new JsonObject();

            subscriberProperties.addProperty(UmurinziConstants.SUBJECT_ID, subject.getSubjectId());
            if (hasHelpLine) {
                subscriberProperties.addProperty(UmurinziConstants.HELP_LINE, subject.getHelpLine());
            }

            subscriberData.add(UmurinziConstants.PROPERTY, subscriberProperties);

            Gson gson = new GsonBuilder().serializeNulls().create();
            String subscriber = gson.toJson(subscriberData);

            Map<String, String> callParams = new HashMap<>();
            callParams.put(UmurinziConstants.API_KEY, config.getApiKey());
            callParams.put(UmurinziConstants.MESSAGE_ID, votoMessageId);
            callParams.put(UmurinziConstants.SEND_TO_PHONES, subject.getPhoneNumber());
            callParams.put(UmurinziConstants.WEBHOOK_URL, config.getStatusCallbackUrl());
            callParams.put(UmurinziConstants.SUBSCRIBER_DATA, subscriber);
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

    private String getVotoMessageId(String messageKey, boolean hasHelpLine, String subjectId) {
        VotoMessage votoMessage = votoMessageDataService.findByMessageKey(messageKey + (hasHelpLine ? UmurinziConstants.WITH_HELPLINE : ""));

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
