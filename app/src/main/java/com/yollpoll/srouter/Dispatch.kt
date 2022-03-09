package com.yollpoll.srouter

import android.util.Log
import com.yollpoll.router.dispatch.DispatchInterceptor
import com.yollpoll.router.dispatch.DispatchResponse
import com.yollpoll.router.dispatch.DispatcherManager
import java.util.logging.Logger

/**
 * Created by spq on 2022/1/10
 */
private const val TAG = "Dispatch"

object DispatchClient {
    var manager: DispatcherManager? = null
        get() {
            if (null == field) {
                field = DispatcherManager.ManagerBuilder().apply {
                    if (BuildConfig.DEBUG) {
                        this.addInterceptors(DispatcherLogInterceptor)
                    }
                }
                    .build()
            }
            return field!!
        }
}

/**
 * 记录每次跳转日志
 */
object DispatcherLogInterceptor : DispatchInterceptor {
    override suspend fun intercept(chain: DispatchInterceptor.Chain): DispatchResponse {
        val request = chain.getRequest()
        val url = request.url
        val param = request.params
        //TODO json转换param
        Log.i("LOG","DispatchLog: url is $url params is ${param}")
        return chain.proceed(request)
    }
}
