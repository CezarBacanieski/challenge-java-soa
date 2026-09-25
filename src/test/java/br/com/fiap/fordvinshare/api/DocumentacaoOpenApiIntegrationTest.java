package br.com.fiap.fordvinshare.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.fiap.fordvinshare.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Documentação OpenAPI coerente com a API")
class DocumentacaoOpenApiIntegrationTest extends IntegrationTestSupport {

	@Test
	@DisplayName("Criações documentam 201 e exclusões 204; o login público documenta 200 e não exige token")
	void statusDeSucessoDocumentados() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.paths['/api/clientes'].post.responses['201']").exists())
				.andExpect(jsonPath("$.paths['/api/clientes'].post.responses['200']").doesNotExist())
				.andExpect(jsonPath("$.paths['/api/clientes/{id}'].delete.responses['204']").exists())
				.andExpect(jsonPath("$.paths['/api/auth/login'].post.responses['200']").exists())
				.andExpect(jsonPath("$.paths['/api/auth/login'].post.security").isEmpty());
	}

	@Test
	@DisplayName("Rotas protegidas documentam 401, rotas restritas por perfil documentam 403 e regras de negócio 422")
	void statusDeErroDocumentados() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
				.andExpect(jsonPath("$.paths['/api/clientes'].get.responses['401']").exists())
				.andExpect(jsonPath("$.paths['/api/dashboard/resumo'].get.responses['403']").exists())
				.andExpect(jsonPath("$.paths['/api/manutencoes'].post.responses['422']").exists())
				.andExpect(jsonPath("$.paths['/api/clientes'].post.responses['409']").exists());
	}
}
