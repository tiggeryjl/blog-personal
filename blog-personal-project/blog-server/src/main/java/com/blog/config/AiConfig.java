package com.blog.config;

import com.blog.properties.AIProperties;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@AllArgsConstructor
public class AiConfig {

    private final AIProperties aiProperties;

    /**
     * 默认Flash模型
     */
    @Bean
    @Primary
    public ChatClient flashChatClient() {
        OpenAiApi api = buildApi();
        OpenAiChatModel model = OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(aiProperties.getFlash().getModel())
                        .temperature(aiProperties.getFlash().getTemperature())
                        .build())
                .build();
        return ChatClient.builder(model).build();
    }

    /**
     * Pro模型深度推理
     */
    @Bean
    public ChatClient proChatClient() {
        OpenAiApi api = buildApi();
        OpenAiChatModel model = OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(aiProperties.getPro().getModel())
                        .temperature(aiProperties.getPro().getTemperature())
                        .build())
                .build();
        return ChatClient.builder(model).build();
    }

    private OpenAiApi buildApi() {
        return OpenAiApi.builder()
                .baseUrl(aiProperties.getBaseUrl())
                .apiKey(aiProperties.getApiKey())
                .build();
    }
}
