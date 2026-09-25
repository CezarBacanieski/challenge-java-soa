package br.com.fiap.fordvinshare.api;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.fiap.fordvinshare.entity.Concessionaria;
import br.com.fiap.fordvinshare.repository.ConcessionariaRepository;
import br.com.fiap.fordvinshare.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

@DisplayName("Autorização por perfil (ADMIN, GESTOR, CONSULTOR)")
class AutorizacaoPerfisIntegrationTest extends IntegrationTestSupport {

	private static final String CNPJ_CENTRO = "11222333000181";
	private static final String CNPJ_CAMPINAS = "44555666000172";

	@Autowired
	private ConcessionariaRepository concessionariaRepository;

	private Long concessionariaPorCnpj(String cnpj) {
		return concessionariaRepository.findAll().stream()
				.filter(c -> c.getCnpj().equals(cnpj))
				.map(Concessionaria::getId)
				.findFirst()
				.orElseThrow();
	}

	@Test
	@DisplayName("CONSULTOR não acessa o dashboard (403)")
	void consultorSemDashboard() throws Exception {
		mockMvc.perform(get("/api/dashboard/resumo").header("Authorization", bearer(CONSULTOR)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.status").value(403));
	}

	@Test
	@DisplayName("GESTOR acessa o dashboard (200)")
	void gestorComDashboard() throws Exception {
		mockMvc.perform(get("/api/dashboard/resumo").header("Authorization", bearer(GESTOR)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.percentualVeiculosRedeOficial").exists());
	}

	@Test
	@DisplayName("CONSULTOR não remove cliente (403); ADMIN remove (204)")
	void exclusaoSomenteAdmin() throws Exception {
		Long clienteId = criarCliente(novoEmail());

		mockMvc.perform(delete("/api/clientes/{id}", clienteId).header("Authorization", bearer(CONSULTOR)))
				.andExpect(status().isForbidden());

		mockMvc.perform(delete("/api/clientes/{id}", clienteId).header("Authorization", bearer(ADMIN)))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/clientes/{id}", clienteId).header("Authorization", bearer(ADMIN)))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("GESTOR não gerencia usuários (403)")
	void gestorNaoCriaUsuario() throws Exception {
		mockMvc.perform(post("/api/usuarios")
						.header("Authorization", bearer(GESTOR))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"nome": "Novo", "email": "novo@ford.com", "senha": "Senha@123", "perfil": "ADMIN"}
								"""))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("ADMIN cria usuário (201 + Location) sem expor a senha")
	void adminCriaUsuario() throws Exception {
		Long centro = concessionariaPorCnpj(CNPJ_CENTRO);

		mockMvc.perform(post("/api/usuarios")
						.header("Authorization", bearer(ADMIN))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"nome": "Consultor Novo", "email": "consultor.novo@ford.com", "senha": "Senha@123",
								 "perfil": "CONSULTOR", "concessionariaId": %d}
								""".formatted(centro)))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", containsString("/api/usuarios/")))
				.andExpect(jsonPath("$.perfil").value("CONSULTOR"))
				.andExpect(jsonPath("$.senha").doesNotExist());
	}

	@Test
	@DisplayName("Usuário GESTOR sem concessionária é rejeitado (422)")
	void gestorSemConcessionaria() throws Exception {
		mockMvc.perform(post("/api/usuarios")
						.header("Authorization", bearer(ADMIN))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"nome": "Gestor", "email": "gestor.sem@ford.com", "senha": "Senha@123", "perfil": "GESTOR"}
								"""))
				.andExpect(status().isUnprocessableEntity());
	}

	@Test
	@DisplayName("Senha fraca é rejeitada (400)")
	void senhaFraca() throws Exception {
		mockMvc.perform(post("/api/usuarios")
						.header("Authorization", bearer(ADMIN))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"nome": "Admin 2", "email": "admin2@ford.com", "senha": "123", "perfil": "ADMIN"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.campos[0].campo").value("senha"));
	}

	@Test
	@DisplayName("GESTOR consulta indicadores da própria concessionária (200)")
	void gestorPropriaConcessionaria() throws Exception {
		Long centro = concessionariaPorCnpj(CNPJ_CENTRO);

		mockMvc.perform(get("/api/concessionarias/{id}/indicadores", centro).header("Authorization", bearer(GESTOR)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.concessionariaId").value(centro.intValue()));
	}

	@Test
	@DisplayName("GESTOR não consulta indicadores de outra concessionária (403) — regra baseada no claim do token")
	void gestorOutraConcessionaria() throws Exception {
		Long campinas = concessionariaPorCnpj(CNPJ_CAMPINAS);

		mockMvc.perform(get("/api/concessionarias/{id}/indicadores", campinas).header("Authorization", bearer(GESTOR)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.erro", containsString("sua concession")));
	}

	@Test
	@DisplayName("ADMIN consulta indicadores de qualquer concessionária (200)")
	void adminQualquerConcessionaria() throws Exception {
		Long campinas = concessionariaPorCnpj(CNPJ_CAMPINAS);

		mockMvc.perform(get("/api/concessionarias/{id}/indicadores", campinas).header("Authorization", bearer(ADMIN)))
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("CONSULTOR não consulta indicadores de concessionária (403)")
	void consultorSemIndicadores() throws Exception {
		Long centro = concessionariaPorCnpj(CNPJ_CENTRO);

		mockMvc.perform(get("/api/concessionarias/{id}/indicadores", centro).header("Authorization", bearer(CONSULTOR)))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("CONSULTOR não dispara a geração automática de leads (403)")
	void consultorSemGeracaoAutomatica() throws Exception {
		mockMvc.perform(post("/api/leads/geracoes-automaticas").header("Authorization", bearer(CONSULTOR)))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("Somente ADMIN cadastra concessionária (403 para GESTOR)")
	void gestorNaoCriaConcessionaria() throws Exception {
		mockMvc.perform(post("/api/concessionarias")
						.header("Authorization", bearer(GESTOR))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"nome": "Ford Santos", "cnpj": "77888999000163", "cidade": "Santos", "uf": "SP"}
								"""))
				.andExpect(status().isForbidden());
	}
}
