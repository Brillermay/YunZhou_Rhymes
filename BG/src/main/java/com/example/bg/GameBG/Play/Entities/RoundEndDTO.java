package com.example.bg.GameBG.Play.Entities;

import lombok.Data;

import java.util.List;

@Data
public class RoundEndDTO {
    /*
    * roomId,string.房间号
    * userId，int，用户id
    * cardsName，list string，里面的card名字
    * round，int，记录当前回合
    * */
    private String roomId;
    private int userId;
    private List<String>cardsName;
    private int round;

}
