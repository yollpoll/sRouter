package com.yollpoll.router.dispatch;


import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;


/**
 * 路由请求
 * Created by spq on 2022/1/10
 */
public class DispatchRequest {
    private String url;
    private Map<String, String> params = new HashMap<>();
    private StartType requestType = StartType.START;
    private OnBackListener onBackListener;

    private DispatchRequest(String url, StartType requestType, OnBackListener onBackListener, Map<String, String> params) {
        this.url = url;
        this.requestType = requestType;
        this.params = params;
        this.onBackListener = onBackListener;
    }

    public OnBackListener getBackListener() {
        return onBackListener;
    }

    public void setBackListener(OnBackListener onBackListener) {
        this.onBackListener = onBackListener;
    }

    public StartType getStartType() {
        return requestType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    interface IBuilder {
        DispatchRequest build();
    }

    interface IUrlBuilder {
        DispatchRequest build() throws URISyntaxException;
    }

    public static class UrlBuilder implements IUrlBuilder {
        String url;
        Map<String, String> params = new HashMap<>();
        StartType startType = StartType.START;
        OnBackListener onBackListener;

        public UrlBuilder setOnBackListener(OnBackListener onBackListener) {
            this.onBackListener = onBackListener;
            return this;
        }

        public UrlBuilder(String url) {
            this.url = url;
        }

        public UrlBuilder params(Map<String, String> params) {
            this.params = params;
            return this;
        }

        public UrlBuilder startType(StartType startType, int reqCode) {
            this.startType = startType;
            this.params.put("reqCode", reqCode + "");
            return this;
        }

        @Override
        public DispatchRequest build() throws URISyntaxException {
            URI uri = new URI(url);
            String query = uri.getRawQuery();
            HashMap<String, String> urlParam = buildParams(query);
            urlParam.putAll(params);
            return new DispatchRequest(url, startType, onBackListener, urlParam);
        }
    }

    public static class RequestBuilder implements IBuilder {
        String scheme = "native";
        String host = "";
        String module = "";
        StartType startType = StartType.START;
        Map<String, String> params = new HashMap<>();
        OnBackListener onBackListener;

        public RequestBuilder setOnBackListener(OnBackListener onBackListener) {
            this.onBackListener = onBackListener;
            return this;
        }

        public RequestBuilder scheme(RouterScheme scheme) {
            switch (scheme) {
                case FLUTTER: {
                    this.scheme = "flutter";
                    break;
                }
                case NATIVE: {
                    this.scheme = "native";
                    break;
                }
                case HTTP: {
                    this.scheme = "http";
                    break;
                }
                case HTTPS: {
                    this.scheme = "https";
                    break;
                }
            }
            return this;
        }

        public RequestBuilder host(String host) {
            this.host = host;
            return this;
        }

        public RequestBuilder module(String module) {
            this.module = module;
            return this;
        }

        public RequestBuilder params(Map<String, String> params) {
            this.params = params;
            return this;
        }

        public RequestBuilder startType(StartType type, int reqCode) {
            this.startType = type;
            this.params.put("reqCode", reqCode + "");
            return this;
        }

        @Override
        public DispatchRequest build() {
            String url = scheme + "://" + host + "?module=" + module;
            return new DispatchRequest(url, startType, onBackListener, params);
        }
    }

    public static HashMap<String, String> buildParams(String query) {
        HashMap<String, String> params = new HashMap<String, String>();
        if (query == null) {
            return params;
        }
        try {
            String[] arrSplit = query.split("[&]");
            for (String strSplit : arrSplit) {
                String[] arrSplitEqual = null;
                arrSplitEqual = strSplit.split("[=]");
                if (arrSplitEqual.length > 1) {
                    params.put(arrSplitEqual[0], URLDecoder.decode(arrSplitEqual[1], "UTF-8"));

                } else {
                    if (arrSplitEqual[0] != "") {
                        params.put(arrSplitEqual[0], "");
                    }
                }
            }
            return params;
        } catch (Exception e) {
            return params;
        }
    }

}
