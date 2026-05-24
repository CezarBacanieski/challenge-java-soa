package br.com.fiap.fordvinshare.security.crypto;

import jakarta.persistence.Convert;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Convert(converter = EncryptedStringAttributeConverter.class)
public @interface EncryptedString {
}

