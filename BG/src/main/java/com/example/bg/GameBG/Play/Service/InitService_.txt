package com.example.bg.GameBG.Play.Service;

import com.example.bg.GameBG.Play.Entities.*;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;

public class InitService {
    /*
    * 处理初始化相关的函数
    * 创建房间
    * 加入房间
    * 初始化用户
    * */
    /**
     * 创建新的游戏房间
     * @param uid1 房主用户ID
     * @return 创建的房间对象，包含随机生成的16位房间号，房主ID已设置，等待第二个玩家加入
     */
    public Room CreateRoom(int uid1) {
        Room now = new Room();
        now.setRoom(RandomStringUtils.randomAlphanumeric(16));
        now.setUid1(uid1);
        now.setUid2(-1);
        now.setRoundNum(0);
        return now;
    }

    /**
     * 第二个玩家加入房间
     * @param now 要加入的房间对象（注意：此方法会直接修改传入的房间对象）
     * @param uid2 第二个玩家的用户ID
     */
    public void joinRoom(Room now, int uid2) {
        now.setUid2(uid2);
    }

    /**
     * 初始化玩家对战状态
     * @param roomId 房间ID
     * @param userId 玩家用户ID
     * @param role 玩家角色
     * @return 初始化完成的玩家对战对象，包含默认的血量、护盾、金币等属性
     */
    public PlayerAgainst init(String roomId, int userId, String role) {
        PlayerAgainst playerAgainst = new PlayerAgainst();
        playerAgainst.setHp(20);
        playerAgainst.setHpMax(20);
        playerAgainst.setShieldMax(10);
        playerAgainst.setRoomNumber(roomId);
        List<CardBattle> cardList = new ArrayList<>();
        playerAgainst.setCards(cardList);
        playerAgainst.setWealthy(0);
        List<Status> statuses = new ArrayList<>();
        playerAgainst.setStatusesBegin(statuses);
        playerAgainst.setStatusesEnd(statuses);
        playerAgainst.setRole(role);
        playerAgainst.setShield(10);
        playerAgainst.setPlayerCondition(new PlayerCondition());
        return playerAgainst;
    }
}
