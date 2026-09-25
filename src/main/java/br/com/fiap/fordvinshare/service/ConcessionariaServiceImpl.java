package br.com.fiap.fordvinshare.service;

import br.com.fiap.fordvinshare.dto.request.ConcessionariaRequest;
import br.com.fiap.fordvinshare.dto.response.ConcessionariaResponse;
import br.com.fiap.fordvinshare.dto.response.IndicadoresConcessionariaResponse;
import br.com.fiap.fordvinshare.entity.Concessionaria;
import br.com.fiap.fordvinshare.exception.ConflitoException;
import br.com.fiap.fordvinshare.repository.ConcessionariaRepository;
import br.com.fiap.fordvinshare.repository.ManutencaoRepository;
import br.com.fiap.fordvinshare.repository.UsuarioRepository;
import br.com.fiap.fordvinshare.security.UsuarioAutenticado;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConcessionariaServiceImpl implements ConcessionariaService {

	private final ConcessionariaRepository concessionariaRepository;
	private final ManutencaoRepository manutencaoRepository;
	private final UsuarioRepository usuarioRepository;

	@Override
	@Transactional
	public ConcessionariaResponse cadastrar(ConcessionariaRequest request) {
		if (concessionariaRepository.existsByCnpj(request.getCnpj())) {
			throw new ConflitoException("Já existe uma concessionária com este CNPJ");
		}

		Concessionaria concessionaria = Concessionaria.builder()
				.nome(request.getNome())
				.cnpj(request.getCnpj())
				.cidade(request.getCidade())
				.uf(request.getUf())
				.build();

		return toResponse(concessionariaRepository.save(concessionaria));
	}

	@Override
	@Transactional(readOnly = true)
	public List<ConcessionariaResponse> listarTodas() {
		return concessionariaRepository.findAll().stream().map(this::toResponse).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public ConcessionariaResponse buscarPorId(Long id) {
		return toResponse(buscarEntidadePorId(id));
	}

	@Override
	@Transactional
	public ConcessionariaResponse atualizar(Long id, ConcessionariaRequest request) {
		Concessionaria concessionaria = buscarEntidadePorId(id);
		if (concessionariaRepository.existsByCnpjAndIdNot(request.getCnpj(), id)) {
			throw new ConflitoException("Já existe uma concessionária com este CNPJ");
		}

		concessionaria.setNome(request.getNome());
		concessionaria.setCnpj(request.getCnpj());
		concessionaria.setCidade(request.getCidade());
		concessionaria.setUf(request.getUf());

		return toResponse(concessionariaRepository.save(concessionaria));
	}

	@Override
	@Transactional
	public void deletar(Long id) {
		Concessionaria concessionaria = buscarEntidadePorId(id);
		if (manutencaoRepository.existsByConcessionariaId(id) || usuarioRepository.existsByConcessionariaId(id)) {
			throw new ConflitoException("Concessionária possui manutenções ou usuários vinculados");
		}
		concessionariaRepository.delete(concessionaria);
	}

	/**
	 * Indicadores de retenção (VIN Share) de uma concessionária.
	 * O GESTOR só pode consultar a concessionária indicada no claim "concessionariaId" do seu token.
	 */
	@Override
	@Transactional(readOnly = true)
	public IndicadoresConcessionariaResponse obterIndicadores(Long id, UsuarioAutenticado usuario) {
		if (!usuario.isAdmin() && !Objects.equals(usuario.concessionariaId(), id)) {
			throw new AccessDeniedException("Acesso negado: você só pode consultar indicadores da sua concessionária");
		}

		Concessionaria concessionaria = buscarEntidadePorId(id);
		long atendidos = manutencaoRepository.contarVeiculosAtendidos(id);
		long retidos = manutencaoRepository.contarVeiculosRetidos(id);

		BigDecimal taxa = atendidos == 0
				? BigDecimal.ZERO.setScale(2)
				: BigDecimal.valueOf(retidos)
						.multiply(BigDecimal.valueOf(100))
						.divide(BigDecimal.valueOf(atendidos), 2, RoundingMode.HALF_UP);

		return IndicadoresConcessionariaResponse.builder()
				.concessionariaId(concessionaria.getId())
				.concessionariaNome(concessionaria.getNome())
				.totalManutencoes(manutencaoRepository.countByConcessionariaId(id))
				.veiculosAtendidos(atendidos)
				.veiculosRetidos(retidos)
				.taxaRetencao(taxa)
				.build();
	}

	private Concessionaria buscarEntidadePorId(Long id) {
		return concessionariaRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Concessionária não encontrada"));
	}

	private ConcessionariaResponse toResponse(Concessionaria concessionaria) {
		return ConcessionariaResponse.builder()
				.id(concessionaria.getId())
				.nome(concessionaria.getNome())
				.cnpj(concessionaria.getCnpj())
				.cidade(concessionaria.getCidade())
				.uf(concessionaria.getUf())
				.build();
	}
}
