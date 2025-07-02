package com.example.bg.GameBG.Play.Entities;

import lombok.Data;

@Data
public class HistoryDTO {
    private String roomid,timestamp;
    private int player1,player2,who_win;

}
