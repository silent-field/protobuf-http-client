package com.silent.client;

import com.google.protobuf.Message;
import com.silent.annotation.ProtobufHttpClientMeta;
import com.silent.annotation.ProtobufHttpRestfulMeta;
import org.asynchttpclient.Response;

@ProtobufHttpClientMeta(serviceName = "phc-test1", desc = "测试服务2")
public interface HttpClientService2 {
	@ProtobufHttpRestfulMeta(desc = "测试方法3", path = "/test/echo3")
	Response echo3(Message request);
}
