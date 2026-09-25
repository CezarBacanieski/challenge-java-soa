package br.com.fiap.fordvinshare.security;

import br.com.fiap.fordvinshare.entity.Perfil;
import br.com.fiap.fordvinshare.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Geração e validação dos tokens JWT da API.
 * Os tokens são assinados com HMAC-SHA256 e carregam os dados necessários para autorizar
 * a requisição (perfil e concessionária), sem precisar consultar o banco a cada chamada.
 */
@Service
public class JwtService {

	private static final String CLAIM_UID = "uid";
	private static final String CLAIM_NOME = "nome";
	private static final String CLAIM_PERFIL = "perfil";
	private static final String CLAIM_CONCESSIONARIA = "concessionariaId";

	private final SecretKey chave;
	private final Duration validade;
	private final String emissor;

	public JwtService(@Value("${jwt.secret}") String segredoBase64,
			@Value("${jwt.expiration-minutes:60}") long validadeEmMinutos,
			@Value("${jwt.issuer:ford-vinshare-api}") String emissor) {
		this.chave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(segredoBase64));
		this.validade = Duration.ofMinutes(validadeEmMinutos);
		this.emissor = emissor;
	}

	public String gerarToken(Usuario usuario) {
		return gerarToken(usuario, validade);
	}

	/** Permite escolher a validade; usado nos testes para gerar tokens já expirados. */
	public String gerarToken(Usuario usuario, Duration validadeDoToken) {
		Instant agora = Instant.now();
		Long concessionariaId = usuario.getConcessionaria() != null ? usuario.getConcessionaria().getId() : null;

		return Jwts.builder()
				.subject(usuario.getEmail())
				.issuer(emissor)
				.id(UUID.randomUUID().toString())
				.issuedAt(Date.from(agora))
				.expiration(Date.from(agora.plus(validadeDoToken)))
				.claim(CLAIM_UID, usuario.getId())
				.claim(CLAIM_NOME, usuario.getNome())
				.claim(CLAIM_PERFIL, usuario.getPerfil().name())
				.claim(CLAIM_CONCESSIONARIA, concessionariaId)
				.signWith(chave, Jwts.SIG.HS256)
				.compact();
	}

	/**
	 * Confere assinatura, emissor e expiração e devolve o usuário contido no token.
	 *
	 * @throws TokenInvalidoException se o token for inválido por qualquer motivo
	 */
	public UsuarioAutenticado validarToken(String token) {
		try {
			Claims claims = Jwts.parser()
					.verifyWith(chave)
					.requireIssuer(emissor)
					.build()
					.parseSignedClaims(token)
					.getPayload();

			return new UsuarioAutenticado(
					toLong(claims.get(CLAIM_UID)),
					claims.get(CLAIM_NOME, String.class),
					claims.getSubject(),
					Perfil.valueOf(claims.get(CLAIM_PERFIL, String.class)),
					toLong(claims.get(CLAIM_CONCESSIONARIA)));
		} catch (ExpiredJwtException ex) {
			throw new TokenInvalidoException("Token expirado", ex);
		} catch (JwtException | IllegalArgumentException ex) {
			throw new TokenInvalidoException("Token inválido", ex);
		}
	}

	public long getValidadeEmSegundos() {
		return validade.toSeconds();
	}

	private static Long toLong(Object valor) {
		return valor instanceof Number numero ? numero.longValue() : null;
	}
}
