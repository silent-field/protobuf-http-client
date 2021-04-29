package com.silent.loadbalance.rule;


import com.silent.loadbalance.ILoadBalancer;
import com.silent.loadbalance.event.RequestExceptionFailEvent;
import com.silent.loadbalance.event.RequestSuccessEvent;
import com.silent.loadbalance.meta.ServiceMetaInfo;

/**
 * 负载均衡策略类
 *
 * @Author gy
 * @Date 2020-03-20 15:25
 */
public interface IRule {
	/**
	 * 通过负载均衡策略选出一个节点
	 * @param key
	 * @return
	 */
	ServiceMetaInfo.NodeMetaInfo choose(Object key);

	/**
	 * 设置负载均衡器
	 * @param lb
	 */
	void setLoadBalancer(ILoadBalancer lb);

	/**
	 * 获取负载均衡器
	 * @return
	 */
	ILoadBalancer getLoadBalancer();

	/**
	 * 当请求发生异常时触发事件
	 * @param event
	 */
	void whenRequestFail(RequestExceptionFailEvent event);

	/**
	 * 当请求发生异常时触发事件
	 * @param event
	 */
	void whenRequestSuccess(RequestSuccessEvent event);
}