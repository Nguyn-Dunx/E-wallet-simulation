import axios from "axios";

const RESET_API_BASE = "http://localhost:8080/api/v1";

const resetApi = axios.create({
  baseURL: RESET_API_BASE,
  headers: {
    "Content-Type": "application/json",
  },
});

const getErrorMessage = (error, fallback = "Đã có lỗi xảy ra") =>
  error?.response?.data?.message ||
  error?.response?.data?.error ||
  error?.message ||
  fallback;

export const resetPasswordApi = {
  createOtp: async (phone) => {
    try {
      const response = await resetApi.post(
        "/auth/users/reset-password/create-otp",
        {
          phone,
        },
      );
      return response.data;
    } catch (error) {
      throw new Error(getErrorMessage(error, "Không thể gửi OTP"));
    }
  },

  verifyOtp: async (phone, otp) => {
    try {
      const response = await resetApi.post(
        "/auth/users/reset-password/verify-otp",
        {
          phone,
          otp,
        },
      );
      return response.data;
    } catch (error) {
      throw new Error(getErrorMessage(error, "OTP không hợp lệ"));
    }
  },

  confirmResetPassword: async (token, newPassword) => {
    try {
      const response = await resetApi.post(
        "/auth/users/reset-password/confirm",
        {
          token,
          newPassword,
        },
      );
      return response.data;
    } catch (error) {
      throw new Error(getErrorMessage(error, "Không thể đặt lại mật khẩu"));
    }
  },
};
