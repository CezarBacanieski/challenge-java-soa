package br.com.fiap.fordvinshare.repository;

import br.com.fiap.fordvinshare.entity.Lead;
import br.com.fiap.fordvinshare.entity.StatusLead;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeadRepository extends JpaRepository<Lead, Long> {

	List<Lead> findByStatus(StatusLead status);

	boolean existsByVeiculoIdAndStatus(Long veiculoId, StatusLead status);

	long countByStatus(StatusLead status);
}
