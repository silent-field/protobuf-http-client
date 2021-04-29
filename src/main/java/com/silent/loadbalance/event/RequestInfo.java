package com.silent.loadbalance.event;

import com.google.gson.JsonObject;
import com.silent.loadbalance.ILoadBalancer;
import com.silent.loadbalance.meta.ServiceMetaInfo;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * @Description
 * @Author gy
 * @Date 2019-08-02 14:52
 */
@Builder
@Data
public class RequestInfo {
    private LoadBalanceRequestWrapper request;

	private String currentHost;

	private String serviceSymbol;

	private ServiceMetaInfo.NodeMetaInfo nodeMetaInfo;

	private String url;

	private String jsonBody;

	private boolean businessAlarmSwitch;

	private long slowThreshold;

	private ILoadBalancer loadBalancer;

	@Data
	public static class LoadBalanceRequestWrapper {
		/**
		 * 请求路径
		 */
		private String path;

		/**
		 * 请求参数集
		 */
		private JsonObject params;

		/**
		 * Http Header
		 */
		private Map<String, String> headers;
	}
}