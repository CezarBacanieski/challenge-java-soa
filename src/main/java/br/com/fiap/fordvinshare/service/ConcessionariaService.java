package br.com.fiap.fordvinshare.service;

import br.com.fiap.fordvinshare.dto.request.ConcessionariaRequest;
import br.com.fiap.fordvinshare.dto.response.ConcessionariaResponse;
import br.com.fiap.fordvinshare.dto.response.IndicadoresConcessionariaResponse;
import br.com.fiap.fordvinshare.security.UsuarioAutenticado;
import java.util.List;

public interface ConcessionariaService {

	ConcessionariaResponse cadastrar(ConcessionariaRequest request);

	List<ConcessionariaResponse> listarTodas();

	ConcessionariaResponse buscarPorId(Long id);

	ConcessionariaResponse atualizar(Long id, ConcessionariaRequest request);

	void deletar(Long id);

	IndicadoresConcessionariaResponse obterIndicadores(Long id, UsuarioAutenticado usuario);
}
