package com.example.bg.ai.config;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RAGConfig {

    private static final String QWEN_API_KEY = "sk-8007e602891742f59f118c678a3ab7d1";

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return QwenChatModel.builder()
                .apiKey(QWEN_API_KEY)
                .modelName("qwen-plus")
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        return QwenEmbeddingModel.builder()
                .apiKey(QWEN_API_KEY)
                .modelName("text-embedding-v2")
                .build();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel() {
        return QwenStreamingChatModel.builder()
                .apiKey(QWEN_API_KEY)
                .modelName("qwen-plus")
                .build();
    }
}