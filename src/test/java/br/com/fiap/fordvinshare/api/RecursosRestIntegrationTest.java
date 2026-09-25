package br.com.fiap.fordvinshare.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.fiap.fordvinshare.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

@DisplayName("Recursos REST: clientes e veículos (métodos, status codes e erros)")
class RecursosRestIntegrationTest extends IntegrationTestSupport {

	@Test
	@DisplayName("POST /api/clientes retorna 201 com header Location")
	void criarCliente() throws Exception {
		mockMvc.perform(post("/api/clientes")
						.header("Authorization", bearer(CONSULTOR))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"nome": "Maria Silva", "email": "%s", "telefone": "11999999999"}
								""".formatted(novoEmail())))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", containsString("/api/clientes/")))
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.nome").value("Maria Silva"));
	}

	@Test
	@DisplayName("GET de cliente inexistente retorna 404 no formato padrão")
	void clienteInexistente() throws Exception {
		mockMvc.perform(get("/api/clientes/{id}", 999999).header("Authorization", bearer(CONSULTOR)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.timestamp").exists());
	}

	@Test
	@DisplayName("POST com e-mail inválido retorna 400 indicando o campo")
	void clienteEmailInvalido() throws Exception {
		mockMvc.perform(post("/api/clientes")
						.header("Authorization", bearer(CONSULTOR))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"nome": "Maria", "email": "email-invalido"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.campos[0].campo").value("email"));
	}

	@Test
	@DisplayName("JSON malformado retorna 400")
	void jsonMalformado() throws Exception {
		mockMvc.perform(post("/api/clientes")
						.header("Authorization", bearer(CONSULTOR))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{ nome: "))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("Cliente com e-mail duplicado retorna 409")
	void clienteDuplicado() throws Exception {
		String email = novoEmail();
		criarCliente(email);

		mockMvc.perform(post("/api/clientes")
						.header("Authorization", bearer(CONSULTOR))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"nome": "Outra Pessoa", "email": "%s"}
								""".formatted(email)))
				.andExpect(status().isConflict());
	}

	@Test
	@DisplayName("PUT /api/clientes/{id} atualiza e retorna 200")
	void atualizarCliente() throws Exception {
		Long id = criarCliente(novoEmail());

		mockMvc.perform(put("/api/clientes/{id}", id)
						.header("Authorization", bearer(CONSULTOR))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"nome": "Nome Atualizado", "email": "%s"}
								""".formatted(novoEmail())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("Nome Atualizado"));
	}

	@Test
	@DisplayName("Método não suportado retorna 405")
	void metodoNaoSuportado() throws Exception {
		mockMvc.perform(patch("/api/clientes").header("Authorization", bearer(ADMIN)))
				.andExpect(status().isMethodNotAllowed());
	}

	@Test
	@DisplayName("Veículo com VIN fora do padrão retorna 400")
	void vinInvalido() throws Exception {
		Long clienteId = criarCliente(novoEmail());

		mockMvc.perform(post("/api/veiculos")
						.header("Authorization", bearer(CONSULTOR))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"vin": "ABC123", "marca": "Ford", "modelo": "Ranger", "clienteId": %d}
								""".formatted(clienteId)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.campos[0].campo").value("vin"));
	}

	@Test
	@DisplayName("Veículo com VIN duplicado retorna 409")
	void vinDuplicado() throws Exception {
		Long clienteId = criarCliente(novoEmail());
		String vin = novoVin();
		criarVeiculo(clienteId, vin);

		mockMvc.perform(post("/api/veiculos")
						.header("Authorization", bearer(CONSULTOR))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"vin": "%s", "marca": "Ford", "modelo": "Territory", "clienteId": %d}
								""".formatted(vin, clienteId)))
				.andExpect(status().isConflict());
	}

	@Test
	@DisplayName("Veículo para cliente inexistente retorna 404")
	void veiculoClienteInexistente() throws Exception {
		mockMvc.perform(post("/api/veiculos")
						.header("Authorization", bearer(CONSULTOR))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"vin": "%s", "marca": "Ford", "modelo": "Ranger", "clienteId": 999999}
								""".formatted(novoVin())))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("GET /api/clientes/{id}/veiculos lista o sub-recurso")
	void veiculosDoCliente() throws Exception {
		Long clienteId = criarCliente(novoEmail());
		criarVeiculo(clienteId, novoVin());

		mockMvc.perform(get("/api/clientes/{id}/veiculos", clienteId).header("Authorization", bearer(CONSULTOR)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)));
	}

	@Test
	@DisplayName("Remover cliente com veículos retorna 409")
	void removerClienteComVeiculo() throws Exception {
		Long clienteId = criarCliente(novoEmail());
		criarVeiculo(clienteId, novoVin());

		mockMvc.perform(delete("/api/clientes/{id}", clienteId).header("Authorization", bearer(ADMIN)))
				.andExpect(status().isConflict());
	}
}
