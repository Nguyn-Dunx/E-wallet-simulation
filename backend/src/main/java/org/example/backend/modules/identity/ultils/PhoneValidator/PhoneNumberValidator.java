package org.example.backend.modules.identity.ultils.PhoneValidator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return PhoneValidatorUtils.isValid(value);
    }
}
