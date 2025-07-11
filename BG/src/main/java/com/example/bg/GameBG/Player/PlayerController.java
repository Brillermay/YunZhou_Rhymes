package com.example.bg.GameBG.Player;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/playerData")
public class PlayerController {
    @Autowired
    private PlayerService playerService ;
    @GetMapping("/test")
    public void tester(){

    }

    @PostMapping("/loadTemp")
    public ArrayList<Player>loadTemp(@RequestBody Map<String, Integer> request) {
        //{
        //    "UID":1,
        //    "PID":2
        //}
        int UID=request.get("UID"),PID=request.get("PID");
        return playerService.getPlayers(UID,PID);
    }

    @PostMapping("/WriteTemp")
    public void saveTemp(@RequestBody @Valid PlayerDTO request) {
        //{
        //        "achievements": "",
        //        "gold": 100,
        //        "UID": 1,
        //        "cardList": [
        //            {
        //                "cardNum": 2,
        //                "cardType": "lel"
        //            }
        //        ],
        //        "lastPlayTime": "28-06-2025 11:55:51",
        //        "pid": 2,
        //        "uid": 1
        //}
        playerService.updateData(request);
    }
}
