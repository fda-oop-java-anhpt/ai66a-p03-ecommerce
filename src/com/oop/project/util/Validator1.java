package com.oop.project.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;
public class Validator1 {
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(,+)$";
    private static final String PHONE_PATTERN = "^\\d{10,11}$";
    private static final String PASSWORD_PATTERN = "^.{6,}$";
    public static boolean checkEmpty(String str){
        return str == null || str.trim().isEmpty();
    }
    public static boolean isValidEmail(String email){
        if (checkEmpty(email)) return false;
        return Pattern.matches(EMAIL_PATTERN, email);
    }
    public static boolean isValidPhone(String phone){
        if (checkEmpty(phone)) return false;
        return Pattern.matches(PHONE_PATTERN, phone);
    }
    public static boolean isValidPassword(String password){
        if (checkEmpty(password)) return false;
        return Pattern.matches(PASSWORD_PATTERN, password);
    }
    public static boolean isValidQuantity(String str){
        if (checkEmpty(str)) return false;
        try {
            int quantity = Integer.parseInt(str);
            return quantity > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    
    }
    public static boolean isValidMoney(String str){
        if (checkEmpty(str)) return false;
        try {
            BigDecimal money = new BigDecimal(str);
            return money.compareTo(BigDecimal.ZERO) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
