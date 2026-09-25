package br.com.fiap.fordvinshare.repository;

import br.com.fiap.fordvinshare.entity.Manutencao;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ManutencaoRepository extends JpaRepository<Manutencao, Long> {

	List<Manutencao> findByVeiculoIdOrderByDataServicoDesc(Long veiculoId);

	Optional<Manutencao> findFirstByVeiculoIdOrderByDataServicoDescIdDesc(Long veiculoId);

	boolean existsByVeiculoIdAndDataServicoGreaterThanEqual(Long veiculoId, LocalDate dataServico);

	boolean existsByVeiculoId(Long veiculoId);

	boolean existsByConcessionariaId(Long concessionariaId);

	long countByDataServicoBetween(LocalDate inicio, LocalDate fim);

	long countByConcessionariaId(Long concessionariaId);

	/** Veículos distintos que já foram atendidos pela concessionária. */
	@Query("select count(distinct m.veiculo.id) from Manutencao m where m.concessionaria.id = :concessionariaId")
	long contarVeiculosAtendidos(@Param("concessionariaId") Long concessionariaId);

	/** Dos veículos atendidos pela concessionária, quantos continuam usando a rede oficial. */
	@Query("""
			select count(distinct m.veiculo.id) from Manutencao m
			where m.concessionaria.id = :concessionariaId
			and m.veiculo.utilizaRedeOficial = true
			""")
	long contarVeiculosRetidos(@Param("concessionariaId") Long concessionariaId);
}
