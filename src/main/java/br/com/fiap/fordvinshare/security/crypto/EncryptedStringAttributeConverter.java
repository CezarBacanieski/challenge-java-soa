package br.com.fiap.fordvinshare.security.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class EncryptedStringAttributeConverter implements AttributeConverter<String, String> {

	@Override
	public String convertToDatabaseColumn(String attribute) {
		if (attribute == null) {
			return null;
		}
		return CryptoContext.getEncryptor().encrypt(attribute);
	}

	@Override
	public String convertToEntityAttribute(String dbData) {
		if (dbData == null) {
			return null;
		}
		try {
			return CryptoContext.getEncryptor().decrypt(dbData);
		} catch (IllegalArgumentException e) {
			return dbData;
		}
	}
}

