package com.huanli233.biliwebapi.httplib

import com.huanli233.biliwebapi.BiliWebApi
import com.huanli233.biliwebapi.api.interfaces.IRequestParamApi
import com.huanli233.biliwebapi.api.util.BiliTicketUtil
import com.huanli233.biliwebapi.api.util.RequestParamUtil
import com.huanli233.biliwebapi.api.util.WbiUtil
import com.huanli233.biliwebapi.bean.requestParam.Buvids
import com.huanli233.biliwebapi.httplib.HttpUtils.parseFormBody
import com.huanli233.biliwebapi.httplib.annotation.API
import com.huanli233.biliwebapi.httplib.annotation.Csrf
import com.huanli233.biliwebapi.httplib.annotation.Fields
import com.huanli233.biliwebapi.httplib.annotation.Queries
import com.huanli233.biliwebapi.httplib.annotation.WbiSign
import kotlinx.coroutines.runBlocking
import okhttp3.Cookie
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.internal.http.HttpMethod
import retrofit2.Invocation
import okio.Buffer

private val otherCookiesMap = mapOf(
    "enable_web_push" to "DISABLE",
    "header_theme_version" to "undefined",
    "home_feed_column" to "4",
    "browser_resolution" to "839-959",
    "fingerprint" to "12ce21cfd0c7d56f6f1d37ba3f15203b"
)

