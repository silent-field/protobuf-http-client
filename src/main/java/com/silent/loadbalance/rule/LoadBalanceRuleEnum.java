package com.silent.loadbalance.rule;

import com.silent.dict.Dict;
import com.silent.loadbalance.rule.impl.RoundRule;
import org.apache.commons.lang3.StringUtils;

/**
 * 负载均衡策略枚举
 *
 * @Author gy
 * @Date 2020-03-20 15:25
 */
public enum LoadBalanceRuleEnum implements Dict<String, String> {
	ROUND("round","轮询"),
	RANDOM("random", "随机"),
	CONSISTENT_HASH("consistent hash","一致性hash"),
	DYNAMIC_WEIGHT_ROUND("dynamic weight round","动态加权轮询"),
	;

	private String code;
	private String desc;

	LoadBalanceRuleEnum(String code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public String getDesc() {
		return desc;
	}

	public static LoadBalanceRuleEnum getByCode(String code) {
		if(StringUtils.isBlank(code)){
			return null;
		}

		for(LoadBalanceRuleEnum loadBalanceRuleEnum : LoadBalanceRuleEnum.values()){
			if(code.equals(loadBalanceRuleEnum.getCode())){
				return loadBalanceRuleEnum;
			}
		}

		return null;
	}
}
