package org.example.backend.common;

public final class MessageKeys {

    private MessageKeys() {
        throw new UnsupportedOperationException();
    }

    //info messages
    public static final String INFO_USER_LOGIN = "info.user.login";
    public static final String INFO_USER_CREATED = "info.user.created";

    //exception messages
    public static final String ERROR_UNKNOWN = "error.unknown";
    public static final String ERROR_UNAUTHORIZED = "error.unauthorized";
    public static final String ERROR_UNAUTHORIZED_DETAILS = "error.unauthorized.details";
    public static final String ERROR_AUTH_SETUP = "error.auth.setup";
    public static final String ERROR_PHONE_NOT_FOUND = "error.phone.not.found";
    public static final String ERROR_JWT_INVALID_SIGNATURE = "error.jwt.invalid.signature";
    public static final String ERROR_JWT_INVALID_TOKEN = "error.jwt.invalid.token";
    public static final String ERROR_JWT_EXPIRED = "error.jwt.expired";
    public static final String ERROR_JWT_UNSUPPORTED = "error.jwt.unsupported";
    public static final String ERROR_JWT_EMPTY_CLAIMS = "error.jwt.empty.claims";
    public static final String ERROR_VALIDATION = "error.validation";
    public static final String ERROR_METHOD_ARGUMENT = "error.method.argument";
    public static final String ERROR_FIELD_VALIDATION = "error.field.validation";
    public static final String ERROR_ALREADY_EXISTS = "error.already.exists";
    public static final String ERROR_PHONE_EXISTS = "error.phone.exists";
    public static final String ERROR_EMAIL_EXISTS = "error.email.exists";
    public static final String ERROR_NOT_FOUND = "error.not.found";
    public static final String ERROR_NO_RECORDS = "error.no.records";
    public static final String ERROR_USERNAME_NOT_FOUND =  "error.username.not.found";
}
