package com.silent.loadbalance.event;

import lombok.Data;

/**
 * @Description
 * @Author gy
 * @Date 2019-08-02 14:52
 */
@Data
public class RequestSuccessEvent {
	private RequestInfo target;

	public RequestSuccessEvent(RequestInfo target) {
		this.target = target;
	}
}
