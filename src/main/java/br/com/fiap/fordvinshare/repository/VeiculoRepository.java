package br.com.fiap.fordvinshare.repository;

import br.com.fiap.fordvinshare.entity.Veiculo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {

	List<Veiculo> findByClienteId(Long clienteId);

	long countByUtilizaRedeOficialTrue();
}
