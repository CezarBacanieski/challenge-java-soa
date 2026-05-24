package br.com.fiap.fordvinshare.security.filter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.nio.charset.Charset;

final class CachedBodyRequestWrapper extends HttpServletRequestWrapper {
	private final byte[] body;

	CachedBodyRequestWrapper(HttpServletRequest request, byte[] body) {
		super(request);
		this.body = body == null ? new byte[0] : body;
	}

	@Override
	public ServletInputStream getInputStream() {
		ByteArrayInputStream bais = new ByteArrayInputStream(body);
		return new ServletInputStream() {
			@Override
			public int read() {
				return bais.read();
			}

			@Override
			public boolean isFinished() {
				return bais.available() == 0;
			}

			@Override
			public boolean isReady() {
				return true;
			}

			@Override
			public void setReadListener(ReadListener readListener) {
			}
		};
	}

	@Override
	public BufferedReader getReader() throws IOException {
		Charset charset = Charset.forName(getCharacterEncoding() == null ? "UTF-8" : getCharacterEncoding());
		return new BufferedReader(new InputStreamReader(getInputStream(), charset));
	}

	byte[] getCachedBody() {
		return body;
	}
}

