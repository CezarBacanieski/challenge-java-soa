package br.com.fiap.fordvinshare.controller;

import br.com.fiap.fordvinshare.dto.request.VeiculoRequest;
import br.com.fiap.fordvinshare.dto.response.VeiculoResponse;
import br.com.fiap.fordvinshare.service.VeiculoService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/veiculos")
@Tag(name = "Veículos", description = "Operações para cadastro e consulta de veículos")
public class VeiculoController {

	private final VeiculoService veiculoService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Cadastrar veículo vinculado a um cliente", operationId = "cadastrarVeiculo")
	public VeiculoResponse cadastrar(@Valid @RequestBody VeiculoRequest request) {
		return veiculoService.cadastrar(request);
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

	@GetMapping("/cliente/{clienteId}")
	@Operation(summary = "Listar veículos de um cliente", operationId = "listarVeiculosPorCliente")
	public List<VeiculoResponse> listarPorCliente(@PathVariable Long clienteId) {
		return veiculoService.listarPorCliente(clienteId);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Atualizar veículo", operationId = "atualizarVeiculo")
	public VeiculoResponse atualizar(@PathVariable Long id, @Valid @RequestBody VeiculoRequest request) {
		return veiculoService.atualizar(id, request);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Deletar veículo", operationId = "deletarVeiculo")
	public ResponseEntity<Void> deletar(@PathVariable Long id) {
		veiculoService.deletar(id);
		return ResponseEntity.noContent().build();
	}
}
