package org.motechproject.umurinzi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import org.motechproject.umurinzi.domain.Subject;
import org.motechproject.umurinzi.dto.ZetesSubjectDto;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ZetesMapper {

  ZetesMapper INSTANCE = Mappers.getMapper(ZetesMapper.class);

  Subject fromDto(ZetesSubjectDto zetesSubjectDto);

  void updateFromDto(ZetesSubjectDto zetesSubjectDto, @MappingTarget Subject subject);
}
