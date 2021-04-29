package com.silent.discovery;

import com.silent.nacos.NacosClient;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

/**
 * @author gy
 * @version 1.0
 * @date 2021/4/28.
 * @description:
 */
@Component
public class NacosDeregisterListener implements ApplicationListener<ContextClosedEvent> {
	@Override
	public void onApplicationEvent(ContextClosedEvent event) {
		try {
			NacosClient.instance().deregister();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
