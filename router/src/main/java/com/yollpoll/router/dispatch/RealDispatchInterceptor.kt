package com.yollpoll.router.dispatch

import java.lang.StringBuilder

/**
 * Created by spq on 2022/1/13
 */
internal class RealDispatchInterceptor(private val dispatcher: Dispatcher) : DispatchInterceptor {
    override suspend fun intercept(chain: DispatchInterceptor.Chain): DispatchResponse {
        val req = chain.getRequest()
        val sb = StringBuilder(req.url)
        req.params.forEach {
            sb.append("&${it.key}=${it.value}")
        }
        sb.append("&startType=${req.startType.name}")

        val res = dispatcher.realDispatch(sb.toString(), chain.getContext(), req.backListener)

        return DispatchResponse.Builder().request(chain.getRequest()).result(true)
            .params(hashMapOf("dispatchResult" to res)).build()
    }
}