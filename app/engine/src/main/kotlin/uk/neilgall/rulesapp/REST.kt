package uk.neilgall.rulesapp

import com.github.kittinunf.fuel.Fuel

typealias Params = List<Pair<String, String>>

private fun get(url: String) =
        Fuel.get(url).responseString().third.get()

private fun post(url: String, params: Params) =
        Fuel.post(url, params).responseString().third.get()

private fun put(url: String, params: Params) =
        Fuel.put(url, params).responseString().third.get()

private fun delete(url: String, params: Params) =
        Fuel.delete(url, params).responseString().third.get()

private fun Map<String, Value>.format(): Params = map { Pair(it.key, it.value.toString()) }

fun doREST(url: String, method: RESTMethod, params: Map<String, Value>): String = when(method) {
    RESTMethod.GET -> get(url)
    RESTMethod.POST -> post(url, params.format())
    RESTMethod.PUT -> put(url, params.format())
    RESTMethod.DELETE -> delete(url, params.format())
}
