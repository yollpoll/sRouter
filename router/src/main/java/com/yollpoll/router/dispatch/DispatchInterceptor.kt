package com.yollpoll.router.dispatch

import android.content.Context

/**
 * Created by spq on 2022/1/10
 */
public interface DispatchInterceptor {
    suspend fun intercept(chain: Chain): DispatchResponse

    interface Chain {
        suspend fun proceed(request: DispatchRequest): DispatchResponse

        fun getRequest(): DispatchRequest

        fun getContext(): Context
    }
}