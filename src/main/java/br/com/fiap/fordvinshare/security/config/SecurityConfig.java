package br.com.fiap.fordvinshare.security.config;

import br.com.fiap.fordvinshare.security.auth.AppUserRepository;
import br.com.fiap.fordvinshare.security.filter.CorrelationIdFilter;
import br.com.fiap.fordvinshare.security.filter.PayloadSignatureFilter;
import br.com.fiap.fordvinshare.security.filter.RateLimitingFilter;
import br.com.fiap.fordvinshare.security.filter.TokenVersionFilter;
import br.com.fiap.fordvinshare.security.jwt.SecurityProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(12);
	}

	@Bean
	public JwtEncoder jwtEncoder(SecurityProperties props) {
		SecretKey key = hmacKey(props.jwt().hmacSecretBase64());
		ImmutableSecret<SecurityContext> jwkSource = new ImmutableSecret<>(key);
		return new NimbusJwtEncoder(jwkSource);
	}

	@Bean
	public JwtDecoder jwtDecoder(SecurityProperties props) {
		SecretKey key = hmacKey(props.jwt().hmacSecretBase64());
		return NimbusJwtDecoder.withSecretKey(key).build();
	}

	private SecretKey hmacKey(String secretBase64) {
		if (secretBase64 == null || secretBase64.isBlank()) {
			throw new IllegalStateException("Missing required app.security.jwt.hmac-secret-base64");
		}
		byte[] keyBytes = Base64.getDecoder().decode(secretBase64.trim());
		if (keyBytes.length < 32) {
			throw new IllegalStateException("app.security.jwt.hmac-secret-base64 must be at least 32 bytes (base64)");
		}
		return new SecretKeySpec(keyBytes, "HmacSHA256");
	}

	@Bean
	SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			ObjectMapper objectMapper,
			SecurityProperties props,
			AppUserRepository userRepo
	) throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.cors(Customizer.withDefaults())
				.headers(h -> h
						.httpStrictTransportSecurity(hsts -> hsts
								.includeSubDomains(true)
								.preload(true)
								.maxAgeInSeconds(31536000)
						)
						.contentSecurityPolicy(csp -> csp.policyDirectives(
								"default-src 'none'; frame-ancestors 'none'; base-uri 'none'"
						))
						.frameOptions(fo -> fo.deny())
						.contentTypeOptions(Customizer.withDefaults())
						.referrerPolicy(rp -> rp.policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
						.permissionsPolicy(pp -> pp.policy(
								"geolocation=(), microphone=(), camera=(), payment=(), usb=()"
						))
				)
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(
								"/api/auth/**",
								"/swagger-ui.html",
								"/swagger-ui/**",
								"/v3/api-docs/**"
						).permitAll()
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.anyRequest().authenticated()
				)
				.oauth2ResourceServer(oauth -> oauth
						.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter()))
						.authenticationEntryPoint((req, res, ex) -> {
							res.setStatus(401);
							res.setContentType(MediaType.APPLICATION_JSON_VALUE);
							res.getWriter().write(objectMapper.writeValueAsString(br.com.fiap.fordvinshare.dto.response.ErroResponse.builder()
									.status(401)
									.erro("Não autenticado")
									.timestamp(LocalDateTime.now())
									.build()));
						})
						.accessDeniedHandler((req, res, ex) -> {
							res.setStatus(403);
							res.setContentType(MediaType.APPLICATION_JSON_VALUE);
							res.getWriter().write(objectMapper.writeValueAsString(br.com.fiap.fordvinshare.dto.response.ErroResponse.builder()
									.status(403)
									.erro("Acesso negado")
									.timestamp(LocalDateTime.now())
									.build()));
						})
				);

		http.addFilterBefore(new CorrelationIdFilter(), UsernamePasswordAuthenticationFilter.class);
		http.addFilterAfter(new RateLimitingFilter(props, objectMapper), CorrelationIdFilter.class);
		http.addFilterAfter(new PayloadSignatureFilter(props, objectMapper), RateLimitingFilter.class);
		http.addFilterAfter(new TokenVersionFilter(userRepo, objectMapper), PayloadSignatureFilter.class);

		return http.build();
	}

	private JwtAuthenticationConverter jwtAuthConverter() {
		JwtGrantedAuthoritiesConverter gac = new JwtGrantedAuthoritiesConverter();
		gac.setAuthoritiesClaimName("role");
		gac.setAuthorityPrefix("");

		JwtAuthenticationConverter conv = new JwtAuthenticationConverter();
		conv.setJwtGrantedAuthoritiesConverter(gac);
		return conv;
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource(SecurityProperties props) {
		CorsConfiguration cfg = new CorsConfiguration();
		List<String> origins = props.cors() == null ? List.of() : props.cors().allowedOrigins();
		cfg.setAllowedOrigins(origins);
		cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		cfg.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Request-Id", "X-Payload-Signature"));
		cfg.setExposedHeaders(List.of("X-Request-Id"));
		cfg.setAllowCredentials(true);
		cfg.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", cfg);
		return source;
	}
}
