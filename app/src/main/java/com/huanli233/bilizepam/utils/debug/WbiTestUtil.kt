package com.huanli233.bilizepam.utils.debug

import android.util.Log
import com.huanli233.biliwebapi.ApiDebugSettings

object WbiTestUtil {
    private const val TAG = "WbiTestUtil"
    
    /**
     * 测试WBI签名是否正确
     * 可以与BiliClient的签名结果进行对比
     */
    fun testWbiSignature(
        originalParams: Map<String, String>,
        mixinKey: String,
        expectedSignature: String? = null
    ) {
        if (!ApiDebugSettings.isDebugEnabled()) return
        
        Log.d(TAG, "=== WBI Signature Test ===")
        
        try {
            // 1. 添加时间戳
            val wts = (System.currentTimeMillis() / 1000).toString()
            val paramsWithWts = originalParams.toMutableMap()
            paramsWithWts["wts"] = wts
            
            // 2. 按键排序
            val sortedParams = paramsWithWts.toSortedMap()
            
            // 3. 构建查询字符串
            val queryString = sortedParams.entries.joinToString("&") { (key, value) ->
                "$key=$value"
            }
            
            // 4. URL编码（简化版本，实际可能需要更复杂的编码）
            val encodedQuery = queryString.replace(" ", "%20")
            
            // 5. 添加mixin key并计算MD5
            val stringToSign = encodedQuery + mixinKey
            val signature = md5(stringToSign)
            
            Log.d(TAG, "Test Results:")
            Log.d(TAG, "  Original params: $originalParams")
            Log.d(TAG, "  Added wts: $wts")
            Log.d(TAG, "  Sorted params: $sortedParams")
            Log.d(TAG, "  Query string: $queryString")
            Log.d(TAG, "  Encoded query: $encodedQuery")
            Log.d(TAG, "  String to sign: $stringToSign")
            Log.d(TAG, "  Mixin key: $mixinKey")
            Log.d(TAG, "  Our signature: $signature")
            
            expectedSignature?.let {
                Log.d(TAG, "  Expected signature: $it")
                Log.d(TAG, "  Signatures match: ${signature == it}")
                
                if (signature != it) {
                    Log.e(TAG, "SIGNATURE MISMATCH!")
                    Log.e(TAG, "Possible issues:")
                    Log.e(TAG, "  1. Parameter encoding differences")
                    Log.e(TAG, "  2. Timestamp differences")
                    Log.e(TAG, "  3. Mixin key differences")
                    Log.e(TAG, "  4. Parameter sorting differences")
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error testing WBI signature: ${e.message}", e)
        }
        
        Log.d(TAG, "=== End WBI Signature Test ===")
    }
    
    /**
     * 比较两个WBI签名的差异
     */
    fun compareSignatures(
        params1: Map<String, String>,
        signature1: String,
        params2: Map<String, String>,
        signature2: String,
        label1: String = "Signature 1",
        label2: String = "Signature 2"
    ) {
        if (!ApiDebugSettings.isDebugEnabled()) return
        
        Log.d(TAG, "=== Signature Comparison ===")
        Log.d(TAG, "$label1:")
        Log.d(TAG, "  Params: $params1")
        Log.d(TAG, "  Signature: $signature1")
        
        Log.d(TAG, "$label2:")
        Log.d(TAG, "  Params: $params2")
        Log.d(TAG, "  Signature: $signature2")
        
        Log.d(TAG, "Differences:")
        val keys1 = params1.keys
        val keys2 = params2.keys
        val allKeys = (keys1 + keys2).toSet()
        
        allKeys.forEach { key ->
            val value1 = params1[key]
            val value2 = params2[key]
            if (value1 != value2) {
                Log.d(TAG, "  $key: '$value1' vs '$value2'")
            }
        }
        
        Log.d(TAG, "Signatures match: ${signature1 == signature2}")
        Log.d(TAG, "=== End Signature Comparison ===")
    }
    
    private fun md5(input: String): String {
        val md = java.security.MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
