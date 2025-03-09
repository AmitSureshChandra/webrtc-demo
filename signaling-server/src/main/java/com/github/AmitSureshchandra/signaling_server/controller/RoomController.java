package com.github.AmitSureshchandra.signaling_server.controller;

import com.github.AmitSureshchandra.signaling_server.dto.MsgResp;
import com.github.AmitSureshchandra.signaling_server.service.RoomService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
@RequestMapping("/room")
@CrossOrigin(origins = "*")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    public MsgResp createRoom() {
        return new MsgResp(roomService.getRandomRoomId());
    }
}
