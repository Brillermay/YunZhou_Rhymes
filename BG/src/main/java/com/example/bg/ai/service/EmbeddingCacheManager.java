package com.example.bg.ai.service;

import com.example.bg.poem.Poem;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.*;

@Component
public class EmbeddingCacheManager {

    // 缓存目录配置 - 相对于项目根目录
    private static final String CACHE_BASE_DIR = "data";
    private static final String EMBEDDINGS_DIR = CACHE_BASE_DIR + "/embeddings";
    private static final String CACHE_INFO_FILE = CACHE_BASE_DIR + "/cache_info.json";

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 缓存信息类
     */
    public static class CacheInfo {
        public int totalPoems;
        public long lastUpdateTime;
        public String dataHash;
        public int processedCount;
        public Set<Integer> cachedPoemIds; // 记录已缓存的诗词ID
        public long lastDatabaseCheckTime; // 最后数据库检查时间

        public CacheInfo() {
            this.cachedPoemIds = new HashSet<>();
        }

        public CacheInfo(int totalPoems, long lastUpdateTime, String dataHash, int processedCount) {
            this.totalPoems = totalPoems;
            this.lastUpdateTime = lastUpdateTime;
            this.dataHash = dataHash;
            this.processedCount = processedCount;
            this.cachedPoemIds = new HashSet<>();
            this.lastDatabaseCheckTime = System.currentTimeMillis();
        }
    }

    /**
     * 向量缓存数据类
     */
    public static class EmbeddingCache {
        public int poemId;
        public String title;
        public String poet;
        public float[] vector;
        public String content;
        public Map<String, String> metadata;
        public long cacheTime;

        public EmbeddingCache() {}

        public EmbeddingCache(int poemId, String title, String poet, float[] vector,
                              String content, Map<String, String> metadata) {
            this.poemId = poemId;
            this.title = title;
            this.poet = poet;
            this.vector = vector;
            this.content = content;
            this.metadata = metadata;
            this.cacheTime = System.currentTimeMillis();
        }
    }

    /**
     * 更新类型枚举
     */
    public enum UpdateType {
        NO_CHANGE,      // 无变化
        INCREMENTAL,    // 增量更新
        FULL_REBUILD    // 全量重建
    }

    /**
     * 更新结果类 - 修复版本
     */
    public static class UpdateResult {
        public final UpdateType type;
        public final List<Poem> allPoems;          // 所有诗词（用于全量重建）
        public final List<Poem> newPoems;          // 新增的诗词
        public final List<Poem> modifiedPoems;     // 修改的诗词
        public final List<Integer> deletedPoemIds; // 删除的诗词ID

        // 🆕 私有构造函数
        private UpdateResult(UpdateType type, List<Poem> allPoems, List<Poem> newPoems,
                             List<Poem> modifiedPoems, List<Integer> deletedPoemIds) {
            this.type = type;
            this.allPoems = allPoems != null ? allPoems : new ArrayList<>();
            this.newPoems = newPoems != null ? newPoems : new ArrayList<>();
            this.modifiedPoems = modifiedPoems != null ? modifiedPoems : new ArrayList<>();
            this.deletedPoemIds = deletedPoemIds != null ? deletedPoemIds : new ArrayList<>();
        }

        // 🆕 静态工厂方法：创建全量重建结果
        public static UpdateResult forFullRebuild(List<Poem> allPoems) {
            return new UpdateResult(
                    UpdateType.FULL_REBUILD,
                    allPoems,
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>()
            );
        }

        // 🆕 静态工厂方法：创建无变化结果
        public static UpdateResult forNoChange() {
            return new UpdateResult(
                    UpdateType.NO_CHANGE,
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>()
            );
        }

        // 🆕 静态工厂方法：创建增量更新结果
        public static UpdateResult forIncremental(List<Poem> newPoems,
                                                  List<Poem> modifiedPoems,
                                                  List<Integer> deletedPoemIds) {
            return new UpdateResult(
                    UpdateType.INCREMENTAL,
                    new ArrayList<>(),
                    newPoems,
                    modifiedPoems,
                    deletedPoemIds
            );
        }

        // 🆕 辅助方法：获取总变化数量
        public int getTotalChanges() {
            return newPoems.size() + modifiedPoems.size() + deletedPoemIds.size();
        }

        // 🆕 辅助方法：获取需要API调用的数量
        public int getApiCallsNeeded() {
            return newPoems.size() + modifiedPoems.size(); // 删除操作不需要API调用
        }

