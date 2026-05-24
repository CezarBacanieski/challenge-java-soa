package br.com.fiap.fordvinshare.security.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
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
@Entity
@Table(
		name = "audit_events",
		indexes = {
				@Index(name = "idx_audit_user", columnList = "userId"),
				@Index(name = "idx_audit_action", columnList = "action"),
				@Index(name = "idx_audit_at", columnList = "at")
		}
)
public class AuditEvent {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, updatable = false)
	private Instant at;

	@Column(nullable = true)
	private Long userId;

	@Column(nullable = false, length = 64)
	private String action;

	@Column(length = 128)
	private String resource;

	@Column(length = 64)
	private String ip;

	@Column(length = 255)
	private String userAgent;

	@Column(nullable = false)
	private boolean success;

	@Column(columnDefinition = "TEXT")
	private String details;

	@PrePersist
	void prePersist() {
		if (at == null) {
			at = Instant.now();
		}
	}
}

