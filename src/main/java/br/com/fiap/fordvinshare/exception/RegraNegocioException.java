package br.com.fiap.fordvinshare.exception;

/** Requisição bem formada, mas que viola uma regra de negócio. Resulta em HTTP 422. */
public class RegraNegocioException extends RuntimeException {

	public RegraNegocioException(String mensagem) {
		super(mensagem);
	}
}
