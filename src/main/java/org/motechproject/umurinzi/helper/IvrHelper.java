package org.motechproject.umurinzi.helper;

import java.util.Collections;
import org.apache.commons.lang3.StringUtils;
import org.motechproject.umurinzi.constants.UmurinziConstants;
import org.motechproject.umurinzi.domain.Config;
import org.motechproject.umurinzi.domain.Subject;
import org.motechproject.umurinzi.exception.IvrException;
import org.motechproject.umurinzi.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class IvrHelper {

  private static final int HTTP_CONNECT_TIMEOUT = 15000;
  private static final int HTTP_READ_TIMEOUT = 10000;

  @Autowired
  private ConfigService configService;

  private final RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());

  private static ClientHttpRequestFactory getClientHttpRequestFactory() {
    HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
    clientHttpRequestFactory.setConnectTimeout(HTTP_CONNECT_TIMEOUT);
    clientHttpRequestFactory.setReadTimeout(HTTP_READ_TIMEOUT);
    return clientHttpRequestFactory;
  }

  public String createSubscriber(Subject subject) {
    if (StringUtils.isBlank(subject.getPhoneNumber())) {
      return null;
    }

    Config config = configService.getConfig();
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(config.getIvrUrl() + UmurinziConstants.SUBSCRIBERS_URL);

    builder.queryParam(UmurinziConstants.API_KEY, config.getApiKey());
    builder.queryParam(UmurinziConstants.PHONE, subject.getPhoneNumber());
    builder.queryParam(UmurinziConstants.SUBJECT_ID_PROPERTY, subject.getSubjectId());
    builder.queryParam(UmurinziConstants.PREFERRED_LANGUAGE, config.getIvrLanguageId());
    builder.queryParam(UmurinziConstants.RECEIVE_SMS, "1");
    builder.queryParam(UmurinziConstants.RECEIVE_VOICE, "1");

    if (StringUtils.isNotBlank(subject.getHelpLine())) {
      builder.queryParam(UmurinziConstants.HELP_LINE_PROPERTY, subject.getHelpLine());
    }

    if (StringUtils.isNotBlank(subject.getName())) {
      builder.queryParam(UmurinziConstants.NAME_PROPERTY, subject.getName());
    }

    return sendIvrRequest(builder, HttpMethod.POST);
  }

  public String updateSubscriber(Subject subject) {
    if (StringUtils.isBlank(subject.getPhoneNumber())) {
      return null;
    }

    if (StringUtils.isBlank(subject.getIvrId())) {
      return createSubscriber(subject);
    }

    Config config = configService.getConfig();
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(config.getIvrUrl()
        + UmurinziConstants.SUBSCRIBERS_URL + "/" + subject.getIvrId());

    builder.queryParam(UmurinziConstants.API_KEY, config.getApiKey());
    builder.queryParam(UmurinziConstants.PHONE, subject.getPhoneNumber());

    if (StringUtils.isNotBlank(subject.getHelpLine())) {
      builder.queryParam(UmurinziConstants.HELP_LINE_PROPERTY, subject.getHelpLine());
    }

    if (StringUtils.isNotBlank(subject.getName())) {
      builder.queryParam(UmurinziConstants.NAME_PROPERTY, subject.getName());
    }

    return sendIvrRequest(builder, HttpMethod.PUT);
  }

  private String sendIvrRequest(UriComponentsBuilder builder, HttpMethod method) {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

    HttpEntity<?> request = new HttpEntity<>(headers);

    try {
      ResponseEntity<VotoResponseDto> responseEntity = restTemplate.exchange(builder.build().toString(),
          method, request, VotoResponseDto.class);

      if (!HttpStatus.CREATED.equals(responseEntity.getStatusCode()) && !HttpStatus.OK.equals(responseEntity.getStatusCode())) {
        String message = "Invalid IVR service response: " + responseEntity.getStatusCode();
        if (responseEntity.getBody() != null && responseEntity.getBody().getMessage() != null) {
          message = message + ", Response body: " + responseEntity.getBody().getMessage();
        }

        throw new IvrException(message);
      }

      return responseEntity.getBody().getData();
    } catch (Exception ex) {
      String message = "Error occurred when sending request to IVR service: " + ex.getMessage();
      throw new IvrException(message, ex);
    }
  }
}
