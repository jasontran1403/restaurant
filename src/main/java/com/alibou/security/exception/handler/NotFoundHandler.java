package com.alibou.security.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.alibou.security.dto.Response;
import com.alibou.security.exception.NotFoundException;

@ControllerAdvice
public class NotFoundHandler {
	@ExceptionHandler
	public ResponseEntity<Response> handlerExistedException(NotFoundException notFoundException) {
		Response response = new Response();
		response.setMessage(notFoundException.getMessage());
		response.setStatus(HttpStatus.NOT_FOUND.value());
		response.setTimeStamp(System.currentTimeMillis());
		
		return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
	}
}

