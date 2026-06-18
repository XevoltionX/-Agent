package com.gaocui.controller;

import com.gaocui.common.Result;
import com.gaocui.dto.LoginDTO;
import com.gaocui.service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/send-code")
    public Result<?> sendCode(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String type = body.getOrDefault("type", "LOGIN");
        authService.sendCode(email, type);
        return Result.ok();
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginDTO dto) {
        return Result.ok(authService.login(dto.getEmail(), dto.getCode()));
    }

    @GetMapping("/me")
    public Result<?> me(@RequestAttribute("merchantId") Long merchantId) {
        return Result.ok(authService.getMe(merchantId));
    }
}
