package com.example.bg.GameBG.Player;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Data
@Getter
@Setter
public class PlayerDTO {
    private Integer PID;      // 统一大小写
    private String LastPlayTime;
    private String achievements;
    private Integer gold;
    private Integer UID;
    private ArrayList<CardDTO> cardList = new ArrayList<>();

    @Data
    public static class CardDTO {
        private String cardType;
        private Integer cardNum;
    }

    // 日期格式处理
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public String getLastPlayTime() {
        return LastPlayTime;
    }
}