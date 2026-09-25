package br.com.fiap.fordvinshare.api;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.fiap.fordvinshare.entity.Concessionaria;
import br.com.fiap.fordvinshare.repository.ConcessionariaRepository;
import br.com.fiap.fordvinshare.support.IntegrationTestSupport;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

@DisplayName("Regras de negócio: manutenções, VIN Share e leads")
class ManutencaoLeadIntegrationTest extends IntegrationTestSupport {

	@Autowired
	private ConcessionariaRepository concessionariaRepository;

	private Long concessionariaCentro() {
		return concessionariaRepository.findAll().stream()
				.filter(c -> c.getCnpj().equals("11222333000181"))
				.map(Concessionaria::getId)
				.findFirst()
				.orElseThrow();
	}

	private void registrarManutencao(Long veiculoId, LocalDate data, boolean redeOficial, Long concessionariaId)
			throws Exception {
		String concessionaria = concessionariaId == null ? "null" : concessionariaId.toString();
		mockMvc.perform(post("/api/manutencoes")
						.header("Authorization", bearer(CONSULTOR))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"veiculoId": %d, "concessionariaId": %s, "dataServico": "%s",
								 "tipoServico": "Revisão", "valor": 500.00, "realizadaNaRedeOficial": %s}
								""".formatted(veiculoId, concessionaria, data, redeOficial)))
				.andExpect(status().isCreated());
	}

	@Test
	@DisplayName("Manutenção na rede oficial sem concessionária retorna 422")
	void redeOficialSemConcessionaria() throws Exception {
		Long veiculoId = criarVeiculo(criarCliente(novoEmail()), novoVin());

		mockMvc.perform(post("/api/manutencoes")
						.header("Authorization", bearer(CONSULTOR))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"veiculoId": %d, "dataServico": "%s", "tipoServico": "Revisão", "realizadaNaRedeOficial": true}
								""".formatted(veiculoId, LocalDate.now())))
				.andExpect(status().isUnprocessableEntity());
	}

	@Test
	@DisplayName("Manutenção com data futura retorna 400")
	void dataFutura() throws Exception {
		Long veiculoId = criarVeiculo(criarCliente(novoEmail()), novoVin());

		mockMvc.perform(post("/api/manutencoes")
						.header("Authorization", bearer(CONSULTOR))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"veiculoId": %d, "dataServico": "%s", "tipoServico": "Revisão", "realizadaNaRedeOficial": false}
								""".formatted(veiculoId, LocalDate.now().plusDays(10))))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("Veículo que sai da rede e volta é contabilizado novamente (uso da rede segue a última manutenção)")
	void clienteVoltaParaRede() throws Exception {
		Long veiculoId = criarVeiculo(criarCliente(novoEmail()), novoVin());

		registrarManutencao(veiculoId, LocalDate.now().minusMonths(2), false, null);
		mockMvc.perform(get("/api/veiculos/{id}", veiculoId).header("Authorization", bearer(CONSULTOR)))
				.andExpect(jsonPath("$.utilizaRedeOficial").value(false));

		registrarManutencao(veiculoId, LocalDate.now(), true, concessionariaCentro());
		mockMvc.perform(get("/api/veiculos/{id}", veiculoId).header("Authorization", bearer(CONSULTOR)))
				.andExpect(jsonPath("$.utilizaRedeOficial").value(true));

		mockMvc.perform(get("/api/veiculos/{id}/manutencoes", veiculoId).header("Authorization", bearer(CONSULTOR)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)));
	}

	@Test
	@DisplayName("Indicadores da concessionária refletem os veículos atendidos e retidos")
	void indicadoresConcessionaria() throws Exception {
		Long centro = concessionariaCentro();
		Long veiculoId = criarVeiculo(criarCliente(novoEmail()), novoVin());
		registrarManutencao(veiculoId, LocalDate.now(), true, centro);

		mockMvc.perform(get("/api/concessionarias/{id}/indicadores", centro).header("Authorization", bearer(GESTOR)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.veiculosAtendidos").value(1))
				.andExpect(jsonPath("$.veiculosRetidos").value(1))
				.andExpect(jsonPath("$.taxaRetencao").value(100.0));
	}

	@Test
	@DisplayName("Geração automática cria lead para veículo sem manutenção e não duplica leads pendentes")
	void geracaoAutomaticaDeLeads() throws Exception {
		criarVeiculo(criarCliente(novoEmail()), novoVin());

		mockMvc.perform(post("/api/leads/geracoes-automaticas").header("Authorization", bearer(GESTOR)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].prioridade").value("ALTA"))
				.andExpect(jsonPath("$[0].status").value("PENDENTE"));

		mockMvc.perform(post("/api/leads/geracoes-automaticas").header("Authorization", bearer(GESTOR)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	@DisplayName("Lead manual (POST /veiculos/{id}/leads) e atualização parcial via PATCH")
	void leadManualEPatch() throws Exception {
		Long veiculoId = criarVeiculo(criarCliente(novoEmail()), novoVin());

		Long leadId = id(mockMvc.perform(post("/api/veiculos/{id}/leads", veiculoId)
						.header("Authorization", bearer(GESTOR))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"motivoLead": "Garantia expirando", "prioridade": "ALTA"}
								"""))
				.andExpect(status().isCreated()));

		mockMvc.perform(patch("/api/leads/{id}", leadId)
						.header("Authorization", bearer(CONSULTOR))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"status": "CONTATADO"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CONTATADO"));

		mockMvc.perform(get("/api/leads").param("status", "CONTATADO").header("Authorization", bearer(CONSULTOR)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)));
	}

	@Test
	@DisplayName("Filtro de lead com status inexistente retorna 400")
	void statusInvalido() throws Exception {
		mockMvc.perform(get("/api/leads").param("status", "INEXISTENTE").header("Authorization", bearer(CONSULTOR)))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("Lead inexistente retorna 404")
	void leadInexistente() throws Exception {
		mockMvc.perform(get("/api/leads/{id}", 999999).header("Authorization", bearer(CONSULTOR)))
				.andExpect(status().isNotFound());
	}
}
