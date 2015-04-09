package com.vendormax.web.orderapp.api;

import android.util.Log;

import com.loopj.android.http.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by goku on 3/12/15.
 */
public class OrderAPI {
    public static String token = "";
    public static String user_id = "null";
    public static String customer_id = "null", customer_ids = "";
    public static String user_role = "admin";
    public static String customer_name = "", user_name = "", user_phone = "", logo_url = "";

    public static void postAuth(Map params, JsonHttpResponseHandler responseHandler) {
        RequestParams requestParams = new RequestParams();
        requestParams.put("email", params.get("email"));
        requestParams.put("password", params.get("password"));
        REST.post("sign_in", requestParams, responseHandler);
//        REST.post("/", requestParams, responseHandler);
    }

    public static void postSignup(Map params, JsonHttpResponseHandler responseHandler) {
        RequestParams requestParams = new RequestParams();
        requestParams.put("email", params.get("email"));
        requestParams.put("password", params.get("password"));
        requestParams.put("password_confirmation", params.get("retype"));
        requestParams.put("role", params.get("role"));
        if (!params.get("userid").equals(""))
            requestParams.put("user_id", params.get("userid"));
        ArrayList arrayList = (ArrayList) params.get("accountid");
        if (!arrayList.isEmpty()) {
            requestParams.put("cusnum", arrayList);
        }
        Log.d("ACCount", requestParams.toString());
        REST.post("sign_up", requestParams, responseHandler);
    }

    public static void postAccount(Map params, JsonHttpResponseHandler responseHandler) {
        RequestParams requestParams = new RequestParams();
        requestParams.put("auth_token", params.get("auth_token"));
        requestParams.put("user_id", params.get("user_id"));
        requestParams.put("account_id", params.get("account_id"));
        Log.d("ACCount", requestParams.toString());
        REST.post("order", requestParams, responseHandler);
    }

    public static void postProducts(Map params, JsonHttpResponseHandler responseHandler) {
        RequestParams requestParams = new RequestParams();
        requestParams.put("auth_token", params.get("auth_token"));
        requestParams.put("user_id", params.get("user_id"));
        requestParams.put("account_id", params.get("account_id"));
        Log.d("ACount", requestParams.toString());
        REST.post("getproducts", requestParams, responseHandler);
    }

    public static void postOrders(Map params, JsonHttpResponseHandler responseHandler) {
        RequestParams requestParams = new RequestParams();
        requestParams.put("auth_token", params.get("auth_token"));
        requestParams.put("user_id", params.get("user_id"));
        requestParams.put("account_id", params.get("account_id"));
        Log.d("ACount", requestParams.toString());
        REST.post("getorders", requestParams, responseHandler);
    }

    public static void postCheckedProduct(Map params, JsonHttpResponseHandler responseHandler) {
        RequestParams requestParams = new RequestParams();
        requestParams.put("auth_token", params.get("auth_token"));
        requestParams.put("user_id", params.get("user_id"));
        requestParams.put("account_id", params.get("account_id"));
        ArrayList arrayList = (ArrayList) params.get("orders");
        requestParams.put("orders", arrayList);
        Log.d("abcd", requestParams.toString());
        REST.post("save", requestParams, responseHandler);
    }

    public static void postPurchase(Map params, JsonHttpResponseHandler responseHandler) {
        RequestParams requestParams = new RequestParams();
        requestParams.put("auth_token", params.get("auth_token"));
        requestParams.put("user_id", params.get("user_id"));
        requestParams.put("account_id", params.get("account_id"));
        requestParams.put("po", params.get("purchase_order"));
        requestParams.put("message", params.get("message"));
        requestParams.put("date", params.get("delivery_date"));
        ArrayList arrayList = (ArrayList) params.get("list");
        requestParams.put("payload", arrayList);
        Log.d("Purchase", requestParams.toString());
        REST.post("confirm_orders", requestParams, responseHandler);
    }

    public static void postSignout(Map params, JsonHttpResponseHandler responseHandler) {
        RequestParams requestParams = new RequestParams();
        requestParams.put("auth_token", params.get("auth_token"));
        REST.post("sign_out", requestParams, responseHandler);
    }
}
