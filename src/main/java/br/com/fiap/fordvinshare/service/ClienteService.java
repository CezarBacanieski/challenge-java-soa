package br.com.fiap.fordvinshare.service;

import br.com.fiap.fordvinshare.dto.request.ClienteRequest;
import br.com.fiap.fordvinshare.dto.response.ClienteResponse;
import java.util.List;

public interface ClienteService {

	ClienteResponse cadastrar(ClienteRequest request);

	List<ClienteResponse> listarTodos();

	ClienteResponse buscarPorId(Long id);

	ClienteResponse atualizar(Long id, ClienteRequest request);

	void deletar(Long id);
}
