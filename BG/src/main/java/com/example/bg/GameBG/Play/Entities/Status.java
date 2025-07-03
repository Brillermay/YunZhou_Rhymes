package com.example.bg.GameBG.Play.Entities;

import lombok.Data;

@Data
public class Status {
    /*
    * consistTime，int，持续回合
    * name，string，状态
    * */
    private String name;
    private int consistTime;
}
