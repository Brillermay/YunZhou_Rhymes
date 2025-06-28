package com.example.bg.ai.controller;

import com.example.bg.ConnetMySQL;
import com.example.bg.ai.service.EasyRAGService;
import com.example.bg.poem.Poem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Easy RAG", description = "简单易用的 RAG 诗词问答系统")
@CrossOrigin(
        origins = {
                "http://localhost:8080",
                "http://localhost:8081",
                "http://127.0.0.1:8081",
                "http://117.72.88.23:8081",
        },
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
        maxAge = 3600,
        allowCredentials = "true"
)
@RestController
@RequestMapping(value = "/ai/easy")
public class EasyRAGController extends ConnetMySQL {

    @Autowired
    private EasyRAGService easyRAGService;

    @GetMapping("/test-database")
    @Operation(summary = "测试数据库连接和字段映射")
    public ResponseEntity<Map<String, Object>> testDatabase() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Poem> poems = easyRAGService.testDatabaseConnection();

            response.put("success", true);
            response.put("total_poems", poems.size());

            if (!poems.isEmpty()) {
                // 返回前3首诗的详细信息
                List<Map<String, Object>> samplePoems = new ArrayList<>();
                for (int i = 0; i < Math.min(3, poems.size()); i++) {
                    Poem poem = poems.get(i);
                    Map<String, Object> poemInfo = new HashMap<>();
                    poemInfo.put("pid", poem.getPID());
                    poemInfo.put("title", poem.getTitle());
                    poemInfo.put("poet", poem.getPoet());
                    poemInfo.put("text_length", poem.getText() != null ? poem.getText().length() : 0);
                    poemInfo.put("text_preview", poem.getText() != null ?
                            poem.getText().substring(0, Math.min(100, poem.getText().length())) + "..." : "null");
                    poemInfo.put("category", poem.getCategory());
                    poemInfo.put("has_background", poem.getBackground() != null && !poem.getBackground().trim().isEmpty());
                    poemInfo.put("has_appreciation", poem.getAppreciation() != null && !poem.getAppreciation().trim().isEmpty());
                    poemInfo.put("has_translation", poem.getTranslation() != null && !poem.getTranslation().trim().isEmpty());
                    samplePoems.add(poemInfo);
                }
                response.put("sample_poems", samplePoems);

                // 统计信息
                int validTextCount = 0;
                int hasBackgroundCount = 0;
                int hasAppreciationCount = 0;
                int hasTranslationCount = 0;
                int hasCategoryCount = 0;

                for (Poem poem : poems) {
                    if (poem.getText() != null && !poem.getText().trim().isEmpty()) validTextCount++;
                    if (poem.getBackground() != null && !poem.getBackground().trim().isEmpty()) hasBackgroundCount++;
                    if (poem.getAppreciation() != null && !poem.getAppreciation().trim().isEmpty()) hasAppreciationCount++;
                    if (poem.getTranslation() != null && !poem.getTranslation().trim().isEmpty()) hasTranslationCount++;
                    if (poem.getCategory() != null && !poem.getCategory().trim().isEmpty()) hasCategoryCount++;
                }

                Map<String, Object> statistics = new HashMap<>();
                statistics.put("valid_text_count", validTextCount);
                statistics.put("has_background_count", hasBackgroundCount);
                statistics.put("has_appreciation_count", hasAppreciationCount);
                statistics.put("has_translation_count", hasTranslationCount);
                statistics.put("has_category_count", hasCategoryCount);
                response.put("statistics", statistics);
            }

