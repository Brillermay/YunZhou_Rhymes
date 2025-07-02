package com.example.bg.user;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.example.bg.poem.Poem;

import java.util.List;

@Mapper
@Repository
public interface UserStarMapper {
    int addStar(int uid, int pid);
    int removeStar(int uid, int pid);
     List<Poem> getStarList(int uid);
}