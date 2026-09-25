package br.com.fiap.fordvinshare.service;

import br.com.fiap.fordvinshare.dto.request.LoginRequest;
import br.com.fiap.fordvinshare.dto.response.TokenResponse;
import br.com.fiap.fordvinshare.dto.response.UsuarioResponse;
import br.com.fiap.fordvinshare.entity.Usuario;
import br.com.fiap.fordvinshare.repository.UsuarioRepository;
import br.com.fiap.fordvinshare.security.JwtService;
import br.com.fiap.fordvinshare.security.UsuarioAutenticado;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	/** Mesma mensagem para e-mail inexistente e senha errada, para não revelar quais e-mails existem. */
	private static final String CREDENCIAIS_INVALIDAS = "E-mail ou senha inválidos";

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	@Override
	@Transactional(readOnly = true)
	public TokenResponse login(LoginRequest request) {
		Usuario usuario = usuarioRepository.findByEmailIgnoreCase(request.getEmail())
				.orElseThrow(() -> new BadCredentialsException(CREDENCIAIS_INVALIDAS));

		if (!passwordEncoder.matches(request.getSenha(), usuario.getSenha())) {
			throw new BadCredentialsException(CREDENCIAIS_INVALIDAS);
		}
		if (!Boolean.TRUE.equals(usuario.getAtivo())) {
			throw new DisabledException("Usuário desativado");
		}

		return TokenResponse.builder()
				.token(jwtService.gerarToken(usuario))
				.tipo("Bearer")
				.expiraEmSegundos(jwtService.getValidadeEmSegundos())
				.email(usuario.getEmail())
				.perfil(usuario.getPerfil())
				.concessionariaId(usuario.getConcessionaria() != null ? usuario.getConcessionaria().getId() : null)
				.build();
	}

	@Override
	@Transactional(readOnly = true)
	public UsuarioResponse usuarioAtual(UsuarioAutenticado autenticado) {
		Usuario usuario = usuarioRepository.findById(autenticado.id())
				.orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
		return UsuarioServiceImpl.toResponse(usuario);
	}
}
