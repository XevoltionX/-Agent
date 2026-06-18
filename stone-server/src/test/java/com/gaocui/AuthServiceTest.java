package com.gaocui;

import com.gaocui.entity.Merchant;
import com.gaocui.mapper.MerchantMapper;
import com.gaocui.service.AuthService;
import com.gaocui.service.EmailService;
import com.gaocui.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class AuthServiceTest {

    @Mock private MerchantMapper merchantMapper;
    @Mock private EmailService emailService;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private JwtUtil jwtUtil;
    @Mock private ValueOperations<String, String> valueOps;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(merchantMapper, emailService, redisTemplate, jwtUtil);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void sendCode_shouldStoreInRedis() {
        authService.sendCode("test@test.com", "LOGIN");
        verify(valueOps).set(eq("code:LOGIN:test@test.com"), anyString(), eq(120L), any());
        verify(emailService).sendCode(eq("test@test.com"), anyString());
    }

    @Test
    void login_backdoorCode_shouldPass() {
        when(merchantMapper.selectOne(any())).thenReturn(null);
        when(merchantMapper.insert(any())).thenAnswer(inv -> {
            com.gaocui.entity.Merchant m = inv.getArgument(0);
            m.setId(1L); // 模拟自增ID
            return 1;
        });
        when(jwtUtil.generate(eq(1L), eq("test@test.com"), eq(false))).thenReturn("jwt_token");

        Map<String, Object> result = authService.login("test@test.com", "PPPPPP");

        assertNotNull(result.get("token"));
        assertFalse((Boolean) result.get("isVip"));
    }

    @Test
    void login_wrongCode_shouldThrow() {
        when(valueOps.get("code:LOGIN:test@test.com")).thenReturn("123456");

        assertThrows(com.gaocui.common.BizException.class, () ->
            authService.login("test@test.com", "000000")
        );
    }

    @Test
    void login_existingMerchant_shouldReturnVipStatus() {
        when(valueOps.get("code:LOGIN:vip@test.com")).thenReturn("123456");
        Merchant m = new Merchant(); m.setId(1L); m.setEmail("vip@test.com"); m.setIsVip(1);
        when(merchantMapper.selectOne(any())).thenReturn(m);
        when(jwtUtil.generate(1L, "vip@test.com", true)).thenReturn("jwt_token");

        Map<String, Object> result = authService.login("vip@test.com", "123456");

        assertTrue((Boolean) result.get("isVip"));
        verify(jwtUtil).generate(1L, "vip@test.com", true);
    }
}
