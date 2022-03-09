package com.yollpoll.srouter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.yollpoll.annotation.Route
import com.yollpoll.router.dispatch.DispatchRequest
import com.yollpoll.router.dispatch.RouterScheme
import com.yollpoll.router.dispatch.StartType
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.log

@Route(url = "native://app?module=main")
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun go(view: View) {
        //为了掩饰方便没有用activity作用域
        GlobalScope.launch {
            val params = hashMapOf(
                Pair("key1", "value"),
                Pair("key2", "value2")
            )
            val request = DispatchRequest.RequestBuilder().scheme(RouterScheme.NATIVE).host("app")
                .module("second").params(params).startType(StartType.START_FOR_RESULT, 123)
                .setOnBackListener { params, context ->
                    Log.i("back","onBack")
                }.build()
            DispatchClient.manager!!.dispatch(this@MainActivity, request)
        }

    }
}