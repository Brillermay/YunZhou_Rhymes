package com.example.bg.GameBG.Player;


/*
* 增删查改
* */

import java.util.List;

public interface PlayerOpMapper {
    void PlayerAdd(int UID);
    void PlayerDel(int UID,int PID);
    List<Player> PlayerFind(int UID);
}
