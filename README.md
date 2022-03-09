# sRouter
Android组件化下的路由管理器，实现跨module跳转，路由拦截、跨平台等功能
文档：
https://juejin.cn/post/7072992272131817502

注册路由：
```
@Route(url = "native://app?module=second")
class SecondActivity :AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tv=TextView(this);
        tv.text = "secondActivity"
        setContentView(tv)
    }
}
```
跳转：

```
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
```
