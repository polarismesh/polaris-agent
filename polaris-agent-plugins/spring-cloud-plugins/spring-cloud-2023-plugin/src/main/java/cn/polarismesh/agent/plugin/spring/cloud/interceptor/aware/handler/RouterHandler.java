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

package cn.polarismesh.agent.plugin.spring.cloud.interceptor.aware.handler;

import java.util.function.Supplier;

import cn.polarismesh.agent.core.common.utils.ClassUtils;
import cn.polarismesh.agent.plugin.spring.cloud.common.Holder;
import com.tencent.cloud.polaris.router.config.FeignAutoConfiguration;
import com.tencent.cloud.polaris.router.config.LoadBalancerConfiguration;
import com.tencent.cloud.polaris.router.config.RouterAutoConfiguration;
import com.tencent.cloud.polaris.router.config.RouterConfigModifierAutoConfiguration;
import com.tencent.cloud.polaris.router.config.properties.PolarisMetadataRouterProperties;
import com.tencent.cloud.polaris.router.config.properties.PolarisNearByRouterProperties;
import com.tencent.cloud.polaris.router.config.properties.PolarisRuleBasedRouterProperties;
import com.tencent.cloud.polaris.router.endpoint.PolarisRouterEndpointAutoConfiguration;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

public class RouterHandler extends AbstractContextHandler {

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		registerBean(applicationContext, "polarisMetadataRouterProperties", (ctx, name) -> {
			ConfigurableApplicationContext cfgCtx = (ConfigurableApplicationContext) ctx;
			DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) cfgCtx.getBeanFactory();
			beanFactory.registerBeanDefinition(name,
					BeanDefinitionBuilder.genericBeanDefinition(PolarisMetadataRouterProperties.class, new Supplier<PolarisMetadataRouterProperties>() {
						@Override
						public PolarisMetadataRouterProperties get() {
							return Holder.getMetadataRouterProperties();
						}
					}).getBeanDefinition());
		});
		registerBean(applicationContext, "polarisNearByRouterProperties", (ctx, name) -> {
			ConfigurableApplicationContext cfgCtx = (ConfigurableApplicationContext) ctx;
			DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) cfgCtx.getBeanFactory();
			beanFactory.registerBeanDefinition(name,
					BeanDefinitionBuilder.genericBeanDefinition(PolarisNearByRouterProperties.class, new Supplier<PolarisNearByRouterProperties>() {
						@Override
						public PolarisNearByRouterProperties get() {
							return Holder.getNearByRouterProperties();
						}
					}).getBeanDefinition());
		});
		registerBean(applicationContext, "polarisRuleBasedRouterProperties", (ctx, name) -> {
			ConfigurableApplicationContext cfgCtx = (ConfigurableApplicationContext) ctx;
			DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) cfgCtx.getBeanFactory();
			beanFactory.registerBeanDefinition(name,
					BeanDefinitionBuilder.genericBeanDefinition(PolarisRuleBasedRouterProperties.class, new Supplier<PolarisRuleBasedRouterProperties>() {
						@Override
						public PolarisRuleBasedRouterProperties get() {
							return Holder.getRouterProperties();
						}
					}).getBeanDefinition());
		});
		registerBean(applicationContext, "routerConfigModifierAutoConfiguration",  (ctx, name) -> {
			ConfigurableApplicationContext cfgCtx = (ConfigurableApplicationContext) ctx;
			DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) cfgCtx.getBeanFactory();
			beanFactory.registerBeanDefinition(name,
					BeanDefinitionBuilder.genericBeanDefinition(RouterConfigModifierAutoConfiguration.class).getBeanDefinition());
		});
		registerBean(applicationContext, "routerAutoConfiguration",  (ctx, name) -> {
			ConfigurableApplicationContext cfgCtx = (ConfigurableApplicationContext) ctx;
			DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) cfgCtx.getBeanFactory();
			beanFactory.registerBeanDefinition(name,
					BeanDefinitionBuilder.genericBeanDefinition(RouterAutoConfiguration.class).getBeanDefinition());
		});
		if (null != ClassUtils.getClazz("feign.RequestInterceptor",
				Thread.currentThread().getContextClassLoader())) {
			registerBean(applicationContext, "feignAutoConfiguration",  (ctx, name) -> {
				ConfigurableApplicationContext cfgCtx = (ConfigurableApplicationContext) ctx;
				DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) cfgCtx.getBeanFactory();
				beanFactory.registerBeanDefinition(name,
						BeanDefinitionBuilder.genericBeanDefinition(FeignAutoConfiguration.class).getBeanDefinition());
			});
		}
		if (null != ClassUtils.getClazz("org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier",
				Thread.currentThread().getContextClassLoader())) {
			registerBean(applicationContext, "loadBalancerConfiguration", (ctx, name) -> {
				ConfigurableApplicationContext cfgCtx = (ConfigurableApplicationContext) ctx;
				DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) cfgCtx.getBeanFactory();
				beanFactory.registerBeanDefinition(name,
						BeanDefinitionBuilder.genericBeanDefinition(LoadBalancerConfiguration.class)
								.getBeanDefinition());
			});
		}
	}

}
