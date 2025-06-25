package com.example.bg.RedisService;

import com.example.bg.Comment.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RedisCommentService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void cacheCommentTree(int rootId, List<Comment> tree) {
        String key = "comment:tree:" + rootId;
        redisTemplate.opsForValue().set(key,  tree, 1, TimeUnit.HOURS);
    }

    public List<Comment> getCachedTree(int rootId) {
        String key = "comment:tree:" + rootId;
        return (List<Comment>) redisTemplate.opsForValue().get(key);
    }

    public void cacheRootComments(List<Comment> roots) {
        redisTemplate.opsForValue().set("comment:roots",  roots, 30, TimeUnit.MINUTES);
    }

    public List<Comment> getCachedRoots() {
        return (List<Comment>) redisTemplate.opsForValue().get("comment:roots");
    }

    public void evictTreeCache(int rootId) {
        redisTemplate.delete("comment:tree:"  + rootId);
    }

    public void evictRootCache() {
        redisTemplate.delete("comment:roots");
    }
}