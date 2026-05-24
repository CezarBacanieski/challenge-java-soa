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
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class SecuritySmokeTest {

	@Autowired WebApplicationContext wac;
	MockMvc mvc;
	@Autowired AppUserRepository users;
	@Autowired PasswordEncoder encoder;

	@BeforeEach
	void setup() {
		this.mvc = MockMvcBuilders.webAppContextSetup(this.wac).apply(springSecurity()).build();
	}

	@Test
	void protectedEndpointRequiresAuth() throws Exception {
		mvc.perform(get("/api/veiculos"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void registerIssuesRefreshCookieAndAccessToken() throws Exception {
		String email = uniqueEmail("user1");
		mvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"" + email + "\",\"password\":\"StrongPass!123\"}"))
				.andExpect(status().isOk())
				.andExpect(cookie().exists("refresh_token"));
	}

	@Test
	void userCannotCallAdminEndpoint() throws Exception {
		String email = uniqueEmail("user2");
		var reg = mvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"" + email + "\",\"password\":\"StrongPass!123\"}"))
				.andExpect(status().isOk())
				.andReturn();

		String body = reg.getResponse().getContentAsString();
		String accessToken = body.split("\"accessToken\":\"")[1].split("\"")[0];

		mvc.perform(post("/api/clientes")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"nome\":\"Teste\",\"email\":\"t@example.com\",\"telefone\":\"+5511999999999\"}"))
				.andExpect(status().isForbidden());
	}

	@Test
	void adminCanCallAdminEndpoint() throws Exception {
		String adminEmail = uniqueEmail("admin");
		AppUser admin = AppUser.builder()
				.email(adminEmail)
				.passwordHash(encoder.encode("StrongAdmin!123"))
				.role(Role.ADMIN)
				.enabled(true)
				.failedLoginAttempts(0)
				.tokenVersion(0)
				.build();
		users.save(admin);

		var login = mvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"" + adminEmail + "\",\"password\":\"StrongAdmin!123\"}"))
				.andExpect(status().isOk())
				.andExpect(cookie().exists("refresh_token"))
				.andReturn();

		String body = login.getResponse().getContentAsString();
		String accessToken = body.split("\"accessToken\":\"")[1].split("\"")[0];

		mvc.perform(post("/api/clientes")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"nome\":\"Teste\",\"email\":\"cliente@example.com\",\"telefone\":\"+5511999999999\"}"))
				.andExpect(status().isCreated());
	}

	private static String uniqueEmail(String prefix) {
		return prefix + "-" + UUID.randomUUID() + "@example.com";
	}
}
