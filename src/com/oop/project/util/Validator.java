package com.oop.project.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * Validator — "Hộp đồ nghề kiểm tra dữ liệu" duy nhất của toàn dự án
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * File này có HAI vai trò gộp làm một:
 *
 *   1. CONSTANTS (hằng số)  — các Pattern regex và giới hạn số được dùng
 *                             bởi Service Layer để validate nghiệp vụ.
 *
 *   2. STATIC METHODS       — các hàm trả về boolean được dùng bởi UI Layer
 *                             để kiểm tra dữ liệu nhập từ người dùng ngay lập tức.
 *
 * Nguyên tắc DRY (Don't Repeat Yourself): Tất cả logic kiểm tra dữ liệu
 * tập trung tại đây, không file nào khác tự định nghĩa regex riêng.
 *
 * Cách dùng:
 *   - UI   : Validator.isValidEmail("abc@gmail.com")  → true/false
 *   - Service: Validator.EMAIL_PATTERN.matcher(email).matches()
 *
 * @author Member 1 (methods) + Member 3 (constants) — merged
 */
public class Validator {

    // =========================================================================
    // PHẦN 1: CONSTANTS — Dùng bởi Service Layer
    // =========================================================================

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

    // ── Username ──────────────────────────────────────────────────────────────
    /** Username: chỉ chữ cái, số và dấu gạch dưới, 3–50 ký tự. */
    public static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_]{3,50}$"
    );
    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MAX_USERNAME_LENGTH = 50;

    // ── Password ──────────────────────────────────────────────────────────────
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_PASSWORD_LENGTH = 50;

    // ── Customer name ─────────────────────────────────────────────────────────
    public static final int MIN_NAME_LENGTH = 2;
    public static final int MAX_NAME_LENGTH = 100;

    // ── SKU ───────────────────────────────────────────────────────────────────
    /**
     * SKU sản phẩm: chữ in hoa, số, dấu gạch ngang; 3–20 ký tự.
     * Ví dụ: SHIRT-001, PROD-ABC-123
     */
    public static final Pattern SKU_PATTERN = Pattern.compile(
        "^[A-Z0-9-]{3,20}$"
    );
    public static final int MIN_SKU_LENGTH = 3;
    public static final int MAX_SKU_LENGTH = 20;

    // ── Price ─────────────────────────────────────────────────────────────────
    public static final double MIN_PRICE = 0.01;
    public static final double MAX_PRICE = 999_999.99;

    // ── Quantity ──────────────────────────────────────────────────────────────
    public static final int MIN_QUANTITY = 1;
    public static final int MAX_QUANTITY = 9999;

    // ── Coupon code ───────────────────────────────────────────────────────────
    /**
     * Mã coupon: chỉ chữ in hoa và số; 4–20 ký tự.
     * Ví dụ: SAVE10, DISCOUNT2024
     */
    public static final Pattern COUPON_CODE_PATTERN = Pattern.compile(
        "^[A-Z0-9]{4,20}$"
    );
    public static final int MIN_COUPON_LENGTH = 4;
    public static final int MAX_COUPON_LENGTH = 20;

    // ── Order status ──────────────────────────────────────────────────────────
    public static final String[] VALID_ORDER_STATUSES = {
        "PENDING", "PAID", "CANCELLED"
    };

    // ── Discount type ─────────────────────────────────────────────────────────
    public static final String[] VALID_DISCOUNT_TYPES = {
        "Percent", "Fixed"
    };

    // ── Permission actions ────────────────────────────────────────────────────
    /** Các hành động chỉ ADMIN mới được thực hiện. */
    public static final String[] ADMIN_ONLY_ACTIONS = {
        "UPDATE_PRICE",
        "DELETE_ORDER",
        "DELETE_CUSTOMER",
        "CREATE_USER",
        "DELETE_COUPON"
    };

    /** Các hành động tất cả user đã đăng nhập đều được thực hiện. */
    public static final String[] USER_ACTIONS = {
        "CREATE_ORDER",
        "VIEW_ORDERS",
        "UPDATE_CUSTOMER",
        "SEARCH_ITEMS"
    };

    // =========================================================================
    // PHẦN 2: STATIC METHODS — Dùng bởi UI Layer
    // =========================================================================

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

    /**
     * Kiểm tra password đủ độ dài tối thiểu (6 ký tự).
     */
    public static boolean isValidPassword(String password) {
        if (checkEmpty(password)) return false;
        return password.length() >= MIN_PASSWORD_LENGTH
            && password.length() <= MAX_PASSWORD_LENGTH;
    }

    /**
     * Kiểm tra chuỗi số lượng hợp lệ (số nguyên dương).
     * UI dùng để validate ô nhập số lượng sản phẩm.
     */
    public static boolean isValidQuantity(String str) {
        if (checkEmpty(str)) return false;
        try {
            int quantity = Integer.parseInt(str.trim());
            return quantity >= MIN_QUANTITY && quantity <= MAX_QUANTITY;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Kiểm tra chuỗi tiền tệ hợp lệ (số thực dương).
     * UI dùng để validate ô nhập giá sản phẩm.
     */
    public static boolean isValidMoney(String str) {
        if (checkEmpty(str)) return false;
        try {
            BigDecimal money = new BigDecimal(str.trim());
            return money.compareTo(BigDecimal.valueOf(MIN_PRICE)) >= 0
                && money.compareTo(BigDecimal.valueOf(MAX_PRICE)) <= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Kiểm tra định dạng SKU hợp lệ.
     * UI dùng để validate ô nhập SKU sản phẩm.
     */
    public static boolean isValidSku(String sku) {
        if (checkEmpty(sku)) return false;
        return SKU_PATTERN.matcher(sku.trim()).matches();
    }

    /**
     * Kiểm tra định dạng mã coupon hợp lệ.
     */
    public static boolean isValidCouponCode(String code) {
        if (checkEmpty(code)) return false;
        return COUPON_CODE_PATTERN.matcher(code.trim()).matches();
    }

    // Constructor private — đây là utility class, không được khởi tạo
    private Validator() {
        throw new AssertionError("Validator là utility class, không khởi tạo được.");
    }
}