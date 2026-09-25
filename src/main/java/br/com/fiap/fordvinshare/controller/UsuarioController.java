package br.com.fiap.fordvinshare.controller;

import br.com.fiap.fordvinshare.dto.request.UsuarioRequest;
import br.com.fiap.fordvinshare.dto.response.UsuarioResponse;
import br.com.fiap.fordvinshare.service.UsuarioService;
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
@RequestMapping("/api/usuarios")
@Tag(name = "Usuários", description = "Gestão de contas de acesso (somente ADMIN)")
public class UsuarioController {

	private final UsuarioService usuarioService;

	@PostMapping
	@Operation(summary = "Cadastrar usuário", operationId = "cadastrarUsuario")
	public ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody UsuarioRequest request) {
		UsuarioResponse criado = usuarioService.cadastrar(request);
		return ResponseEntity.created(LocationUtil.of("/api/usuarios/{id}", criado.getId())).body(criado);
	}

	@GetMapping
	@Operation(summary = "Listar usuários", operationId = "listarUsuarios")
	public List<UsuarioResponse> listarTodos() {
		return usuarioService.listarTodos();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Buscar usuário por ID", operationId = "buscarUsuario")
	public UsuarioResponse buscarPorId(@PathVariable Long id) {
		return usuarioService.buscarPorId(id);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Remover usuário", operationId = "deletarUsuario")
	public ResponseEntity<Void> deletar(@PathVariable Long id) {
		usuarioService.deletar(id);
		return ResponseEntity.noContent().build();
	}
}
