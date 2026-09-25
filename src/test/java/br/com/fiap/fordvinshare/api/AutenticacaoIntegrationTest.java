package br.com.fiap.fordvinshare.api;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.fiap.fordvinshare.entity.Usuario;
import br.com.fiap.fordvinshare.repository.UsuarioRepository;
import br.com.fiap.fordvinshare.security.JwtService;
import br.com.fiap.fordvinshare.support.IntegrationTestSupport;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

@DisplayName("Autenticação e JWT")
class AutenticacaoIntegrationTest extends IntegrationTestSupport {

	@Autowired
	private JwtService jwtService;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Test
	@DisplayName("Login válido retorna 200 com token Bearer e perfil")
	void loginValido() throws Exception {
		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email": "admin@ford.com", "senha": "Admin@123"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isNotEmpty())
				.andExpect(jsonPath("$.tipo").value("Bearer"))
				.andExpect(jsonPath("$.perfil").value("ADMIN"))
				.andExpect(jsonPath("$.expiraEmSegundos").value(3600));
	}

	@Test
	@DisplayName("Login com senha errada retorna 401")
	void loginSenhaErrada() throws Exception {
		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email": "admin@ford.com", "senha": "SenhaErrada@1"}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401));
	}

	@Test
	@DisplayName("Login com e-mail inexistente retorna 401 (mesma resposta, sem revelar o e-mail)")
	void loginEmailInexistente() throws Exception {
		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email": "ninguem@ford.com", "senha": "Admin@123"}
								"""))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("Login com corpo inválido retorna 400 com lista de campos")
	void loginCorpoInvalido() throws Exception {
		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email": "nao-e-email", "senha": ""}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.campos").isArray());
	}

	@Test
	@DisplayName("Rota protegida sem token retorna 401 no formato padrão de erro")
	void semToken() throws Exception {
		mockMvc.perform(get("/api/clientes"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.path").value("/api/clientes"));
	}

	@Test
	@DisplayName("Token malformado retorna 401")
	void tokenMalformado() throws Exception {
		mockMvc.perform(get("/api/clientes").header("Authorization", "Bearer isto.nao.e-um-jwt"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.erro", containsString("Token")));
	}

	@Test
	@DisplayName("Token expirado retorna 401 com a mensagem 'Token expirado'")
	void tokenExpirado() throws Exception {
		Usuario consultor = usuarioRepository.findByEmailIgnoreCase(CONSULTOR).orElseThrow();
		String expirado = jwtService.gerarToken(consultor, Duration.ofMinutes(-5));

		mockMvc.perform(get("/api/clientes").header("Authorization", "Bearer " + expirado))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.erro").value("Token expirado"));
	}

	@Test
	@DisplayName("GET /api/auth/me devolve o usuário do token e nunca expõe a senha")
	void usuarioAtual() throws Exception {
		mockMvc.perform(get("/api/auth/me").header("Authorization", bearer(GESTOR)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value(GESTOR))
				.andExpect(jsonPath("$.perfil").value("GESTOR"))
				.andExpect(jsonPath("$.concessionariaId").isNumber())
				.andExpect(jsonPath("$.senha").doesNotExist());
	}

	@Test
	@DisplayName("Documentação OpenAPI é pública")
	void swaggerPublico() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk());
	}
}
