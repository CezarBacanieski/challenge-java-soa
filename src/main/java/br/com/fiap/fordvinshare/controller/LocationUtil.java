package br.com.fiap.fordvinshare.controller;

import java.net.URI;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** Monta o header Location das respostas 201 Created. */
final class LocationUtil {

	private LocationUtil() {
	}

	static URI of(String path, Object... variaveis) {
		return ServletUriComponentsBuilder.fromCurrentContextPath()
				.path(path)
				.buildAndExpand(variaveis)
				.toUri();
	}
}
