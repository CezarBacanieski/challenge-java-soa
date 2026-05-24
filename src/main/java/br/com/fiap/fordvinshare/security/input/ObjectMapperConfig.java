package br.com.fiap.fordvinshare.security.input;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ObjectMapperConfig {

	@Bean
	public ObjectMapper objectMapper(List<Module> modules) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
		for (Module m : modules) {
			mapper.registerModule(m);
		}
		return mapper;
	}
}

