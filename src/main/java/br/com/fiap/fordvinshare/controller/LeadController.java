package br.com.fiap.fordvinshare.controller;

import br.com.fiap.fordvinshare.dto.request.LeadStatusRequest;
import br.com.fiap.fordvinshare.dto.response.LeadResponse;
import br.com.fiap.fordvinshare.entity.StatusLead;
import br.com.fiap.fordvinshare.service.LeadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/leads")
@Tag(name = "Leads", description = "Leads proativos de serviço para as concessionárias")
public class LeadController {

	private final LeadService leadService;

	@GetMapping
	@Operation(summary = "Listar leads (filtro opcional por status)", operationId = "listarLeads")
	public List<LeadResponse> listar(
			@Parameter(description = "PENDENTE, CONTATADO, CONVERTIDO ou PERDIDO")
			@RequestParam(required = false) StatusLead status) {
		return leadService.listar(status);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Buscar lead por ID", operationId = "buscarLead")
	public LeadResponse buscarPorId(@PathVariable Long id) {
		return leadService.buscarPorId(id);
	}

	@PatchMapping("/{id}")
	@Operation(summary = "Atualizar status do lead", operationId = "atualizarStatusLead")
	public LeadResponse atualizarStatus(@PathVariable Long id, @Valid @RequestBody LeadStatusRequest request) {
		return leadService.atualizarStatus(id, request);
	}

	/**
	 * Cria um recurso "geração automática": varre a frota e gera leads para veículos em risco.
	 * Devolve os leads criados nesta execução.
	 */
	@PostMapping("/geracoes-automaticas")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Executar geração automática de leads (ADMIN ou GESTOR)", operationId = "gerarLeadsAutomatico",
			description = "Gera leads para veículos sem manutenção há mais de 180 dias (ALTA) ou fora da rede oficial (MEDIA).")
	public List<LeadResponse> gerarAutomatico() {
		return leadService.gerarAutomatico();
	}
}
