package br.com.fiap.fordvinshare.controller;

import br.com.fiap.fordvinshare.dto.request.LoginRequest;
import br.com.fiap.fordvinshare.dto.response.TokenResponse;
import br.com.fiap.fordvinshare.dto.response.UsuarioResponse;
import br.com.fiap.fordvinshare.security.UsuarioAutenticado;
import br.com.fiap.fordvinshare.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Login e emissão de token JWT")
public class AuthController {

	private final AuthService authService;

	@PostMapping("/login")
	@SecurityRequirements
	@Operation(summary = "Autenticar e obter token JWT", operationId = "login",
			description = "Endpoint público. Devolve um token Bearer com expiração configurável.")
	public TokenResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

	@GetMapping("/me")
	@Operation(summary = "Dados do usuário autenticado", operationId = "usuarioAtual")
	public UsuarioResponse usuarioAtual(@AuthenticationPrincipal UsuarioAutenticado usuario) {
		return authService.usuarioAtual(usuario);
	}
}
