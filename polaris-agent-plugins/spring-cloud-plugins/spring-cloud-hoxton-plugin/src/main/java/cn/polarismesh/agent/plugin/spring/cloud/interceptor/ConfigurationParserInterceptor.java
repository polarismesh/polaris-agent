/*
 * Tencent is pleased to support the open source community by making Polaris available.
 *
 * Copyright (C) 2019 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package cn.polarismesh.agent.plugin.spring.cloud.interceptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import cn.polarismesh.agent.core.common.utils.ClassUtils;
import cn.polarismesh.agent.core.common.utils.ReflectionUtils;
import cn.polarismesh.agent.core.extension.interceptor.Interceptor;
import cn.polarismesh.agent.plugin.spring.cloud.common.BeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.inject.CircuitBreakerBeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.inject.CommonBeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.inject.ConfigBeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.inject.LoadbalancerBeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.inject.LosslessBeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.inject.MetadataTransferBeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.inject.PolarisContextBeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.inject.RateLimitBeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.inject.RegistryBeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.inject.RouterBeanInjector;
import cn.polarismesh.agent.plugin.spring.cloud.inject.RpcEnhancementBeanInjector;

import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.Environment;

public class ConfigurationParserInterceptor implements Interceptor {

	private final List<BeanInjector> beanInjectors = new ArrayList<>();

	public ConfigurationParserInterceptor() {
		beanInjectors.add(new CommonBeanInjector());
		beanInjectors.add(new PolarisContextBeanInjector());
		beanInjectors.add(new MetadataTransferBeanInjector());
		beanInjectors.add(new RegistryBeanInjector());
		beanInjectors.add(new ConfigBeanInjector());
		beanInjectors.add(new RpcEnhancementBeanInjector());
		beanInjectors.add(new LosslessBeanInjector());
		beanInjectors.add(new LoadbalancerBeanInjector());
		beanInjectors.add(new RouterBeanInjector());
		//beanInjectors.add(new CircuitBreakerBeanInjector());
		beanInjectors.add(new RateLimitBeanInjector());
	}


	private static boolean isMainBeanDefinition(BeanDefinitionHolder beanDefinitionHolder) {
		BeanDefinition beanDefinition = beanDefinitionHolder.getBeanDefinition();
		if (beanDefinition instanceof AnnotatedGenericBeanDefinition) {
			AnnotatedGenericBeanDefinition annotatedBeanDefinition = (AnnotatedGenericBeanDefinition)beanDefinition;
			Class<?> beanClass = annotatedBeanDefinition.getBeanClass();
			Annotation[] annotations = beanClass.getAnnotations();
			for (Annotation annotation : annotations) {
				Class<? extends Annotation> aClass = annotation.annotationType();
				if ("org.springframework.boot.autoconfigure.SpringBootApplication".equals(aClass.getCanonicalName())) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void after(Object target, Object[] args, Object result, Throwable throwable) {
		Set<?> candidates = (Set<?>) args[0];
		if (candidates.size() != 1) {
			return;
		}
		BeanDefinitionHolder beanDefinitionHolder = (BeanDefinitionHolder) candidates.iterator().next();
		if ("bootstrapImportSelectorConfiguration".equals(beanDefinitionHolder.getBeanName())) {
			// bootstrap
			Class<?> clazz = ClassUtils.getClazz("org.springframework.context.annotation.ConfigurationClass", null);
			Constructor<?> constructor = ReflectionUtils.accessibleConstructor(clazz, Class.class, String.class);
			Method processConfigurationClass = ReflectionUtils.findMethod(target.getClass(), "processConfigurationClass", clazz, Predicate.class);
			ReflectionUtils.makeAccessible(processConfigurationClass);

			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) ReflectionUtils.getObjectByFieldName(target, "registry");
			Environment environment = (Environment) ReflectionUtils.getObjectByFieldName(target, "environment");
			for (BeanInjector beanInjector : beanInjectors) {
				beanInjector.onBootstrapStartup(target, constructor, processConfigurationClass, registry, environment);
			}

		} else if (isMainBeanDefinition(beanDefinitionHolder)) {
			Class<?> clazz = ClassUtils.getClazz("org.springframework.context.annotation.ConfigurationClass", null);
			Constructor<?> constructor = ReflectionUtils.accessibleConstructor(clazz, Class.class, String.class);
			Method processConfigurationClass = ReflectionUtils.findMethod(target.getClass(), "processConfigurationClass", clazz, Predicate.class);
			ReflectionUtils.makeAccessible(processConfigurationClass);

			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) ReflectionUtils.getObjectByFieldName(target, "registry");
			Environment environment = (Environment) ReflectionUtils.getObjectByFieldName(target, "environment");
			for (BeanInjector beanInjector : beanInjectors) {
				beanInjector.onApplicationStartup(target, constructor, processConfigurationClass, registry, environment);
			}

		}
	}
}


