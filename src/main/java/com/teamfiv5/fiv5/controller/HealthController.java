package com.teamfiv5.fiv5.controller;

import com.teamfiv5.fiv5.global.response.CustomResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @GetMapping("/health")
    public ResponseEntity<CustomResponse<String>> health() {
        return ResponseEntity.ok(CustomResponse.ok("ok"));
    }
}