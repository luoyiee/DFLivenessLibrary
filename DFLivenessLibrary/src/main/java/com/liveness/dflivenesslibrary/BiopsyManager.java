package com.liveness.dflivenesslibrary;

import android.app.Application;

/**
 * @author 学客
 */
public class BiopsyManager {
    public String HOST_URL = "";
    public String API_ID = "";
    public String API_SECRET = "";
    private static final BiopsyManager instance = new BiopsyManager();

    public static BiopsyManager getInstance() {
        return instance;
    }

    private BiopsyManager() {
    }

    public void init(Application application, String hostUrl, String apiId, String apiSecret) {
        this.HOST_URL = hostUrl;
        this.API_ID = apiId;
        this.API_SECRET = apiSecret;
    }

    public String getHostUrl() {
        return HOST_URL;
    }

    public String getApiId() {
        return API_ID;
    }

    public String getApiSecret() {
        return API_SECRET;
    }
}
