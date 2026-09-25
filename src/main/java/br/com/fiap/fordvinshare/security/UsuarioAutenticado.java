package br.com.fiap.fordvinshare.security;

import br.com.fiap.fordvinshare.entity.Perfil;

/**
 * Usuário autenticado montado a partir dos claims do JWT.
 * É o "principal" do SecurityContext e pode ser recebido nos controllers com {@code @AuthenticationPrincipal}.
 * Não consulta o banco: tudo o que a API precisa para autorizar está no token.
 */
public record UsuarioAutenticado(Long id, String nome, String email, Perfil perfil, Long concessionariaId) {

	public boolean isAdmin() {
		return perfil == Perfil.ADMIN;
	}
}
