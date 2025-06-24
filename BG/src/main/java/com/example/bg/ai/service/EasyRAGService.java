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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EasyRAGService extends ConnetMySQL {

    @Autowired
    private ChatLanguageModel chatLanguageModel;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    @Autowired
    private EmbeddingCacheManager cacheManager; // 🆕 注入缓存管理器

    private boolean isInitialized = false;
    private int successfullyProcessed = 0;

    /**
     * 智能初始化 RAG 系统 - 支持缓存
     */
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

            // 3. 检查更新策略
            EmbeddingCacheManager.UpdateResult updateResult = cacheManager.checkAndUpdateCache(poems, embeddingStore);

            switch (updateResult.type) {
                case NO_CHANGE:
                    // 无变化，加载现有缓存
                    successfullyProcessed = cacheManager.loadFromCache(embeddingStore);
                    System.out.println("✅ 缓存加载完成！API 消耗: 0 次");
                    break;

                case INCREMENTAL:
                    // 增量更新
                    successfullyProcessed = cacheManager.loadFromCache(embeddingStore);
                    System.out.println("🔄 执行增量更新...");

                    cacheManager.performIncrementalUpdate(
                            updateResult,
                            embeddingStore,
                            embeddingModel,
                            this::buildPoemContent
                    );

                    successfullyProcessed += updateResult.newPoems.size() + updateResult.modifiedPoems.size();
                    System.out.println("✅ 增量更新完成！API 消耗: " +
                            (updateResult.newPoems.size() + updateResult.modifiedPoems.size()) + " 次");
                    break;

                case FULL_REBUILD:
                    // 全量重建
                    System.out.println("🔄 执行全量重建...");
                    System.out.println("⚠️ 注意：这将消耗约 " + poems.size() + " 次 API 调用");

                    cacheManager.clearCache();
                    cacheManager.initializeCacheDirectories();
                    processAndCachePoems(poems);

                    System.out.println("✅ 全量重建完成！API 消耗: " + successfullyProcessed + " 次");
                    break;
            }

            isInitialized = true;
            System.out.println("🎉 Easy RAG 系统初始化完成！总向量数: " + successfullyProcessed);

        } catch (Exception e) {
            System.err.println("❌ RAG 系统初始化失败: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    /**
     * 强制检查数据库更新
     */
    public synchronized String checkDatabaseUpdates() throws Exception {
        System.out.println("🔍 开始检查数据库更新...");

        try {
            List<Poem> currentPoems = loadPoemsFromDatabase();
            EmbeddingCacheManager.UpdateResult updateResult = cacheManager.checkAndUpdateCache(currentPoems, embeddingStore);

            StringBuilder report = new StringBuilder();
            report.append("📊 数据库更新检查报告：\n\n");

            switch (updateResult.type) {
                case NO_CHANGE:
                    report.append("✅ 数据库无变化，缓存保持最新状态");
                    break;

                case INCREMENTAL:
                    report.append("🔄 检测到增量变化：\n");
                    report.append("  • 新增诗词：").append(updateResult.newPoems.size()).append(" 首\n");
                    report.append("  • 修改诗词：").append(updateResult.modifiedPoems.size()).append(" 首\n");
                    report.append("  • 删除诗词：").append(updateResult.deletedPoemIds.size()).append(" 首\n");
                    report.append("\n建议执行增量更新，预计消耗 API：").append(updateResult.newPoems.size() + updateResult.modifiedPoems.size()).append(" 次");
                    break;

                case FULL_REBUILD:
                    report.append("⚠️ 检测到大量变化，建议全量重建\n");
                    report.append("预计消耗 API：").append(currentPoems.size()).append(" 次");
                    break;
            }

            return report.toString();

        } catch (Exception e) {
            throw new RuntimeException("检查数据库更新失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发增量更新
     */
    public synchronized String performManualUpdate() throws Exception {
        System.out.println("🔄 开始手动执行数据库更新...");

        try {
            List<Poem> currentPoems = loadPoemsFromDatabase();
            EmbeddingCacheManager.UpdateResult updateResult = cacheManager.checkAndUpdateCache(currentPoems, embeddingStore);

            if (updateResult.type == EmbeddingCacheManager.UpdateType.NO_CHANGE) {
                return "✅ 数据库无变化，无需更新";
            }

            if (updateResult.type == EmbeddingCacheManager.UpdateType.INCREMENTAL) {
                cacheManager.performIncrementalUpdate(
                        updateResult,
                        embeddingStore,
                        embeddingModel,
                        this::buildPoemContent
                );

                return String.format("✅ 增量更新完成！新增 %d 首，修改 %d 首，删除 %d 首，API 消耗 %d 次",
                        updateResult.newPoems.size(),
                        updateResult.modifiedPoems.size(),
                        updateResult.deletedPoemIds.size(),
                        updateResult.newPoems.size() + updateResult.modifiedPoems.size());
            }

            if (updateResult.type == EmbeddingCacheManager.UpdateType.FULL_REBUILD) {
                // 执行全量重建前，需要用户确认
                return "⚠️ 检测到大量变化，需要全量重建。请使用 /ai/easy/force-rebuild 接口执行全量重建";
            }

            return "❓ 未知的更新类型";

        } catch (Exception e) {
            throw new RuntimeException("手动更新失败: " + e.getMessage());
        }
    }

    /**
     * 处理诗词并保存到缓存
     */
    private void processAndCachePoems(List<Poem> poems) throws Exception {
        int batchSize = 5; // 小批次处理
        successfullyProcessed = 0;
        int failedCount = 0;

        for (int i = 0; i < poems.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, poems.size());
            List<Poem> batch = poems.subList(i, endIndex);

            for (Poem poem : batch) {
                try {
                    String content = buildPoemContent(poem);

                    if (content.length() < 20) {
                        System.out.println("⚠️ 跳过内容过短的诗词，PID: " + poem.getPID());
                        continue;
                    }

                    // 创建元数据
                    Map<String, String> metadataMap = new HashMap<>();
                    metadataMap.put("poem_id", String.valueOf(poem.getPID()));
                    metadataMap.put("title", poem.getTitle() != null ? poem.getTitle() : "");
                    metadataMap.put("poet", poem.getPoet() != null ? poem.getPoet() : "");
                    metadataMap.put("category", poem.getCategory() != null ? poem.getCategory() : "");

                    Metadata metadata = Metadata.from(metadataMap);
                    TextSegment segment = TextSegment.from(content, metadata);

                    // 🔴 API 调用点 - 生成嵌入
                    Response<Embedding> response = embeddingModel.embed(segment);
                    Embedding embedding = response.content();
                    embeddingStore.add(embedding, segment);

                    // 💾 保存到缓存
                    cacheManager.savePoemCache(poem, content, embedding.vector(), metadataMap);

                    successfullyProcessed++;

                    // API 限制延迟
                    Thread.sleep(100);

                } catch (Exception e) {
                    failedCount++;
                    System.err.println("❌ 处理诗词失败，ID: " + poem.getPID() + ", 错误: " + e.getMessage());

                    // 如果失败太多，暂停一下
                    if (failedCount % 10 == 0) {
                        System.out.println("⚠️ 失败次数较多，暂停5秒...");
                        Thread.sleep(5000);
                    }
                }
            }

            // 显示进度
            double progress = (double) (i + batch.size()) / poems.size() * 100;
            System.out.printf("⚡ 处理进度: %d/%d (%.1f%%) - 成功: %d, 失败: %d, API已消耗: %d\n",
                    i + batch.size(), poems.size(), progress,
                    successfullyProcessed, failedCount, successfullyProcessed);

            // 批次间延迟
            if (endIndex < poems.size()) {
                Thread.sleep(1000);
            }
        }
    }

    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getCacheStatistics() {
        return cacheManager.getCacheStatistics();
    }

    /**
     * 清理缓存
     */
    public void clearCache() {
        cacheManager.clearCache();
        isInitialized = false;
        successfullyProcessed = 0;
        System.out.println("🗑️ 缓存已清理，需要重新初始化");
    }

    // ...existing methods...
    // (保留你现有的所有方法，如 chat、testDatabaseConnection 等)

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
                    .maxResults(3)
                    .minScore(0.6)
                    .build();

            EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);
            List<EmbeddingMatch<TextSegment>> matches = searchResult.matches();

            // 3. 构建上下文
            StringBuilder context = new StringBuilder();
            if (!matches.isEmpty()) {
                context.append("相关诗词资料：\n\n");

                for (int i = 0; i < matches.size(); i++) {
                    TextSegment segment = matches.get(i).embedded();
                    context.append("【资料").append(i + 1).append("】\n");
                    context.append(segment.text()).append("\n\n");
                }
            } else {
                context.append("未找到直接相关的诗词资料。");
            }

            // 4. 构建完整提示
            String prompt = buildPrompt(userMessage, context.toString());

            // 5. 调用千问模型生成回答
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
    private String buildPoemContent(Poem poem) {
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
            int totalCount = mapper.countAllPoems();
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
}