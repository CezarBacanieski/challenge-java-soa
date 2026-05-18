package br.com.fiap.fordvinshare.service;

import br.com.fiap.fordvinshare.dto.request.VeiculoRequest;
import br.com.fiap.fordvinshare.dto.response.VeiculoResponse;
import java.util.List;

public interface VeiculoService {

	VeiculoResponse cadastrar(VeiculoRequest request);

	List<VeiculoResponse> listarTodos();

	VeiculoResponse buscarPorId(Long id);

	List<VeiculoResponse> listarPorCliente(Long clienteId);

	VeiculoResponse atualizar(Long id, VeiculoRequest request);

	void deletar(Long id);
}
