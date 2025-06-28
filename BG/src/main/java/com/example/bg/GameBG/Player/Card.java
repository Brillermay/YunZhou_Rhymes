package com.example.bg.GameBG.Player;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
public class Card {
    public String CardType;
    public int CardNum; // 新增数量字段
    public boolean shouldInsert() {
        return CardNum > 0;
    }
    public boolean shouldDelete() {
        return CardNum <= 0;
    }
}
