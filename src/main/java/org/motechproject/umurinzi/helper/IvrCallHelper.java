package org.motechproject.umurinzi.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    private static final Integer MAX_BULK_SIZE = 200;

    private static final Logger LOGGER = LoggerFactory.getLogger(IvrCallHelper.class);

    @Autowired
    private ConfigService configService;

    @Autowired
    private VotoMessageDataService votoMessageDataService;

    @Autowired
    private SubjectService subjectService;

    private OutboundCallService outboundCallService;

    public void sendCallsToParticipants(String messageKey, List<String> participantIds) {
        List<String> ivrIds = new ArrayList<>();

        for (String participantId : participantIds) {
            if (StringUtils.isNotBlank(participantId)) {
                Subject subject = subjectService.findSubjectBySubjectId(participantId.trim());
                if (subject != null && StringUtils.isNotBlank(subject.getIvrId())) {
                    ivrIds.add(subject.getIvrId());
                }
            }
        }

        sendCallsInBulk(messageKey, ivrIds);
    }

    public void sendCallsInBulk(String messageKey, List<String> ivrIds) {
        VotoMessage votoMessage = votoMessageDataService.findByMessageKey(messageKey);

        if (votoMessage == null) {
            throw new UmurinziInitiateCallException("Cannot initiate call, because Voto Message with key: %s not found", messageKey);
        }

        if (!ivrIds.isEmpty()) {
            for (int index = 0; index * MAX_BULK_SIZE < ivrIds.size(); index++) {
                int start = index * MAX_BULK_SIZE;
                int end = Math.min(start + MAX_BULK_SIZE, ivrIds.size());

                sendIvrCall(messageKey, votoMessage.getVotoIvrId(),
                    StringUtils.join(ivrIds.subList(start, end), ","));
            }
        }
    }

    public void initiateIvrCall(String messageKey, String externalId) {
        Subject subject = getSubject(externalId);
        initiateIvrCall(messageKey, subject);
    }

    public void initiateIvrCall(String messageKey, Subject subject) {
        boolean hasHelpLine = StringUtils.isNotBlank(subject.getHelpLine());
        String votoMessageId = getVotoMessageId(messageKey, hasHelpLine, subject.getSubjectId());

        sendIvrCall(messageKey, votoMessageId, subject.getIvrId());
    }

    private void sendIvrCall(String messageKey, String votoMessageId, String subscriberId) {
        Config config = configService.getConfig();

        if (config.getSendIvrCalls() != null && config.getSendIvrCalls()
            && StringUtils.isNotBlank(subscriberId)) {

            if (StringUtils.isNotBlank(config.getSuspendedMessages())
                && config.getSuspendedMessages().contains(messageKey)) {

                LOGGER.info("Message: {} suspended, call for subscriber: {} will not be send",
                    messageKey, subscriberId);
                return;
            }

            Map<String, String> callParams = new HashMap<>();
            if (StringUtils.isNotBlank(config.getVoiceSenderId())) {
                callParams.put(UmurinziConstants.VOICE_SENDER_ID, config.getVoiceSenderId());
            }
            if (StringUtils.isNotBlank(config.getSmsSenderId())) {
                callParams.put(UmurinziConstants.SMS_SENDER_ID, config.getSmsSenderId());
            }
            callParams.put(UmurinziConstants.API_KEY, config.getApiKey());
            callParams.put(UmurinziConstants.MESSAGE_ID, votoMessageId);
            callParams.put(UmurinziConstants.SEND_TO_SUBSCRIBERS, subscriberId);
            callParams.put(UmurinziConstants.WEBHOOK_URL, config.getStatusCallbackUrl());
            callParams.put(UmurinziConstants.SEND_SMS_IF_VOICE_FAILS, config.getSendSmsIfVoiceFails() ? "1" : "0");
            callParams.put(UmurinziConstants.DETECT_VOICEMAIL, config.getDetectVoiceMail() ? "1" : "0");
            callParams.put(UmurinziConstants.RETRY_ATTEMPTS_SHORT, config.getRetryAttempts().toString());
            callParams.put(UmurinziConstants.RETRY_DELAY_SHORT, config.getRetryDelay().toString());
            callParams.put(UmurinziConstants.RETRY_ATTEMPTS_LONG, UmurinziConstants.RETRY_ATTEMPTS_LONG_DEFAULT);

            LOGGER.debug("Initiating call: {}", callParams.toString());

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
