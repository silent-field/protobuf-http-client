package com.silent.http;

import com.silent.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.*;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * async http client
 *
 * @author gy
 * @date 2020/3/20
 */
@Slf4j
@Component
public class BaseAsyncHttpClient {
	private BaseAsyncHttpClient() {
	}

	private AsyncHttpClient asyncHttpClient;

	private static final Integer REQUEST_TIMEOUT = 500;
	private static final Integer READ_TIMEOUT = 500;
	private static final Integer CONNECT_TIMEOUT = 1000;
	private static final Integer MAX_CONNECTIONS = 100;
	private static final Integer MAX_CONNECTIONS_PER_HOST = 25;
	private static final Integer CONNECTION_TTL = 60000;
	private static final Boolean KEEP_ALIVE = true;
	private static final Integer MAX_RETRY = 1;

	@PostConstruct
	public void init() {
		if (null != asyncHttpClient) {
			return;
		}
		DefaultAsyncHttpClientConfig.Builder config = Dsl.config();
		config.setStrict302Handling(false);
		config.setConnectTimeout(AppConfig.get("http.connect.timeout", CONNECT_TIMEOUT));
		config.setRequestTimeout(AppConfig.get("http.request.timeout", REQUEST_TIMEOUT));
		config.setReadTimeout(AppConfig.get("http.read.timeout", READ_TIMEOUT));
		config.setKeepAlive(AppConfig.isKey("http.keep.alive", KEEP_ALIVE));
		config.setConnectionTtl(AppConfig.get("http.connection.ttl", CONNECTION_TTL));
		config.setMaxConnections(AppConfig.get("http.max.connections", MAX_CONNECTIONS));
		config.setMaxConnectionsPerHost(AppConfig.get("http.connections.per.host", MAX_CONNECTIONS_PER_HOST));
		config.setMaxRequestRetry(AppConfig.get("http.request.max.retry", MAX_RETRY));
		asyncHttpClient = Dsl.asyncHttpClient(config);
	}

	public Response post(String service, String host, Integer port, String uri, byte[] content) throws Exception {
		log.info("目标服务器 {}:{}", host, port);

		try {
			String url = "http://" + host + ":" + port + uri;
			BoundRequestBuilder builder = asyncHttpClient.preparePost(url);
			int timeout = AppConfig.get("http_timeout_" + service, 2000);
			builder.setRequestTimeout(timeout);
			builder.setBody(content);
			builder.addHeader("Content-Type", "application/x-protobuf;charset=UTF-8");
			builder.addHeader("Connection", "keep-alive");

			ListenableFuture<Response> future = builder.execute();
			return future.get(timeout, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			throw e;
		}
	}

}
