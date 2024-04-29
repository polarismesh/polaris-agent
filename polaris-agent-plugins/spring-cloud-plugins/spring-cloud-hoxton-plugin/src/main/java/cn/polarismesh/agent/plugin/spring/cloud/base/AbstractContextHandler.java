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

package cn.polarismesh.agent.plugin.spring.cloud.base;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.function.BiConsumer;

public abstract class AbstractContextHandler implements ApplicationContextAware {

	public static boolean hasBeanDefinition(ApplicationContext context, String name) {
		if (context.containsBeanDefinition(name)) {
			return true;
		}
		ApplicationContext parent = context.getParent();
		if (null != parent) {
			return hasBeanDefinition(parent, name);
		}
		return false;
	}

	protected void registerBean(ApplicationContext context, String name, BiConsumer<ApplicationContext, String> callback) {
		if (hasBeanDefinition(context, name)) {
			return;
		}
		callback.accept(context, name);
	}
}
