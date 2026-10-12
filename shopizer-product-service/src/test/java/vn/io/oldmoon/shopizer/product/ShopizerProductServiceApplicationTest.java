package vn.io.oldmoon.shopizer.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

class ShopizerProductServiceApplicationTest {

  @Test
  @DisplayName("test ShopizerProductServiceApplication main and instantiation")
  void testApplication() {
    ShopizerProductServiceApplication application = new ShopizerProductServiceApplication();
    assertThat(application).isNotNull();

    try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
      mocked
          .when(() -> SpringApplication.run(ShopizerProductServiceApplication.class, new String[] {}))
          .thenReturn(null);

      ShopizerProductServiceApplication.main(new String[] {});

      mocked.verify(
          () -> SpringApplication.run(ShopizerProductServiceApplication.class, new String[] {}));
    }
  }
}
