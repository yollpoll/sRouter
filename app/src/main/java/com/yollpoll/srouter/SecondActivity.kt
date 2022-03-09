package com.yollpoll.srouter

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.yollpoll.annotation.Route
import org.w3c.dom.Text

/**
 * Created by spq on 2022/3/9
 */
@Route(url = "native://app?module=second")
class SecondActivity :AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tv=TextView(this);
        tv.text = "secondActivity"
        setContentView(tv)
    }
}