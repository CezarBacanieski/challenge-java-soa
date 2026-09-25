package br.com.fiap.fordvinshare.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.Set;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	private static final String BEARER = "bearerAuth";

	@Bean
	public OpenAPI fordVinShareOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Ford VIN Share API")
						.description("""
								API REST para retenção de clientes pós-venda Ford: clientes, veículos, manutenções, \
								concessionárias, indicadores de VIN Share e leads proativos.

								**Como autenticar:** faça `POST /api/auth/login` com um dos usuários de teste, copie o \
								`token` e clique em **Authorize** informando apenas o token.""")
						.version("2.0.0"))
				.components(new Components().addSecuritySchemes(BEARER, new SecurityScheme()
						.type(SecurityScheme.Type.HTTP)
						.scheme("bearer")
						.bearerFormat("JWT")))
				.addSecurityItem(new SecurityRequirement().addList(BEARER));
	}

	/** Operações que só alguns perfis acessam (as demais aceitam qualquer usuário autenticado). */
	private static final Set<String> RESTRITAS_POR_PERFIL = Set.of(
			"cadastrarConcessionaria", "atualizarConcessionaria", "indicadoresConcessionaria",
			"obterResumoDashboard", "criarLeadDoVeiculo", "gerarLeadsAutomatico");

	/** Operações que podem responder 409 (registro duplicado ou exclusão com vínculos). */
	private static final Set<String> COM_CONFLITO = Set.of(
			"cadastrarCliente", "atualizarCliente", "deletarCliente",
			"cadastrarVeiculo", "atualizarVeiculo", "deletarVeiculo",
			"cadastrarConcessionaria", "atualizarConcessionaria", "deletarConcessionaria",
			"cadastrarUsuario");

	/** Operações que podem responder 422 (regra de negócio violada). */
	private static final Set<String> COM_REGRA_DE_NEGOCIO = Set.of("registrarManutencao", "cadastrarUsuario");

	/**
	 * Documenta automaticamente os status de cada operação, para que o contrato no Swagger
	 * reflita exatamente o comportamento real da API (inclusive as regras do SecurityConfig).
	 */
	@Bean
	public OpenApiCustomizer respostasPadraoCustomizer() {
		return openApi -> {
			if (openApi.getPaths() == null) {
				return;
			}
			openApi.getPaths().forEach((path, pathItem) ->
				pathItem.readOperationsMap().forEach((metodo, operacao) -> {
					if (operacao.getResponses() == null) {
						operacao.setResponses(new ApiResponses());
					}
					ApiResponses respostas = operacao.getResponses();
					String id = operacao.getOperationId();
					boolean publico = operacao.getSecurity() != null && operacao.getSecurity().isEmpty();
					boolean temParametros = operacao.getParameters() != null && !operacao.getParameters().isEmpty();

					ajustarSucesso(respostas, metodo, publico);
					if (operacao.getRequestBody() != null || temParametros) {
						adicionar(respostas, "400", "Requisição inválida, campos inválidos ou parâmetro com tipo errado");
					}
					if (publico) {
						adicionar(respostas, "401", "Credenciais inválidas");
					} else {
						adicionar(respostas, "401", "Token ausente, inválido ou expirado");
						if (metodo == PathItem.HttpMethod.DELETE || path.startsWith("/api/usuarios")
								|| RESTRITAS_POR_PERFIL.contains(id)) {
							adicionar(respostas, "403", "Perfil sem permissão para este recurso");
						}
					}
					if (path.contains("{")) {
						adicionar(respostas, "404", "Recurso não encontrado");
					}
					if (COM_CONFLITO.contains(id)) {
						adicionar(respostas, "409", "Conflito: registro duplicado ou com vínculos");
					}
					if (COM_REGRA_DE_NEGOCIO.contains(id)) {
						adicionar(respostas, "422", "Regra de negócio violada");
					}
				}));
		};
	}

	private static void adicionar(ApiResponses respostas, String codigo, String descricao) {
		respostas.computeIfAbsent(codigo, c -> new ApiResponse().description(descricao));
	}

	/** O springdoc assume 200 para tudo; aqui alinhamos com o que os controllers realmente devolvem. */
	private static void ajustarSucesso(ApiResponses respostas, PathItem.HttpMethod metodo, boolean publico) {
		ApiResponse ok = respostas.get("200");
		if (ok == null) {
			return;
		}
		if (metodo == PathItem.HttpMethod.DELETE) {
			respostas.remove("200");
			respostas.addApiResponse("204", new ApiResponse().description("Removido com sucesso"));
		} else if (metodo == PathItem.HttpMethod.POST && !publico) {
			respostas.remove("200");
			respostas.addApiResponse("201", ok.description("Criado; o header Location aponta para o novo recurso"));
		}
	}
}
