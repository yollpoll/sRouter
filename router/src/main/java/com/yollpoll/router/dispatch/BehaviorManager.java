package com.yollpoll.router.dispatch;

import android.content.Context;

import java.util.HashMap;

/**
 * Created by spq on 2020-05-20
 */
public enum BehaviorManager implements OnDispatchListener {
    INSTANCE;

    @Override
    public String onDispatch(HashMap<String, String> params, Context context, OnBackListener onBackListener) {
        return "";
    }
}
