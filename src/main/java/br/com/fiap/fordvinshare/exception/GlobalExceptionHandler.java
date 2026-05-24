package br.com.fiap.fordvinshare.exception;

import br.com.fiap.fordvinshare.dto.response.CampoErroResponse;
import br.com.fiap.fordvinshare.dto.response.ErroResponse;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.MDC;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<ErroResponse> handleEntityNotFound(EntityNotFoundException ex) {
		return buildResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", "Recurso não encontrado", null);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErroResponse> handleValidation(MethodArgumentNotValidException ex) {
		List<CampoErroResponse> campos = ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(erro -> CampoErroResponse.builder()
						.campo(erro.getField())
						.mensagem(erro.getDefaultMessage())
						.build())
				.toList();

		return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Campos inválidos", campos);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErroResponse> handleDataIntegrityViolation() {
		return buildResponse(HttpStatus.CONFLICT, "CONFLICT", "Registro já existente", null);
	}

	@ExceptionHandler({
			MethodArgumentTypeMismatchException.class,
			HttpMessageNotReadableException.class
	})
	public ResponseEntity<ErroResponse> handleBadRequest() {
		return buildResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Requisição inválida", null);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErroResponse> handleIllegalArgument(IllegalArgumentException ex) {
		if ("Invalid credentials".equals(ex.getMessage())) {
			return buildResponse(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Credenciais inválidas", null);
		}
		return buildResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Requisição inválida", null);
	}

	@ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
	public ResponseEntity<ErroResponse> handleAccessDenied() {
		return buildResponse(HttpStatus.FORBIDDEN, "FORBIDDEN", "Acesso negado", null);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErroResponse> handleGenericException() {
		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Erro interno no servidor", null);
	}

	private ResponseEntity<ErroResponse> buildResponse(HttpStatus status, String codigo, String mensagem, List<CampoErroResponse> campos) {
		ErroResponse response = ErroResponse.builder()
				.status(status.value())
				.codigo(codigo)
				.erro(mensagem)
				.requestId(MDC.get("requestId"))
				.timestamp(LocalDateTime.now())
				.campos(campos)
				.build();

		return ResponseEntity.status(status).body(response);
	}
}
