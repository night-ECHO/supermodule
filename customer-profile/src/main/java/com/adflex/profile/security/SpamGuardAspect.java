package com.adflex.profile.security;



import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


// Lưu ý: Để parse body JSON tìm sdt thì cần logic phức tạp hơn chút (dùng ArgumentResolver),
// nhưng ở đây tôi demo cách check IP trước cho gọn.

@Aspect
@Component
@RequiredArgsConstructor
public class SpamGuardAspect {

    private final SpamProtectionService spamService;

    @Before("@annotation(spamGuard)") // Chạy trước bất kỳ hàm nào có gắn @SpamGuard
    public void checkSpam(JoinPoint joinPoint, SpamGuard spamGuard) {
        // 1. Lấy Request hiện tại
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ip = getClientIp(request);

        // 2. Check Rate Limit (IP)
        spamService.validateRateLimit(ip, spamGuard.maxRequests());

        // 3. (Mở rộng) Nếu cần check SĐT, bạn có thể lấy tham số từ joinPoint.getArgs()
        // Ví dụ: Nếu tham số đầu tiên là DTO có field 'sdt'
        /*
        if (spamGuard.checkPhone()) {
            Object[] args = joinPoint.getArgs();
            if (args.length > 0 && args[0] instanceof HasPhoneNumber dto) {
                spamService.validatePhoneNumber(dto.getPhone());
            }
        }
        */
    }

    // Hàm lấy IP chuẩn (xuyên qua Proxy/Cloudflare)
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
