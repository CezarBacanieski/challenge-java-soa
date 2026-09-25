package br.com.fiap.fordvinshare.config;

import br.com.fiap.fordvinshare.security.JwtAuthenticationFilter;
import br.com.fiap.fordvinshare.security.JwtService;
import br.com.fiap.fordvinshare.security.RestAccessDeniedHandler;
import br.com.fiap.fordvinshare.security.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Regras de acesso por rota e perfil. A API é stateless: não há sessão nem cookie,
 * cada requisição se autentica com o JWT no header Authorization.
 * A regra "GESTOR só vê a própria concessionária" depende do dado e fica em ConcessionariaServiceImpl.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private static final String ADMIN = "ADMIN";
	private static final String GESTOR = "GESTOR";

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService,
			RestAuthenticationEntryPoint entryPoint, RestAccessDeniedHandler accessDeniedHandler) throws Exception {
		http
				// CSRF protege sessões baseadas em cookie; com JWT no header ele não se aplica.
				.csrf(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(rotas -> rotas
						// Públicas
						.requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
						.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/error").permitAll()

						// Somente ADMIN
						.requestMatchers(HttpMethod.DELETE, "/api/**").hasRole(ADMIN)
						.requestMatchers("/api/usuarios/**").hasRole(ADMIN)
						.requestMatchers(HttpMethod.POST, "/api/concessionarias").hasRole(ADMIN)
						.requestMatchers(HttpMethod.PUT, "/api/concessionarias/**").hasRole(ADMIN)

						// GESTOR e ADMIN
						.requestMatchers(HttpMethod.GET, "/api/concessionarias/*/indicadores").hasAnyRole(GESTOR, ADMIN)
						.requestMatchers("/api/dashboard/**").hasAnyRole(GESTOR, ADMIN)
						.requestMatchers(HttpMethod.POST, "/api/veiculos/*/leads").hasAnyRole(GESTOR, ADMIN)
						.requestMatchers(HttpMethod.POST, "/api/leads/geracoes-automaticas").hasAnyRole(GESTOR, ADMIN)

						// Demais rotas: qualquer usuário autenticado (CONSULTOR, GESTOR ou ADMIN)
						.anyRequest().authenticated())
				.exceptionHandling(erros -> erros
						.authenticationEntryPoint(entryPoint)
						.accessDeniedHandler(accessDeniedHandler))
				.addFilterBefore(new JwtAuthenticationFilter(jwtService, entryPoint),
						UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
