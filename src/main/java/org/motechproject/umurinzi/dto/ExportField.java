package org.motechproject.umurinzi.dto;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class ExportField {

  @Getter
  @Setter
  private String displayName;

  @Getter
  @Setter
  private List<String> path;

  @Getter
  @Setter
  private String fieldType;

  public ExportField(String displayName, String fieldType, String... path) {
    this.displayName = displayName;
    this.fieldType = fieldType;
    this.path = Arrays.asList(path);
  }
}
