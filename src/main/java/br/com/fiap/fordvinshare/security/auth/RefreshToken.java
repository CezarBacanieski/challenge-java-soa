package br.com.fiap.fordvinshare.security.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
		name = "refresh_tokens",
		indexes = {
				@Index(name = "idx_refresh_token_hash", columnList = "tokenHash", unique = true),
				@Index(name = "idx_refresh_token_user", columnList = "user_id")
		}
)
public class RefreshToken {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private AppUser user;

	@Column(nullable = false, length = 64, unique = true)
	private String tokenHash;

	@Column(nullable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant expiresAt;

	private Instant revokedAt;

	@Column(length = 64)
	private String replacedByTokenHash;

	private Instant lastUsedAt;

	@Column(length = 64)
	private String createdByIp;

	@Column(length = 255)
	private String createdByUserAgent;
}

