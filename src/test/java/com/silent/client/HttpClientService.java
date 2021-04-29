package com.silent.client;

import com.google.protobuf.Message;
import com.silent.annotation.ProtobufHttpClientMeta;
import com.silent.annotation.ProtobufHttpRestfulMeta;
import org.asynchttpclient.Response;

@ProtobufHttpClientMeta(serviceName = "phc-test1", desc = "测试服务1")
public interface HttpClientService {
	@ProtobufHttpRestfulMeta(desc = "测试方法1", path = "/test/echo1")
	Response echo1(Message request);

	@ProtobufHttpRestfulMeta(desc = "测试方法2", path = "/test/echo2")
	Response echo2(Message request);
}
