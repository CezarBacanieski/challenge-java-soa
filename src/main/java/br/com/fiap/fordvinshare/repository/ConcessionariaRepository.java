package br.com.fiap.fordvinshare.repository;

import br.com.fiap.fordvinshare.entity.Concessionaria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcessionariaRepository extends JpaRepository<Concessionaria, Long> {

	boolean existsByCnpj(String cnpj);

	boolean existsByCnpjAndIdNot(String cnpj, Long id);
}
