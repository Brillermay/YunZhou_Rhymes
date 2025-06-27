package com.example.bg.ai.service;

import com.example.bg.ConnetMySQL;
import com.example.bg.poem.Poem;
import com.example.bg.poem.PoemGetMapper;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.bg.ai.util.RoleProfileUtil;

@Service
public class EasyRAGService extends ConnetMySQL {

    @Autowired
    private ChatLanguageModel chatLanguageModel;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    // 🆕 注入缓存管理器
    @Autowired
    private EmbeddingCacheManager cacheManager;

    @Autowired
    private StreamingChatLanguageModel streamingChatLanguageModel;

    private boolean isInitialized = false;
    private int successfullyProcessed = 0;

    // 🆕 添加 getCacheManager 方法
    public EmbeddingCacheManager getCacheManager() {
        return cacheManager;
    }

    // 🆕 添加 getCacheStatistics 方法
    public Map<String, Object> getCacheStatistics() {
        return cacheManager.getCacheStatistics();
    }

    /**
     * 🆕 检查缓存是否有效（未失败）
     */
    private boolean isValidCache(String poemId) {
        try {
            File cacheFile = new File("data/embeddings/poem_" + poemId + ".json");
            if (!cacheFile.exists()) return false;

            ObjectMapper mapper = new ObjectMapper();
            EmbeddingCacheManager.EmbeddingCache cache = mapper.readValue(cacheFile, EmbeddingCacheManager.EmbeddingCache.class);

            // 🔧 检查向量是否存在（表示处理成功）
            return cache.vector != null && cache.vector.length > 0;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 🔧 修复的初始化方法 - 增强调试信息
     */
    // 🔧 完全重写 initializeRAG 方法，集成缓存

    public synchronized void initializeRAG() throws Exception {
        if (isInitialized) {
            System.out.println("RAG 系统已初始化，跳过重复初始化");
            return;
        }

        System.out.println("🚀 开始智能初始化 Easy RAG 系统...");

        try {
            // 1. 初始化缓存目录
            cacheManager.initializeCacheDirectories();

            // 2. 从数据库加载诗词
            List<Poem> poems = loadPoemsFromDatabase();
            System.out.println("📚 从数据库加载诗词: " + poems.size() + " 首");

            if (poems.isEmpty()) {
                throw new RuntimeException("数据库中没有诗词数据");
            }

            // 3. 🎯 检查缓存是否有效
            if (cacheManager.isCacheValid(poems)) {
                System.out.println("⚡ 发现有效缓存，开始快速加载...");

                // 从缓存加载
                int loadedCount = cacheManager.loadFromCache(embeddingStore);

                if (loadedCount > 0) {
                    isInitialized = true;
                    successfullyProcessed = loadedCount;
                    System.out.println("✅ 缓存加载完成！总向量数: " + loadedCount);
                    return;  // 🎯 直接返回，不重新处理
                } else {
                    System.out.println("⚠️ 缓存加载失败，转为全量构建");
                }
            } else {
                System.out.println("📝 缓存无效或不存在，开始全量构建");
            }

            // 4. 执行全量构建
            System.out.println("🔄 开始全量构建向量库...");
            successfullyProcessed = 0;
            int failed = 0;

            int batchSize = 5;
            for (int i = 0; i < poems.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, poems.size());
                List<Poem> batch = poems.subList(i, endIndex);

                for (Poem poem : batch) {
                    try {
                        String content = buildPoemContent(poem);

                        // 🔧 长度检查和截断
                        if (content.length() > 2048) {
                            content = content.substring(0, 2045) + "...";
                        }

                        // 创建元数据
                        Map<String, String> metadataMap = new HashMap<>();
                        metadataMap.put("poem_id", String.valueOf(poem.getPID()));
                        metadataMap.put("title", poem.getTitle() != null ? poem.getTitle() : "");
                        metadataMap.put("poet", poem.getPoet() != null ? poem.getPoet() : "");
                        metadataMap.put("category", poem.getCategory() != null ? poem.getCategory() : "");

                        Metadata metadata = Metadata.from(metadataMap);
                        TextSegment segment = TextSegment.from(content, metadata);

                        // 生成嵌入并存储
                        Response<Embedding> response = embeddingModel.embed(segment);
                        Embedding embedding = response.content();
                        embeddingStore.add(embedding, segment);

                        // 🆕 保存成功的缓存
                        cacheManager.savePoemCache(poem, content, embedding.vector(), metadataMap);
                        successfullyProcessed++;

                        Thread.sleep(100);

                    } catch (Exception e) {
                        // 🆕 保存失败的缓存（空向量）
                        cacheManager.savePoemCache(poem, buildPoemContent(poem), null, new HashMap<>());
                        failed++;
                        System.err.println("处理失败，ID: " + poem.getPID() + ", 错误: " + e.getMessage());
                    }
                }

                System.out.println("⚡ 已处理: " + (successfullyProcessed + failed) + "/" + poems.size() +
                        " (成功: " + successfullyProcessed + ", 失败: " + failed + ")");

                if (endIndex < poems.size()) {
                    Thread.sleep(1000);
                }
            }

            // 5. 🆕 保存缓存信息
            cacheManager.saveCacheInfo(poems, successfullyProcessed);

            isInitialized = true;
            System.out.println("✅ 初始化完成！成功: " + successfullyProcessed + " 首，失败: " + failed + " 首");

        } catch (Exception e) {
            System.err.println("❌ RAG 系统初始化失败: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 🆕 检查数据库更新
     */
    public String checkDatabaseUpdates() throws Exception {
        System.out.println("🔍 开始检查数据库更新...");

        List<Poem> currentPoems = loadPoemsFromDatabase();
        EmbeddingCacheManager.UpdateResult updateResult = cacheManager.checkAndUpdateCache(currentPoems, embeddingStore);

        StringBuilder report = new StringBuilder();
        switch (updateResult.type) {
            case NO_CHANGE:
                report.append("✅ 数据库无变化，缓存最新");
                break;
            case INCREMENTAL:
                report.append("⚡ 检测到增量变化");
                break;
            case FULL_REBUILD:
                report.append("⚠️ 检测到大量变化，建议全量重建");
                break;
        }
        return report.toString();
    }

    /**
     * 🔧 执行手动增量更新
     */
    public String performManualUpdate() throws Exception {
        try {
            System.out.println("🔄 开始执行增量更新检查...");

            List<Poem> currentPoems = loadPoemsFromDatabase();
            EmbeddingCacheManager.UpdateResult updateResult = cacheManager.checkAndUpdateCache(currentPoems, embeddingStore);

            switch (updateResult.type) {
                case NO_CHANGE:
                    System.out.println("✅ 数据库无变化，无需更新");
                    return "✅ 数据库无变化，无需更新";

                case INCREMENTAL:
                    System.out.println("⚡ 检测到增量变化，开始处理...");

                    // 🔧 使用 EmbeddingCacheManager 的增量更新功能，传入当前诗词列表
                    cacheManager.performIncrementalUpdate(
                            updateResult,
                            embeddingStore,
                            embeddingModel,
                            this::buildPoemContent,  // 传递内容构建函数
                            currentPoems  // 🆕 传入当前诗词列表
                    );

                    // 🔧 更新本地状态
                    if (!updateResult.newPoems.isEmpty() || !updateResult.modifiedPoems.isEmpty()) {
                        successfullyProcessed += updateResult.getApiCallsNeeded();
                    }

                    return String.format("✅ 增量更新完成！\n" +
                                    "📊 处理统计：\n" +
                                    "  • 新增: %d 首\n" +
                                    "  • 修改: %d 首\n" +
                                    "  • 删除: %d 首\n" +
                                    "  • API 调用: %d 次",
                            updateResult.newPoems.size(),
                            updateResult.modifiedPoems.size(),
                            updateResult.deletedPoemIds.size(),
                            updateResult.getApiCallsNeeded());

                case FULL_REBUILD:
                    System.out.println("⚠️ 检测到大量变化，需要全量重建");
                    return "⚠️ 检测到大量变化（超过30%），建议使用全量重建功能";

                default:
                    return "❌ 未知的更新类型: " + updateResult.type;
            }

        } catch (Exception e) {
            String errorMsg = "增量更新失败: " + e.getMessage();
            System.err.println("❌ " + errorMsg);
            e.printStackTrace();
            throw new RuntimeException(errorMsg, e);
        }
    }

    /**
     * 🆕 清理缓存
     */
    public void clearCache() throws Exception {
        cacheManager.clearCache();
        isInitialized = false;
        successfullyProcessed = 0;
        System.out.println("🗑️ 缓存已清理，系统需要重新初始化");
    }



    /**
     * 测试数据库连接 - 公开方法
     */
    public List<Poem> testDatabaseConnection() throws Exception {
        System.out.println("🔍 开始测试数据库连接...");

        List<Poem> poems = loadPoemsFromDatabase();

        if (poems != null && !poems.isEmpty()) {
            System.out.println("✅ 数据库连接成功，共获取 " + poems.size() + " 条记录");

            // 打印前几条记录的详细信息进行调试
            for (int i = 0; i < Math.min(3, poems.size()); i++) {
                Poem poem = poems.get(i);
                System.out.println("\n📖 诗词 " + (i+1) + " 详细信息:");
                System.out.println("  PID: " + poem.getPID());
                System.out.println("  Title: '" + poem.getTitle() + "'");
                System.out.println("  Poet: '" + poem.getPoet() + "'");
                System.out.println("  Text length: " + (poem.getText() != null ? poem.getText().length() : 0));
                System.out.println("  Text preview: " + (poem.getText() != null ?
                        poem.getText().substring(0, Math.min(50, poem.getText().length())) + "..." : "null"));
                System.out.println("  Category: '" + poem.getCategory() + "'");
                System.out.println("  Background: " + (poem.getBackground() != null ? "有内容(" + poem.getBackground().length() + "字)" : "无"));
                System.out.println("  Appreciation: " + (poem.getAppreciation() != null ? "有内容(" + poem.getAppreciation().length() + "字)" : "无"));
                System.out.println("  Translation: " + (poem.getTranslation() != null ? "有内容(" + poem.getTranslation().length() + "字)" : "无"));
                System.out.println("  ---");
            }
        } else {
            System.err.println("❌ 数据库查询返回空结果");
        }

        return poems != null ? poems : new ArrayList<>();
    }

    /**
     * Easy RAG 对话 - 核心方法
     */
    public String chat(String userMessage) throws Exception {
        if (!isInitialized) {
            initializeRAG();
        }

        System.out.println("💬 处理用户提问: " + userMessage);

        try {
            // 1. 生成查询嵌入
            Response<Embedding> embeddingResponse = embeddingModel.embed(userMessage);
            Embedding queryEmbedding = embeddingResponse.content();

            // 2. 搜索相关内容 - 使用新的 API
            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(5) // 🔧 增加搜索数量，因为可能有失败的
                    .minScore(0.6)
                    .build();

            EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);
            List<EmbeddingMatch<TextSegment>> matches = searchResult.matches();

// 3. 🆕 过滤掉失败的缓存文件对应的结果
            List<EmbeddingMatch<TextSegment>> validMatches = new ArrayList<>();
            for (EmbeddingMatch<TextSegment> match : matches) {
                String poemId = match.embedded().metadata().getString("poem_id");
                if (poemId != null && isValidCache(poemId)) {
                    validMatches.add(match);
                    if (validMatches.size() >= 3) break; // 只要3个有效结果
                }
            }

// 4. 构建上下文
            StringBuilder context = new StringBuilder();
            if (!validMatches.isEmpty()) {
                context.append("相关诗词资料：\n\n");

                for (int i = 0; i < validMatches.size(); i++) {
                    TextSegment segment = validMatches.get(i).embedded();
                    context.append("【资料").append(i + 1).append("】\n");
                    context.append(segment.text()).append("\n\n");
                }
            } else {
                context.append("未找到直接相关的诗词资料。");
            }

            // 5. 构建完整提示
            String prompt = buildPrompt(userMessage, context.toString());

            // 6. 调用千问模型生成回答
            String response = chatLanguageModel.generate(prompt);

            System.out.println("✅ Easy RAG 响应生成完成");
            return response;

        } catch (Exception e) {
            System.err.println("❌ Easy RAG 对话失败: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 诗词推荐
     */
    public String recommendPoetry(String theme, String preference) throws Exception {
        String query = "推荐关于" + theme + "的诗词，偏好：" + preference;
        return chat(query);
    }

    /**
     * 诗词分析
     */
    public String analyzePoetry(String poemTitle, String analysisType) throws Exception {
        String query = "请分析《" + poemTitle + "》这首诗的" + analysisType;
        return chat(query);
    }

    /**
     * 测试检索功能 - 使用新的 API
     */
    public List<String> testRetrieve(String query, int maxResults) throws Exception {
        if (!isInitialized) {
            initializeRAG();
        }

        try {
            Response<Embedding> embeddingResponse = embeddingModel.embed(query);
            Embedding queryEmbedding = embeddingResponse.content();

            // 使用新的搜索 API
            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(maxResults)
                    .minScore(0.5)
                    .build();

            EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);
            List<EmbeddingMatch<TextSegment>> matches = searchResult.matches();

            List<String> results = new ArrayList<>();
            for (EmbeddingMatch<TextSegment> match : matches) {
                String content = match.embedded().text();
                String preview = content.length() > 100 ? content.substring(0, 100) + "..." : content;
                results.add(String.format("相似度: %.3f\n内容: %s", match.score(), preview));
            }

            return results;

        } catch (Exception e) {
            System.err.println("检索失败: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 构建诗词内容
     */
    public String buildPoemContent(Poem poem) {
        StringBuilder content = new StringBuilder();

        content.append("标题：").append(poem.getTitle() != null ? poem.getTitle() : "无标题").append("\n");
        content.append("作者：").append(poem.getPoet() != null ? poem.getPoet() : "佚名").append("\n");
        content.append("正文：").append(poem.getText() != null ? poem.getText() : "").append("\n");

        if (poem.getCategory() != null && !poem.getCategory().trim().isEmpty()) {
            content.append("分类：").append(poem.getCategory()).append("\n");
        }

        if (poem.getBackground() != null && !poem.getBackground().trim().isEmpty()) {
            content.append("创作背景：").append(poem.getBackground()).append("\n");
        }

        if (poem.getAppreciation() != null && !poem.getAppreciation().trim().isEmpty()) {
            content.append("文学赏析：").append(poem.getAppreciation()).append("\n");
        }

        if (poem.getTranslation() != null && !poem.getTranslation().trim().isEmpty()) {
            content.append("现代译文：").append(poem.getTranslation()).append("\n");
        }

        return content.toString();
    }

    /**
     * 构建提示
     */
    private String buildPrompt(String userMessage, String context) {
        return """
            你是一位专业的古典诗词专家。请基于以下资料回答用户问题：
            
            %s
            
            用户问题：%s
            
            请根据提供的资料给出专业、详细的回答。如果资料不足，请基于你的专业知识给出回答，并说明是基于一般知识。
            回答要求：
            - 语言优美流畅
            - 内容专业准确
            - 结构清晰有条理
            - 诗词名称用《》标注，作者名用""标注
            """.formatted(context, userMessage);
    }

    /**
     * 从数据库加载所有诗词 - 确保完全读取
     */
    private List<Poem> loadPoemsFromDatabase() throws Exception {
        InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
        SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(in);

        try (SqlSession session = factory.openSession()) {
            PoemGetMapper mapper = session.getMapper(PoemGetMapper.class);

            // 获取总数
            int totalCount = mapper.countAllPoems(); // 你需要在 Mapper 中添加这个方法
            System.out.println("📊 数据库中诗词总数: " + totalCount);

            // 获取所有诗词
            List<Poem> poems = mapper.getAllPoems();
            System.out.println("📚 实际读取诗词数: " + (poems != null ? poems.size() : 0));

            if (poems == null) {
                return new ArrayList<>();
            }

            // 验证是否完全读取
            if (poems.size() != totalCount) {
                System.err.println("⚠️ 警告：读取数量与总数不匹配！总数:" + totalCount + ", 读取:" + poems.size());
            }

            return poems;
        }
    }

    /**
     * 测试嵌入 API - 修复版本
     */
    public String testEmbeddingAPI() throws Exception {
        try {
            Response<Embedding> response = embeddingModel.embed("测试嵌入");
            Embedding embedding = response.content();

            // 修复：embedding.vector() 返回的是 float[] 数组，不是 List
            float[] vector = embedding.vector();
            StringBuilder vectorPreview = new StringBuilder();

            // 显示前5个向量值
            int showCount = Math.min(5, vector.length);
            vectorPreview.append("[");
            for (int i = 0; i < showCount; i++) {
                vectorPreview.append(String.format("%.4f", vector[i]));
                if (i < showCount - 1) {
                    vectorPreview.append(", ");
                }
            }
            if (vector.length > 5) {
                vectorPreview.append("...");
            }
            vectorPreview.append("]");

            return "嵌入向量维度: " + embedding.dimension() +
                    ", 向量长度: " + vector.length +
                    ", 前" + showCount + "个值: " + vectorPreview.toString();

        } catch (Exception e) {
            throw new RuntimeException("嵌入 API 测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试聊天 API
     */
    public String testChatAPI() throws Exception {
        try {
            return chatLanguageModel.generate("请回复：API测试成功");
        } catch (Exception e) {
            throw new RuntimeException("聊天 API 测试失败: " + e.getMessage());
        }
    }

    /**
     * 🆕 保存诗词缓存（包含失败状态）
     */
    public void chatStream(String userMessage, SseEmitter emitter) throws Exception {
        if (!isInitialized) {
            initializeRAG();
        }

        // 构建上下文和 prompt，和 chat 方法一致
        Response<Embedding> embeddingResponse = embeddingModel.embed(userMessage);
        Embedding queryEmbedding = embeddingResponse.content();

        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(5)
                .minScore(0.6)
                .build();

        EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);
        List<EmbeddingMatch<TextSegment>> matches = searchResult.matches();

        List<EmbeddingMatch<TextSegment>> validMatches = new ArrayList<>();
        for (EmbeddingMatch<TextSegment> match : matches) {
            String poemId = match.embedded().metadata().getString("poem_id");
            if (poemId != null && isValidCache(poemId)) {
                validMatches.add(match);
                if (validMatches.size() >= 3) break;
            }
        }

        StringBuilder context = new StringBuilder();
        if (!validMatches.isEmpty()) {
            context.append("相关诗词资料：\n\n");
            for (int i = 0; i < validMatches.size(); i++) {
                TextSegment segment = validMatches.get(i).embedded();
                context.append("【资料").append(i + 1).append("】\n");
                context.append(segment.text()).append("\n\n");
            }
        } else {
            context.append("未找到直接相关的诗词资料。");
        }

        String prompt = buildPrompt(userMessage, context.toString());

        streamingChatLanguageModel.generate(prompt, new StreamingResponseHandler() {
            @Override
            public void onNext(String token) {
                try {
                    emitter.send(SseEmitter.event().data(token));
                } catch (Exception e) {
                    emitter.completeWithError(e);
                }
            }
            
            public void onComplete() {
                try {
                    emitter.send(SseEmitter.event().data("[END]"));
                } catch (Exception ignored) {}
                emitter.complete();
            }

            @Override
            public void onError(Throwable error) {
                try {
                    emitter.send(SseEmitter.event().data("流式输出错误：" + error.getMessage()));
                } catch (Exception ignored) {}
                emitter.completeWithError(error);
            }
        });
    }

    /**
     * 🆕 保存诗词缓存（包含失败状态）并支持历史记录
     */
    public void chatStreamWithHistory(String userMessage, List<Map<String, String>> history, SseEmitter emitter) throws Exception {
        if (!isInitialized) {
            initializeRAG();
        }

        System.out.println("💬 处理用户提问: " + userMessage);

        // 构建上下文（同原 chatStream）
        Response<Embedding> embeddingResponse = embeddingModel.embed(userMessage);
        Embedding queryEmbedding = embeddingResponse.content();

        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(5)
                .minScore(0.6)
                .build();

        EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);
        List<EmbeddingMatch<TextSegment>> matches = searchResult.matches();

        List<EmbeddingMatch<TextSegment>> validMatches = new ArrayList<>();
        for (EmbeddingMatch<TextSegment> match : matches) {
            String poemId = match.embedded().metadata().getString("poem_id");
            if (poemId != null && isValidCache(poemId)) {
                validMatches.add(match);
                if (validMatches.size() >= 3) break;
            }
        }

        StringBuilder context = new StringBuilder();
        if (!validMatches.isEmpty()) {
            context.append("相关诗词资料：\n\n");
            for (int i = 0; i < validMatches.size(); i++) {
                TextSegment segment = validMatches.get(i).embedded();
                context.append("【资料").append(i + 1).append("】\n");
                context.append(segment.text()).append("\n\n");
            }
        } else {
            context.append("未找到直接相关的诗词资料。");
        }

        // 拼接历史对话
        StringBuilder historyPrompt = new StringBuilder();
        if (history != null) {
            for (Map<String, String> turn : history) {
                String role = turn.get("role");
                String content = turn.get("content");
                if ("user".equals(role)) {
                    historyPrompt.append("用户：").append(content).append("\n");
                } else if ("assistant".equals(role)) {
                    historyPrompt.append("助手：").append(content).append("\n");
                }
            }
        }

        // 构建最终 prompt
        String prompt = """
            你是一位专业的古典诗词专家。请基于以下资料和历史对话回答用户问题：

            %s

            历史对话：
            %s

            当前用户问题：%s

            请根据资料和历史对话，给出专业、详细的回答。
            """.formatted(context, historyPrompt, userMessage);

    final long[] lastTokenTime = {System.currentTimeMillis()};
    final boolean[] completed = {false};

    // 定时任务线程，超时自动结束
    Thread timeoutThread = new Thread(() -> {
        try {
            while (!completed[0]) {
                Thread.sleep(2000); // 检查间隔
                if (System.currentTimeMillis() - lastTokenTime[0] > 2500 && !completed[0]) {
                    emitter.send(SseEmitter.event().data("[END]"));
                    emitter.complete();
                    completed[0] = true;
                    System.out.println("超时自动结束");
                }
            }
        } catch (Exception ignored) {}
    });
    timeoutThread.start();

    streamingChatLanguageModel.generate(prompt, new StreamingResponseHandler() {
        @Override
        public void onNext(String token) {
            lastTokenTime[0] = System.currentTimeMillis();
            try {
                emitter.send(SseEmitter.event().data(token));
            } catch (Exception e) {
                emitter.completeWithError(e);
                completed[0] = true;
            }
        }

        public void onComplete() {
            try {
                emitter.send(SseEmitter.event().data("[END]"));
            } catch (Exception ignored) {}
            emitter.complete();
            completed[0] = true;
            System.out.println("onComplete 被调用");
        }
        @Override
        public void onError(Throwable error) {
            try {
                emitter.send(SseEmitter.event().data("流式输出错误：" + error.getMessage()));
            } catch (Exception ignored) {}
            emitter.completeWithError(error);
            completed[0] = true;
            System.out.println("onError 被调用");
        }
    });
}

    /**
     * 🆕 保存诗词缓存（包含失败状态）并支持角色扮演
     */
    public void chatStreamWithRole(String userMessage, String role, List<Map<String, String>> history, SseEmitter emitter) throws Exception {
        if (!RoleProfileUtil.getSupportedRoles().contains(role)) {
            emitter.send(SseEmitter.event().data("不支持的角色：" + role));
            emitter.complete();
            return;
        }
        if (!isInitialized) {
            initializeRAG();
        }

        // 检索相关诗词资料（同 chatStreamWithHistory）
        Response<Embedding> embeddingResponse = embeddingModel.embed(userMessage);
        Embedding queryEmbedding = embeddingResponse.content();

        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(5)
                .minScore(0.6)
                .build();

        EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);
        List<EmbeddingMatch<TextSegment>> matches = searchResult.matches();

        List<EmbeddingMatch<TextSegment>> validMatches = new ArrayList<>();
        for (EmbeddingMatch<TextSegment> match : matches) {
            String poemId = match.embedded().metadata().getString("poem_id");
            if (poemId != null && isValidCache(poemId)) {
                validMatches.add(match);
                if (validMatches.size() >= 3) break;
            }
        }

        StringBuilder context = new StringBuilder();
        if (!validMatches.isEmpty()) {
            context.append("相关诗词资料：\n\n");
            for (int i = 0; i < validMatches.size(); i++) {
                TextSegment segment = validMatches.get(i).embedded();
                context.append("【资料").append(i + 1).append("】\n");
                context.append(segment.text()).append("\n\n");
            }
        } else {
            context.append("未找到直接相关的诗词资料。");
        }

        // 拼接历史对话
        StringBuilder historyPrompt = new StringBuilder();
        if (history != null) {
            for (Map<String, String> turn : history) {
                String turnRole = turn.get("role");
                String content = turn.get("content");
                if ("user".equals(turnRole)) {
                    historyPrompt.append("用户：").append(content).append("\n");
                } else if ("assistant".equals(turnRole)) {
                    historyPrompt.append(role).append("：").append(content).append("\n");
                }
            }
        }

        // 读取角色信息
        String roleProfile = RoleProfileUtil.getProfile(role);

        // 构建角色扮演 prompt
        String prompt = """
            你现在是一位古代著名文人【%s】，以下是你的详细资料：
            %s

            请以他的身份与用户对话，风格、语气、知识储备都要贴合该人物。
            你可以结合下列诗词资料和历史对话，专业、风趣、真实地回答用户问题。

            %s

            历史对话：
            %s

            当前用户问题：%s

            回答要求：
            - 以“%s”的身份作答，风格贴合其历史形象
            - 语言优美、符合古人气质，可适当引用诗句
            - 如资料不足，可结合常识和想象补充
            """.formatted(role, roleProfile, context, historyPrompt, userMessage, role);

    final long[] lastTokenTime = {System.currentTimeMillis()};
    final boolean[] completed = {false};

    Thread timeoutThread = new Thread(() -> {
        try {
            while (!completed[0]) {
                Thread.sleep(2000);
                if (System.currentTimeMillis() - lastTokenTime[0] > 2500 && !completed[0]) {
                    emitter.send(SseEmitter.event().data("[END]"));
                    emitter.complete();
                    completed[0] = true;
                }
            }
        } catch (Exception ignored) {}
    });
    timeoutThread.start();

    streamingChatLanguageModel.generate(prompt, new StreamingResponseHandler() {
        @Override
        public void onNext(String token) {
            lastTokenTime[0] = System.currentTimeMillis();
            try {
                emitter.send(SseEmitter.event().data(token));
            } catch (Exception e) {
                emitter.completeWithError(e);
                completed[0] = true;
            }
        }

        public void onComplete() {
            try {
                emitter.send(SseEmitter.event().data("[END]"));
            } catch (Exception ignored) {}
            emitter.complete();
            completed[0] = true;
        }
        @Override
        public void onError(Throwable error) {
            try {
                emitter.send(SseEmitter.event().data("流式输出错误：" + error.getMessage()));
            } catch (Exception ignored) {}
            emitter.completeWithError(error);
            completed[0] = true;
        }
    });
}

    /**
     * 前世今生·灵魂碎片配对器（AI主动提问+分析，流式）
     */
    public void soulMatcherStream(List<Map<String, String>> history, SseEmitter emitter) throws Exception {
        // 构建灵魂配对专用 prompt
        StringBuilder historyPrompt = new StringBuilder();
        if (history != null && !history.isEmpty()) {
            for (Map<String, String> turn : history) {
                String role = turn.get("role");
                String content = turn.get("content");
                if ("ai".equals(role)) {
                    historyPrompt.append("AI提问：").append(content).append("\n");
                } else if ("user".equals(role)) {
                    historyPrompt.append("用户回答：").append(content).append("\n");
                }
            }
        }

        String prompt = """
            你是“前世今生·灵魂碎片配对器”，请以心理测试专家和古诗词鉴赏家的身份，和用户进行一场“前世今生”灵魂配对互动。
            规则如下：
            1. 你会主动提出性格、情绪、偏好等问题（每次只问一个），引导用户作答。
            2. 用户说“开始”，就开始测试。
            3. 用户回答一个测试题后，可以给出一定的情绪价值的回复，再进行下一题。
            4. 当你觉得信息足够时（5道题），输出最终配对结果：告诉用户“你的前世是哪个古人/哪句诗”，并给出一段AI评语和推荐诗词。
            5. 互动风格温暖有趣，适合社交分享。
            6. 把自己当成心理测试专家和古诗词鉴赏家，提问要有趣、引人思考，回答要专业、富有情感。
            7. 对于诗句和选项的输出，都是每行一句诗，每行一个选项。
            8. 历史对话如下（AI提问和用户回答）：
            %s
            如果还没问完，请继续提问；如果可以分析，请直接输出配对结果和解析。
            """.formatted(historyPrompt);

        final long[] lastTokenTime = {System.currentTimeMillis()};
        final boolean[] completed = {false};

        Thread timeoutThread = new Thread(() -> {
            try {
                while (!completed[0]) {
                    Thread.sleep(2000);
                    if (System.currentTimeMillis() - lastTokenTime[0] > 2000 && !completed[0]) {
                        emitter.send(SseEmitter.event().data("[END]"));
                        emitter.complete();
                        completed[0] = true;
                    }
                }
            } catch (Exception ignored) {}
        });
        timeoutThread.start();

        streamingChatLanguageModel.generate(prompt, new StreamingResponseHandler() {
            @Override
            public void onNext(String token) {
                lastTokenTime[0] = System.currentTimeMillis();
                try {
                    emitter.send(SseEmitter.event().data(token));
                } catch (Exception e) {
                    emitter.completeWithError(e);
                    completed[0] = true;
                }
            }

            public void onComplete() {
                try {
                    emitter.send(SseEmitter.event().data("[END]"));
                } catch (Exception ignored) {}
                emitter.complete();
                completed[0] = true;
            }
            @Override
            public void onError(Throwable error) {
                try {
                    emitter.send(SseEmitter.event().data("流式输出错误：" + error.getMessage()));
                } catch (Exception ignored) {}
                emitter.completeWithError(error);
                completed[0] = true;
            }
        });
    }
}