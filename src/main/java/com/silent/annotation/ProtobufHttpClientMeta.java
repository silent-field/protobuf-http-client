package com.silent.annotation;

import com.silent.loadbalance.rule.LoadBalanceRuleEnum;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ProtobufHttpClientMeta {
	/**
	 * 服务名
	 */
	String serviceName();

	/**
	 * 服务描述
	 */
	String desc() default "";

	/**
	 * 路由策略
	 * @return
	 */
	LoadBalanceRuleEnum lrRule() default LoadBalanceRuleEnum.ROUND;
}
