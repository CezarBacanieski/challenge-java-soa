package br.com.fiap.fordvinshare.security;

import org.springframework.security.core.AuthenticationException;

/** Token JWT ausente de assinatura válida, adulterado, de outro emissor, malformado ou expirado. Vira 401. */
public class TokenInvalidoException extends AuthenticationException {

	public TokenInvalidoException(String mensagem) {
		super(mensagem);
	}

	public TokenInvalidoException(String mensagem, Throwable causa) {
		super(mensagem, causa);
	}
}
