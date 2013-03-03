package com.etsy.esp;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.params.HttpMethodParams;
import java.io.*;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONTokener;
import org.json.JSONException;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import org.mortbay.log.Log;



public class EtsyAPI {

    private static String _api_key = "59v7he5jmht7825xg6ektjgp";
    private static Map<String, String> _methods = 
        new HashMap<String, String>();
    private static Random _rand = new Random();


    static {
        _methods.put("getUserDetails", "/users/{user_id}");
        _methods.put("getFavorersOfUser", "/users/{user_id}/favorers");
        _methods.put("getFavorersOfListing", "/listings/{listing_id}/favorers");
        _methods.put("getUsersByName", "/users/keywords/{search_name}");
        _methods.put("getShopDetails", "/shops/{user_id}");
        _methods.put("getFeaturedSellers", "/shops/featured");
        _methods.put("getShopsByName", "/shops/keywords/{search_name}");
        _methods.put("getFavoriteShopsOfUser", "/users/{user_id}/favorites/shops");
        _methods.put("getListingDetails", "/listings/{listing_id}");
        _methods.put("getListings", "/shops/{user_id}/listings");
        _methods.put("getFeaturedDetails", "/shops/{user_id}/listings/featured");
        _methods.put("getFrontFeaturedListings", "/listings/featured/front");
        _methods.put("getFavoriteListingsOfUser", "/users/{user_id}/favorites/listings");
        _methods.put("getGiftGuideListings", "/gift-guides/{guide_id}/listings");
        _methods.put("getListingsByKeyword", "/listings/keywords/{search_terms}");
        _methods.put("getListingsByTags", "/listings/tags/{tags}");
        _methods.put("getChildTags", "/tags/{tag}/children");
        _methods.put("getTopTags", "/tags/top");
        _methods.put("getGiftGuides", "/gift-guides");
        _methods.put("getMethodTable", "/");
        _methods.put("getServerEpoch", "/server/epoch");
        _methods.put("ping", "/server/ping");
        _methods.put("getAllListings", "/listings/all");
    }


    public static int nextListingId() throws JSONException, IOException {
        Map<String, String> args = new HashMap<String, String>();
        args.put("limit", "100");
        JSONObject r = invoke("getAllListings", args);

        int n = _rand.nextInt(100);
        JSONObject l = r.getJSONArray("results").getJSONObject(n);
        return l.getInt("listing_id");
    }


    public static String getAvatarUrl(String userName) 
        throws JSONException, IOException {
        Map<String, String> args = new HashMap<String, String>();
        args.put("user_id", userName);
        JSONObject r = invoke("getUserDetails", args);
        JSONArray results = r.getJSONArray("results");
        if(results.length() == 0) {
            return "";
        }
        return results.getJSONObject(0).getString("image_url_50x50");
    }


    private static JSONObject invoke(String command) 
        throws JSONException, IOException {

        Map<String, String> m = new HashMap<String, String>();
        return invoke(command, m);
    }

    private static JSONObject invoke(String command, Map<String, String> args) 
        throws JSONException, IOException {

        HttpClient c = new HttpClient();
        GetMethod m = new GetMethod(getUrl(command, args));
        m.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
                                   new DefaultHttpMethodRetryHandler(3, false));
        String s = null;
        try {
            c.executeMethod(m);
            s = new String(m.getResponseBody());
        } finally {
            m.releaseConnection();
        }

        return new JSONObject(new JSONTokener(s));
    }


    private static String getUrl(String command, Map<String, String> args) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("api_key", _api_key);
        String url = "http://beta-api.etsy.com/v1" + _methods.get(command);
        
        for(String param : args.keySet()) {
            String val = args.get(param);
            String url2 = url.replaceAll("\\{" + param + "\\}", val);
            if(url2 != url) {
                url = url2;
            } else {
                params.put(param, val);
            }
        }

        String append = "?";
        for(String k : params.keySet()) {
            url += append + k + "=" + params.get(k);
            append = "&";
        }

        Log.info("GET " + url);
        return url;
    }

}