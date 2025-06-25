package com.example.bg.GameBG.Player;


import lombok.Data;

/*
 * 组织方式：
 * 考虑到一卡多用和卡组有多张卡片
 * 设计用户编号对应卡牌的列联表，包含
 *
 * PID       CID     NUM
 * 用户ID    卡牌ID   数量
 *
 * 三项关键元素
 * 这里的PID既是存档名也是玩家ID
 * */


@Data
public class Player {
    public int PID;//Player ID
    public String LastPlayTime;//最后上线时间
}
