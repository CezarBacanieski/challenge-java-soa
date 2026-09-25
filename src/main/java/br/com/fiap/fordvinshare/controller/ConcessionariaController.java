package br.com.fiap.fordvinshare.controller;

import br.com.fiap.fordvinshare.dto.request.ConcessionariaRequest;
import br.com.fiap.fordvinshare.dto.response.ConcessionariaResponse;
import br.com.fiap.fordvinshare.dto.response.IndicadoresConcessionariaResponse;
import br.com.fiap.fordvinshare.security.UsuarioAutenticado;
import br.com.fiap.fordvinshare.service.ConcessionariaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/api/concessionarias")
@Tag(name = "Concessionárias", description = "Rede de concessionárias Ford e seus indicadores de retenção")
public class ConcessionariaController {

	private final ConcessionariaService concessionariaService;

	@PostMapping
	@Operation(summary = "Cadastrar concessionária (ADMIN)", operationId = "cadastrarConcessionaria")
	public ResponseEntity<ConcessionariaResponse> cadastrar(@Valid @RequestBody ConcessionariaRequest request) {
		ConcessionariaResponse criada = concessionariaService.cadastrar(request);
		return ResponseEntity.created(LocationUtil.of("/api/concessionarias/{id}", criada.getId())).body(criada);
	}

	@GetMapping
	@Operation(summary = "Listar concessionárias", operationId = "listarConcessionarias")
	public List<ConcessionariaResponse> listarTodas() {
		return concessionariaService.listarTodas();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Buscar concessionária por ID", operationId = "buscarConcessionaria")
	public ConcessionariaResponse buscarPorId(@PathVariable Long id) {
		return concessionariaService.buscarPorId(id);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Atualizar concessionária (ADMIN)", operationId = "atualizarConcessionaria")
	public ConcessionariaResponse atualizar(@PathVariable Long id, @Valid @RequestBody ConcessionariaRequest request) {
		return concessionariaService.atualizar(id, request);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Remover concessionária (ADMIN)", operationId = "deletarConcessionaria")
	public ResponseEntity<Void> deletar(@PathVariable Long id) {
		concessionariaService.deletar(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{id}/indicadores")
	@Operation(summary = "Indicadores de retenção da concessionária (ADMIN ou GESTOR da própria concessionária)",
			operationId = "indicadoresConcessionaria")
	public IndicadoresConcessionariaResponse indicadores(
			@PathVariable Long id,
			@AuthenticationPrincipal UsuarioAutenticado usuario) {
		return concessionariaService.obterIndicadores(id, usuario);
	}
}
