package com.yollpoll.router.dispatch

import android.content.Context

/**
 * Created by spq on 2022/1/10
 */
class RealDispatchChain(
    private val mContext: Context,
    private val index: Int,
    private val interceptors: List<DispatchInterceptor>,
    private val mRequest: DispatchRequest
) : DispatchInterceptor.Chain {
    override suspend fun proceed(request: DispatchRequest): DispatchResponse {
        if (index >= interceptors.size) {
            throw Exception("路由拦截器数量错误")
        }
        val nextChain =
            RealDispatchChain(mContext, index + 1, interceptors, request)
        val interceptor = interceptors[index]
        return interceptor.intercept(nextChain)
    }

    override fun getRequest(): DispatchRequest = mRequest

    override fun getContext(): Context = mContext

}