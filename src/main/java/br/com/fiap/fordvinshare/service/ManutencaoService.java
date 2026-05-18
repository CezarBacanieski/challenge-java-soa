package br.com.fiap.fordvinshare.service;

import br.com.fiap.fordvinshare.dto.request.ManutencaoRequest;
import br.com.fiap.fordvinshare.dto.response.ManutencaoResponse;
import java.util.List;

public interface ManutencaoService {

	ManutencaoResponse registrar(ManutencaoRequest request);

	List<ManutencaoResponse> listarPorVeiculo(Long veiculoId);

	List<ManutencaoResponse> listarTodas();

	void deletar(Long id);
}
