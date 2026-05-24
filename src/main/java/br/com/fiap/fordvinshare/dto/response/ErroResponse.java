package br.com.fiap.fordvinshare.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErroResponse {

	private int status;
	private String codigo;
	private String erro;
	private String requestId;
	private LocalDateTime timestamp;
	private List<CampoErroResponse> campos;
}
