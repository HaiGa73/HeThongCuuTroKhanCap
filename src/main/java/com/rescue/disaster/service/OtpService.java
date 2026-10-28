package com.rescue.disaster.service;

import com.rescue.disaster.entity.User;
import com.rescue.disaster.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class OtpService {

    private final JavaMailSender mailSender;
    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired(required = false)
    private UserRepository userRepository;

    @Value("${spring.mail.username:noreply@rescue.vn}")
    private String senderEmail;

    // Bộ nhớ lưu tạm OTP: key = username, value = thông tin mã + hạn 5 phút
    private final Map<String, OtpEntry> otpCache = new ConcurrentHashMap<>();

    private static class OtpEntry {
        final String code;
        final LocalDateTime expiresAt;

        OtpEntry(String code, LocalDateTime expiresAt) {
            this.code = code;
            this.expiresAt = expiresAt;
        }
    }

    @Autowired
    public OtpService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // 1. Sinh chuỗi ngẫu nhiên 6 chữ số
    public String generateOtpCode() {
        int code = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(code);
    }

    // 2. Tạo mã và lưu vào bộ nhớ đệm (hạn 5 phút)
    public String generateOtp(String username) {
        String code = generateOtpCode();
        otpCache.put(username, new OtpEntry(code, LocalDateTime.now().plusMinutes(5)));
        
        // Luôn in ra console để nhà phát triển copy test ngay khi không có mạng/lỗi SMTP
        log.info("=================================================");
        log.info(">> [OTP LOCAL DEBUG] Mã OTP của tài khoản '{}' là: {}", username, code);
        log.info("=================================================");
        return code;
    }

    // 3. Hàm gọi từ AuthService: Tự tìm email và bắn OTP
    public void generateAndSendOtp(String username) {
        String targetEmail = null;
        if (userRepository != null) {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null && user.getEmail() != null && !user.getEmail().isBlank()) {
                targetEmail = user.getEmail();
            }
        }
        generateAndSendOtp(username, targetEmail);
    }

    // 4. Sinh mã và gửi mail nếu có email hợp lệ
    public void generateAndSendOtp(String username, String targetEmail) {
        String code = generateOtp(username);
        if (targetEmail != null && !targetEmail.trim().isEmpty()) {
            try {
                sendOtp(targetEmail, username, code);
            } catch (Exception e) {
                log.warn(">> Không thể gửi email thực tế qua SMTP (vẫn có thể lấy mã tại Console): {}", e.getMessage());
            }
        } else {
            log.warn(">> Tài khoản '{}' chưa có địa chỉ email, chỉ lưu OTP tại bộ nhớ hệ thống.", username);
        }
    }

    // 5. Kiểm tra mã OTP người dùng nhập vào
    public boolean validateOtp(String username, String otpInput) {
        if (username == null || otpInput == null) {
            return false;
        }
        OtpEntry entry = otpCache.get(username);
        if (entry == null) {
            log.warn(">> Không tìm thấy OTP cho tài khoản '{}'", username);
            return false;
        }
        if (LocalDateTime.now().isAfter(entry.expiresAt)) {
            otpCache.remove(username);
            log.warn(">> Mã OTP của '{}' đã hết hạn 5 phút", username);
            return false;
        }
        boolean isMatch = entry.code.equals(otpInput.trim());
        log.info(">> Kiểm tra OTP cho '{}': Nhập [{}], Mã đúng [{}], Khớp: {}", username, otpInput.trim(), entry.code, isMatch);
        return isMatch;
    }

    // 6. Xóa OTP sau khi đổi mật khẩu / kích hoạt thành công
    public void clearOtp(String username) {
        if (username != null) {
            otpCache.remove(username);
        }
    }

    // 7. Gửi email HTML chứa mã OTP qua SMTP
    public void sendOtp(String targetEmail, String username, String otpCode) {
        log.info(">> Đang chuẩn bị gửi OTP tới email: {}", targetEmail);

        if (targetEmail == null || targetEmail.trim().isEmpty()) {
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(senderEmail, "Hệ Thống Cứu Nạn Thiên Tai");
            helper.setTo(targetEmail.trim());
            helper.setSubject("🚨 [Xác Thực Tài Khoản] Mã OTP Kích Hoạt");

            String htmlContent = "<div style=\"font-family: Arial, sans-serif; max-width: 520px; margin: 0 auto; border: 1px solid #ddd; border-radius: 8px; overflow: hidden;\">"
                    + "<div style=\"background-color: #dc3545; color: white; padding: 18px; text-align: center;\">"
                    + "<h2 style=\"margin: 0; font-size: 20px;\">🚨 HỆ THỐNG CỨU NẠN THIÊN TAI</h2>"
                    + "</div>"
                    + "<div style=\"padding: 24px; color: #333;\">"
                    + "<p>Xin chào <strong>" + username + "</strong>,</p>"
                    + "<p>Mã xác thực OTP của bạn là:</p>"
                    + "<div style=\"background-color: #f8f9fa; border: 2px dashed #dc3545; border-radius: 6px; text-align: center; padding: 16px; margin: 20px 0;\">"
                    + "<span style=\"font-size: 32px; font-weight: bold; letter-spacing: 10px; color: #dc3545;\">" + otpCode + "</span>"
                    + "</div>"
                    + "<p style=\"color: #666; font-size: 13px;\">⚠️ <i>Mã này có hiệu lực trong vòng <strong>5 phút</strong>. Tuyệt đối không chia sẻ mã này.</i></p>"
                    + "<hr style=\"border: none; border-top: 1px solid #eee; margin: 20px 0;\">"
                    + "<p style=\"font-size: 12px; color: #888; text-align: center;\">Đây là email tự động, vui lòng không phản hồi thư này.</p>"
                    + "</div>"
                    + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
            log.info(">> ĐÃ GỬI EMAIL THÀNH CÔNG TỚI: {}", targetEmail);
        } catch (Exception e) {
            log.error(">> Lỗi khi gửi email SMTP: {}", e.getMessage());
            throw new RuntimeException("Không thể gửi email xác thực: " + e.getMessage());
        }
    }
}