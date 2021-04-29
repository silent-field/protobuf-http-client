package com.silent.loadbalance.rule.impl;


import com.silent.loadbalance.ILoadBalancer;
import com.silent.loadbalance.meta.ServiceMetaInfo;
import com.silent.loadbalance.rule.AbstractLoadBalancerRule;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机策略
 *
 * @Author gy
 * @Date 2020-03-20 15:51
 */
public class RandomRule extends AbstractLoadBalancerRule {
    public RandomRule(ILoadBalancer loadBalancer) {
        super(loadBalancer);
    }

    @Override
    public ServiceMetaInfo.NodeMetaInfo choose(ILoadBalancer lb, Object key) {
        List<ServiceMetaInfo.NodeMetaInfo> clusterNetworkMetaInfo = lb.getServiceMetaInfo().getClusterNetworkMetaInfo();
        int nextServerIndex = chooseRandomInt(clusterNetworkMetaInfo.size());
        return clusterNetworkMetaInfo.get(nextServerIndex);
    }

    /**
     * 随机返回 0 ~ serverCount - 1
     *
     * @param serverCount
     * @return
     */
    protected int chooseRandomInt(int serverCount) {
        return ThreadLocalRandom.current().nextInt(serverCount);
    }
}
