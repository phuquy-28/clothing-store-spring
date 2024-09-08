package com.example.clothingstore.constant;

public interface ErrorMessage {

  String ACCESS_TOKEN_INVALID = "Token không hợp lệ (hết hạn, không đúng định dạng, hoặc không truyền JWT ở header)...";
  String ACCESS_DENIED = "Bạn không có quyền truy cập vào tài nguyên này";
  String REFRESH_TOKEN_INVALID = "Refresh token không hợp lệ";
  String EMAIL_INVALID = "Email không hợp lệ";
  String ACTIVATION_TOKEN_INVALID = "Mã kích hoạt không hợp lệ";
  String RESET_TOKEN_INVALID = "Mã reset password không hợp lệ hoặc đã hết hạn";
  String PASSWORD_NOT_MATCH = "Mật khẩu không khớp";
  String USERNAME_OR_PASSWORD_INVALID = "Tên đăng nhập hoặc mật khẩu không hợp lệ";
}
