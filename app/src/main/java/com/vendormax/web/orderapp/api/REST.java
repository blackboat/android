package com.vendormax.web.orderapp.api;

import android.util.Log;

import com.loopj.android.http.*;

/**
 * Created by goku on 3/12/15.
 */
public class REST {
    private static final String baseURL = "http://54.253.114.254/api/v1/mobile/";
//    private static final String baseURL = "http://192.168.5.130:3000/api/v1/mobile/";
//    private static final String baseURL = "http://192.168.5.111/property-crunch-php/public/client/search/";
    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.setTimeout(30 * 1000);
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.setTimeout(30 * 1000);
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String url) {
        return baseURL + url;
    }
}
