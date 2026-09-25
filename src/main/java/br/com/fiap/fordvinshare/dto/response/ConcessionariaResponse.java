package br.com.fiap.fordvinshare.dto.response;

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
public class ConcessionariaResponse {

	private Long id;
	private String nome;
	private String cnpj;
	private String cidade;
	private String uf;
}
