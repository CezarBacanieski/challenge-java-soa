package br.com.fiap.fordvinshare.controller;

import br.com.fiap.fordvinshare.dto.request.ManutencaoRequest;
import br.com.fiap.fordvinshare.dto.response.ManutencaoResponse;
import br.com.fiap.fordvinshare.service.ManutencaoService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/manutencoes")
@Tag(name = "Manutenções", description = "Registro de manutenções (dentro ou fora da rede oficial)")
public class ManutencaoController {

	private final ManutencaoService manutencaoService;

	@PostMapping
	@Operation(summary = "Registrar manutenção", operationId = "registrarManutencao",
			description = "Na rede oficial, concessionariaId é obrigatório. O status de uso da rede do veículo "
					+ "é recalculado com base na manutenção mais recente.")
	public ResponseEntity<ManutencaoResponse> registrar(@Valid @RequestBody ManutencaoRequest request) {
		ManutencaoResponse criada = manutencaoService.registrar(request);
		return ResponseEntity.created(LocationUtil.of("/api/manutencoes/{id}", criada.getId())).body(criada);
	}

	@GetMapping
	@Operation(summary = "Listar todas as manutenções", operationId = "listarManutencoes")
	public List<ManutencaoResponse> listarTodas() {
		return manutencaoService.listarTodas();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Buscar manutenção por ID", operationId = "buscarManutencao")
	public ManutencaoResponse buscarPorId(@PathVariable Long id) {
		return manutencaoService.buscarPorId(id);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Remover manutenção (ADMIN)", operationId = "deletarManutencao")
	public ResponseEntity<Void> deletar(@PathVariable Long id) {
		manutencaoService.deletar(id);
		return ResponseEntity.noContent().build();
	}
}
