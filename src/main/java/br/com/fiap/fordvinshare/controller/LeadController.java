package br.com.fiap.fordvinshare.controller;

import br.com.fiap.fordvinshare.dto.request.LeadRequest;
import br.com.fiap.fordvinshare.dto.request.LeadStatusRequest;
import br.com.fiap.fordvinshare.dto.response.LeadResponse;
import br.com.fiap.fordvinshare.entity.StatusLead;
import br.com.fiap.fordvinshare.service.LeadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/leads")
@Tag(name = "Leads", description = "Operações para geração e acompanhamento de leads")
public class LeadController {

	private final LeadService leadService;

	@PostMapping("/gerar/{veiculoId}")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Gerar lead manualmente para um veículo", operationId = "gerarLeadManual")
	@PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
	public LeadResponse gerarManual(
			@PathVariable Long veiculoId,
			@Valid @RequestBody(required = false) LeadRequest request) {
		return leadService.gerarManual(veiculoId, request);
	}

	@PostMapping("/gerar-automatico")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Gerar leads automáticos para veículos em risco", operationId = "gerarLeadsAutomatico")
	@PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
	public List<LeadResponse> gerarAutomatico() {
		return leadService.gerarAutomatico();
	}

	@GetMapping
	@Operation(summary = "Listar todos os leads", operationId = "listarLeads")
	@PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
	public List<LeadResponse> listarTodos() {
		return leadService.listarTodos();
	}

	@GetMapping("/status/{status}")
	@Operation(summary = "Filtrar leads por status", operationId = "listarLeadsPorStatus")
	@PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
	public List<LeadResponse> listarPorStatus(@PathVariable StatusLead status) {
		return leadService.listarPorStatus(status);
	}

	@PutMapping("/{id}/status")
	@Operation(summary = "Atualizar status do lead", operationId = "atualizarStatusLead")
	@PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
	public LeadResponse atualizarStatus(@PathVariable Long id, @Valid @RequestBody LeadStatusRequest request) {
		return leadService.atualizarStatus(id, request);
	}
}
