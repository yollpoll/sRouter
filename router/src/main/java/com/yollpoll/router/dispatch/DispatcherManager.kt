package com.yollpoll.router.dispatch

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import java.util.HashMap

/**
 * Created by spq on 2022/1/10
 */
class DispatcherManager private constructor(private val interceptors: ArrayList<DispatchInterceptor>) {
    private val dispatcher = Dispatcher()

    //每次初始化的时候注册下路由表
    init {
        dispatcher.init()
        interceptors.add(RealDispatchInterceptor(dispatcher))
    }

    fun registerScheme(map: HashMap<String, OnDispatchListener>) {
        dispatcher.registerScheme(map)
    }

    fun registerDispatch(map: HashMap<String, OnDispatchListener>) {
        dispatcher.registerDispatch(map)
    }


    /**
     * 根据请求获得对应的fragment
     * @param context Context
     * @param request DispatchRequest
     * @return Fragment?
     */
    suspend fun newFragment(context: Context, request: DispatchRequest): Fragment? {
        val chain = RealDispatchChain(context, 0, interceptors, request)
        val dispatchResult = chain.proceed(request).params?.get("dispatchResult")
        return dispatchResult?.run {
            val clz = Class.forName(this)
            if (Fragment::class.java.isAssignableFrom(clz)) {
                val fragment: Fragment = clz.newInstance() as Fragment
                val args = Bundle()
                request.params.forEach {
                    args.putString(it.key, it.value)
                }
                fragment.arguments = args
                return@run fragment
            } else {
                return@run null
            }
        }
    }

    /**
     * 发送请求
     * @param context Context
     * @param request DispatchRequest
     * @return DispatchResponse
     */
    suspend fun dispatch(
        context: Context,
        request: DispatchRequest,
    ): DispatchResponse {
        val chain = RealDispatchChain(context, 0, interceptors, request)
        return chain.proceed(request)
    }

    internal interface IBuild {
        fun build(): DispatcherManager
    }

    class ManagerBuilder : IBuild {
        private val interceptors: ArrayList<DispatchInterceptor> = arrayListOf()
        fun addInterceptor(interceptors: DispatchInterceptor): ManagerBuilder {
            this.interceptors.add(interceptors)
            return this
        }

        fun addInterceptors(vararg interceptors: DispatchInterceptor): ManagerBuilder {
            this.interceptors.addAll(interceptors.asList())
            return this
        }

        override fun build(): DispatcherManager {
            return DispatcherManager(interceptors)
        }

    }
}