package br.com.fiap.fordvinshare.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Lê o header {@code Authorization: Bearer <token>} de cada requisição.
 * Token válido: coloca o {@link UsuarioAutenticado} no SecurityContext com a autoridade {@code ROLE_<PERFIL>}.
 * Token inválido ou expirado: responde 401 na hora, com o motivo.
 * Sem token: segue sem autenticação e o Spring Security decide (rota pública passa; protegida dá 401).
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String PREFIXO = "Bearer ";

	private final JwtService jwtService;
	private final AuthenticationEntryPoint entryPoint;

	public JwtAuthenticationFilter(JwtService jwtService, AuthenticationEntryPoint entryPoint) {
		this.jwtService = jwtService;
		this.entryPoint = entryPoint;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header == null || !header.startsWith(PREFIXO)) {
			chain.doFilter(request, response);
			return;
		}

		try {
			UsuarioAutenticado usuario = jwtService.validarToken(header.substring(PREFIXO.length()).trim());
			var autenticacao = new UsernamePasswordAuthenticationToken(
					usuario, null, List.of(new SimpleGrantedAuthority("ROLE_" + usuario.perfil().name())));
			autenticacao.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(autenticacao);
		} catch (TokenInvalidoException ex) {
			SecurityContextHolder.clearContext();
			entryPoint.commence(request, response, ex);
			return;
		}

		chain.doFilter(request, response);
	}
}
