package com.silent.annotation.impl;

import com.google.protobuf.Message;
import com.silent.annotation.ProtobufHttpClientMeta;
import com.silent.annotation.ProtobufHttpRestfulMeta;
import com.silent.rpc.RpcClient;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;

/**
 * @author gy
 * @version 1.0
 * @date 2021/4/28.
 * @description:
 */
public class ProtobufHttpClientFactoryBean<T> implements FactoryBean<T>, ApplicationContextAware {
	private Class<T> clz;

	public ProtobufHttpClientFactoryBean() {
	}

	public ProtobufHttpClientFactoryBean(Class<T> clz) {
		this.clz = clz;
	}

	@Override
	public T getObject() throws Exception {
		return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{this.clz},
				(proxy, method, args) -> {
					ProtobufHttpClientMeta protobufHttpClientMeta = clz.getAnnotation(ProtobufHttpClientMeta.class);
					String serviceName = protobufHttpClientMeta.serviceName();

					ProtobufHttpRestfulMeta protobufHttpRestfulMeta
							= method.getAnnotation(ProtobufHttpRestfulMeta.class);
					if (protobufHttpRestfulMeta == null) {
						throw new IllegalStateException("not exist annotation ProtobufHttpRestfulMeta");
					}

					String path = protobufHttpRestfulMeta.path();

					Parameter[] methodParameters = method.getParameters();

					if (ArrayUtils.isEmpty(methodParameters)) {
						throw new IllegalStateException("There are methods which use ProtobufHttpRestfulMeta annotation," +
								" but have no one arg");
					}

					if (!methodParameters[0].getType().isAssignableFrom(Message.class)) {
						throw new IllegalStateException("There are methods which use ProtobufHttpRestfulMeta annotation," +
								" but have no 'com.google.protobuf.Message' arg");
					}


					return rpcClient.post(serviceName, path, (Message) args[0]);
				});
	}

	@Override
	public Class<?> getObjectType() {
		return this.clz;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	private ApplicationContext applicationContext;
	private RpcClient rpcClient;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
		this.rpcClient = applicationContext.getBean(RpcClient.class);
	}
}
