package com.ephimero.main;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Set;

@RestController
public class StatusController {

    @Operation(summary = "Lista los hashes de usuarios conectados actualmente")
    @GetMapping("/api/status")
    public Set<String> getConnectedUsers() {
        return EphimeroHandler.sessions.keySet();
    }
}