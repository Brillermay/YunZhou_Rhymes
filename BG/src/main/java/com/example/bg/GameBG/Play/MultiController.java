package com.example.bg.GameBG.Play;


import com.example.bg.GameBG.Play.Service.MainService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
