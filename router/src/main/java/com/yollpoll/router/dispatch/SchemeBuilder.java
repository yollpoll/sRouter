package com.yollpoll.router.dispatch;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by spq on 2021/12/16
 */
public class SchemeBuilder {
    String scheme = RouterScheme.NATIVE.name().toLowerCase();
    String host = "";
    String module = "";
    Map<String, String> params = new HashMap<>();

    public SchemeBuilder scheme(RouterScheme scheme) {
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

    public SchemeBuilder host(String host) {
        this.host = host;
        return this;
    }

    public SchemeBuilder module(String module) {
        this.module = module;
        return this;
    }

    public SchemeBuilder params(Map<String, String> params) {
        this.params = params;
        return this;
    }

    public String build() {
        StringBuilder url = new StringBuilder(scheme + "://" + host + "?module=" + module);
        if (null == params) {
            return url.toString();
        }
        for (Map.Entry<String, String> entry : params.entrySet()) {
            url.append("&").append(entry.getKey()).append("=").append(entry.getValue());
        }
        return url.toString();
    }
}
