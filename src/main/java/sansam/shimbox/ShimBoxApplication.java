package sansam.shimbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class ShimBoxApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShimBoxApplication.class, args);
	}

}
