package com.example.bg.user;

import com.example.bg.poem.Poem;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import java.util.List;

@Mapper
@Repository
public interface UserStarMapper {
    int addStar(int uid, int pid);
    int removeStar(int uid, int pid);
    List<Poem> getStarList(int uid);
}