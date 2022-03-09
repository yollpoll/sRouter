package com.yollpoll.router.dispatch

/**
 * Created by spq on 2022/1/13
 */
class DispatchResponse private constructor(
    var result: Boolean,
    var request: DispatchRequest?,
    var params: Map<String, String>?
) {
    interface IBuilder {
        fun build(): DispatchResponse
    }

    class Builder : IBuilder {
        var request: DispatchRequest? = null
        var result: Boolean = true
        var params: HashMap<String, String>? = hashMapOf()

        fun request(request: DispatchRequest?): Builder {
            this.request = request
            return this
        }

        fun result(result: Boolean): Builder {
            this.result = result
            return this
        }

        fun params(params: HashMap<String, String>?): Builder {
            this.params = params
            return this
        }

        override fun build(): DispatchResponse {
            return DispatchResponse(result, request, params)
        }
    }
}