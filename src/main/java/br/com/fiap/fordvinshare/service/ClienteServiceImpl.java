package br.com.fiap.fordvinshare.service;

import br.com.fiap.fordvinshare.dto.request.ClienteRequest;
import br.com.fiap.fordvinshare.dto.response.ClienteResponse;
import br.com.fiap.fordvinshare.entity.Cliente;
import br.com.fiap.fordvinshare.repository.ClienteRepository;
import br.com.fiap.fordvinshare.security.audit.AuditAction;
import br.com.fiap.fordvinshare.security.audit.AuditService;
import br.com.fiap.fordvinshare.security.util.SecurityContextUtil;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

	private final ClienteRepository clienteRepository;
	private final AuditService audit;

	@Override
	@Transactional
	public ClienteResponse cadastrar(ClienteRequest request) {
		Cliente cliente = Cliente.builder()
				.nome(request.getNome())
				.email(request.getEmail())
				.telefone(request.getTelefone())
				.build();

		Cliente saved = clienteRepository.save(cliente);
		audit.record(AuditAction.CLIENTE_CREATE, SecurityContextUtil.currentUserIdOrNull(), "cliente:" + saved.getId(), null, null, true, null);
		return toResponse(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ClienteResponse> listarTodos() {
		return clienteRepository.findAll()
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public ClienteResponse buscarPorId(Long id) {
		return toResponse(buscarEntidadePorId(id));
	}

	@Override
	@Transactional
	public ClienteResponse atualizar(Long id, ClienteRequest request) {
		Cliente cliente = buscarEntidadePorId(id);
		cliente.setNome(request.getNome());
		cliente.setEmail(request.getEmail());
		cliente.setTelefone(request.getTelefone());

		Cliente saved = clienteRepository.save(cliente);
		audit.record(AuditAction.CLIENTE_UPDATE, SecurityContextUtil.currentUserIdOrNull(), "cliente:" + saved.getId(), null, null, true, null);
		return toResponse(saved);
	}

	@Override
	@Transactional
	public void deletar(Long id) {
		Cliente cliente = buscarEntidadePorId(id);
		clienteRepository.delete(cliente);
		audit.record(AuditAction.CLIENTE_DELETE, SecurityContextUtil.currentUserIdOrNull(), "cliente:" + id, null, null, true, null);
	}

	private Cliente buscarEntidadePorId(Long id) {
		return clienteRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
	}

	private ClienteResponse toResponse(Cliente cliente) {
		return ClienteResponse.builder()
				.id(cliente.getId())
				.nome(cliente.getNome())
				.email(cliente.getEmail())
				.telefone(cliente.getTelefone())
				.dataCadastro(cliente.getDataCadastro())
				.build();
	}
}
