package br.com.fiap.fordvinshare.controller;

import br.com.fiap.fordvinshare.dto.response.DashboardResumoResponse;
import br.com.fiap.fordvinshare.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Indicadores consolidados do VIN Share")
public class DashboardController {

	private final DashboardService dashboardService;

	@GetMapping("/resumo")
	@Operation(summary = "Obter resumo do dashboard", operationId = "obterResumoDashboard")
	public DashboardResumoResponse obterResumo() {
		return dashboardService.obterResumo();
	}
}
