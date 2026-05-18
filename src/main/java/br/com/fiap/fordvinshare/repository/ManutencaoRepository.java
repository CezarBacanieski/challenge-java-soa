package br.com.fiap.fordvinshare.repository;

import br.com.fiap.fordvinshare.entity.Manutencao;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManutencaoRepository extends JpaRepository<Manutencao, Long> {

	List<Manutencao> findByVeiculoIdOrderByDataServicoDesc(Long veiculoId);

	boolean existsByVeiculoIdAndDataServicoGreaterThanEqual(Long veiculoId, LocalDate dataServico);

	long countByDataServicoBetween(LocalDate inicio, LocalDate fim);
}
