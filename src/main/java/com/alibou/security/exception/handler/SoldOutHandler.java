package com.alibou.security.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.alibou.security.dto.Response;
import com.alibou.security.exception.SoldOutException;

@ControllerAdvice
public class SoldOutHandler {
	@ExceptionHandler
	public ResponseEntity<Response> handlerExistedException(SoldOutException soldOutException) {
		Response response = new Response();
		response.setMessage(soldOutException.getMessage());
		response.setStatus(HttpStatus.LOCKED.value());
		response.setTimeStamp(System.currentTimeMillis());
		
		return new ResponseEntity<>(response, HttpStatus.LOCKED);
	}
}

