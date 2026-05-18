package br.com.fiap.fordvinshare.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI fordVinShareOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Ford VIN Share API")
						.description("API REST para retenção de clientes pós-venda, monitoramento de veículos, manutenções e geração de leads proativos.")
						.version("1.0.0"));
	}
}
