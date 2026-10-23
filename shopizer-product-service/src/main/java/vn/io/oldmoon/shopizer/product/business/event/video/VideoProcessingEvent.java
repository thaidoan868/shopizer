package vn.io.oldmoon.shopizer.product.business.event.video;

import java.util.UUID;
import vn.io.oldmoon.shopizer.common.core.event.ApplicationEvent;
import vn.io.oldmoon.shopizer.product.infra.model.media.File;
import vn.io.oldmoon.shopizer.product.infra.model.media.VideoQuality;

public record VideoProcessingEvent(UUID assetId, File file, VideoQuality targetQuality)
    implements ApplicationEvent {}
