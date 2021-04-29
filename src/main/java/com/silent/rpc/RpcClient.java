package com.silent.rpc;

import com.google.protobuf.Message;
import com.silent.discovery.NacosSubcriber;
import com.silent.http.BaseAsyncHttpClient;
import com.silent.loadbalance.meta.ServiceMetaInfo;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author gy
 * @version 1.0
 * @date 2021/4/28.
 * @description:
 */
@Slf4j
@Service
public class RpcClient {
	@Autowired
	private BaseAsyncHttpClient httpClient;

	@Autowired
	private NacosSubcriber nacosSubcriber;

	public Response post(String serviceName, String uri, Message message) throws Exception {
		ServiceMetaInfo.NodeMetaInfo nodeMetaInfo = nacosSubcriber.choose(serviceName, message);

		if (null == nodeMetaInfo) {
			throw new IllegalStateException("没有可用的实例，serviceName : " + serviceName);
		}

		return httpClient.post(serviceName, nodeMetaInfo.getHost(), nodeMetaInfo.getPort(), uri, message.toByteArray());
	}
}
