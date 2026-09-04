package vn.io.oldmoon.shopizer.user.infra.model.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

@Builder
public record Address(
    @Schema(example = "No. 123 Ly Thai To")
    @Length(max = 255, message = "Details address must be 255 characters or fewer")
    String detailsAddress,

    @Schema(example = "Phuong Hoa Hung")
    @Length(max = 100, message = "Ward or commune must be 100 characters or fewer")
    String wardOrCommune,

    @Schema(example = "Tp HCM")
    @Length(max = 100, message = "City or province must be 100 characters or fewer")
    String cityOrProvince,

    @Schema(example = "VN")
    @Length(max = 100, message = "Country must be 100 characters or fewer")
    String country) {}
