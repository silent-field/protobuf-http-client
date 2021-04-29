package com.silent.discovery;

import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.silent.annotation.ProtobufHttpClientMeta;
import com.silent.annotation.ProtobufHttpRestfulMeta;
import com.silent.loadbalance.BaseLoadBalancer;
import com.silent.loadbalance.ILoadBalancer;
import com.silent.loadbalance.meta.IServiceMetaHolder;
import com.silent.loadbalance.meta.ServiceMetaInfo;
import com.silent.loadbalance.rule.IRule;
import com.silent.loadbalance.rule.LoadBalanceRuleEnum;
import com.silent.loadbalance.rule.impl.ConsistentHashRule;
import com.silent.loadbalance.rule.impl.DynamicWeightRoundRule;
import com.silent.loadbalance.rule.impl.RandomRule;
import com.silent.loadbalance.rule.impl.RoundRule;
import com.silent.nacos.NacosClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author gy
 * @version 1.0
 * @date 2021/4/28.
 * @description:
 */
@Slf4j
@Component
@Order(0)
public class NacosSubcriber implements ApplicationListener<ApplicationReadyEvent>, EventListener, IServiceMetaHolder {
	private Map<String, ProtobufHttpClientMeta> serviceName2ProtobufHttpClientMeta;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
		serviceName2ProtobufHttpClientMeta = new HashMap<>();
		Map<String, Object> beansWithAnnotationMap = applicationReadyEvent.getApplicationContext().getBeansWithAnnotation(ProtobufHttpClientMeta.class);
		Set<String> subscribeServiceNames = new HashSet<>();
		for (Map.Entry<String, Object> entry : beansWithAnnotationMap.entrySet()) {
			Class clazz = entry.getValue().getClass();
			Class<?>[] interfaces = clazz.getInterfaces();
			ProtobufHttpClientMeta protobufHttpClientMeta = interfaces[0].getAnnotation(ProtobufHttpClientMeta.class);

			if (StringUtils.isBlank(protobufHttpClientMeta.serviceName())) {
				throw new IllegalArgumentException("Annotation ProtobufHttpClientMeta's serviceName is empty");
			}

			Method[] methods = interfaces[0].getMethods();
			for (Method method : methods) {
				ProtobufHttpRestfulMeta protobufHttpRestfulMeta = method.getAnnotation(ProtobufHttpRestfulMeta.class);
				if (null != protobufHttpRestfulMeta) {
					if (StringUtils.isBlank(protobufHttpRestfulMeta.path())) {
						throw new IllegalArgumentException("Annotation ProtobufHttpRestfulMeta's path is empty");
					}
				}
			}

			subscribeServiceNames.add(protobufHttpClientMeta.serviceName());
			serviceName2ProtobufHttpClientMeta.put(protobufHttpClientMeta.serviceName(), protobufHttpClientMeta);
		}

		if (CollectionUtils.isNotEmpty(subscribeServiceNames)) {
			for (String subscribeServiceName : subscribeServiceNames) {
				try {
					NacosClient.instance().subscribe(subscribeServiceName, this);
				} catch (Exception e) {
					throw new IllegalArgumentException(e);
				}
			}
		}
	}

	// ------------------- 订阅相关
	private Map<String, ServiceMetaInfo> serviceName2Instances = new ConcurrentHashMap<>();
	private Map<String, ILoadBalancer> serviceName2LoadBalancer = new ConcurrentHashMap<>();
	private ReentrantLock updateLock = new ReentrantLock();

	public ServiceMetaInfo.NodeMetaInfo choose(String serviceName, Object key) {
		if (serviceName2LoadBalancer.containsKey(serviceName)) {
			ILoadBalancer loadBalancer = serviceName2LoadBalancer.get(serviceName);
			return loadBalancer.chooseNode(key);
		}

		return null;
	}

	@Override
	public void onEvent(Event event) {
		if (event instanceof NamingEvent) {
			NamingEvent namingEvent = (NamingEvent) event;

			String serviceName = namingEvent.getServiceName();
			serviceName = serviceName.split("@@")[1];
			List<Instance> instances = namingEvent.getInstances();

			updateLock.lock();

			try {
				List<Instance> actives = instances.stream().filter(new Predicate<Instance>() {
					@Override
					public boolean test(Instance instance) {
						return instance.isEnabled() && instance.isHealthy();
					}
				}).collect(Collectors.toList());

				ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
				serviceMetaInfo.setSymbol(serviceName);

				if (CollectionUtils.isNotEmpty(actives)) {
					for (Instance active : actives) {
						ServiceMetaInfo.NodeMetaInfo nodeMetaInfo = ServiceMetaInfo.NodeMetaInfo.builder()
								.host(active.getIp()).port(active.getPort()).build();

						serviceMetaInfo.addNodeNetworkMetaInfo(nodeMetaInfo);
					}
				}

				serviceName2Instances.put(serviceName, serviceMetaInfo);

				ILoadBalancer loadBalancer = serviceName2LoadBalancer.computeIfAbsent(serviceName, s -> {
					ProtobufHttpClientMeta protobufHttpClientMeta = serviceName2ProtobufHttpClientMeta.get(s);
					LoadBalanceRuleEnum lrRule = protobufHttpClientMeta.lrRule();
					BaseLoadBalancer temp = new BaseLoadBalancer();
					temp.setRule(getByLoadBalanceRuleEnum(lrRule, temp));
					return temp;
				});
				loadBalancer.setServers(serviceMetaInfo);
				serviceName2LoadBalancer.put(serviceName, loadBalancer);
			} catch (Exception e) {
				log.error("DaemonClientWithStateMachine init loop", e);
			} finally {
				updateLock.unlock();
			}
		}
	}

	private IRule getByLoadBalanceRuleEnum(LoadBalanceRuleEnum loadBalanceRuleEnum, ILoadBalancer loadBalancer) {
		switch (loadBalanceRuleEnum) {
			case ROUND:
				return new RoundRule(loadBalancer);
			case RANDOM:
				return new RandomRule(loadBalancer);
			case CONSISTENT_HASH:
				return new ConsistentHashRule(loadBalancer);
			case DYNAMIC_WEIGHT_ROUND:
				return new DynamicWeightRoundRule(loadBalancer);
			default:
				return null;
		}
	}
}
