package br.com.fiap.fordvinshare.dto.request;

import br.com.fiap.fordvinshare.entity.StatusLead;
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
public class LeadStatusRequest {

	@NotNull(message = "Status é obrigatório")
	private StatusLead status;
}
