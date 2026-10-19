package vn.io.oldmoon.shopizer.product.app.dto.media;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import vn.io.oldmoon.shopizer.product.infra.model.media.File;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MediaMapper {

  @Mapping(target = "url", ignore = true)
  FileResponse toDto(File file);
}
