package br.com.fiap.fordvinshare.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
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

	@NotNull(message = "Data do serviço é obrigatória")
	private LocalDate dataServico;

	@NotBlank(message = "Tipo do serviço é obrigatório")
	@Size(min = 1, max = 80, message = "Tipo do serviÃ§o invÃ¡lido")
	@Pattern(regexp = "^[\\p{L}0-9 .,'-]{1,80}$", message = "Tipo do serviÃ§o invÃ¡lido")
	private String tipoServico;

	@PositiveOrZero(message = "Valor deve ser maior ou igual a zero")
	private BigDecimal valor;

	@NotNull(message = "Informar se foi realizada na rede oficial é obrigatório")
	private Boolean realizadaNaRedeOficial;

	@Size(max = 1000, message = "ObservaÃ§Ãµes invÃ¡lidas")
	private String observacoes;
}
