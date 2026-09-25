package br.com.fiap.fordvinshare.controller;

import br.com.fiap.fordvinshare.dto.request.ClienteRequest;
import br.com.fiap.fordvinshare.dto.response.ClienteResponse;
import br.com.fiap.fordvinshare.dto.response.VeiculoResponse;
import br.com.fiap.fordvinshare.service.ClienteService;
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
@RequestMapping("/api/clientes")
@Tag(name = "Clientes", description = "Operações para cadastro e manutenção de clientes")
public class ClienteController {

	private final ClienteService clienteService;
	private final VeiculoService veiculoService;

	@PostMapping
	@Operation(summary = "Cadastrar cliente", operationId = "cadastrarCliente")
	public ResponseEntity<ClienteResponse> cadastrar(@Valid @RequestBody ClienteRequest request) {
		ClienteResponse criado = clienteService.cadastrar(request);
		return ResponseEntity.created(LocationUtil.of("/api/clientes/{id}", criado.getId())).body(criado);
	}

	@GetMapping
	@Operation(summary = "Listar todos os clientes", operationId = "listarClientes")
	public List<ClienteResponse> listarTodos() {
		return clienteService.listarTodos();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Buscar cliente por ID", operationId = "buscarCliente")
	public ClienteResponse buscarPorId(@PathVariable Long id) {
		return clienteService.buscarPorId(id);
	}

	@GetMapping("/{id}/veiculos")
	@Operation(summary = "Listar veículos do cliente", operationId = "listarVeiculosDoCliente")
	public List<VeiculoResponse> listarVeiculos(@PathVariable Long id) {
		return veiculoService.listarPorCliente(id);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Atualizar cliente", operationId = "atualizarCliente")
	public ClienteResponse atualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest request) {
		return clienteService.atualizar(id, request);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Remover cliente (ADMIN)", operationId = "deletarCliente")
	public ResponseEntity<Void> deletar(@PathVariable Long id) {
		clienteService.deletar(id);
		return ResponseEntity.noContent().build();
	}
}
