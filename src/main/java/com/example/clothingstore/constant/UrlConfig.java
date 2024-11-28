package com.example.clothingstore.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UrlConfig {

	public static String API_VERSION;

	@Value("${api.version}")
	public void setApiVersion(String apiVersion) {
		API_VERSION = "/" + apiVersion;
	}

	// Common
	public static final String ID = "/{id}";

	// Public endpoints
	public static final String ROOT = "/";
	public static final String API_DOCS = "/v3/api-docs/**";
	public static final String SWAGGER_UI = "/swagger-ui/**";
	public static final String SWAGGER_UI_HTML = "/swagger-ui.html";

	// Auth controller
	public static final String AUTH = "/auth";
	public static final String REGISTER = "/register";
	public static final String LOGIN = "/login";
	public static final String LOGOUT = "/logout";
	public static final String REFRESH = "/refresh";
	public static final String SEND_ACTIVATION_EMAIL = "/send-activation-email";
	public static final String ACTIVATE = "/activate";
	public static final String RECOVER_PASSWORD = "/recover-password";
	public static final String RESET_PASSWORD = "/reset-password";

	// Product controller
	public static final String PRODUCT = "/products";
	public static final String PRODUCT_ID = "/ids";
	public static final String UPLOAD_IMAGES = "/upload-images";
	public static final String PRODUCT_SLUG = "/{slug}";
	// Category controller
	public static final String CATEGORY = "/categories";

	// User controller
	public static final String USER = "/users";
	public static final String PROFILE = "/profile";
	public static final String EDIT_PROFILE = "/edit-profile";
	public static final String CHANGE_PASSWORD = "/change-password";
	public static final String INFO = "/info";

	// Shipping profile controller
	public static final String SHIPPING_PROFILE = "/shipping-profiles";
	public static final String DEFAULT = "/default";

	// Order controller
	public static final String ORDERS = "/orders";
	public static final String PREVIEW = "/preview";
	public static final String CHECK_OUT = "/check-out";
	public static final String MY_ORDERS = "/my-orders";
	public static final String LINE_ITEM = "/line-items";
	public static final String ORDER_ID = "/{orderId}";
	public static final String STATUS = "/status";

	// Review controller
  public static final String REVIEW = "/reviews";

	// Payment controller
	public static final String PAYMENT = "/payment";
	public static final String VNPAY_RETURN = "/vnpay_return";

	// Promotion controller
	public static final String PROMOTION = "/promotions";

  // Cart controller
  public static final String CART = "/carts";
  public static final String ITEMS = "/items";

	// Full paths for public endpoints
	public static String[] PUBLIC_ENDPOINTS() {
		return new String[]{
			ROOT,
			API_DOCS,
			SWAGGER_UI,
			SWAGGER_UI_HTML
		};
	}

	// Full paths for public GET endpoints
	public static String[] PUBLIC_GET_ENDPOINTS() {
		return new String[]{
			API_VERSION + AUTH + ACTIVATE + "/**",
			API_VERSION + AUTH + SEND_ACTIVATION_EMAIL + "/**",
			API_VERSION + AUTH + RESET_PASSWORD + "/**",
			API_VERSION + AUTH + REFRESH,
			API_VERSION + PRODUCT,
			API_VERSION + PRODUCT + PRODUCT_SLUG,
			API_VERSION + PRODUCT + PRODUCT_ID + ID,
			API_VERSION + CATEGORY,
			API_VERSION + USER,
			API_VERSION + PAYMENT,
			API_VERSION + PAYMENT + VNPAY_RETURN
		};
	}

	// Full paths for public POST endpoints
	public static String[] PUBLIC_POST_ENDPOINTS() {
		return new String[]{
			API_VERSION + AUTH + LOGIN,
			API_VERSION + AUTH + REGISTER,
			API_VERSION + AUTH + RECOVER_PASSWORD,
			API_VERSION + ORDERS,
			API_VERSION + ORDERS + CHECK_OUT
		};
	}

	// Full paths for public PUT endpoints
	public static String[] PUBLIC_PUT_ENDPOINTS() {
		return new String[]{
			// Add any public PUT endpoints here if needed
		};
	}
}
