package com.example.lectureregistration.lecture.exception;

public class ExcessCapacityException extends RuntimeException {

	private static final String DEFAULT_MESSAGE = "수강 정원이 초과되었습니다.";

	public ExcessCapacityException() {
		super(DEFAULT_MESSAGE);
	}

	public ExcessCapacityException(String message) {
		super(message);
	}
}
