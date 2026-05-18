package br.com.fiap.fordvinshare.dto.response;

import br.com.fiap.fordvinshare.entity.PrioridadeLead;
import br.com.fiap.fordvinshare.entity.StatusLead;
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
public class LeadResponse {

	private Long id;
	private Long veiculoId;
	private String vin;
	private LocalDate dataGeracao;
	private String motivoLead;
	private StatusLead status;
	private PrioridadeLead prioridade;
}
