package br.com.fiap.fordvinshare.exception;

/** Conflito com o estado atual do recurso (duplicidade ou vínculo existente). Resulta em HTTP 409. */
public class ConflitoException extends RuntimeException {

	public ConflitoException(String mensagem) {
		super(mensagem);
	}
}
