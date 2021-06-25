package com.crista.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CustomerException extends RuntimeException {

	public CustomerException(String message) {
		
	}

}
