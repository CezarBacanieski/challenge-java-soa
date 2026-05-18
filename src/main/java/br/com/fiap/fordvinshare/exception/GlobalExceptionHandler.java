package br.com.fiap.fordvinshare.exception;

import br.com.fiap.fordvinshare.dto.response.CampoErroResponse;
import br.com.fiap.fordvinshare.dto.response.ErroResponse;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
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
		return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null);
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

		return buildResponse(HttpStatus.BAD_REQUEST, "Campos inválidos", campos);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErroResponse> handleDataIntegrityViolation() {
		return buildResponse(HttpStatus.CONFLICT, "Registro já existente", null);
	}

	@ExceptionHandler({
			MethodArgumentTypeMismatchException.class,
			HttpMessageNotReadableException.class
	})
	public ResponseEntity<ErroResponse> handleBadRequest() {
		return buildResponse(HttpStatus.BAD_REQUEST, "Requisição inválida", null);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErroResponse> handleGenericException() {
		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no servidor", null);
	}

	private ResponseEntity<ErroResponse> buildResponse(HttpStatus status, String mensagem, List<CampoErroResponse> campos) {
		ErroResponse response = ErroResponse.builder()
				.status(status.value())
				.erro(mensagem)
				.timestamp(LocalDateTime.now())
				.campos(campos)
				.build();

		return ResponseEntity.status(status).body(response);
	}
}
