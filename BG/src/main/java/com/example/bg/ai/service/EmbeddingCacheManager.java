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
    // 同时修改这些常量：
    private static final String CACHE_BASE_DIR = System.getProperty("user.dir") +File.separator + "BG" +File.separator + "data";
    private static final String EMBEDDINGS_DIR = CACHE_BASE_DIR + File.separator + "embeddings";  
    private static final String CACHE_INFO_FILE = CACHE_BASE_DIR + File.separator + "cache_info.json";

    private final ObjectMapper objectMapper = new ObjectMapper();


    // 🆕 添加这两个成员变量
    private File cacheDir;
    private File cacheInfoFile;
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
// 修改缓存目录初始化方法
public void initializeCacheDirectories() throws Exception {
    try {
        // 🔧 修复：使用相对于jar包的路径
        String baseDir;
        
        // 检查是否在jar包中运行
        String jarPath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        if (jarPath.endsWith(".jar")) {
            // 在jar包中运行，使用jar包所在目录
            File jarFile = new File(jarPath);
            baseDir = jarFile.getParent() + "/data";
        } else {
            // 在IDE中运行，使用相对路径
            baseDir = "data";
        }
        
        // 创建缓存目录
        cacheDir = new File(baseDir + "/embeddings");
        if (!cacheDir.exists()) {
            boolean created = cacheDir.mkdirs();
            if (!created) {
                throw new RuntimeException("无法创建缓存目录: " + cacheDir.getAbsolutePath());
            }
        }
        
        // 设置缓存信息文件路径
        cacheInfoFile = new File(baseDir + "/cache_info.json");
        
        System.out.println("📁 缓存目录初始化完成: " + cacheDir.getAbsolutePath());
        System.out.println("📄 缓存信息文件: " + cacheInfoFile.getAbsolutePath());
        
    } catch (Exception e) {
        System.err.println("❌ 缓存目录初始化失败: " + e.getMessage());
        throw e;
    }
}





    /**
     * 执行增量缓存更新 - 修复版本
     */
    public void performIncrementalUpdate(UpdateResult updateResult,
                                         EmbeddingStore<TextSegment> embeddingStore,
                                         EmbeddingModel embeddingModel,
                                         java.util.function.Function<Poem, String> contentBuilder,
                                         List<Poem> currentPoems) throws Exception { // 🆕 添加当前诗词列表参数

        if (updateResult.type != UpdateType.INCREMENTAL) {
            throw new IllegalArgumentException("只能对增量更新类型执行此操作，当前类型: " + updateResult.type);
        }

        CacheInfo cacheInfo = loadCacheInfo();
        if (cacheInfo == null) {
            throw new RuntimeException("缓存信息不存在，无法执行增量更新");
        }

        int apiCallsUsed = 0;
        int successfullyProcessed = 0;
        int failed = 0;

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

                // 🔧 长度检查和截断
                if (content.length() > 2048) {
                    content = content.substring(0, 2045) + "...";
                }

                if (content.length() < 20) {
                    System.out.println("⚠️ 跳过内容过短的诗词，PID: " + newPoem.getPID());
                    // 🔧 仍然保存失败状态的缓存
                    savePoemCache(newPoem, content, null, new HashMap<>());
                    cacheInfo.cachedPoemIds.add(newPoem.getPID());
                    failed++;
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

                // 保存成功的缓存
                savePoemCache(newPoem, content, embedding.vector(), metadataMap);
                cacheInfo.cachedPoemIds.add(newPoem.getPID());
                apiCallsUsed++;
                successfullyProcessed++;

                System.out.println("➕ 新增缓存：《" + (newPoem.getTitle() != null ? newPoem.getTitle() : "无标题") + "》");

                // API 限制延迟
                Thread.sleep(100);

            } catch (Exception e) {
                // 🔧 保存失败状态的缓存
                savePoemCache(newPoem, contentBuilder.apply(newPoem), null, new HashMap<>());
                cacheInfo.cachedPoemIds.add(newPoem.getPID());
                failed++;
                System.err.println("❌ 处理新增诗词失败，ID: " + newPoem.getPID() + ", 错误: " + e.getMessage());
            }
        }

        // 3. 处理修改的诗词（重新生成缓存）
        for (Poem modifiedPoem : updateResult.modifiedPoems) {
            try {
                // 删除旧缓存
                deleteCacheFile(modifiedPoem.getPID());

                String content = contentBuilder.apply(modifiedPoem);

                // 🔧 长度检查和截断
                if (content.length() > 2048) {
                    content = content.substring(0, 2045) + "...";
                }

                if (content.length() < 20) {
                    System.out.println("⚠️ 跳过内容过短的修改诗词，PID: " + modifiedPoem.getPID());
                    // 🔧 保存失败状态的缓存
                    savePoemCache(modifiedPoem, content, null, new HashMap<>());
                    failed++;
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

                // 保存成功的缓存
                savePoemCache(modifiedPoem, content, embedding.vector(), metadataMap);

                apiCallsUsed++;
                successfullyProcessed++;
                System.out.println("🔄 更新缓存：《" + (modifiedPoem.getTitle() != null ? modifiedPoem.getTitle() : "无标题") + "》");

                Thread.sleep(100);

            } catch (Exception e) {
                // 🔧 保存失败状态的缓存
                savePoemCache(modifiedPoem, contentBuilder.apply(modifiedPoem), null, new HashMap<>());
                failed++;
                System.err.println("❌ 处理修改诗词失败，ID: " + modifiedPoem.getPID() + ", 错误: " + e.getMessage());
            }
        }

        // 4. 🔧 完整更新缓存信息
        cacheInfo.lastUpdateTime = System.currentTimeMillis();
        cacheInfo.lastDatabaseCheckTime = System.currentTimeMillis();

// 🔧 修复：确保统计信息与数据库同步
        cacheInfo.totalPoems = currentPoems.size();           // 数据库当前总数
        cacheInfo.processedCount = currentPoems.size();       // 设为总数（包含失败的）
        cacheInfo.dataHash = calculateDataHash(currentPoems); // 重新计算哈希

        saveCacheInfo(cacheInfo);

        System.out.println("✅ 增量更新完成！");
        System.out.println("📊 最终统计：");
        System.out.println("  • API 调用次数: " + apiCallsUsed);
        System.out.println("  • 成功处理: " + successfullyProcessed + " 首");
        System.out.println("  • 失败处理: " + failed + " 首");
        System.out.println("  • 新增处理: " + updateResult.newPoems.size() + " 首");
        System.out.println("  • 修改处理: " + updateResult.modifiedPoems.size() + " 首");
        System.out.println("  • 删除处理: " + updateResult.deletedPoemIds.size() + " 首");
        System.out.println("  • 🔧 数据库总数: " + currentPoems.size() + " 首");
        System.out.println("  • 🔧 缓存记录总数: " + cacheInfo.totalPoems + " 首");
        System.out.println("  • 🔧 处理计数: " + cacheInfo.processedCount + " 首");
        System.out.println("  • 当前缓存ID数: " + cacheInfo.cachedPoemIds.size() + " 首");
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
     * 保存单个诗词的向量到缓存（支持失败状态）
     */
    public void savePoemCache(Poem poem, String content, float[] vector, Map<String, String> metadata) {
        try {
            EmbeddingCache cache = new EmbeddingCache(
                    poem.getPID(),
                    poem.getTitle(),
                    poem.getPoet(),
                    vector,  // 🔧 如果失败，vector 为 null
                    content,
                    metadata
            );

            String fileName = EMBEDDINGS_DIR + "/poem_" + poem.getPID() + ".json";
            objectMapper.writeValue(new File(fileName), cache);

            if (vector == null) {
                System.out.println("⚠️ 保存失败状态缓存: PID " + poem.getPID());
            }

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
     * 🔧 修复版：加载缓存信息
     */
    public CacheInfo loadCacheInfo() {
        try {
            File file = this.cacheInfoFile != null ? this.cacheInfoFile : new File(CACHE_INFO_FILE);
            System.out.println("🔍 尝试加载缓存信息文件: " + file.getAbsolutePath());

            if (!file.exists()) {
                System.out.println("📝 缓存信息文件不存在: " + file.getAbsolutePath());
                return null;
            }

            if (file.length() == 0) {
                System.out.println("📝 缓存信息文件为空: " + file.getAbsolutePath());
                return null;
            }

            System.out.println("📄 缓存信息文件存在，大小: " + file.length() + " 字节");

            CacheInfo cacheInfo = objectMapper.readValue(file, CacheInfo.class);

            // 🆕 验证加载的数据
            if (cacheInfo != null) {
                System.out.println("✅ 缓存信息加载成功:");
                System.out.println("  • 总诗词数: " + cacheInfo.totalPoems);
                System.out.println("  • 已处理数: " + cacheInfo.processedCount);
                System.out.println("  • 缓存ID数量: " + (cacheInfo.cachedPoemIds != null ? cacheInfo.cachedPoemIds.size() : 0));
                System.out.println("  • 最后更新: " + new java.util.Date(cacheInfo.lastUpdateTime));
            }

            return cacheInfo;

        } catch (Exception e) {
            System.err.println("❌ 缓存信息加载失败: " + e.getMessage());
            e.printStackTrace(); // 🆕 添加详细错误信息
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
     * 🔧 修复版：保存缓存信息（重载方法）
     */
    /**
     * 🔧 修复版：保存缓存信息（包含失败的诗词）
     */
    /**
     * 🔧 修复版：保存缓存信息（确保与数据库同步）
     */
    public void saveCacheInfo(List<Poem> poems, int processedCount) {
        try {
            System.out.println("🔧 开始保存缓存信息...");
            System.out.println("  • 数据库诗词总数: " + poems.size());
            System.out.println("  • 实际成功处理数: " + processedCount);

            String dataHash = calculateDataHash(poems);
            CacheInfo cacheInfo = new CacheInfo(
                    poems.size(),        // totalPoems = 数据库总数
                    System.currentTimeMillis(),
                    dataHash,
                    poems.size()         // 🔧 processedCount = 数据库总数（这样缓存检查不会失败）
            );

            // 添加所有诗词ID
            cacheInfo.cachedPoemIds.clear();
            for (Poem poem : poems) {
                cacheInfo.cachedPoemIds.add(poem.getPID());
            }

            System.out.println("  • 🔧 totalPoems: " + cacheInfo.totalPoems);
            System.out.println("  • 🔧 processedCount: " + cacheInfo.processedCount);
            System.out.println("  • 缓存ID数量: " + cacheInfo.cachedPoemIds.size());
            System.out.println("  • 数据哈希: " + dataHash);

            // 确保目录存在
            Files.createDirectories(Paths.get(CACHE_BASE_DIR));

            File cacheFile = new File(CACHE_INFO_FILE);
            objectMapper.writeValue(cacheFile, cacheInfo);

            if (cacheFile.exists() && cacheFile.length() > 0) {
                System.out.println("✅ 缓存信息文件保存成功！文件大小: " + cacheFile.length() + " 字节");
                System.out.println("📊 记录全部 " + poems.size() + " 首诗词");
            } else {
                System.err.println("❌ 缓存信息文件保存失败或文件为空！");
            }

        } catch (Exception e) {
            System.err.println("❌ 缓存信息保存失败: " + e.getMessage());
            e.printStackTrace();
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
     * 🆕 调试方法：检查缓存文件状态
     */
    public void debugCacheStatus() {
        System.out.println("\n🔍 === 缓存状态诊断 ===");

        // 检查基础目录
        File baseDir = new File(CACHE_BASE_DIR);
        System.out.println("📁 基础目录: " + baseDir.getAbsolutePath());
        System.out.println("   存在: " + baseDir.exists());
        System.out.println("   可读: " + baseDir.canRead());
        System.out.println("   可写: " + baseDir.canWrite());

        // 检查嵌入目录
        File embeddingsDir = new File(EMBEDDINGS_DIR);
        System.out.println("📁 嵌入目录: " + embeddingsDir.getAbsolutePath());
        System.out.println("   存在: " + embeddingsDir.exists());
        if (embeddingsDir.exists()) {
            File[] files = embeddingsDir.listFiles((dir, name) -> name.endsWith(".json"));
            System.out.println("   JSON文件数: " + (files != null ? files.length : 0));
        }

        // 检查缓存信息文件
        File cacheInfoFile = new File(CACHE_INFO_FILE);
        System.out.println("📄 缓存信息文件: " + cacheInfoFile.getAbsolutePath());
        System.out.println("   存在: " + cacheInfoFile.exists());
        if (cacheInfoFile.exists()) {
            System.out.println("   大小: " + cacheInfoFile.length() + " 字节");
            System.out.println("   最后修改: " + new java.util.Date(cacheInfoFile.lastModified()));
        }

        System.out.println("=== 诊断结束 ===\n");
    }

    /**
     * 计算数据哈希值（用于检测数据变化）
     */
    public String calculateDataHash(List<Poem> poems) {
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

    /**
     * 🎯 简化的缓存检查：确保不会误判
     */
    public boolean isCacheValid(List<Poem> currentPoems) {
        try {
            CacheInfo cacheInfo = loadCacheInfo();
            if (cacheInfo == null) {
                System.out.println("📝 无缓存信息文件");
                return false;
            }

            // 🔧 只检查总数是否匹配
            if (cacheInfo.totalPoems != currentPoems.size()) {
                System.out.println("📊 诗词数量变化：缓存 " + cacheInfo.totalPoems +
                        " 首，当前 " + currentPoems.size() + " 首");
                return false;
            }

            // 🔧 只检查ID集合是否匹配
            Set<Integer> currentIds = new HashSet<>();
            for (Poem poem : currentPoems) {
                currentIds.add(poem.getPID());
            }

            if (!currentIds.equals(cacheInfo.cachedPoemIds)) {
                System.out.println("🔄 诗词ID集合发生变化");
                return false;
            }

            // 检查缓存目录是否存在
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

            // 🔧 宽松检查：只要文件数量大于总数的80%就认为有效
            if (cacheFiles.length < currentPoems.size() * 0.8) {
                System.out.println("📉 缓存文件数量太少：期望约 " + currentPoems.size() +
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
     * 🎯 简化的更新检查：只比较ID集合
     */
    public UpdateResult checkAndUpdateCache(List<Poem> currentPoems, EmbeddingStore<TextSegment> embeddingStore) {
        try {
            CacheInfo cacheInfo = loadCacheInfo();

            if (cacheInfo == null) {
                System.out.println("🆕 无缓存信息，需要全量处理");
                return UpdateResult.forFullRebuild(currentPoems);
            }

            // 🎯 只比较ID集合，忽略其他变化
            Set<Integer> currentPoemIds = new HashSet<>();
            for (Poem poem : currentPoems) {
                currentPoemIds.add(poem.getPID());
            }

            // 检查是否完全相同
            if (currentPoemIds.equals(cacheInfo.cachedPoemIds) && currentPoems.size() == cacheInfo.totalPoems) {
                System.out.println("✅ 诗词ID集合无变化，使用现有缓存");
                return UpdateResult.forNoChange();
            }

            // 找出新增的诗词
            List<Poem> newPoems = new ArrayList<>();
            for (Poem poem : currentPoems) {
                if (!cacheInfo.cachedPoemIds.contains(poem.getPID())) {
                    newPoems.add(poem);
                }
            }

            // 找出删除的诗词
            List<Integer> deletedPoemIds = new ArrayList<>();
            for (Integer cachedId : cacheInfo.cachedPoemIds) {
                if (!currentPoemIds.contains(cachedId)) {
                    deletedPoemIds.add(cachedId);
                }
            }

            // 决定更新策略
            if (newPoems.isEmpty() && deletedPoemIds.isEmpty()) {
                return UpdateResult.forNoChange();
            }

            // 如果变化太大，执行全量重建
            double changeRatio = (double)(newPoems.size() + deletedPoemIds.size()) / Math.max(cacheInfo.totalPoems, 1);
            if (changeRatio > 0.3) {
                System.out.println("🔄 数据变化较大(" + String.format("%.1f", changeRatio * 100) + "%)，执行全量重建");
                return UpdateResult.forFullRebuild(currentPoems);
            }

            // 执行增量更新
            System.out.println("⚡ 检测到增量变化：新增 " + newPoems.size() + " 首，删除 " + deletedPoemIds.size() + " 首");
            return UpdateResult.forIncremental(newPoems, new ArrayList<>(), deletedPoemIds);

        } catch (Exception e) {
            System.err.println("❌ 缓存更新检查失败: " + e.getMessage());
            return UpdateResult.forFullRebuild(currentPoems);
        }
    }


}