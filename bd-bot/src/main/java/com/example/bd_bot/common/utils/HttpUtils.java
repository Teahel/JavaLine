package com.example.bd_bot.common.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP 请求工具类。
 *
 * <p>基于 JDK HttpURLConnection 实现，不依赖第三方包。</p>
 */
public final class HttpUtils {

    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;
    private static final int DEFAULT_READ_TIMEOUT = 10000;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private HttpUtils() {
    }

    public static String get(String url) throws IOException {
        return get(url, null);
    }

    public static String get(String url, Map<String, String> params) throws IOException {
        return get(url, params, null);
    }

    public static String get(String url, Map<String, String> params, Map<String, String> headers) throws IOException {
        String requestUrl = appendQuery(url, params, DEFAULT_CHARSET);
        return request("GET", requestUrl, null, null, headers, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT).getBody();
    }

    public static String postJson(String url, String jsonBody) throws IOException {
        return postJson(url, jsonBody, null);
    }

    public static String postJson(String url, String jsonBody, Map<String, String> headers) throws IOException {
        Map<String, String> requestHeaders = copyHeaders(headers);
        requestHeaders.put("Content-Type", "application/json; charset=UTF-8");
        return request("POST", url, jsonBody, DEFAULT_CHARSET, requestHeaders,
                DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT).getBody();
    }

    public static String postForm(String url, Map<String, String> formParams) throws IOException {
        return postForm(url, formParams, null);
    }

    public static String postForm(String url, Map<String, String> formParams, Map<String, String> headers)
            throws IOException {
        Map<String, String> requestHeaders = copyHeaders(headers);
        requestHeaders.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        String body = buildQuery(formParams, DEFAULT_CHARSET);
        return request("POST", url, body, DEFAULT_CHARSET, requestHeaders,
                DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT).getBody();
    }

    public static HttpResponse request(String method, String url, String body, Charset charset,
                                       Map<String, String> headers, int connectTimeout, int readTimeout)
            throws IOException {
        HttpURLConnection connection = null;
        Charset actualCharset = charset == null ? DEFAULT_CHARSET : charset;

        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod(method);
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);
            connection.setUseCaches(false);

            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            if (body != null && !body.isEmpty()) {
                connection.setDoOutput(true);
                try (BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(connection.getOutputStream(), actualCharset))) {
                    writer.write(body);
                }
            }

            int statusCode = connection.getResponseCode();
            String responseBody = readResponseBody(connection, actualCharset, statusCode);
            return new HttpResponse(statusCode, responseBody, connection.getHeaderFields());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String appendQuery(String url, Map<String, String> params, Charset charset) throws IOException {
        String query = buildQuery(params, charset);
        if (query.isEmpty()) {
            return url;
        }
        return url + (url.contains("?") ? "&" : "?") + query;
    }

    private static String buildQuery(Map<String, String> params, Charset charset) throws IOException {
        if (params == null || params.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (builder.length() > 0) {
                builder.append('&');
            }
            builder.append(URLEncoder.encode(entry.getKey(), charset.name()));
            builder.append('=');
            builder.append(URLEncoder.encode(entry.getValue() == null ? "" : entry.getValue(), charset.name()));
        }
        return builder.toString();
    }

    private static String readResponseBody(HttpURLConnection connection, Charset charset, int statusCode)
            throws IOException {
        InputStream inputStream = statusCode >= HttpURLConnection.HTTP_BAD_REQUEST
                ? connection.getErrorStream()
                : connection.getInputStream();

        if (inputStream == null) {
            return "";
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (builder.length() > 0) {
                    builder.append(System.lineSeparator());
                }
                builder.append(line);
            }
            return builder.toString();
        }
    }

    private static Map<String, String> copyHeaders(Map<String, String> headers) {
        return headers == null ? new HashMap<String, String>() : new HashMap<String, String>(headers);
    }

    public static final class HttpResponse {

        private final int statusCode;
        private final String body;
        private final Map<String, java.util.List<String>> headers;

        private HttpResponse(int statusCode, String body, Map<String, java.util.List<String>> headers) {
            this.statusCode = statusCode;
            this.body = body;
            this.headers = headers == null ? Collections.<String, java.util.List<String>>emptyMap() : headers;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getBody() {
            return body;
        }

        public Map<String, java.util.List<String>> getHeaders() {
            return headers;
        }

        public boolean isSuccess() {
            return statusCode >= 200 && statusCode < 300;
        }
    }
}
