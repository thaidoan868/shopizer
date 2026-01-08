package vn.io.oldmoon.shopizer.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class ShopizerConfigServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopizerConfigServerApplication.class, args);
	}

}
