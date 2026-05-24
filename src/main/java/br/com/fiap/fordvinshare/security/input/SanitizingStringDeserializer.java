package br.com.fiap.fordvinshare.security.input;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.io.IOException;
import java.text.Normalizer;

/**
 * Centralized input normalization for Strings:
 * - Unicode normalization (NFKC)
 * - trim
 * - reject dangerous control characters
 *
 * Note: This is not an HTML sanitizer (API returns JSON), but it helps reduce weird Unicode tricks and log-forging.
 */
public class SanitizingStringDeserializer extends JsonDeserializer<String> {

	@Override
	public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		String raw = p.getValueAsString();
		if (raw == null) {
			return null;
		}

		String normalized = Normalizer.normalize(raw, Normalizer.Form.NFKC).trim();
		if (containsDisallowedControlChars(normalized)) {
			throw InvalidFormatException.from(p, "String contains disallowed control characters", raw, String.class);
		}

		return normalized;
	}

	private static boolean containsDisallowedControlChars(String s) {
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == 0x00) return true;
			if (c < 0x20 && c != '\t' && c != '\n' && c != '\r') return true;
			if (c == 0x7F) return true;
		}
		return false;
	}
}
