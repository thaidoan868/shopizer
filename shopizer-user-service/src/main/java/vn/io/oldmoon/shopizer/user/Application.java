package vn.io.oldmoon.shopizer.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(
    basePackages = {
      "vn.io.oldmoon.shopizer.common",
      "vn.io.oldmoon.shopizer.rabbitmq",
      "vn.io.oldmoon.shopizer"
    })
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
