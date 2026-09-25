package br.com.fiap.fordvinshare.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
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
public class ManutencaoRequest {

	@NotNull(message = "Veículo é obrigatório")
	private Long veiculoId;

	/** Obrigatório quando a manutenção foi realizada na rede oficial. */
	private Long concessionariaId;

	@NotNull(message = "Data do serviço é obrigatória")
	@PastOrPresent(message = "Data do serviço não pode ser futura")
	private LocalDate dataServico;

	@NotBlank(message = "Tipo do serviço é obrigatório")
	private String tipoServico;

	@PositiveOrZero(message = "Valor deve ser maior ou igual a zero")
	private BigDecimal valor;

	@NotNull(message = "Informar se foi realizada na rede oficial é obrigatório")
	private Boolean realizadaNaRedeOficial;

	private String observacoes;
}
