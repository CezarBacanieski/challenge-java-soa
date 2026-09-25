package br.com.fiap.fordvinshare.service;

import br.com.fiap.fordvinshare.dto.request.UsuarioRequest;
import br.com.fiap.fordvinshare.dto.response.UsuarioResponse;
import br.com.fiap.fordvinshare.entity.Concessionaria;
import br.com.fiap.fordvinshare.entity.Perfil;
import br.com.fiap.fordvinshare.entity.Usuario;
import br.com.fiap.fordvinshare.exception.ConflitoException;
import br.com.fiap.fordvinshare.exception.RegraNegocioException;
import br.com.fiap.fordvinshare.repository.ConcessionariaRepository;
import br.com.fiap.fordvinshare.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

	private final UsuarioRepository usuarioRepository;
	private final ConcessionariaRepository concessionariaRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	@Transactional
	public UsuarioResponse cadastrar(UsuarioRequest request) {
		if (usuarioRepository.existsByEmailIgnoreCase(request.getEmail())) {
			throw new ConflitoException("Já existe um usuário com este e-mail");
		}

		Concessionaria concessionaria = null;
		if (request.getPerfil() != Perfil.ADMIN) {
			if (request.getConcessionariaId() == null) {
				throw new RegraNegocioException("Usuários GESTOR e CONSULTOR devem estar vinculados a uma concessionária");
			}
			concessionaria = concessionariaRepository.findById(request.getConcessionariaId())
					.orElseThrow(() -> new EntityNotFoundException("Concessionária não encontrada"));
		}

		Usuario usuario = Usuario.builder()
				.nome(request.getNome())
				.email(request.getEmail().toLowerCase())
				.senha(passwordEncoder.encode(request.getSenha()))
				.perfil(request.getPerfil())
				.concessionaria(concessionaria)
				.ativo(true)
				.build();

		return toResponse(usuarioRepository.save(usuario));
	}

	@Override
	@Transactional(readOnly = true)
	public List<UsuarioResponse> listarTodos() {
		return usuarioRepository.findAll().stream().map(UsuarioServiceImpl::toResponse).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public UsuarioResponse buscarPorId(Long id) {
		return toResponse(buscarEntidadePorId(id));
	}

	@Override
	@Transactional
	public void deletar(Long id) {
		usuarioRepository.delete(buscarEntidadePorId(id));
	}

	private Usuario buscarEntidadePorId(Long id) {
		return usuarioRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
	}

	/** A senha nunca é devolvida nas respostas. */
	static UsuarioResponse toResponse(Usuario usuario) {
		return UsuarioResponse.builder()
				.id(usuario.getId())
				.nome(usuario.getNome())
				.email(usuario.getEmail())
				.perfil(usuario.getPerfil())
				.concessionariaId(usuario.getConcessionaria() != null ? usuario.getConcessionaria().getId() : null)
				.ativo(usuario.getAtivo())
				.build();
	}
}
