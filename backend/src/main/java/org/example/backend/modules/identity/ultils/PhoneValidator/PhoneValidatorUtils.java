package org.example.backend.modules.identity.ultils.PhoneValidator;

public class PhoneValidatorUtils {
    private static final String PHONE_REGEX = "^(0|+84)(3|5|7|8|9)\\d{8}";
    public static boolean isValid(String phone) {
        if (phone == null || phone.isBlank()) {
            return false;
        }
        return phone.matches(PHONE_REGEX);
    }
}
