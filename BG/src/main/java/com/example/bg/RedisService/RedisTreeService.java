package com.example.bg.RedisService;
import com.example.bg.Comment.Comment;
import com.example.bg.poem.Poem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RedisTreeService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    public void cacheCommentTree(String keyword, List<Poem> lst) {
        String key="keyword"+keyword;
        redisTemplate.opsForValue().set(key,lst,30,TimeUnit.SECONDS);
    }
    public List<Poem> getCachedTree(String ky) {
        String key = "keyword" + ky;
        return (List<Poem>) redisTemplate.opsForValue().get(key);
    }
}
