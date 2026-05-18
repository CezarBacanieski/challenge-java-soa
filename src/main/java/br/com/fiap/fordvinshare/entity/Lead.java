package br.com.fiap.fordvinshare.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
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
@Table(name = "leads")
public class Lead {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "veiculo_id", nullable = false)
	private Veiculo veiculo;

	@Column(nullable = false, updatable = false)
	private LocalDate dataGeracao;

	@Column(nullable = false)
	private String motivoLead;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StatusLead status;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PrioridadeLead prioridade;

	@PrePersist
	public void prePersist() {
		if (dataGeracao == null) {
			dataGeracao = LocalDate.now();
		}
		if (status == null) {
			status = StatusLead.PENDENTE;
		}
	}
}