internal class BilibiliApiInterceptor(
    private val biliWebApi: BiliWebApi
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val invocation = originalRequest.tag(Invocation::class.java)

        invocation?.let {
            if (it.service() != IRequestParamApi::class.java) checkCookieParams(originalRequest)
        }

        var requestBuilder = originalRequest.newBuilder()

        requestBuilder = requestBuilder.addHeaders()
        requestBuilder = requestBuilder.overrideUrl(invocation)
        requestBuilder = requestBuilder.processUrlParam(requestBuilder.build().url, invocation)
        requestBuilder = requestBuilder.wbiSign(requestBuilder.build().url, invocation)
        requestBuilder = requestBuilder.processFormParams(requestBuilder.build(), invocation)


        return chain.proceed(requestBuilder.build())
    }

    private fun checkCookieParams(request: Request) {
        val cookies = biliWebApi.cookieManager.loadForRequest(request.url)
        val httpUrl = "https://www.bilibili.com".toHttpUrl()

        if (cookies.any { it.name == "bili_ticket" }
                .not() || cookies.find { it.name == "bili_ticket_expires" }?.value?.toLongOrNull()
                ?.let { System.currentTimeMillis() / 1000 < it } != true
        ) {
            BiliTicketUtil.genBiliTicketSync(biliWebApi).data?.let {
                biliWebApi.cookieManager.saveFromResponse(
                    url = httpUrl,
                    cookies = listOf(
                        newCookie("bili_ticket", it.ticket),
                        newCookie("bili_ticket_expires",
                            (it.createTime + (3 * 24 * 60 * 60)).toString()
                        )
                    )
                )
            }
        }

        if (cookies.any { it.name == "_uuid" }.not()) {
            biliWebApi.cookieManager.saveFromResponse(
                url = httpUrl,
                cookies = listOf(
                    newCookie("_uuid", RequestParamUtil.genUuidInfoc())
                )
            )
        }

        if (cookies.any { it.name == "b_lsid" }.not()) {
            biliWebApi.cookieManager.saveFromResponse(
                url = httpUrl,
                cookies = listOf(
                    newCookie("b_lsid", RequestParamUtil.genBlsid())
                )
            )
        }

        if (cookies.any { it.name == "buvid_fp" }.not()) {
            biliWebApi.cookieManager.saveFromResponse(
                url = httpUrl,
                cookies = listOf(
                    newCookie("buvid_fp", "30c3020be6cee8345ddc4c3c6b77f60f")
                )
            )
        }

        if ((cookies.any { it.name == "buvid3" } && (cookies.any { it.name == "buvid4" }).not())) {
            runBlocking {
                Buvids.generate(biliWebApi)
            }.data?.let {
                biliWebApi.cookieManager.saveFromResponse(
                    url = httpUrl,
                    cookies = listOf(
                        newCookie("buvid3", it.buvid3),
                        newCookie("buvid4", it.buvid4)
                    )
                )
            }
        }

        if (cookies.any { it.name == "b_nut" }.not()) {
            biliWebApi.cookieManager.saveFromResponse(
                url = httpUrl,
                cookies = listOf(
                    newCookie("b_nut", RequestParamUtil.genBnut())
                )
            )
        }

        otherCookiesMap.forEach {
                (key, value) ->
            if (cookies.any { it.name == key }.not()) {
                biliWebApi.cookieManager.saveFromResponse(
                    url = httpUrl,
                    cookies = listOf(
                        newCookie(key, value)
                    )
                )
            }
        }
    }

    private fun Request.Builder.addHeaders(): Request.Builder = apply {
        header(HeaderNames.USER_AGENT, HeaderValues.USER_AGENT_VAL)
        header(HeaderNames.SEC_CH_UA, HeaderValues.SEC_CH_UA)
        header(HeaderNames.SEC_CH_UA_PLATFORM, HeaderValues.SEC_CH_UA_PLATFORM)
        header(HeaderNames.SEC_CH_UA_MOBILE, HeaderValues.SEC_CH_UA_MOBILE)
    }

    private fun Request.Builder.overrideUrl(invocation: Invocation?): Request.Builder =
        (invocation?.method()?.getAnnotation(API::class.java) ?: invocation?.service()
            ?.getAnnotation(API::class.java))?.let {
            url(this.build().url.newBuilder().host(it.value).build())
        } ?: this

    private fun Request.Builder.processUrlParam(httpUrl: HttpUrl, invocation: Invocation?): Request.Builder =
        invocation?.method()?.getAnnotation(Queries::class.java)?.let {
            require(it.keys.size == it.values.size) { "@Queries keys and values size not match" }
            val urlBuilder = httpUrl.newBuilder()
            it.keys.forEachIndexed { index, key ->
                urlBuilder.addQueryParameter(key, it.values[index])
            }
            url(urlBuilder.build())
        } ?: this

    private fun Request.Builder.wbiSign(url: HttpUrl, invocation: Invocation?): Request.Builder = invocation?.method()?.let {
        if (it.isAnnotationPresent(WbiSign::class.java)) {
            url(WbiUtil.signUrl(biliWebApi, url))
        }
        this
    } ?: this

    private fun Request.Builder.processFormParams(request: Request, invocation: Invocation?): Request.Builder = apply {
        invocation?.method()?.let { method ->
            val csrf = biliWebApi.cookieManager.loadForRequest(request.url).find { it.name == "bili_jct" }?.value.orEmpty()
            if (HttpMethod.requiresRequestBody(request.method)) {
                val formBody = request.body?.readString().orEmpty().parseFormBody()
                method.getAnnotation(Fields::class.java)?.let {
                    require(it.keys.size == it.values.size) { "@Fields keys and values size not match" }
                    it.keys.forEachIndexed { index, key ->
                        formBody.add(key, it.values[index])
                    }
                }
                method.getAnnotation(Csrf::class.java)?.let {
                    if (it.forceQuery) {
                        url(this.build().url.newBuilder().addQueryParameter("csrf", csrf).build())
                    } else {
                        formBody.add("csrf", csrf)
                    }
                }
                this@processFormParams.method(request.method, formBody.build())
            } else {
                url(this.build().url.newBuilder().addQueryParameter("csrf", csrf).build())
            }
        }
    }
}

private fun RequestBody.readString(): String = Buffer().also {
    writeTo(it)
}.readUtf8()

private fun newCookie(name: String, value: String) =
    Cookie.Builder()
        .name(name)
        .value(value)
        .domain("bilibili.com")
        .build()