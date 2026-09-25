package br.com.fiap.fordvinshare.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "manutencoes")
public class Manutencao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "veiculo_id", nullable = false)
	private Veiculo veiculo;

	/** Concessionária onde o serviço foi feito. Obrigatória quando realizada na rede oficial. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "concessionaria_id")
	private Concessionaria concessionaria;

	@Column(nullable = false)
	private LocalDate dataServico;

	@Column(nullable = false)
	private String tipoServico;

	@Column(precision = 10, scale = 2)
	private BigDecimal valor;

	@Column(nullable = false)
	private Boolean realizadaNaRedeOficial;

	@Column(columnDefinition = "TEXT")
	private String observacoes;
}
