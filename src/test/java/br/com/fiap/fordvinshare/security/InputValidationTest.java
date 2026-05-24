package br.com.fiap.fordvinshare.security;

import br.com.fiap.fordvinshare.security.auth.AppUser;
import br.com.fiap.fordvinshare.security.auth.AppUserRepository;
import br.com.fiap.fordvinshare.security.auth.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class InputValidationTest {
	@Autowired WebApplicationContext wac;
	MockMvc mvc;
	@Autowired AppUserRepository users;
	@Autowired PasswordEncoder encoder;

	@BeforeEach
	void setup() {
		this.mvc = MockMvcBuilders.webAppContextSetup(this.wac).apply(springSecurity()).build();
	}

	@Test
	void invalidVinIsRejected() throws Exception {
		AppUser admin = AppUser.builder()
				.email("admin2@example.com")
				.passwordHash(encoder.encode("StrongAdmin!123"))
				.role(Role.ADMIN)
				.enabled(true)
				.failedLoginAttempts(0)
				.tokenVersion(0)
				.build();
		users.save(admin);

		var login = mvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"admin2@example.com\",\"password\":\"StrongAdmin!123\"}"))
				.andExpect(status().isOk())
				.andReturn();

		String body = login.getResponse().getContentAsString();
		String accessToken = body.split("\"accessToken\":\"")[1].split("\"")[0];

		mvc.perform(post("/api/veiculos")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"vin\":\"../etc/passwd\",\"marca\":\"Ford\",\"modelo\":\"Ka\",\"anoFabricacao\":2020,\"clienteId\":1}"))
				.andExpect(status().isBadRequest());
	}
}
