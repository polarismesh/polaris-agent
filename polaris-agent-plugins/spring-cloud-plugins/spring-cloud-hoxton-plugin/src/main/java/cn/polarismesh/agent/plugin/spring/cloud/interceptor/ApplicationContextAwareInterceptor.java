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

import cn.polarismesh.agent.core.common.logger.CommonLogger;
import cn.polarismesh.agent.core.common.logger.StdoutCommonLoggerFactory;
import cn.polarismesh.agent.plugin.spring.cloud.base.BaseBeanHandler;
import cn.polarismesh.agent.plugin.spring.cloud.common.Holder;
import cn.polarismesh.agent.plugin.spring.cloud.discovery.DiscoveryHandler;
import cn.polarismesh.agent.plugin.spring.cloud.metadata.MetadataHandler;
import com.google.common.collect.Maps;
import com.tencent.cloud.common.metadata.MetadataContext;
import com.tencent.cloud.common.util.ApplicationContextAwareUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import com.tencent.cloud.polaris.registry.PolarisAutoServiceRegistration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ApplicationContextAwareInterceptor extends BaseInterceptor {

	private static final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE
			.getLogger(ApplicationContextAwareInterceptor.class.getCanonicalName());


	@Override
	public void onAfter(Object target, Object[] args, Object result, Throwable throwable) {


		ConfigurableApplicationContext context = (ConfigurableApplicationContext) args[0];
		String enable = context.getEnvironment().getProperty("spring.cloud.polaris.enabled");
		if (null != enable && !Boolean.parseBoolean(enable)) {
			logger.warn("polaris is disabled, no polaris inject actions will be taken");
			return;
		}
		ApplicationContextAwareUtils utils = new ApplicationContextAwareUtils();

		// MetadataContext 需要读取到 agent 配置的内容
		AnnotationConfigApplicationContext tmpCtx = new AnnotationConfigApplicationContext((DefaultListableBeanFactory) context.getBeanFactory());
		tmpCtx.setEnvironment((ConfigurableEnvironment) Holder.getEnvironment());
		utils.setApplicationContext(tmpCtx);
		// 触发 MetadataContext 的加载机制
		MetadataContext metadataContext = new MetadataContext();
		// 重写 SCT 中错误的 LOCAL_SERVICE 信息
		MetadataContext.LOCAL_SERVICE = Holder.getDiscoveryProperties().getService();

		// 设置为真正的 ApplicationContext
		utils.setApplicationContext(context);
		buildAwares().forEach(aware -> aware.setApplicationContext(context));

//		@Component
//		class TimeCostBeanPostProcessor implements BeanPostProcessor {
//
//			Map<String, Long> costMap = Maps.newConcurrentMap();
//
//			@Autowired
//			private ApplicationContext applicationContext;
//
//			@Override
//			public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
//				costMap.put(beanName, System.currentTimeMillis());
//				return bean;
//			}
//
//			@Override
//			public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
//				if ("autoServiceRegistrationProperties".equals(beanName)) {
//					// 在polarisAutoServiceRegistration初始化之后注册autoServiceRegistrationProperties
//					ConfigurableApplicationContext cfgCtx = (ConfigurableApplicationContext) applicationContext;
//					DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) cfgCtx.getBeanFactory();
//					beanFactory.registerBeanDefinition("polarisAutoServiceRegistration",
//							BeanDefinitionBuilder.genericBeanDefinition(PolarisAutoServiceRegistration.class)
//									.getBeanDefinition());
//				}
//				Long start = costMap.get(beanName);
//				long cost = System.currentTimeMillis() - start;
//				costMap.put(beanName, cost);
////				System.out.println("class: " + bean.getClass().getName()
////						+ "\tbean: "+ beanName
////						+ "\ttime: "+ cost);
//
//				return bean;
//			}
//		}
	}

	private List<ApplicationContextAware> buildAwares() {
		return Arrays.asList(
				new BaseBeanHandler(),
				new DiscoveryHandler(),
				new MetadataHandler()
		);
	}
}
