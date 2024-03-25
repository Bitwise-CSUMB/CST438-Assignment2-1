package com.cst438.test.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class TestUtils {

	public static String asJsonString(final Object obj) {
		try {
			return new ObjectMapper().writeValueAsString(obj);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T fromJsonString(String str, Class<T> valueType) {
		try {
			return new ObjectMapper().readValue(str, valueType);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static LocalDateTime getNow() {
		return LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
	}

	public static Date getSqlDate(LocalDateTime dateTime) {
		return Date.valueOf(dateTime.toLocalDate());
	}
}
