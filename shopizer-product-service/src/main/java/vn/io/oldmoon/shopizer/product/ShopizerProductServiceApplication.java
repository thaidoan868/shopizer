package vn.io.oldmoon.shopizer.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EntityScan(basePackages = {"vn.io.oldmoon.shopizer"})
@ComponentScan(basePackages = {"vn.io.oldmoon.shopizer.common", "vn.io.oldmoon.shopizer"})
public class ShopizerProductServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopizerProductServiceApplication.class, args);
	}

}
