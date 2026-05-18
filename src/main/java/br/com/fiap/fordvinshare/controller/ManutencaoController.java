package br.com.fiap.fordvinshare.controller;

import br.com.fiap.fordvinshare.dto.request.ManutencaoRequest;
import br.com.fiap.fordvinshare.dto.response.ManutencaoResponse;
import br.com.fiap.fordvinshare.service.ManutencaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/manutencoes")
@Tag(name = "Manutenções", description = "Operações para registro e histórico de manutenções")
public class ManutencaoController {

	private final ManutencaoService manutencaoService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Registrar manutenção", operationId = "registrarManutencao")
	public ManutencaoResponse registrar(@Valid @RequestBody ManutencaoRequest request) {
		return manutencaoService.registrar(request);
	}

	@GetMapping("/veiculo/{veiculoId}")
	@Operation(summary = "Listar histórico de manutenções de um veículo", operationId = "listarManutencoesPorVeiculo")
	public List<ManutencaoResponse> listarPorVeiculo(@PathVariable Long veiculoId) {
		return manutencaoService.listarPorVeiculo(veiculoId);
	}

	@GetMapping
	@Operation(summary = "Listar todas as manutenções", operationId = "listarManutencoes")
	public List<ManutencaoResponse> listarTodas() {
		return manutencaoService.listarTodas();
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Deletar manutenção", operationId = "deletarManutencao")
	public ResponseEntity<Void> deletar(@PathVariable Long id) {
		manutencaoService.deletar(id);
		return ResponseEntity.noContent().build();
	}
}
