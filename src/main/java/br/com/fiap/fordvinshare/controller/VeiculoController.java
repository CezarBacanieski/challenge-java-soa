package br.com.fiap.fordvinshare.controller;

import br.com.fiap.fordvinshare.dto.request.LeadRequest;
import br.com.fiap.fordvinshare.dto.request.VeiculoRequest;
import br.com.fiap.fordvinshare.dto.response.LeadResponse;
import br.com.fiap.fordvinshare.dto.response.ManutencaoResponse;
import br.com.fiap.fordvinshare.dto.response.VeiculoResponse;
import br.com.fiap.fordvinshare.service.LeadService;
import br.com.fiap.fordvinshare.service.ManutencaoService;
import br.com.fiap.fordvinshare.service.VeiculoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/veiculos")
@Tag(name = "Veículos", description = "Cadastro de veículos e seus sub-recursos (manutenções e leads)")
public class VeiculoController {

	private final VeiculoService veiculoService;
	private final ManutencaoService manutencaoService;
	private final LeadService leadService;

	@PostMapping
	@Operation(summary = "Cadastrar veículo vinculado a um cliente", operationId = "cadastrarVeiculo")
	public ResponseEntity<VeiculoResponse> cadastrar(@Valid @RequestBody VeiculoRequest request) {
		VeiculoResponse criado = veiculoService.cadastrar(request);
		return ResponseEntity.created(LocationUtil.of("/api/veiculos/{id}", criado.getId())).body(criado);
	}

	@GetMapping
	@Operation(summary = "Listar todos os veículos", operationId = "listarVeiculos")
	public List<VeiculoResponse> listarTodos() {
		return veiculoService.listarTodos();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Buscar veículo por ID", operationId = "buscarVeiculo")
	public VeiculoResponse buscarPorId(@PathVariable Long id) {
		return veiculoService.buscarPorId(id);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Atualizar veículo", operationId = "atualizarVeiculo")
	public VeiculoResponse atualizar(@PathVariable Long id, @Valid @RequestBody VeiculoRequest request) {
		return veiculoService.atualizar(id, request);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Remover veículo (ADMIN)", operationId = "deletarVeiculo")
	public ResponseEntity<Void> deletar(@PathVariable Long id) {
		veiculoService.deletar(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{id}/manutencoes")
	@Operation(summary = "Histórico de manutenções do veículo", operationId = "listarManutencoesDoVeiculo")
	public List<ManutencaoResponse> listarManutencoes(@PathVariable Long id) {
		return manutencaoService.listarPorVeiculo(id);
	}

	@PostMapping("/{id}/leads")
	@Operation(summary = "Criar lead manual para o veículo (ADMIN ou GESTOR)", operationId = "criarLeadDoVeiculo",
			description = "Corpo opcional. Sem corpo, o motivo é 'Lead gerado manualmente' e a prioridade MEDIA.")
	public ResponseEntity<LeadResponse> criarLead(
			@PathVariable Long id,
			@Valid @RequestBody(required = false) LeadRequest request) {
		LeadResponse criado = leadService.gerarManual(id, request);
		return ResponseEntity.created(LocationUtil.of("/api/leads/{id}", criado.getId())).body(criado);
	}
}
