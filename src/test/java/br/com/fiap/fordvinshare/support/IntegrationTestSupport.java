package br.com.fiap.fordvinshare.support;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * Base dos testes de integração: sobe a aplicação completa (com Spring Security e JWT reais)
 * sobre um banco H2 em memória. Cada teste roda em uma transação desfeita ao final.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class IntegrationTestSupport {

	protected static final String ADMIN = "admin@ford.com";
	protected static final String GESTOR = "gestor@ford.com";
	protected static final String GESTOR_CAMPINAS = "gestor.campinas@ford.com";
	protected static final String CONSULTOR = "consultor@ford.com";

	private static final AtomicInteger SEQUENCIA = new AtomicInteger(100000);

	@Autowired
	private WebApplicationContext context;

	protected MockMvc mockMvc;

	@BeforeEach
	void configurarMockMvc() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context)
				.apply(springSecurity())
				.build();
	}

	protected String senhaDe(String email) {
		return switch (email) {
			case ADMIN -> "Admin@123";
			case CONSULTOR -> "Consultor@123";
			default -> "Gestor@123";
		};
	}

	/** Faz login real em /api/auth/login e devolve o header "Bearer <token>". */
	protected String bearer(String email) throws Exception {
		String json = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email": "%s", "senha": "%s"}
								""".formatted(email, senhaDe(email))))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
		return "Bearer " + JsonPath.read(json, "$.token");
	}

	protected static String corpo(ResultActions resultado) throws Exception {
		return resultado.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
	}

	protected static Long id(ResultActions resultado) throws Exception {
		Number id = JsonPath.read(corpo(resultado), "$.id");
		return id.longValue();
	}

	/** VIN válido (17 caracteres, sem I/O/Q) e único a cada chamada. */
	protected static String novoVin() {
		return "9BFZH55L3P8" + SEQUENCIA.incrementAndGet();
	}

	protected static String novoEmail() {
		return "cliente" + SEQUENCIA.incrementAndGet() + "@email.com";
	}

	protected Long criarCliente(String email) throws Exception {
		return id(mockMvc.perform(post("/api/clientes")
						.header("Authorization", bearer(ADMIN))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"nome": "Cliente Teste", "email": "%s", "telefone": "11999999999"}
								""".formatted(email)))
				.andExpect(status().isCreated()));
	}

	protected Long criarVeiculo(Long clienteId, String vin) throws Exception {
		return id(mockMvc.perform(post("/api/veiculos")
						.header("Authorization", bearer(ADMIN))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"vin": "%s", "marca": "Ford", "modelo": "Ranger", "anoFabricacao": 2024, "clienteId": %d}
								""".formatted(vin, clienteId)))
				.andExpect(status().isCreated()));
	}
}
