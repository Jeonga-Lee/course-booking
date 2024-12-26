package com.example.lectureregistration.lecture.exception;

public class DuplicatedRegistrationException extends RuntimeException {

	private static final String DEFAULT_MESSAGE = "이미 신청한 특강입니다.";

	public DuplicatedRegistrationException() {
		super(DEFAULT_MESSAGE);
	}
	public DuplicatedRegistrationException(String message) {
		super(message);
	}
}
