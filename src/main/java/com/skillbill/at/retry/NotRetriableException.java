package com.skillbill.at.retry;

public class NotRetriableException extends RuntimeException {
	private static final long serialVersionUID = -468433659750252949L;

	public NotRetriableException(String s) {
        super(s);
    }	
}