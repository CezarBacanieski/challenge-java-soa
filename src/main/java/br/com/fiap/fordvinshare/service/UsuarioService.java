package br.com.fiap.fordvinshare.service;

import br.com.fiap.fordvinshare.dto.request.UsuarioRequest;
import br.com.fiap.fordvinshare.dto.response.UsuarioResponse;
import java.util.List;

public interface UsuarioService {

	UsuarioResponse cadastrar(UsuarioRequest request);

	List<UsuarioResponse> listarTodos();

	UsuarioResponse buscarPorId(Long id);

	void deletar(Long id);
}
