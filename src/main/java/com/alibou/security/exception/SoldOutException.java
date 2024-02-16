package com.alibou.security.exception;

public class SoldOutException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public SoldOutException() {
	}

	public SoldOutException(String message) {
		super(message);
	}

	public SoldOutException(Throwable cause) {
		super(cause);
	}

	public SoldOutException(String message, Throwable cause) {
		super(message, cause);
	}

}
