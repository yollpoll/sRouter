package com.yollpoll.router.dispatch;

import android.content.Context;

import java.util.HashMap;

public interface OnDispatchListener {
    String onDispatch(HashMap<String, String> params, Context context, OnBackListener onBackListener);
}
