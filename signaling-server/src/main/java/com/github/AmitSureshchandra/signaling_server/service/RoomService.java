package com.github.AmitSureshchandra.signaling_server.service;

import org.springframework.stereotype.Service;

@Service
public class RoomService {
    public String getRandomRoomId() {
        return "room" + (int) (Math.random() * 100000);
    }
}
