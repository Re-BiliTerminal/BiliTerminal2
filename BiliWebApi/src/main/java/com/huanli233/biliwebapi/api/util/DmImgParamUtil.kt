package com.huanli233.biliwebapi.api.util

import android.util.Base64 // Note: android.util.Base64 is still used for Base64 encoding
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.util.Random

object DmImgParamUtil {

    private val gson = Gson()

    fun getDmImgParams(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        map["dm_img_str"] = "V2ViR0wgMS4wIChPcGVuR0wgRVMgMi4wIENocm9taXVtKQ"
        map["dm_cover_img_str"] = "QU5HTEUgKEludGVsLCBJbnRlbChSKSBVSEQgR3JhcGhpY3MgNjMwIERpcmVjdDNEMTEgdnNfNV8wIHBzXzVfMCwgRDNEMTEpR29vZ2xlIEluYy4gKEludGVsKQ"
        map["dm_img_list"] = generateDmImgList()
        map["dm_img_inter"] = generateDmImgInter()
        return map
    }

    fun getDmImgParamsUrl(url: HttpUrl): HttpUrl {
        val builder = url.newBuilder()
        val params = getDmImgParams()
        for ((key, value) in params) {
            builder.addQueryParameter(key, value)
        }
        return builder.build()
    }

    fun f114i(a: Int, b: Int, i: Int): IntArray {
        val random = Random()
        val t = (random.nextDouble() * (114 * i)).toInt()
        return intArrayOf(3 * a + 2 * b + t, 4 * a - 5 * b + t, t)
    }

    fun f114(a: Int, b: Int): IntArray {
        val random = Random()
        val t = (random.nextDouble() * 114).toInt()
        return intArrayOf(2 * a + 2 * b + 3 * t, 4 * a - b + t, t)
    }

    fun f514(a: Int, b: Int): IntArray {
        val random = Random()
        val t = (random.nextDouble() * 514).toInt()
        return intArrayOf(3 * a + 2 * b + t, 4 * a - 4 * b + 2 * t, t)
    }

    private val timestamps = intArrayOf(2943, 3046, 3152, 3252, 3354, 3454, 3558, 3665, 3767, 5566, 5676, 5778, 5881, 7296, 7573, 135289)

    private fun generateDmImgList(): String {
        val random = Random()
        val dmImgListJson = JsonArray()

        val firstItem = JsonObject().apply {
            addProperty("x", 2355)
            addProperty("y", 725)
            addProperty("z", 0)
            addProperty("timestamp", 2145)
            addProperty("k", (random.nextDouble() * 67 + 60).toInt())
            addProperty("type", 0)
        }
        dmImgListJson.add(firstItem)

        val num = random.nextInt(timestamps.size - 6) + 6
        for (i in 0..num) {
            dmImgListJson.add(generateDmImgItem(random.nextInt(200), random.nextInt(200), i + 1, timestamps[i], if (i == num) 1 else 0))
        }
        return gson.toJson(dmImgListJson)
    }

    private fun generateDmImgItem(x: Int, y: Int, index: Int, timestamp: Int, type: Int): JsonObject {
        val xyz = f114i(x, y, index)
        val random = Random()
        return JsonObject().apply {
            addProperty("x", xyz[0])
            addProperty("y", xyz[1])
            addProperty("z", xyz[2])
            addProperty("timestamp", timestamp)
            addProperty("k", (random.nextDouble() * 67 + 60).toInt())
            addProperty("type", type)
        }
    }

    private val classNames = arrayOf(
        "clearfix", "g-search", "section", "loading", "full-rows", "n-btn", "router-link-exact-active", "router-link-active", "active"
    )

    private fun generateDmImgInter(): String {
        val random = Random()
        val result = JsonObject()
        val ds = JsonArray()

        val y = getRandomNumberInRange(-1500, -300)
        val x = getRandomNumberInRange(100, 700)
        val width = getRandomNumberInRange(100, 8000)
        val height = getRandomNumberInRange(20, 500)

        val xyz1 = f114(y, x)
        val xyz2 = f514(width, height)

        val pArray = JsonArray().apply {
            add(xyz1[0])
            add(xyz1[2])
            add(xyz1[1])
        }

        val sArray = JsonArray().apply {
            add(xyz2[2])
            add(xyz2[0])
            add(xyz2[1])
        }

        val dsItem = JsonObject().apply {
            addProperty("t", random.nextInt(6))
            addProperty("c", Base64.encodeToString((classNames[random.nextInt(classNames.size)] + " " + classNames[random.nextInt(classNames.size)]).toByteArray(), Base64.DEFAULT))
            add("p", pArray)
            add("s", sArray)
        }
        ds.add(dsItem)

        val whArray = JsonArray().apply {
            f114(width, height).forEach { add(it) }
        }

        val ofArray = JsonArray().apply {
            f514(y, x).forEach { add(it) }
        }

        result.add("ds", ds)
        result.add("wh", whArray)
        result.add("of", ofArray)

        return gson.toJson(result)
    }

    private fun getRandomNumberInRange(min: Int, max: Int): Int {
        require(min <= max) { "max must be greater than or equal to min" }
        val random = Random()
        return random.nextInt((max - min) + 1) + min
    }
}