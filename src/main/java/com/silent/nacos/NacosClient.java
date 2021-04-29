package com.silent.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.silent.config.AppConfig;
import com.silent.util.LocalIpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author gy
 * @version 1.0
 * @date 2021/4/27.
 * @description:
 */
@Slf4j
public class NacosClient {
	private static AtomicBoolean init = new AtomicBoolean(false);

	private ConfigService configService;

	private NamingService namingService;

	private String nacosAddr;
	private String appName;
	private String appGroup;
	private String logDir;
	private String serverPort;

	private NacosClient() {
	}

	private static class InstanceHolder {
		private static final NacosClient instance = new NacosClient();
	}

	public static NacosClient instance() {
		NacosClient client = InstanceHolder.instance;
		return client;
	}

	public void init(ConfigurableEnvironment env) throws NacosException {
		nacosAddr = env.getProperty("nacos.addr");
		appName = env.getProperty("application.name");
		appGroup = env.getProperty("application.group");
		serverPort = env.getProperty("server.port");

		log.info("nacos params. nacosAddr : {}, appName : {}, appGroup : {}", nacosAddr, appName, appGroup);
		if (StringUtils.isAnyBlank(nacosAddr, appName, appGroup)) {
			throw new IllegalArgumentException("nacosAddr/appName/appGroup/serverName");
		}

		logDir = "/data/java_service/log/" + appName;

		initConfig();
		namingService = NamingFactory.createNamingService(nacosAddr);
	}

	private void initConfig() throws NacosException {
		if (init.compareAndSet(false, true)) {
			System.setProperty("nacos.logging.path", logDir);


			Properties properties = new Properties();
			properties.put(PropertyKeyConst.SERVER_ADDR, nacosAddr);

			configService = NacosFactory.createConfigService(properties);
			String content = configService.getConfig(appName, appGroup, 5000);

			log.info("loading nacos config : {}", content);

			AppConfig.refreshNacosConfig(content);
			// 注册监听
			configService.addListener(appName, appGroup, new Listener() {
				@Override
				public void receiveConfigInfo(String configInfo) {
					log.info("receive nacos config:" + configInfo);
					AppConfig.refreshNacosConfig(configInfo);
				}

				@Override
				public Executor getExecutor() {
					return null;
				}
			});
		}
	}

	public void register() throws NacosException {
		if (!AppConfig.isKey("is_nacos_discovery", true)) {
			return;
		}

		String ip = LocalIpUtil.getLocalIPList().get(0);
		if (StringUtils.isAnyBlank(appName, appGroup, ip, serverPort)) {
			log.error("nacos discovery(register) can not find appName:{}/appGroup:{}/ip:{}/port:{}", appName, appGroup, ip, serverPort);
			return;
		}

		/**
		 * 后续注册时可以带上区域信息，便于实现更复杂的负载均衡策略
		 */
		Instance instance = new Instance();
		instance.setIp(ip);
		instance.setPort(Integer.parseInt(serverPort));
		namingService.registerInstance(appName, appGroup, instance);
	}

	public void deregister() throws NacosException {
		if (!AppConfig.isKey("is_nacos_discovery", true)) {
			return;
		}

		String ip = LocalIpUtil.getExtranetIp();
		if (StringUtils.isAnyBlank(appName, appGroup, ip, serverPort)) {
			log.error("nacos discovery(deregister) can not find appName:{}/appGroup:{}/ip:{}/port:{}", appName, appGroup, ip, serverPort);
			return;
		}
		namingService.deregisterInstance(appName, appGroup, ip, Integer.parseInt(serverPort));
	}

	public void subscribe(String subscribeServiceName, EventListener listener) throws NacosException{
		namingService.subscribe(subscribeServiceName, appGroup, listener);
	}
}
