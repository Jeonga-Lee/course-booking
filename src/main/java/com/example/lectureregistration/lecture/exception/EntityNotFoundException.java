package com.example.lectureregistration.lecture.exception;

public class EntityNotFoundException extends RuntimeException {

	private static final String DEFAULT_MESSAGE = "조회된 특강이 없습니다.";

	public EntityNotFoundException() {
		super(DEFAULT_MESSAGE);
	}
	public EntityNotFoundException(String message) {
		super(message);
	}
}
