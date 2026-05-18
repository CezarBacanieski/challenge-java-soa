package br.com.fiap.fordvinshare.service;

import br.com.fiap.fordvinshare.dto.request.LeadRequest;
import br.com.fiap.fordvinshare.dto.request.LeadStatusRequest;
import br.com.fiap.fordvinshare.dto.response.LeadResponse;
import br.com.fiap.fordvinshare.entity.StatusLead;
import java.util.List;

public interface LeadService {

	LeadResponse gerarManual(Long veiculoId, LeadRequest request);

	List<LeadResponse> gerarAutomatico();

	List<LeadResponse> listarTodos();

	List<LeadResponse> listarPorStatus(StatusLead status);

	LeadResponse atualizarStatus(Long id, LeadStatusRequest request);
}
