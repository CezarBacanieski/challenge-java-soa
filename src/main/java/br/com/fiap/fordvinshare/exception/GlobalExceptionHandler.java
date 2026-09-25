package br.com.fiap.fordvinshare.exception;

import br.com.fiap.fordvinshare.dto.response.CampoErroResponse;
import br.com.fiap.fordvinshare.dto.response.ErroResponse;
import br.com.fiap.fordvinshare.security.TokenInvalidoException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Padroniza todas as respostas de erro da API no formato {@link ErroResponse}.
 * Também é usado pelos handlers de segurança (401/403), para que erros de autenticação
 * sigam exatamente o mesmo formato dos demais erros.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<ErroResponse> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
		return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null, request);
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ErroResponse> handleNoResource(HttpServletRequest request) {
		return buildResponse(HttpStatus.NOT_FOUND, "Recurso não encontrado", null, request);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErroResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
		List<CampoErroResponse> campos = ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(erro -> CampoErroResponse.builder()
						.campo(erro.getField())
						.mensagem(erro.getDefaultMessage())
						.build())
				.toList();

		return buildResponse(HttpStatus.BAD_REQUEST, "Campos inválidos", campos, request);
	}

	@ExceptionHandler({
			MethodArgumentTypeMismatchException.class,
			HttpMessageNotReadableException.class
	})
	public ResponseEntity<ErroResponse> handleBadRequest(HttpServletRequest request) {
		return buildResponse(HttpStatus.BAD_REQUEST, "Requisição inválida", null, request);
	}

	@ExceptionHandler({BadCredentialsException.class, DisabledException.class})
	public ResponseEntity<ErroResponse> handleCredenciais(AuthenticationException ex, HttpServletRequest request) {
		return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), null, request);
	}

	@ExceptionHandler(TokenInvalidoException.class)
	public ResponseEntity<ErroResponse> handleTokenInvalido(TokenInvalidoException ex, HttpServletRequest request) {
		return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), null, request);
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ErroResponse> handleAuthentication(HttpServletRequest request) {
		return buildResponse(HttpStatus.UNAUTHORIZED, "Autenticação necessária: envie um token JWT válido", null, request);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErroResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
		String mensagem = ex.getMessage() != null && !ex.getMessage().isBlank() && !"Access Denied".equals(ex.getMessage())
				? ex.getMessage()
				: "Acesso negado: seu perfil não tem permissão para este recurso";
		return buildResponse(HttpStatus.FORBIDDEN, mensagem, null, request);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErroResponse> handleMethodNotSupported(HttpServletRequest request) {
		return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP não suportado para este recurso", null, request);
	}

	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<ErroResponse> handleMediaType(HttpServletRequest request) {
		return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content-Type não suportado; use application/json", null, request);
	}

	@ExceptionHandler(ConflitoException.class)
	public ResponseEntity<ErroResponse> handleConflito(ConflitoException ex, HttpServletRequest request) {
		return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), null, request);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErroResponse> handleDataIntegrityViolation(HttpServletRequest request) {
		return buildResponse(HttpStatus.CONFLICT, "Operação viola a integridade dos dados (registro duplicado ou vinculado)", null, request);
	}

	@ExceptionHandler(RegraNegocioException.class)
	public ResponseEntity<ErroResponse> handleRegraNegocio(RegraNegocioException ex, HttpServletRequest request) {
		return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), null, request);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErroResponse> handleGenericException(Exception ex, HttpServletRequest request) {
		log.error("Erro inesperado em {} {}", request.getMethod(), request.getRequestURI(), ex);
		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no servidor", null, request);
	}

	private ResponseEntity<ErroResponse> buildResponse(HttpStatus status, String mensagem,
			List<CampoErroResponse> campos, HttpServletRequest request) {
		ErroResponse response = ErroResponse.builder()
				.status(status.value())
				.erro(mensagem)
				.timestamp(LocalDateTime.now())
				.path(request.getRequestURI())
				.campos(campos)
				.build();

		return ResponseEntity.status(status).body(response);
	}
}
