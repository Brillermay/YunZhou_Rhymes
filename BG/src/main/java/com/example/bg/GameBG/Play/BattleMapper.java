package com.example.bg.GameBG.Play;


import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BattleMapper {
    void saveCache(HistoryDTO historyDTO);
}