            response.put("message", "数据库连接测试成功");
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "数据库测试失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/initialize")
    @Operation(summary = "初始化 Easy RAG 系统")
    public ResponseEntity<Map<String, Object>> initializeRAG() {
        Map<String, Object> response = new HashMap<>();

        try {
            easyRAGService.initializeRAG();

            response.put("success", true);
            response.put("message", "Easy RAG 系统初始化成功");
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "初始化失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/chat")
    @Operation(summary = "Easy RAG 对话")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String question = request.get("question");

            if (question == null || question.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "问题不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            String answer = easyRAGService.chat(question);

            response.put("success", true);
            response.put("answer", answer);
            response.put("question", question);
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "对话失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/recommend")
    @Operation(summary = "诗词推荐")
    public ResponseEntity<Map<String, Object>> recommend(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String theme = request.get("theme");
            String preference = request.getOrDefault("preference", "");

            if (theme == null || theme.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "主题不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            String answer = easyRAGService.recommendPoetry(theme, preference);

            response.put("success", true);
            response.put("answer", answer);
            response.put("theme", theme);
            response.put("preference", preference);
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "推荐失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/analyze")
    @Operation(summary = "诗词分析")
    public ResponseEntity<Map<String, Object>> analyze(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String poemTitle = request.get("poemTitle");
            String analysisType = request.getOrDefault("analysisType", "综合分析");

            if (poemTitle == null || poemTitle.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "诗词标题不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            String answer = easyRAGService.analyzePoetry(poemTitle, analysisType);

            response.put("success", true);
            response.put("answer", answer);
            response.put("poemTitle", poemTitle);
            response.put("analysisType", analysisType);
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "分析失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/retrieve")
    @Operation(summary = "检索测试")
    public ResponseEntity<Map<String, Object>> retrieve(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String query = request.get("query");
            int maxResults = Integer.parseInt(request.getOrDefault("maxResults", "5"));

            if (query == null || query.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "查询内容不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            List<String> results = easyRAGService.testRetrieve(query, maxResults);

            response.put("success", true);
            response.put("results", results);
            response.put("count", results.size());
            response.put("query", query);
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "检索失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/status")
    @Operation(summary = "系统状态")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new HashMap<>();

        response.put("success", true);
        response.put("message", "Easy RAG 系统运行正常");
        response.put("model", "qwen-plus");
        response.put("embedding", "text-embedding-v1");
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/test-api")
    @Operation(summary = "测试 API 连接")
    public ResponseEntity<Map<String, Object>> testAPI() {
        Map<String, Object> response = new HashMap<>();

        try {
            // 测试聊天 API
            String chatResponse = easyRAGService.testChatAPI();

            // 测试嵌入 API
            String embeddingInfo = easyRAGService.testEmbeddingAPI();

            response.put("success", true);
            response.put("chat_response", chatResponse);
            response.put("embedding_info", embeddingInfo);
            response.put("message", "API 连接测试成功");
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "API 测试失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/cache/status")
    @Operation(summary = "获取缓存状态")
    public ResponseEntity<Map<String, Object>> getCacheStatus() {
        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, Object> cacheStats = easyRAGService.getCacheStatistics();

            response.put("success", true);
            response.put("cache_statistics", cacheStats);
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "获取缓存状态失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/cache/clear")
    @Operation(summary = "清理缓存")
    public ResponseEntity<Map<String, Object>> clearCache() {
        Map<String, Object> response = new HashMap<>();

        try {
            easyRAGService.clearCache();

            response.put("success", true);
            response.put("message", "缓存已清理");
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "清理缓存失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }


    @GetMapping("/database/check-updates")
    @Operation(summary = "检查数据库更新")
    public ResponseEntity<Map<String, Object>> checkDatabaseUpdates() {
        Map<String, Object> response = new HashMap<>();

        try {
            String report = easyRAGService.checkDatabaseUpdates();

            response.put("success", true);
            response.put("report", report);
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "检查数据库更新失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/database/incremental-update")
    @Operation(summary = "执行增量更新")
    public ResponseEntity<Map<String, Object>> performIncrementalUpdate() {
        Map<String, Object> response = new HashMap<>();

        try {
            String result = easyRAGService.performManualUpdate();

            response.put("success", true);
            response.put("result", result);
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "增量更新失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/database/force-rebuild")
    @Operation(summary = "强制全量重建")
    public ResponseEntity<Map<String, Object>> forceFullRebuild() {
        Map<String, Object> response = new HashMap<>();

        try {
            // 清理缓存并重新初始化
            easyRAGService.clearCache();
            easyRAGService.initializeRAG();

            response.put("success", true);
            response.put("message", "全量重建完成");
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "全量重建失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/cache/debug")
    @Operation(summary = "调试缓存状态")
    public ResponseEntity<Map<String, Object>> debugCache() {
        Map<String, Object> response = new HashMap<>();

        try {
            // 调用调试方法
            easyRAGService.getCacheManager().debugCacheStatus();

            response.put("success", true);
            response.put("message", "调试信息已输出到控制台");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Easy RAG 流式对话（SSE，多轮）")
    public SseEmitter chatStream(@RequestBody Map<String, Object> request) {
        SseEmitter emitter = new SseEmitter(0L);
        try {
            String question = (String) request.get("question");
            List<Map<String, String>> history = (List<Map<String, String>>) request.get("history"); // 新增

            if (question == null || question.trim().isEmpty()) {
                emitter.send(SseEmitter.event().data("问题不能为空"));
                emitter.complete();
                return emitter;
            }
            easyRAGService.chatStreamWithHistory(question, history, emitter); // 新方法
        } catch (Exception e) {
            try {
                emitter.send(SseEmitter.event().data("流式对话失败：" + e.getMessage()));
            } catch (Exception ignored) {}
            emitter.completeWithError(e);
        }
        return emitter;
    }

    @PostMapping(value = "/chat/stream/role", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
@Operation(summary = "古人角色扮演流式对话（SSE，多轮）")
public SseEmitter chatStreamWithRole(@RequestBody Map<String, Object> request) {
    SseEmitter emitter = new SseEmitter(0L);
    try {
        String question = (String) request.get("question");
        String role = (String) request.get("role");
        List<Map<String, String>> history = (List<Map<String, String>>) request.get("history");

        if (question == null || question.trim().isEmpty()) {
            emitter.send(SseEmitter.event().data("问题不能为空"));
            emitter.complete();
            return emitter;
        }
        if (role == null || role.trim().isEmpty()) {
            emitter.send(SseEmitter.event().data("角色不能为空"));
            emitter.complete();
            return emitter;
        }
        // 只允许五个角色
        if (!com.example.bg.ai.RoleProfileUtil.getSupportedRoles().contains(role)) {
            emitter.send(SseEmitter.event().data("仅支持角色：" + com.example.bg.ai.RoleProfileUtil.getSupportedRoles()));
            emitter.complete();
            return emitter;
        }
        easyRAGService.chatStreamWithRole(question, role, history, emitter);
    } catch (Exception e) {
        try {
            emitter.send(SseEmitter.event().data("流式对话失败：" + e.getMessage()));
        } catch (Exception ignored) {}
        emitter.completeWithError(e);
    }
    return emitter;
}

@PostMapping(value = "/soul-matcher/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
@Operation(summary = "前世今生·灵魂碎片配对器（AI主动提问+分析，流式）")
public SseEmitter soulMatcherStream(@RequestBody Map<String, Object> request) {
    SseEmitter emitter = new SseEmitter(0L);
    try {
        List<Map<String, String>> history = (List<Map<String, String>>) request.get("history");
        easyRAGService.soulMatcherStream(history, emitter);
    } catch (Exception e) {
        try {
            emitter.send(SseEmitter.event().data("配对器流式对话失败：" + e.getMessage()));
        } catch (Exception ignored) {}
        emitter.completeWithError(e);
    }
    return emitter;
}

    /**
     * 🆕 AI智能搜索诗词接口
     */
    @GetMapping("/ai-search/{query}")
    @Operation(summary = "AI语义搜索诗词")
    public ResponseEntity<Map<String, Object>> aiSearchPoems(
            @PathVariable String query,
            @RequestParam(defaultValue = "10") int maxResults) {

        Map<String, Object> response = new HashMap<>();

        try {
            if (!easyRAGService.isInitialized()) {
                response.put("success", false);
                response.put("message", "AI搜索服务未初始化，请先初始化系统");
                return ResponseEntity.ok(response);
            }

            if (query == null || query.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "搜索查询不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 调用RAG检索
            List<String> rawResults = easyRAGService.testRetrieve(query, Math.min(maxResults, 20));

            // 解析RAG结果为诗词数据
            List<Map<String, Object>> poems = easyRAGService.parseRAGResultsToPoems(rawResults, query);

            response.put("success", true);
            response.put("data", poems);
            response.put("total", poems.size());
            response.put("query", query);
            response.put("searchType", "ai_semantic");
            response.put("timestamp", System.currentTimeMillis());

            System.out.println("✅ AI搜索完成: 查询=" + query + ", 结果数=" + poems.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "AI搜索失败: " + e.getMessage());
            response.put("query", query);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 🆕 AI搜索统计接口
     */
    @GetMapping("/ai-search-stats")
    @Operation(summary = "获取AI搜索统计信息")
    public ResponseEntity<Map<String, Object>> getAISearchStats() {
        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, Object> stats = easyRAGService.getAISearchStatistics();

            response.put("success", true);
            response.put("stats", stats);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "获取统计信息失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

/**
 * 🔧 调试接口：检查RAG原始返回
 */
@GetMapping("/debug-rag/{query}")
@Operation(summary = "调试RAG原始返回数据")
public ResponseEntity<Map<String, Object>> debugRAG(@PathVariable String query) {
    Map<String, Object> response = new HashMap<>();
    
    try {
        // 获取原始RAG结果
        List<String> rawResults = easyRAGService.testRetrieve(query, 5);
        
        response.put("success", true);
        response.put("query", query);
        response.put("rawResultsCount", rawResults.size());
        response.put("rawResults", rawResults);
        
        // 尝试解析每个结果
        List<Map<String, Object>> debugInfo = new ArrayList<>();
        for (int i = 0; i < rawResults.size(); i++) {
            Map<String, Object> debug = new HashMap<>();
            debug.put("index", i);
            debug.put("rawContent", rawResults.get(i));
            debug.put("contentLength", rawResults.get(i).length());
            
            // 尝试提取PID
            try {
                String content = rawResults.get(i);
                if (content.startsWith("相似度:")) {
                    String[] parts = content.split("\n", 2);
                    if (parts.length >= 2) {
                        content = parts[1].replace("内容:", "").trim();
                    }
                }
                debug.put("extractedContent", content.substring(0, Math.min(200, content.length())));
                
                // 检查是否包含poem_id
                debug.put("containsPoemId", content.contains("poem_id"));
                debug.put("containsPID", content.contains("PID"));
                
            } catch (Exception e) {
                debug.put("extractError", e.getMessage());
            }
            
            debugInfo.add(debug);
        }
        
        response.put("debugInfo", debugInfo);
        return ResponseEntity.ok(response);
        
    } catch (Exception e) {
        e.printStackTrace();
        response.put("success", false);
        response.put("message", "调试失败: " + e.getMessage());
        return ResponseEntity.status(500).body(response);
    }
}

}