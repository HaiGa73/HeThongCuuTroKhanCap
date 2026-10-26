package com.rescue.disaster.service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Slf4j
@Service
public class OtpService {

    private final JavaMailSender mailSender;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${spring.mail.username:noreply@rescue.vn}")
    private String senderEmail;

    @Autowired
    public OtpService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // Sinh mã OTP 6 số
    public String generateOtpCode() {
        int code = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(code);
    }

    // Gửi email HTML chứa mã OTP
    public void sendOtp(String targetEmail, String username, String otpCode) {
        log.info(">> Đang chuẩn bị gửi OTP tới email: {}", targetEmail);

        if (targetEmail == null || targetEmail.trim().isEmpty()) {
            log.warn(">> Email người nhận rỗng, bỏ qua gửi mail.");
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(senderEmail, "Hệ Thống Cứu Nạn Thiên Tai");
            helper.setTo(targetEmail.trim());
            helper.setSubject("🚨 [Xác Thực Tài Khoản] Mã OTP Kích Hoạt");

            // Template HTML gửi đến hòm thư người dùng
            String htmlContent = "<div style=\"font-family: Arial, sans-serif; max-width: 520px; margin: 0 auto; border: 1px solid #ddd; border-radius: 8px; overflow: hidden;\">"
                    + "<div style=\"background-color: #dc3545; color: white; padding: 18px; text-align: center;\">"
                    + "<h2 style=\"margin: 0; font-size: 20px;\">🚨 HỆ THỐNG CỨU NẠN THIÊN TAI</h2>"
                    + "</div>"
                    + "<div style=\"padding: 24px; color: #333;\">"
                    + "<p>Xin chào <strong>" + username + "</strong>,</p>"
                    + "<p>Cảm ơn bạn đã đăng ký tài khoản tham gia mạng lưới ứng cứu và kết nối người thân. Dưới đây là mã xác thực OTP của bạn:</p>"
                    + "<div style=\"background-color: #f8f9fa; border: 2px dashed #dc3545; border-radius: 6px; text-align: center; padding: 16px; margin: 20px 0;\">"
                    + "<span style=\"font-size: 32px; font-weight: bold; letter-spacing: 10px; color: #dc3545;\">" + otpCode + "</span>"
                    + "</div>"
                    + "<p style=\"color: #666; font-size: 13px;\">⚠️ <i>Mã này có hiệu lực trong vòng <strong>5 phút</strong>. Tuyệt đối không cung cấp mã này cho người khác.</i></p>"
                    + "<hr style=\"border: none; border-top: 1px solid #eee; margin: 20px 0;\">"
                    + "<p style=\"font-size: 12px; color: #888; text-align: center;\">Đây là email tự động, vui lòng không phản hồi thư này.</p>"
                    + "</div>"
                    + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
            log.info(">> ĐÃ GỬI EMAIL THÀNH CÔNG TỚI: {}", targetEmail);

        } catch (Exception e) {
            log.error(">> Lỗi khi gửi email SMTP: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể gửi email xác thực: " + e.getMessage());
        }
    }
}