package vn.io.oldmoon.shopizer.common.web.config;

import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import vn.io.oldmoon.shopizer.common.web.controller.AbstractController;

@Configuration
@EnableJpaAuditing
public class AuditConfig {
  @Bean
  public AuditorAware<UUID> auditorAware() {
    return () -> Optional.of(AbstractController.getCurrentUserId());
  }
}
