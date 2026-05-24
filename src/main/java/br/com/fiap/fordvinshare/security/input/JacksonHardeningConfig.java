package br.com.fiap.fordvinshare.security.input;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonHardeningConfig {

	@Bean
	public Module sanitizingStringsModule() {
		SimpleModule module = new SimpleModule("sanitizing-strings");
		module.addDeserializer(String.class, new SanitizingStringDeserializer());
		return module;
	}
}

