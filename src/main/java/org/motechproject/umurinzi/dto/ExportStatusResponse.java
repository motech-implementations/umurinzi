package org.motechproject.umurinzi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
public class ExportStatusResponse {

  @Getter
  @Setter
  private ExportStatus status;

  @Getter
  @Setter
  private Double progress;
}
