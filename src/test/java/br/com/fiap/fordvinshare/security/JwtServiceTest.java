package br.com.fiap.fordvinshare.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.fiap.fordvinshare.entity.Concessionaria;
import br.com.fiap.fordvinshare.entity.Perfil;
import br.com.fiap.fordvinshare.entity.Usuario;
import java.time.Duration;
import org.junit.jupiter.api.Test;

/** Testes unitários de geração e validação do JWT (sem subir o Spring). */
class JwtServiceTest {

	private static final String SECRET = "dGVzdGUtc2VjcmV0LWZvcmQtdmluc2hhcmUtYXBpLWNoYXZlLWRlLTI1Ni1iaXRzLW1pbmltbw==";
	private static final String OUTRA_SECRET = "b3V0cmEtY2hhdmUtY29tcGxldGFtZW50ZS1kaWZlcmVudGUtcGFyYS10ZXN0ZXMtand0LTEyMw==";

	private final JwtService jwtService = new JwtService(SECRET, 60, "ford-vinshare-api");

	private Usuario gestor() {
		return Usuario.builder()
				.id(7L)
				.nome("Gestor Teste")
				.email("gestor@teste.com")
				.perfil(Perfil.GESTOR)
				.concessionaria(Concessionaria.builder().id(3L).build())
				.build();
	}

	@Test
	void deveGerarTokenEExtrairClaims() {
		String token = jwtService.gerarToken(gestor());

		UsuarioAutenticado usuario = jwtService.validarToken(token);

		assertThat(usuario.id()).isEqualTo(7L);
		assertThat(usuario.email()).isEqualTo("gestor@teste.com");
		assertThat(usuario.perfil()).isEqualTo(Perfil.GESTOR);
		assertThat(usuario.concessionariaId()).isEqualTo(3L);
	}

	@Test
	void adminNaoPossuiConcessionariaNoToken() {
		Usuario admin = Usuario.builder().id(1L).nome("Admin").email("admin@teste.com").perfil(Perfil.ADMIN).build();

		UsuarioAutenticado usuario = jwtService.validarToken(jwtService.gerarToken(admin));

		assertThat(usuario.isAdmin()).isTrue();
		assertThat(usuario.concessionariaId()).isNull();
	}

	@Test
	void deveRejeitarTokenExpirado() {
		String expirado = jwtService.gerarToken(gestor(), Duration.ofMinutes(-1));

		assertThatThrownBy(() -> jwtService.validarToken(expirado))
				.isInstanceOf(TokenInvalidoException.class)
				.hasMessage("Token expirado");
	}

	@Test
	void deveRejeitarTokenAssinadoComOutraChave() {
		String tokenDeOutraChave = new JwtService(OUTRA_SECRET, 60, "ford-vinshare-api").gerarToken(gestor());

		assertThatThrownBy(() -> jwtService.validarToken(tokenDeOutraChave))
				.isInstanceOf(TokenInvalidoException.class);
	}

	@Test
	void deveRejeitarTokenAdulterado() {
		String token = jwtService.gerarToken(gestor());
		String[] partes = token.split("\\.");
		// Troca o payload pelo de outro token: a assinatura deixa de conferir.
		String outroPayload = jwtService.gerarToken(Usuario.builder()
				.id(1L).nome("X").email("x@x.com").perfil(Perfil.ADMIN).build()).split("\\.")[1];
		String adulterado = partes[0] + "." + outroPayload + "." + partes[2];

		assertThatThrownBy(() -> jwtService.validarToken(adulterado))
				.isInstanceOf(TokenInvalidoException.class);
	}

	@Test
	void deveRejeitarTokenDeOutroEmissor() {
		String token = new JwtService(SECRET, 60, "outro-emissor").gerarToken(gestor());

		assertThatThrownBy(() -> jwtService.validarToken(token))
				.isInstanceOf(TokenInvalidoException.class);
	}
}
