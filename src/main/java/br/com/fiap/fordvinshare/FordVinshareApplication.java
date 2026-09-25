package br.com.fiap.fordvinshare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

/**
 * A autenticação é feita só por JWT (ver SecurityConfig), então o usuário padrão em memória
 * que o Spring Boot criaria, com senha gerada no log, fica desativado.
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class FordVinshareApplication {

	public static void main(String[] args) {
		SpringApplication.run(FordVinshareApplication.class, args);
	}
}
