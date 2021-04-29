package com.silent.client;

import com.silent.pb.Echo;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Timer;
import java.util.TimerTask;

@Slf4j
@Component
public class TestSender {
	@Autowired
	private HttpClientService httpClientService;

	@Autowired
	private HttpClientService2 httpClientService2;

	private boolean first = true;

	@PostConstruct
	private void init() {
		new Timer().scheduleAtFixedRate(new TimerTask() {
			public void run() {
				if(first) {
					try {
						Thread.sleep(5000);
					} catch (Exception e) {

					} finally {
						first = false;
					}
				}

				try {
					call();
				} catch (Exception e) {
				    log.error("",e);
				}
			}
		} , 1000 , 10000);
	}

	private void call() throws Exception{
		Echo.EchoRequest echo =  Echo.EchoRequest.newBuilder().setMessage("echo1 test request").build();
		Response response = httpClientService.echo1(echo);
		byte[] bytes = response.getResponseBodyAsBytes();
		System.out.println(Echo.EchoResponse.parseFrom(bytes));

		// -----------------
		echo =  Echo.EchoRequest.newBuilder().setMessage("echo2 test request").build();
		response = httpClientService.echo2(echo);
		bytes = response.getResponseBodyAsBytes();
		System.out.println(Echo.EchoResponse.parseFrom(bytes));

		// -----------------
		echo =  Echo.EchoRequest.newBuilder().setMessage("echo3 test request").build();
		response = httpClientService2.echo3(echo);
		bytes = response.getResponseBodyAsBytes();
		System.out.println(Echo.EchoResponse.parseFrom(bytes));
	}
}
