package com.silent.loadbalance;

import com.silent.loadbalance.event.RequestExceptionFailEvent;
import com.silent.loadbalance.event.RequestSuccessEvent;
import com.silent.loadbalance.meta.ServiceMetaInfo;
import com.silent.loadbalance.rule.IRule;
import com.silent.loadbalance.rule.impl.ConsistentHashRule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.helpers.MessageFormatter;

import java.util.Comparator;

/**
 * @Description 基础负载均衡器实现, 默认负载均衡策略是一致性hash
 * @Author  gy
 * @Date 2019-06-14 15:25
 */
@Slf4j
public class BaseLoadBalancer implements ILoadBalancer {
	private final IRule defaultRule = new ConsistentHashRule(this);
	// 避免子类覆盖了构造函数
	protected IRule rule = defaultRule;

	private static final String DEFAULT_NAME = "LoadBalancer_default";
	protected String name = DEFAULT_NAME;

	private ServiceMetaInfo serviceMetaInfo;

	public BaseLoadBalancer() {
		setRule(rule);
	}

	public void setRule(IRule rule) {
		if (rule != null) {
			this.rule = rule;
		} else {
			this.rule = new ConsistentHashRule(this);
		}
		if (this.rule.getLoadBalancer() != this) {
			this.rule.setLoadBalancer(this);
		}
	}

	@Override
	public void setServers(ServiceMetaInfo serviceMetaInfo) {
		this.serviceMetaInfo = serviceMetaInfo;
		// 如果有需要可以对服务列表进行排序
		if (null != getSortComparator() && null != serviceMetaInfo && CollectionUtils
				.isNotEmpty(serviceMetaInfo.getClusterNetworkMetaInfo())) {
			serviceMetaInfo.getClusterNetworkMetaInfo().sort(getSortComparator());
		}
	}

	@Override
	public ServiceMetaInfo.NodeMetaInfo chooseNode(Object key) {
		if (key == null) {
			log.error("load balance key can not be null");
			return null;
		}

		if (rule == null) {
			log.warn("LoadBalancer[{}] not exist load balance rule]", name);
			return null;
		} else {
			try {
				return rule.choose(key);
			} catch (Exception e) {
				log.warn(MessageFormatter.arrayFormat("LoadBalancer[{}],Select Node Error，key[{}]", new Object[]{name, key}).getMessage(), e);
				return null;
			}
		}
	}

	@Override
	public ServiceMetaInfo getServiceMetaInfo() {
		return this.serviceMetaInfo;
	}

	@Override
	public Comparator<ServiceMetaInfo.NodeMetaInfo> getSortComparator() {
		return null;
	}

	@Override
	public void fireRequestException(RequestExceptionFailEvent event) {
		rule.whenRequestFail(event);
	}

	@Override
	public void fireRequestSuccess(RequestSuccessEvent event) {
		rule.whenRequestSuccess(event);
	}
}
