package br.com.fiap.fordvinshare.dto.response;

import br.com.fiap.fordvinshare.entity.Perfil;
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
public class UsuarioResponse {

	private Long id;
	private String nome;
	private String email;
	private Perfil perfil;
	private Long concessionariaId;
	private Boolean ativo;
}
