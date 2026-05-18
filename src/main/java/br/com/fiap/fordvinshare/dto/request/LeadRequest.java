package br.com.fiap.fordvinshare.dto.request;

import br.com.fiap.fordvinshare.entity.PrioridadeLead;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class LeadRequest {

	@NotBlank(message = "Motivo do lead é obrigatório")
	private String motivoLead;

	@NotNull(message = "Prioridade é obrigatória")
	private PrioridadeLead prioridade;
}
