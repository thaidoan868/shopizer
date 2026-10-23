package vn.io.oldmoon.shopizer.product.infra.model.media;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VideoQuality {
  P360("360p", 360, "800k"),
  P720("720p", 720, "2500k"),
  P1080("1080p", 1080, "5000k");

  private final String label;
  private final int height;
  private final String videoBitrate;
}
