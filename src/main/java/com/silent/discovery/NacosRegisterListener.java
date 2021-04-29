package com.silent.discovery;

import com.silent.nacos.NacosClient;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author gy
 * @version 1.0
 * @date 2021/4/28.
 * @description:
 */
@Component
@Order(10)
public class NacosRegisterListener implements ApplicationListener<ApplicationReadyEvent> {
	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		try {
			NacosClient.instance().register();
		} catch (Exception e) {
		    throw new RuntimeException(e);
		}
	}
}
