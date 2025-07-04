package com.example.bg.GameBG.Play;


import com.example.bg.GameBG.Play.Service.MainService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "/multiController")
public class MultiController {

    MainService mainService = new MainService();
    @GetMapping("/init/{userId}")
    @Operation(summary = "初始化房间")
    public String InitRooms(@PathVariable int userId){
        return mainService.CreateRoom(userId);
    }

    @GetMapping("/joinRoom/{roomId}/{userId}")
    @Operation(summary = "通过房间号和uid加入房间")
    public String JoinRooms(@PathVariable String roomId,@PathVariable int userId){
        return mainService.JoinRoom(roomId,userId);
    }

    @GetMapping("/startGame/{roomId}/{role1}/{role2}")
    @Operation(summary = "发送开始信号，包括房间号")
    public String StartGame(@PathVariable String roomId,@PathVariable String role1,@PathVariable String role2)
    {
        return mainService.StartGame(roomId,role1,role2);
    }

    @PostMapping("/synthesizeCards")
    @Operation(summary = "合成卡牌，传三个参数。和用户id即可")
    public void SynthesizeCards(@RequestBody Map<String,String>request){
        String cardA=request.get("cardA");
        String cardB=request.get("cardB");
        String cardC=request.get("cardC");
        int uid= Integer.parseInt(request.get("uid"));
        mainService.SynthesizeCards(uid,cardA,cardB,cardC);
    }
}
