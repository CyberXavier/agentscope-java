/*
 * Copyright 2024-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.agentscope.builder.web.config;

import io.agentscope.core.model.Model;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.extensions.model.openai.compat.deepseek.DeepSeekFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * DeepSeek 模型配置（走 OpenAI 兼容协议）。
 *
 * <p>DeepSeek 官方 API 提供 OpenAI 兼容的 HTTP 端点（{@code https://api.deepseek.com}），因此直接复用
 * {@link OpenAIChatModel} + {@link DeepSeekFormatter} 即可接入，无需自定义协议适配。
 *
 * <p>与 {@link BuilderConfig#dashscopeModel()} 的协同：{@link BuilderConfig} 中 DashScope 兜底 Bean 标注了
 * {@code @ConditionalOnMissingBean(Model.class)}，本类只要成功创建一个 {@link Model} Bean，DashScope
 * 模型就不会被装配，从而优先使用 DeepSeek。
 */
@Configuration
public class DeepSeekModelConfig {

    /**
     * 创建 DeepSeek Chat 模型 Bean。
     *
     * <p>配置项（前缀 {@code builder.deepseek.*}，均支持环境变量覆盖）：
     * <ul>
     *   <li>{@code builder.deepseek.api-key}：DeepSeek API Key，默认取环境变量
     *       {@code DEEPSEEK_API_KEY}，两者都为空则不创建该 Bean</li>
     *   <li>{@code builder.deepseek.model-name}：模型名，如 {@code deepseek-chat}（V3 对话模型）、
     *       {@code deepseek-reasoner}（R1 推理模型），默认 {@code deepseek-chat}</li>
     *   <li>{@code builder.deepseek.stream}：是否开启流式输出，默认 {@code true}</li>
     * </ul>
     *
     * @param apiKey    DeepSeek API Key
     * @param modelName 模型名称
     * @param stream    是否流式输出
     * @return 可被 Builder 直接使用的 {@link Model} Bean
     */
    @Bean
    @ConditionalOnMissingBean(Model.class)
    // 仅当配置了 DeepSeek API Key 时才注册该 Bean；未配置时走 BuilderConfig 的 DashScope 兜底
    @ConditionalOnExpression("'${builder.deepseek.api-key:${DEEPSEEK_API_KEY:}}' != ''")
    public Model deepSeekModel(
            @Value("${builder.deepseek.api-key:${DEEPSEEK_API_KEY:}}") String apiKey,
            @Value("${builder.deepseek.model-name:${DEEPSEEK_MODEL_NAME:deepseek-chat}}")
                    String modelName,
            @Value("${builder.deepseek.stream:${DEEPSEEK_STREAM:true}}") boolean stream) {
        return OpenAIChatModel.builder()
                .baseUrl("https://api.deepseek.com") // DeepSeek OpenAI 兼容端点
                .apiKey(apiKey)
                .modelName(modelName)
                .formatter(new DeepSeekFormatter()) // 处理 thinking 模式与 tool 参数兼容
                .stream(stream)
                .build();
    }
}
