package com.example.bg.GameBG.Play;


import com.example.bg.GameBG.Play.Entities.HistoryDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BattleMapper {
    void saveCache(HistoryDTO historyDTO);
}
