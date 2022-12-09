package com.liveness.dflivenesslibrary.net;

import android.graphics.Bitmap;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class DFHttpManager {
    private static final String KEY_API_ID = "X-DF-API-ID";
    private static final String KEY_API_SECRET = "X-DF-API-SECRET";
    private static final String TAG = "DFHttpManager";
    private static final String crlf = "\r\n";
    private static final String twoHyphens = "--";
    private static final String boundary = "*****";
    private static final String charset = "UTF-8";

    public static String doPost(String url, HashMap<String, String> hashMapParam) {
        try {
            HttpURLConnection httpConn = getTextHttpURLConnection(url);
            if (httpConn == null) return null;

            OutputStream output = httpConn.getOutputStream();
            DataOutputStream request = new DataOutputStream(output);

            if (hashMapParam != null) {
                StringBuilder params = new StringBuilder();
                for (Map.Entry<String, String> entry : hashMapParam.entrySet()) {
                    params.append(entry.getKey())
                            .append("=")
                            .append(entry.getValue())
                            .append("&");

                }
                addFormField(request, params.toString().substring(0, params.toString().length() - 1));
            }
            request.flush();
            request.close();

            StringBuilder result = new StringBuilder();
            InputStream inputStream = null;
            int responseCode = httpConn.getResponseCode();
            if (responseCode == 200) {
                inputStream = httpConn.getInputStream();
            } else if (responseCode > 200) {
                inputStream = httpConn.getErrorStream();
            }
            String line;
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                while ((line = reader.readLine()) != null) {
                    result.append("\n").append(line);
                }
                reader.close();
            }
            httpConn.disconnect();
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String doPost(String url, HashMap<String, String> params, HashMap<String, byte[]> files) {
        try {
            HttpURLConnection httpConn = (HttpURLConnection) new URL(url).openConnection();
            if (httpConn == null) {
                Log.e(TAG, "create http failed");
                return makeResultString(-2);
            }
            int status = getHttpURLConnection(url, httpConn);
            if (status != 0) {
                return makeResultString(status);
            }

            OutputStream output = httpConn.getOutputStream();
            DataOutputStream request = new DataOutputStream(output);

            if (params != null) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    addFormField(request, entry.getKey(), entry.getValue());
                }
            }

            if (files != null) {
                for (Map.Entry<String, byte[]> entry : files.entrySet()) {
                    addFormImageField(request, entry.getKey(), entry.getValue(), entry.getKey() + ".jpg");
                }
            }

            request.writeBytes(twoHyphens + boundary + twoHyphens + crlf);
            request.flush();
            request.close();

            StringBuilder result = new StringBuilder();
            Log.e(TAG, "code: " + httpConn.getResponseCode() + " reason: " + httpConn.getResponseMessage());
            InputStream inputStream = null;
            int responseCode = httpConn.getResponseCode();
            if (responseCode == 200) {
                inputStream = httpConn.getInputStream();
            } else if (responseCode > 200) {
                inputStream = httpConn.getErrorStream();
            }
            String line;

            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                while ((line = reader.readLine()) != null) {
                    result.append("\n").append(line);
                }
                reader.close();
            }

            httpConn.disconnect();
            return result.toString();
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
        return null;
    }

    private static String makeResultString(int status) {
        JSONObject jsonObject = new JSONObject();
        String strStatus;
        if (status == -1) {
            strStatus = "CONNECTION_TIMEOUT";
        } else {
            strStatus = "CONNECTION_ERROR";
        }
        try {
            jsonObject.put("mNetworkResultStatus", strStatus);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    private static int getHttpURLConnection(String url, HttpURLConnection httpConn) {
        try {
            httpConn.setUseCaches(false);
            httpConn.setDoOutput(true);
            httpConn.setConnectTimeout(30000);
            httpConn.setReadTimeout(30000);
            httpConn.setRequestMethod("POST");
            httpConn.setRequestProperty("Connection", "Keep-Alive");
            httpConn.setRequestProperty("Cache-Control", "no-cache");
            httpConn.setRequestProperty(KEY_API_ID, DFNetworkUtil.API_ID);
            httpConn.setRequestProperty(KEY_API_SECRET, DFNetworkUtil.API_SECRET);
            httpConn.setRequestProperty(
                    "Content-Type", "multipart/form-data;boundary=" + boundary);

            httpConn.connect();
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            return -1;
        } catch (IOException e) {
            e.printStackTrace();
            return -3;
        }
        return 0;
    }

    private static HttpURLConnection getTextHttpURLConnection(String url) throws IOException {
        HttpURLConnection httpConn = (HttpURLConnection) new URL(url).openConnection();
        if (httpConn == null) {
            Log.e(TAG, "create http failed");
            return null;
        }
        httpConn.setReadTimeout(3000);
        httpConn.setConnectTimeout(3000);
        httpConn.setRequestMethod("POST");
        httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        httpConn.setRequestProperty("Connection", "Keep-Alive");
        httpConn.setRequestProperty("Charset", "UTF-8");
        httpConn.setRequestProperty("Cache-Control", "no-cache");
        httpConn.setDoInput(true);
        httpConn.setDoOutput(true);
        httpConn.connect();
        return httpConn;
    }

    private static void addFormField(DataOutputStream request, String param) throws IOException {
        request.writeBytes(param);
    }

    /**
     * Adds a form field to the request
     *
     * @param request the request output stream
     * @param name    field name
     * @param value   field value
     * @param value   field value
     * @throws IOException
     */
    private static void addFormField(DataOutputStream request, String name, String value) throws IOException {
        request.writeBytes(twoHyphens + boundary + crlf);
        request.writeBytes("Content-Disposition: form-data; name=\"" +
                name + "\"" + crlf);
        request.writeBytes("Content-Type: text/plain; charset=" + charset + crlf);
        request.writeBytes(crlf);
        request.writeBytes(value + crlf);
    }

    /**
     * Adds a form field to the request
     *
     * @param request  the request output stream
     * @param name     field name
     * @param image    the bitmap that will be compress to JPEG with quality 90.
     * @param filename the filename of this image
     * @throws IOException
     */
    private static void addFormImageField(DataOutputStream request, String name, Object image, String filename) throws IOException {
        request.writeBytes(twoHyphens + boundary + crlf);
        request.writeBytes("Content-Disposition: form-data; name=\"" +
                name + "\"; filename=\"" +
                filename + "\"" + crlf);
        request.writeBytes("Content-Type: image/jpeg" + crlf);
        request.writeBytes(crlf);
        if (image instanceof Bitmap) {
            ((Bitmap) image).compress(Bitmap.CompressFormat.JPEG, 90, request);
        } else {
            request.write((byte[]) image);
        }
        request.writeBytes(crlf);
    }
}
