package br.com.fiap.fordvinshare.security.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
	private static final Logger log = LoggerFactory.getLogger("AUDIT");

	private final AuditEventRepository repo;

	public AuditService(AuditEventRepository repo) {
		this.repo = repo;
	}

	@Transactional
	public void record(AuditAction action, Long userId, String resource, String ip, String userAgent, boolean success, String details) {
		AuditEvent ev = AuditEvent.builder()
				.action(action.name())
				.userId(userId)
				.resource(resource)
				.ip(ip)
				.userAgent(truncate(userAgent, 255))
				.success(success)
				.details(details)
				.build();
		repo.save(ev);

		log.info("action={} userId={} resource={} success={}", action.name(), userId, resource, success);
	}

	private static String truncate(String s, int max) {
		if (s == null) return null;
		return s.length() <= max ? s : s.substring(0, max);
	}
}

