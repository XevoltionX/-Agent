package com.gaocui.service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    @org.springframework.beans.factory.annotation.Value("${spring.mail.username}")
    private String mailFrom;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendCode(String to, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(to);
            helper.setSubject("高翠网 - 验证码");
            helper.setText(
                "<div style='font-family:sans-serif;padding:20px'>" +
                "<h2 style='color:#056f35'>高翠网</h2>" +
                "<p>您的验证码为：</p>" +
                "<h1 style='color:#056f35;letter-spacing:4px'>" + code + "</h1>" +
                "<p style='color:#999'>验证码5分钟内有效，请勿泄露。</p>" +
                "</div>", true);
            mailSender.send(message);
            log.info("验证码已发送至 {}", to);
        } catch (Exception e) {
            log.error("邮件发送失败: {}", e.getMessage());
            // 发送失败不中断流程，验证码仍存数据库
        }
    }
}
