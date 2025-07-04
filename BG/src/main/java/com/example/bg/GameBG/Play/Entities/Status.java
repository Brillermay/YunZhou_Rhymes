package com.example.bg.GameBG.Play.Entities;

import lombok.Data;

@Data
public class Status {
    /*
    * consistTime，int，持续回合
    * name，string，状态
    * */
    public Status(){}
    public Status(String name,int consistTime)
    {
        this.name=name;
        this.consistTime=consistTime;
    }
    private String name;
    private int consistTime;
}
