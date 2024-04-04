package com.cst438.test.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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

	public static <T> List<T> fromJsonListString(String str, Class<T> valueType) {
		try {
			return new ObjectMapper().readerForListOf(valueType).readValue(str);
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

	public static <T> T updateEntity(Function<Integer, Optional<T>> findFunc, Supplier<Integer> idSupplier) {
		Optional<T> updated = findFunc.apply(idSupplier.get());
		assertTrue(updated.isPresent());
		return updated.get();
	}

	public static void assertStudentHome(WebDriver driver) {
		try {
			driver.findElement(By.id("studentHome"));
		}
		catch (NoSuchElementException ignored) {
			fail("Home page is not set to 'STUDENT'");
		}
	}

	public static void assertInstructorHome(WebDriver driver) {
		try {
			driver.findElement(By.id("instructorHome"));
		}
		catch (NoSuchElementException ignored) {
			fail("Home page is not set to 'INSTRUCTOR'");
		}
	}
}
