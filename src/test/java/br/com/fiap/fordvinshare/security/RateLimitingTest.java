package br.com.fiap.fordvinshare.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
		"app.security.rate-limit.auth-per-ip-per-minute=2",
		"app.security.rate-limit.per-ip-per-minute=2",
		"app.security.rate-limit.per-user-per-minute=2"
})
@ActiveProfiles("test")
class RateLimitingTest {
	@Autowired WebApplicationContext wac;
	MockMvc mvc;

	@BeforeEach
	void setup() {
		this.mvc = MockMvcBuilders.webAppContextSetup(this.wac).apply(springSecurity()).build();
	}

	@Test
	void authEndpointsAreRateLimitedByIp() throws Exception {
		for (int i = 0; i < 2; i++) {
			mvc.perform(post("/api/auth/login")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"email\":\"nope@example.com\",\"password\":\"whatever\"}"))
					.andExpect(status().isUnauthorized());
		}
		mvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"nope@example.com\",\"password\":\"whatever\"}"))
				.andExpect(status().isTooManyRequests());
	}
}
