package br.com.fiap.fordvinshare.dto.response;

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
public class ClienteResponse {

	private Long id;
	private String nome;
	private String email;
	private String telefone;
	private LocalDate dataCadastro;
}
