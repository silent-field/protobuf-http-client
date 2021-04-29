//package com.silent.annotation.impl;
//
//import com.silent.annotation.ProtobufHttpClientMeta;
//import com.silent.annotation.ProtobufHttpRestfulMeta;
//import org.apache.commons.lang3.ArrayUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.BeansException;
//import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
//import org.springframework.beans.factory.config.BeanDefinitionHolder;
//import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
//import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
//import org.springframework.beans.factory.support.BeanDefinitionRegistry;
//import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
//import org.springframework.beans.factory.support.GenericBeanDefinition;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.ApplicationContextAware;
//import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
//import org.springframework.core.type.filter.AnnotationTypeFilter;
//import org.springframework.stereotype.Component;
//import sun.plugin2.message.Message;
//
//import java.lang.reflect.Parameter;
//import java.lang.reflect.Proxy;
//import java.util.Set;
//
///**
// * @author gy
// * @version 1.0
// * @date 2021/4/28.
// * @description: 第二种基于接口注解生成实现类注册到spring方式
// */
//@Component
//public class ProtobufHttpClientRegistryProcessor2 implements
//		BeanDefinitionRegistryPostProcessor, ApplicationContextAware, InstantiationAwareBeanPostProcessor {
//	private ApplicationContext applicationContext;
//
//	@Override
//	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
//		String basePackages = applicationContext.getEnvironment().getProperty("protobuf.http.client.basePackages");
//
//		ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry) {
//
//			protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
//				return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
//			}
//		};
//		scanner.setResourceLoader(this.applicationContext);
//		//此处设置扫描包含类的过滤去，AnnotationTypeFilter将会通过注解进行过滤，只有加了RemoteService注解的类会被注册进来
//		scanner.addIncludeFilter(new AnnotationTypeFilter(ProtobufHttpClientMeta.class));
//		//要扫描的包路径
//		scanner.scan(basePackages);
//	}
//
//	@Override
//	public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
//		if (beanClass.isAnnotationPresent(ProtobufHttpClientMeta.class)) {
//			return Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{beanClass},
//					(proxy, method, args) -> {
//						ProtobufHttpRestfulMeta protobufHttpRestfulMeta
//								= method.getAnnotation(ProtobufHttpRestfulMeta.class);
//						if (protobufHttpRestfulMeta == null) {
//							throw new IllegalStateException("not exist annotation ProtobufHttpRestfulMeta");
//						}
//
//						String path = protobufHttpRestfulMeta.path();
//
//						Parameter[] methodParameters = method.getParameters();
//
//						if (ArrayUtils.isEmpty(methodParameters)) {
//							throw new IllegalStateException("There are methods which use ProtobufHttpRestfulMeta annotation," +
//									" but have no one arg");
//						}
//
//						if (!methodParameters[0].getType().isAssignableFrom(Message.class)) {
//							throw new IllegalStateException("There are methods which use ProtobufHttpRestfulMeta annotation," +
//									" but have no 'com.google.protobuf.Message' arg");
//						}
//
//
//						return null;
//					});
//		}
//		return null;
//	}
//
//	@Override
//	public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
//
//	}
//
//	@Override
//	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//		this.applicationContext = applicationContext;
//	}
//}
