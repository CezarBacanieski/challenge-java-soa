package br.com.fiap.fordvinshare.security.audit;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit")
@Tag(name = "Audit", description = "Trilha de auditoria (admin)")
public class AuditController {
	private final AuditEventRepository repo;

	public AuditController(AuditEventRepository repo) {
		this.repo = repo;
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Listar últimos 100 eventos de auditoria", operationId = "listarAuditoria")
	public List<AuditEvent> list() {
		return repo.findTop100ByOrderByAtDesc();
	}
}

