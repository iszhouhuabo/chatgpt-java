package com.github.iszhouhuabo.configrations;

import com.github.iszhouhuabo.domain.ChatGptConfig;
import com.unfbx.chatgpt.OpenAiStreamClient;
import com.unfbx.chatgpt.function.KeyRandomStrategy;
import com.unfbx.chatgpt.interceptor.OpenAILogger;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @author louye
 */
@Configuration
@RequiredArgsConstructor
public class InitStreamClient {
    private final ChatGptConfig chatGptConfig;

    @Bean
    public OpenAiStreamClient openAiStreamClient() {
        return OpenAiStreamClient
                .builder()
                .apiHost(chatGptConfig.getApiHost())
                .apiKey(chatGptConfig.getApiKey())
                .keyStrategy(new KeyRandomStrategy())
                .okHttpClient(okHttpClient())
                .build();
    }

    @Bean
    public OkHttpClient okHttpClient() {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new OpenAILogger());
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        return new OkHttpClient
                .Builder()
                .addInterceptor(httpLoggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(600, TimeUnit.SECONDS)
                .readTimeout(600, TimeUnit.SECONDS)
                .build();
    }
}
