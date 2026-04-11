package com.oop.project.util;

import java.util.regex.Pattern;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * Validator — "Hộp đồ nghề kiểm tra dữ liệu" duy nhất của toàn dự án
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * File này chứa các hàm trả về boolean được dùng bởi UI Layer và Service Layer
 * để kiểm tra dữ liệu nhập từ người dùng.
 *
 * @author Member 1 (methods) + Member 3 (constants) — merged
 */
public class Validator {

    // ── Email ─────────────────────────────────────────────────────────────────
    /**
     * Email regex chuẩn RFC 5322 (simplified).
     * Hợp lệ: user@domain.com, user.name+tag@sub.domain.org
     */
    public static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    // ── Phone ─────────────────────────────────────────────────────────────────
    /**
     * Số điện thoại Việt Nam.
     * Hợp lệ: 0912345678, 84912345678, +84912345678
     */
    public static final Pattern PHONE_PATTERN = Pattern.compile(
        "^(\\+?84|0)[0-9]{9,10}$"
    );

    /**
     * Kiểm tra chuỗi rỗng hoặc null.
     * UI dùng trước khi gọi bất kỳ hàm validate nào.
     *
     * @return true nếu rỗng/null
     */
    public static boolean checkEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Kiểm tra định dạng email hợp lệ.
     * Sử dụng EMAIL_PATTERN chuẩn (đã fix bug regex cũ).
     */
    public static boolean isValidEmail(String email) {
        if (checkEmpty(email)) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Kiểm tra số điện thoại Việt Nam hợp lệ.
     * Chấp nhận: 0912345678, +84912345678, 84912345678
     */
    public static boolean isValidPhone(String phone) {
        if (checkEmpty(phone)) return false;
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    // Constructor private — đây là utility class, không được khởi tạo
    private Validator() {
        throw new AssertionError("Validator là utility class, không khởi tạo được.");
    }
}