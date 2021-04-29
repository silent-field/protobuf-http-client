package com.silent.controller;

import com.silent.pb.Echo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.concurrent.ThreadLocalRandom;

@Controller
@RequestMapping(value = "/test")
public class TestController {
	@RequestMapping(value = "/echo1", method = RequestMethod.POST, produces = "application/x-protobuf")
	@ResponseBody
	public Echo.EchoResponse echo1(@RequestBody Echo.EchoRequest request) {
		Echo.EchoResponse echoResp = Echo.EchoResponse.newBuilder()
				.setMessage(request.getMessage() + "-" + ThreadLocalRandom.current().nextLong()).build();

		return echoResp;
	}

	@RequestMapping(value = "/echo2", method = RequestMethod.POST, produces = "application/x-protobuf")
	@ResponseBody
	public Echo.EchoResponse echo2(@RequestBody Echo.EchoRequest request) {
		Echo.EchoResponse echoResp = Echo.EchoResponse.newBuilder()
				.setMessage(request.getMessage() + "-" + ThreadLocalRandom.current().nextLong()).build();

		return echoResp;
	}

	@RequestMapping(value = "/echo3", method = RequestMethod.POST, produces = "application/x-protobuf")
	@ResponseBody
	public Echo.EchoResponse echo3(@RequestBody Echo.EchoRequest request) {
		Echo.EchoResponse echoResp = Echo.EchoResponse.newBuilder()
				.setMessage(request.getMessage() + "-" + ThreadLocalRandom.current().nextLong()).build();

		return echoResp;
	}

	@RequestMapping(value = "/echo4", method = RequestMethod.POST)
	@ResponseBody
	public String echo4() {
		return "哈哈";
	}
}