        @Override
        public String toString() {
            return String.format("UpdateResult{type=%s, new=%d, modified=%d, deleted=%d}",
                    type, newPoems.size(), modifiedPoems.size(), deletedPoemIds.size());
        }
    }

    /**
     * 初始化缓存目录
     */
    public void initializeCacheDirectories() throws IOException {
        Files.createDirectories(Paths.get(CACHE_BASE_DIR));
        Files.createDirectories(Paths.get(EMBEDDINGS_DIR));
        System.out.println("📁 缓存目录初始化完成: " + new File(CACHE_BASE_DIR).getAbsolutePath());
    }

    /**
     * 检查缓存是否有效
     */
    public boolean isCacheValid(List<Poem> currentPoems) {
        try {
            CacheInfo cacheInfo = loadCacheInfo();
            if (cacheInfo == null) {
                System.out.println("📝 无缓存信息文件");
                return false;
            }

            String currentDataHash = calculateDataHash(currentPoems);

            // 检查诗词数量
            if (cacheInfo.totalPoems != currentPoems.size()) {
                System.out.println("📊 诗词数量变化：缓存 " + cacheInfo.totalPoems +
                        " 首，当前 " + currentPoems.size() + " 首");
                return false;
            }

            // 检查数据哈希
            if (!currentDataHash.equals(cacheInfo.dataHash)) {
                System.out.println("🔄 数据内容发生变化");
                return false;
            }

            // 检查缓存文件完整性
            File embeddingsDir = new File(EMBEDDINGS_DIR);
            if (!embeddingsDir.exists()) {
                System.out.println("📁 缓存目录不存在");
                return false;
            }

            File[] cacheFiles = embeddingsDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (cacheFiles == null || cacheFiles.length == 0) {
                System.out.println("📄 缓存文件为空");
                return false;
            }

            if (cacheFiles.length < cacheInfo.processedCount) {
                System.out.println("📉 缓存文件数量不匹配：期望 " + cacheInfo.processedCount +
                        "，实际 " + cacheFiles.length);
                return false;
            }

            System.out.println("✅ 缓存有效，可以使用");
            return true;

        } catch (Exception e) {
            System.err.println("❌ 缓存有效性检查失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 检查数据库更新并执行增量处理 - 修复版本
     */
    public UpdateResult checkAndUpdateCache(List<Poem> currentPoems, EmbeddingStore<TextSegment> embeddingStore) {
        try {
            CacheInfo cacheInfo = loadCacheInfo();

            if (cacheInfo == null) {
                System.out.println("🆕 无缓存信息，需要全量处理");
                return UpdateResult.forFullRebuild(currentPoems);
            }

            // 1. 分析数据库变化
            Set<Integer> currentPoemIds = new HashSet<>();

            for (Poem poem : currentPoems) {
                currentPoemIds.add(poem.getPID());
            }

            // 2. 找出新增的诗词
            List<Poem> newPoems = new ArrayList<>();
            for (Poem poem : currentPoems) {
                if (!cacheInfo.cachedPoemIds.contains(poem.getPID())) {
                    newPoems.add(poem);
                }
            }

            // 3. 找出删除的诗词
            List<Integer> deletedPoemIds = new ArrayList<>();
            for (Integer cachedId : cacheInfo.cachedPoemIds) {
                if (!currentPoemIds.contains(cachedId)) {
                    deletedPoemIds.add(cachedId);
                }
            }

            // 4. 检查修改的诗词（简单版本：暂时跳过）
            List<Poem> modifiedPoems = new ArrayList<>();

            // 5. 决定更新策略
            if (newPoems.isEmpty() && deletedPoemIds.isEmpty() && modifiedPoems.isEmpty()) {
                System.out.println("✅ 数据库无变化，使用现有缓存");
                return UpdateResult.forNoChange();
            }

            // 6. 如果变化太大，执行全量重建
            double changeRatio = (double)(newPoems.size() + deletedPoemIds.size() + modifiedPoems.size()) / cacheInfo.totalPoems;
            if (changeRatio > 0.3) { // 变化超过30%
                System.out.println("🔄 数据变化较大(" + String.format("%.1f", changeRatio * 100) + "%)，执行全量重建");
                return UpdateResult.forFullRebuild(currentPoems);
            }

            // 7. 执行增量更新
            System.out.println("⚡ 检测到增量变化：新增 " + newPoems.size() +
                    " 首，删除 " + deletedPoemIds.size() +
                    " 首，修改 " + modifiedPoems.size() + " 首");

            return UpdateResult.forIncremental(newPoems, modifiedPoems, deletedPoemIds);

        } catch (Exception e) {
            System.err.println("❌ 缓存更新检查失败: " + e.getMessage());
            return UpdateResult.forFullRebuild(currentPoems);
        }
    }

    /**
     * 执行增量缓存更新 - 修复版本
     */
    public void performIncrementalUpdate(UpdateResult updateResult,
                                         EmbeddingStore<TextSegment> embeddingStore,
                                         EmbeddingModel embeddingModel,
                                         java.util.function.Function<Poem, String> contentBuilder) throws Exception {

        if (updateResult.type != UpdateType.INCREMENTAL) {
            throw new IllegalArgumentException("只能对增量更新类型执行此操作，当前类型: " + updateResult.type);
        }

        CacheInfo cacheInfo = loadCacheInfo();
        if (cacheInfo == null) {
            throw new RuntimeException("缓存信息不存在，无法执行增量更新");
        }

        int apiCallsUsed = 0;

        System.out.println("🔄 开始执行增量更新...");
        System.out.println("📊 变化统计：" + updateResult.toString());
        System.out.println("💰 预计API消耗：" + updateResult.getApiCallsNeeded() + " 次");

        // 1. 删除已删除的诗词缓存
        for (Integer deletedId : updateResult.deletedPoemIds) {
            deleteCacheFile(deletedId);
            cacheInfo.cachedPoemIds.remove(deletedId);
            System.out.println("🗑️ 删除缓存：诗词ID " + deletedId);
        }

        // 2. 处理新增诗词
        for (Poem newPoem : updateResult.newPoems) {
            try {
                String content = contentBuilder.apply(newPoem);
                if (content.length() < 20) {
                    System.out.println("⚠️ 跳过内容过短的诗词，PID: " + newPoem.getPID());
                    continue;
                }

                // 创建元数据
                Map<String, String> metadataMap = createMetadata(newPoem);
                Metadata metadata = Metadata.from(metadataMap);
                TextSegment segment = TextSegment.from(content, metadata);

                // 🔴 API 调用 - 生成嵌入
                Response<Embedding> response = embeddingModel.embed(segment);
                Embedding embedding = response.content();
                embeddingStore.add(embedding, segment);

                // 保存到缓存
                savePoemCache(newPoem, content, embedding.vector(), metadataMap);
                cacheInfo.cachedPoemIds.add(newPoem.getPID());
                apiCallsUsed++;

                System.out.println("➕ 新增缓存：《" + (newPoem.getTitle() != null ? newPoem.getTitle() : "无标题") + "》");

                // API 限制延迟
                Thread.sleep(100);

            } catch (Exception e) {
                System.err.println("❌ 处理新增诗词失败，ID: " + newPoem.getPID() + ", 错误: " + e.getMessage());
            }
        }

        // 3. 处理修改的诗词（重新生成缓存）
        for (Poem modifiedPoem : updateResult.modifiedPoems) {
            try {
                // 删除旧缓存
                deleteCacheFile(modifiedPoem.getPID());
                cacheInfo.cachedPoemIds.remove(modifiedPoem.getPID());

                String content = contentBuilder.apply(modifiedPoem);
                if (content.length() < 20) {
                    System.out.println("⚠️ 跳过内容过短的修改诗词，PID: " + modifiedPoem.getPID());
                    continue;
                }

                // 重新生成缓存
                Map<String, String> metadataMap = createMetadata(modifiedPoem);
                Metadata metadata = Metadata.from(metadataMap);
                TextSegment segment = TextSegment.from(content, metadata);

                // 🔴 API 调用 - 生成嵌入
                Response<Embedding> response = embeddingModel.embed(segment);
                Embedding embedding = response.content();
                embeddingStore.add(embedding, segment);

                // 保存到缓存
                savePoemCache(modifiedPoem, content, embedding.vector(), metadataMap);
                cacheInfo.cachedPoemIds.add(modifiedPoem.getPID());

                apiCallsUsed++;
                System.out.println("🔄 更新缓存：《" + (modifiedPoem.getTitle() != null ? modifiedPoem.getTitle() : "无标题") + "》");

                Thread.sleep(100);

            } catch (Exception e) {
                System.err.println("❌ 处理修改诗词失败，ID: " + modifiedPoem.getPID() + ", 错误: " + e.getMessage());
            }
        }

        // 4. 更新缓存信息
        cacheInfo.processedCount = cacheInfo.cachedPoemIds.size();
        cacheInfo.lastUpdateTime = System.currentTimeMillis();
        cacheInfo.lastDatabaseCheckTime = System.currentTimeMillis();

        // 重新计算总数（考虑删除的情况）
        // 这里简化处理，实际应该重新查询数据库
        cacheInfo.totalPoems = cacheInfo.processedCount;

        saveCacheInfo(cacheInfo);

        System.out.println("✅ 增量更新完成！");
        System.out.println("📊 最终统计：");
        System.out.println("  • API 调用次数: " + apiCallsUsed);
        System.out.println("  • 新增处理: " + updateResult.newPoems.size() + " 首");
        System.out.println("  • 修改处理: " + updateResult.modifiedPoems.size() + " 首");
        System.out.println("  • 删除处理: " + updateResult.deletedPoemIds.size() + " 首");
        System.out.println("  • 当前缓存总数: " + cacheInfo.cachedPoemIds.size() + " 首");
    }

    /**
     * 从缓存加载向量数据到 EmbeddingStore
     */
    public int loadFromCache(EmbeddingStore<TextSegment> embeddingStore) {
        try {
            File embeddingsDir = new File(EMBEDDINGS_DIR);
            File[] cacheFiles = embeddingsDir.listFiles((dir, name) -> name.endsWith(".json"));

            if (cacheFiles == null || cacheFiles.length == 0) {
                System.out.println("📄 没有找到缓存文件");
                return 0;
            }

            int loadedCount = 0;
            System.out.println("🔄 开始从缓存加载向量数据...");

            // 按文件名排序，确保加载顺序
            Arrays.sort(cacheFiles, Comparator.comparing(File::getName));

            for (File file : cacheFiles) {
                try {
                    EmbeddingCache cache = objectMapper.readValue(file, EmbeddingCache.class);

                    // 重建 TextSegment
                    Metadata metadata = Metadata.from(cache.metadata != null ? cache.metadata : new HashMap<>());
                    TextSegment segment = TextSegment.from(cache.content, metadata);

                    // 重建 Embedding
                    Embedding embedding = new Embedding(cache.vector);

                    // 添加到存储
                    embeddingStore.add(embedding, segment);
                    loadedCount++;

                    // 每100个显示一次进度
                    if (loadedCount % 100 == 0) {
                        System.out.println("📦 已加载: " + loadedCount + " 个向量");
                    }

                } catch (Exception e) {
                    System.err.println("❌ 加载缓存文件失败: " + file.getName() + ", 错误: " + e.getMessage());
                }
            }

            System.out.println("✅ 缓存加载完成，共加载 " + loadedCount + " 个向量");
            return loadedCount;

        } catch (Exception e) {
            System.err.println("❌ 缓存加载过程失败: " + e.getMessage());
            return 0;
        }
    }

    /**
     * 保存单个诗词的向量到缓存
     */
    public void savePoemCache(Poem poem, String content, float[] vector, Map<String, String> metadata) {
        try {
            EmbeddingCache cache = new EmbeddingCache(
                    poem.getPID(),
                    poem.getTitle(),
                    poem.getPoet(),
                    vector,
                    content,
                    metadata
            );

            String fileName = EMBEDDINGS_DIR + "/poem_" + poem.getPID() + ".json";
            objectMapper.writeValue(new File(fileName), cache);

        } catch (Exception e) {
            System.err.println("❌ 保存诗词缓存失败, PID: " + poem.getPID() + ", 错误: " + e.getMessage());
        }
    }

    /**
     * 删除单个诗词的缓存文件
     */
    private void deleteCacheFile(int poemId) {
        try {
            String fileName = EMBEDDINGS_DIR + "/poem_" + poemId + ".json";
            File file = new File(fileName);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            System.err.println("删除缓存文件失败，PID: " + poemId + ", 错误: " + e.getMessage());
        }
    }

    /**
     * 创建诗词元数据
     */
    private Map<String, String> createMetadata(Poem poem) {
        Map<String, String> metadataMap = new HashMap<>();
        metadataMap.put("poem_id", String.valueOf(poem.getPID()));
        metadataMap.put("title", poem.getTitle() != null ? poem.getTitle() : "");
        metadataMap.put("poet", poem.getPoet() != null ? poem.getPoet() : "");
        metadataMap.put("category", poem.getCategory() != null ? poem.getCategory() : "");
        return metadataMap;
    }

    /**
     * 加载缓存信息
     */
    public CacheInfo loadCacheInfo() {
        try {
            File file = new File(CACHE_INFO_FILE);
            if (!file.exists()) {
                return null;
            }
            return objectMapper.readValue(file, CacheInfo.class);
        } catch (Exception e) {
            System.err.println("❌ 缓存信息加载失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 保存缓存信息
     */
    public void saveCacheInfo(CacheInfo cacheInfo) {
        try {
            Files.createDirectories(Paths.get("data"));
            objectMapper.writeValue(new File(CACHE_INFO_FILE), cacheInfo);
            System.out.println("💾 缓存信息已保存 - 总数: " + cacheInfo.totalPoems +
                    ", 已缓存: " + cacheInfo.cachedPoemIds.size());
        } catch (Exception e) {
            System.err.println("❌ 缓存信息保存失败: " + e.getMessage());
        }
    }

    /**
     * 保存缓存信息（重载方法）
     */
    public void saveCacheInfo(List<Poem> poems, int processedCount) {
        try {
            String dataHash = calculateDataHash(poems);
            CacheInfo cacheInfo = new CacheInfo(
                    poems.size(),
                    System.currentTimeMillis(),
                    dataHash,
                    processedCount
            );

            // 添加已处理的诗词ID
            for (int i = 0; i < Math.min(processedCount, poems.size()); i++) {
                cacheInfo.cachedPoemIds.add(poems.get(i).getPID());
            }

            objectMapper.writeValue(new File(CACHE_INFO_FILE), cacheInfo);
            System.out.println("💾 缓存信息已保存 - 总数: " + poems.size() + ", 已处理: " + processedCount);

        } catch (Exception e) {
            System.err.println("❌ 缓存信息保存失败: " + e.getMessage());
        }
    }

    /**
     * 清理所有缓存
     */
    public void clearCache() {
        try {
            File embeddingsDir = new File(EMBEDDINGS_DIR);
            if (embeddingsDir.exists()) {
                File[] files = embeddingsDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        file.delete();
                    }
                }
                System.out.println("🗑️ 缓存文件已清理");
            }

            File cacheInfoFile = new File(CACHE_INFO_FILE);
            if (cacheInfoFile.exists()) {
                cacheInfoFile.delete();
                System.out.println("🗑️ 缓存信息文件已删除");
            }

        } catch (Exception e) {
            System.err.println("❌ 缓存清理失败: " + e.getMessage());
        }
    }

    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try {
            CacheInfo cacheInfo = loadCacheInfo();
            if (cacheInfo != null) {
                stats.put("total_poems", cacheInfo.totalPoems);
                stats.put("processed_count", cacheInfo.processedCount);
                stats.put("last_update", new Date(cacheInfo.lastUpdateTime));
                stats.put("data_hash", cacheInfo.dataHash);
                stats.put("cached_poem_ids_count", cacheInfo.cachedPoemIds.size());
            }

            File embeddingsDir = new File(EMBEDDINGS_DIR);
            if (embeddingsDir.exists()) {
                File[] files = embeddingsDir.listFiles((dir, name) -> name.endsWith(".json"));
                stats.put("cache_files_count", files != null ? files.length : 0);

                if (files != null && files.length > 0) {
                    long totalSize = Arrays.stream(files).mapToLong(File::length).sum();
                    stats.put("cache_size_mb", String.format("%.2f", totalSize / 1024.0 / 1024.0));
                }
            }

            stats.put("cache_directory", new File(CACHE_BASE_DIR).getAbsolutePath());

        } catch (Exception e) {
            stats.put("error", e.getMessage());
        }

        return stats;
    }

    /**
     * 计算数据哈希值（用于检测数据变化）
     */
    private String calculateDataHash(List<Poem> poems) {
        try {
            StringBuilder sb = new StringBuilder();

            // 使用前10首诗的关键信息计算哈希
            int sampleSize = Math.min(10, poems.size());
            for (int i = 0; i < sampleSize; i++) {
                Poem poem = poems.get(i);
                sb.append(poem.getPID())
                        .append(poem.getTitle())
                        .append(poem.getPoet())
                        .append(poem.getText() != null ? poem.getText().length() : 0);
            }

            // 添加总数到哈希中
            sb.append("total:").append(poems.size());

            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(sb.toString().getBytes("UTF-8"));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (Exception e) {
            System.err.println("❌ 计算数据哈希失败: " + e.getMessage());
            return "unknown_" + System.currentTimeMillis();
        }
    }
}