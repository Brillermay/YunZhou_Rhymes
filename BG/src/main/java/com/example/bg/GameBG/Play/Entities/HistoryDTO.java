package com.example.bg.GameBG.Play.Entities;

import lombok.Data;

@Data
public class HistoryDTO {
    /*
    * roomId，string，房间号
    * timestamp，string，时间
    * player1，int，玩家1id
    * player2，int，玩家2id
    * who_win,int,赢家id
    * */

    private String roomId,timestamp;
    private int player1,player2,who_win;

}
