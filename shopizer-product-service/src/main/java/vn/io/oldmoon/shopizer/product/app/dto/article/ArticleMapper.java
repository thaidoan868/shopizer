package vn.io.oldmoon.shopizer.product.app.dto.article;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import vn.io.oldmoon.shopizer.product.infra.model.media.Article;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    builder = @org.mapstruct.Builder(disableBuilder = true))
public interface ArticleMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "created", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "modified", ignore = true)
  @Mapping(target = "modifiedBy", ignore = true)
  Article toEntity(CreateArticleRequest dto);

  ArticleResponse toDto(Article article);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "created", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "modified", ignore = true)
  @Mapping(target = "modifiedBy", ignore = true)
  void updateEntityFromDto(UpdateArticleRequest dto, @MappingTarget Article article);
}
