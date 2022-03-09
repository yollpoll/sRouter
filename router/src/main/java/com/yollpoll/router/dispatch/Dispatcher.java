package com.yollpoll.router.dispatch;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;


import java.lang.reflect.Field;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

class Dispatcher {
    private static final String TAG = "Dispatcher";
    private HashMap<String, OnDispatchListener> moduleMap = new HashMap<>();
    private HashMap<String, OnDispatchListener> schemeMap = new HashMap<>();


    public void init() {
        String name = "com.yollpoll.srouter";
        if (TextUtils.isEmpty(name)) {
//            ToastUtil.showShortToast("路由初始化失败，请检查包名");
            return;
        }
        String[] array = null;
        try {
            //拿到所有module建立的registerMap
            String packageName = name;
            Class buildClz = Class.forName(packageName + ".BuildConfig");
            Object buildConfig = buildClz.newInstance();
            Field routeArray = buildClz.getField("ROUTE_ARRAY");
            array = (String[]) routeArray.get(buildConfig);

        } catch (ClassNotFoundException | NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        if (null == array) {
            return;
        }
        //根据每个注册表，拿到所有的onDispatchListener(根据host区分)
        HashMap<String, OnDispatchListener> registerMap = new HashMap<>();
        for (String routePackageName : array) {
            Class clz = null;
            try {
                clz = Class.forName(routePackageName + ".RegisterMap");
                Object obj = clz.newInstance();
                Field field = clz.getField("registerMap");
                field.setAccessible(true);
                Map<String, String> map = (Map<String, String>) field.get(obj);
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    String host = entry.getKey();
                    String clzName = entry.getValue();
                    Class clzDispatch = Class.forName(clzName);
                    OnDispatchListener onDispatchListener = (OnDispatchListener) clzDispatch.newInstance();
                    registerMap.put(host, onDispatchListener);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        //注册所有onDispatchListener
        for (Map.Entry<String, OnDispatchListener> entry : registerMap.entrySet()) {
            moduleMap.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 验证协议头
     *
     * @param url
     * @return
     */
    public boolean isPrefixValid(String url) {
        if (url == null)
            return false;
        if ((url.startsWith("http://") || url.startsWith("https://")
                || url.startsWith("react://")
                || url.startsWith("native://") || url.startsWith("behavior://"))) {
            return true;
        }
        for (String key : schemeMap.keySet()) {
            if (url.startsWith(key)) {
                return true;
            }
        }
        return false;

    }

    /**
     * 注册各个模块的分发器
     *
     * @param map 指定模块对应的处理器
     */
    public void registerDispatch(Map<String, OnDispatchListener> map) {
        for (Map.Entry<String, OnDispatchListener> entry : map.entrySet()) {
            moduleMap.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 注册各个协议
     *
     * @param map
     */
    public void registerScheme(HashMap<String, OnDispatchListener> map) {
        for (Map.Entry<String, OnDispatchListener> entry : map.entrySet()) {
            schemeMap.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 分发请求
     *
     * @param url 请求url
     */
    public void dispatch(String url) {
        dispatch(url, null, null);
    }

    /**
     * 带回调的分发请求
     *
     * @param url            请求url
     * @param onBackListener 请求完成后的回调
     */
    public void dispatch(String url, OnBackListener onBackListener) {
        dispatch(url, null, onBackListener);
    }

    /**
     * 分发请求
     *
     * @param url     请求url
     * @param context Context
     */
    public void dispatch(String url, Context context) {
        dispatch(url, context, null);
    }

    /**
     * 带回调的分发请求
     *
     * @param url            请求url
     * @param context        Context
     * @param onBackListener 请求完成后的回调
     */
    public void dispatch(final String url, final Context context, final OnBackListener onBackListener) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> realDispatch(url, context, onBackListener));
    }

    public String realDispatch(String url, Context context, OnBackListener onBackListener) {
        try {
            if (!isPrefixValid(url)) {
                return "";
            }
            URI uri = new URI(url);
            String query = uri.getRawQuery();
            HashMap<String, String> param = getParams(query);
            //自定义协议
            if (schemeMap.containsKey(uri.getScheme())) {
                String host = uri.getHost();
                if (host == null) {
                    return "";
                }
                param.put("host", host);
                return schemeMap.get(uri.getScheme()).onDispatch(param, context, onBackListener);
            }

            if (url.startsWith("native://")) {
                String module = uri.getHost();
                if (module == null || moduleMap.get(module) == null) {
                    return "";
                }
                OnDispatchListener onDispatchListener = moduleMap.get(module);
                return onDispatchListener.onDispatch(param, context, onBackListener);
            }

            if (url.startsWith("behavior://")) {
                String module = uri.getHost();
                if (module == null) {
                    return "";
                }
                param.put("module", module);
                param.put("url", url);
                return BehaviorManager.INSTANCE.onDispatch(param, context, onBackListener);
            }

            //TODO:修改web跳转方式
            if (url.startsWith("http://") || url.startsWith("https://")) {
                param.put("url", url);
                String dispatchUrl = "native://web/?act=web";
                for (String key : param.keySet()) {
                    try {
                        dispatchUrl += "&" + key + "="
                                + URLEncoder.encode(param.get(key), "UTF-8");
                    } catch (Exception e) {
//                        LogUtils.e(e.getMessage(), e);
                    }
                }
                return realDispatch(dispatchUrl, context, onBackListener);
                //WebManager.INSTANCE.onDispatch(param, context ,onBackListener);
            }

//            }
        } catch (Exception e) {
//            LogUtils.e(e, e.getMessage());
        }
        return "";
    }

    public static HashMap<String, String> getParams(String query) {
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
