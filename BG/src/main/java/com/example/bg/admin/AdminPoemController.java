package com.example.bg.admin;

import com.example.bg.poem.Poem;
import com.example.bg.poem.PoemGetMapper;
import com.example.bg.ConnetMySQL;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/poem")
public class AdminPoemController extends ConnetMySQL {

    /**
     * 获取所有诗词列表（分页）
     */
    @GetMapping("/list")
    @Operation(summary = "获取诗词列表，支持分页和搜索")
    public Map<String, Object> getPoemList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category) {

        Map<String, Object> result = new HashMap<>();

        try (InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
             SqlSession session = getSession(in)) {

            AdminPoemMapper mapper = session.getMapper(AdminPoemMapper.class);

            // 计算偏移量
            int offset = (page - 1) * size;

            // 获取诗词列表
            List<Poem> poems = mapper.getPoemList(offset, size, search, category);

            // 获取总数
            int total = mapper.getPoemCount(search, category);

            result.put("success", true);
            result.put("data", poems);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("totalPages", (int) Math.ceil((double) total / size));

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取诗词列表失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 获取单首诗词详情
     */
    @GetMapping("/{pid}")
    @Operation(summary = "获取诗词详情")
    public Map<String, Object> getPoemDetail(@PathVariable int pid) {
        Map<String, Object> result = new HashMap<>();

        try (InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
             SqlSession session = getSession(in)) {

            AdminPoemMapper mapper = session.getMapper(AdminPoemMapper.class);
            Poem poem = mapper.getPoemById(pid);

            if (poem != null) {
                result.put("success", true);
                result.put("data", poem);
            } else {
                result.put("success", false);
                result.put("message", "诗词不存在");
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取诗词详情失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 添加新诗词
     */
    @PostMapping("/add")
    @Operation(summary = "添加新诗词")
    public Map<String, Object> addPoem(@RequestBody Poem poem) {
        Map<String, Object> result = new HashMap<>();

        try (InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
             SqlSession session = getSession(in)) {

            AdminPoemMapper mapper = session.getMapper(AdminPoemMapper.class);

            // 验证必填字段
            if (poem.getTitle() == null || poem.getTitle().trim().isEmpty() ||
                    poem.getPoet() == null || poem.getPoet().trim().isEmpty() ||
                    poem.getText() == null || poem.getText().trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "标题、作者和内容不能为空");
                return result;
            }

            mapper.insertPoem(poem);
            session.commit();

            result.put("success", true);
            result.put("message", "诗词添加成功");
            result.put("data", poem);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "添加诗词失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 更新诗词
     */
    @PutMapping("/{pid}")
    @Operation(summary = "更新诗词")
    public Map<String, Object> updatePoem(@PathVariable int pid, @RequestBody Poem poem) {
        Map<String, Object> result = new HashMap<>();

        try (InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
             SqlSession session = getSession(in)) {

            AdminPoemMapper mapper = session.getMapper(AdminPoemMapper.class);

            // 设置PID
            poem.setPID(pid);

            // 验证诗词是否存在
            Poem existingPoem = mapper.getPoemById(pid);
            if (existingPoem == null) {
                result.put("success", false);
                result.put("message", "诗词不存在");
                return result;
            }

            // 验证必填字段
            if (poem.getTitle() == null || poem.getTitle().trim().isEmpty() ||
                    poem.getPoet() == null || poem.getPoet().trim().isEmpty() ||
                    poem.getText() == null || poem.getText().trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "标题、作者和内容不能为空");
                return result;
            }

            mapper.updatePoem(poem);
            session.commit();

            result.put("success", true);
            result.put("message", "诗词更新成功");
            result.put("data", poem);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新诗词失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 删除诗词
     */
    @DeleteMapping("/{pid}")
    @Operation(summary = "删除诗词")
    public Map<String, Object> deletePoem(@PathVariable int pid) {
        Map<String, Object> result = new HashMap<>();

        try (InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
             SqlSession session = getSession(in)) {

            AdminPoemMapper mapper = session.getMapper(AdminPoemMapper.class);

            // 验证诗词是否存在
            Poem existingPoem = mapper.getPoemById(pid);
            if (existingPoem == null) {
                result.put("success", false);
                result.put("message", "诗词不存在");
                return result;
            }

            mapper.deletePoem(pid);
            session.commit();

            result.put("success", true);
            result.put("message", "诗词删除成功");

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除诗词失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 批量删除诗词
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除诗词")
    public Map<String, Object> batchDeletePoems(@RequestBody List<Integer> pids) {
        Map<String, Object> result = new HashMap<>();

        try (InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
             SqlSession session = getSession(in)) {

            AdminPoemMapper mapper = session.getMapper(AdminPoemMapper.class);

            mapper.batchDeletePoems(pids);
            session.commit();

            result.put("success", true);
            result.put("message", "批量删除成功，共删除 " + pids.size() + " 首诗词");

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "批量删除失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 获取诗词分类列表
     */
    @GetMapping("/categories")
    @Operation(summary = "获取所有诗词分类")
    public Map<String, Object> getCategories() {
        Map<String, Object> result = new HashMap<>();

        try (InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
             SqlSession session = getSession(in)) {

            AdminPoemMapper mapper = session.getMapper(AdminPoemMapper.class);
            List<String> categories = mapper.getAllCategories();

            result.put("success", true);
            result.put("data", categories);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取分类失败：" + e.getMessage());
        }

        return result;
    }
}