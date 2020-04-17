package org.motechproject.umurinzi.dto;

import java.io.ByteArrayOutputStream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
public class ExportResult {

  @Getter
  @Setter
  private String fileName;

  @Getter
  @Setter
  private String exportFormat;

  @Getter
  @Setter
  private ByteArrayOutputStream outputStream;
}
