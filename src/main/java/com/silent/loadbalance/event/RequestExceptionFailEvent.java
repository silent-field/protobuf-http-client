package com.silent.loadbalance.event;

import lombok.Data;

/**
 * @Description
 * @Author gy
 * @Date 2019-08-02 14:52
 */
@Data
public class RequestExceptionFailEvent {
	private RequestInfo target;

	private Exception failException;

	public RequestExceptionFailEvent(RequestInfo target, Exception failException) {
		this.target = target;
		this.failException = failException;
	}
}
