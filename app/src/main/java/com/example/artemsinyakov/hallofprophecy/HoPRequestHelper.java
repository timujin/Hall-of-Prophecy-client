package com.example.artemsinyakov.hallofprophecy;
import android.content.Context;
import android.util.Log;

import com.loopj.android.http.*;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import cz.msebera.android.httpclient.entity.StringEntity;

public class HoPRequestHelper {
    private static final String BASE_URL = "https://hallofprophecy.xyz";
    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void setUp() {
        client.setTimeout(5);
        client.setResponseTimeout(5);
        client.setConnectTimeout(5);
        client.setMaxRetriesAndTimeout(5,5);
        Log.e("1", "set up");
    }

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Log.e("CLIENT", getAbsoluteUrl(url));
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Log.d("fun", client.post(getAbsoluteUrl(url), params, responseHandler).toString());
    }

    public static void post(Context context, String url, JSONObject json, AsyncHttpResponseHandler responseHandler) {
        //try {
            StringEntity entity = new StringEntity(json.toString(), "UTF-8");
            Log.d("fun", client.post(context, getAbsoluteUrl(url), entity, "application/json", responseHandler).toString());
        //} catch (UnsupportedEncodingException e) {
        //    Log.d("1", e.toString());
        //}
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

}
