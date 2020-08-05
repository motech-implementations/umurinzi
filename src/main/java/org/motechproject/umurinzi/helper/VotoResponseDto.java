package org.motechproject.umurinzi.helper;

import lombok.Getter;
import lombok.Setter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VotoResponseDto {

  @Getter
  @Setter
  private String status;

  @Getter
  @Setter
  private String code;

  @Getter
  @Setter
  private String data;

  @Getter
  @Setter
  private String message;

  @JsonProperty("more_info")
  @Getter
  @Setter
  private String moreInfo;
}
