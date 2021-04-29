package com.silent.annotation.impl;

import com.google.protobuf.Message;
import com.silent.annotation.ProtobufHttpClientMeta;
import com.silent.annotation.ProtobufHttpRestfulMeta;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.Set;

/**
 * @author gy
 * @version 1.0
 * @date 2021/4/28.
 * @description:
 */
@Component
public class ProtobufHttpClientRegistryProcessor implements BeanDefinitionRegistryPostProcessor, ApplicationContextAware {
	private ApplicationContext applicationContext;

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		String basePackages = applicationContext.getEnvironment().getProperty("client.basePackages");
//
		ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry) {

			protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
				return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
			}


			protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
				Set<BeanDefinitionHolder> beanDefinitionHolders = super.doScan(basePackages);

				for (BeanDefinitionHolder definitionHolder : beanDefinitionHolders) {
					GenericBeanDefinition definition = (GenericBeanDefinition) definitionHolder.getBeanDefinition();
					definition.getConstructorArgumentValues().addGenericArgumentValue(definition.getBeanClassName());
					definition.setBeanClass(ProtobufHttpClientFactoryBean.class);
				}

				return beanDefinitionHolders;
			}
		};
		scanner.setResourceLoader(this.applicationContext);
		scanner.addIncludeFilter(new AnnotationTypeFilter(ProtobufHttpClientMeta.class));
		scanner.scan(basePackages);
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
