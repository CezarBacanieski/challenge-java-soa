package br.com.fiap.fordvinshare.service;

import br.com.fiap.fordvinshare.dto.request.LoginRequest;
import br.com.fiap.fordvinshare.dto.response.TokenResponse;
import br.com.fiap.fordvinshare.dto.response.UsuarioResponse;
import br.com.fiap.fordvinshare.security.UsuarioAutenticado;

public interface AuthService {

	TokenResponse login(LoginRequest request);

	UsuarioResponse usuarioAtual(UsuarioAutenticado usuario);
}
