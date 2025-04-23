package com.huanli233.biliterminal2.activity.dynamic

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Nullable
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.activity.base.BaseActivity
import com.huanli233.biliterminal2.activity.base.RefreshMainActivity
import com.huanli233.biliterminal2.adapter.dynamic.DynamicAdapter
import com.huanli233.biliterminal2.api.DynamicApi
import com.huanli233.biliterminal2.bean.Dynamic
import com.huanli233.biliterminal2.helper.TutorialHelper
import com.huanli233.biliterminal2.util.MsgUtil
import com.huanli233.biliterminal2.util.ThreadManager
import java.util.ArrayList
import java.util.regex.Pattern

// 扩展函数1：简化动态列表更新逻辑
private fun DynamicAdapter.safeNotifyChanges(position: Int, count: Int) {
    try {
        notifyItemRangeInserted(position, count)
    } catch (e: Exception) {
        notifyDataSetChanged()
    }
}

// 扩展函数2：简化@用户提取逻辑
private fun String?.extractMentions(): Map<String, Long> {
    val atUids = linkedMapOf<String, Long>()  // 改用LinkedHashMap保持顺序
    this?.let { 
        Regex("@(\\S+)\\s").findAll(it).forEach { match ->
            match.groupValues.getOrNull(1)?.let { name ->
                DynamicApi.mentionAtFindUser(name).takeIf { it != -1L }?.let {
                    atUids[name] = it
                }
            }
        }
    }
    return atUids
}

// 扩展函数3：简化动态发布结果处理
private fun BaseActivity.handlePublishResult(
    success: Boolean,
    onSuccess: (() -> Unit)? = null
) {
    runOnUiThread {
        if (success) {
            MsgUtil.showMsg("发送成功~")
            onSuccess?.invoke()
        } else {
            MsgUtil.showMsg("发送失败")
        }
    }
}

class DynamicActivity : RefreshMainActivity() {
    private lateinit var dynamicList: ArrayList<Dynamic>
    private lateinit var dynamicAdapter: DynamicAdapter
    private var offset: Long = 0
    private var firstRefresh = true
    private var type = "all"
    
    companion object {
        private val typeNameMap = mapOf(
            "全部" to "all",
            "视频投稿" to "video",
            "追番" to "pgc",
            "专栏" to "article"
        )
    }

    private val selectTypeLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.getStringExtra("item")?.let { selectedItem ->
                typeNameMap[selectedItem]?.let { newType ->
                    if (isRefreshing) {
                        MsgUtil.showMsg("还在加载中OvO")
                    } else {
                        type = newType
                        setRefreshing(true)
                        refreshDynamic()
                    }
                }
            }
        }
    }

    val writeDynamicLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) return@registerForActivityResult
    
        val text = result.data?.getStringExtra("text").orEmpty()
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 1. 处理@提及
                val atUids = text.extractMentions()
                
                // 2. 发布动态
                val dynId = withContext(Dispatchers.IO) {
                    if (atUids.isEmpty()) {
                        DynamicApi.publishTextContent(text)
                    } else {
                        DynamicApi.publishTextContent(text, atUids)
                    }
                }
    
                // 3. 处理发布结果
                handlePublishResult(dynId != -1L) {
                // 4. 获取并添加动态
                    DynamicApi.getDynamic(dynId)?.let { dynamic ->
                        withContext(Dispatchers.Main) {
                            addNewDynamicToList(dynamic)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    MsgUtil.error(e)
                }
            }
        }
    }
    
    // 提取新增动态逻辑
    private fun addNewDynamicToList(dynamic: Dynamic) {
        if (type != "all") return
        
        dynamicList.add(0, dynamic)
        when {
            dynamicList.size == 1 -> dynamicAdapter.notifyDataSetChanged()
            else -> dynamicAdapter.notifyItemRangeInserted(0, 1)
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setMenuClick()
        setOnRefreshListener(this::refreshDynamic)
        setOnLoadMoreListener { addDynamic(type) }
        setPageName("动态")
        
        TutorialHelper.showTutorialList(this, R.array.tutorial_dynamic, 6)
        refreshDynamic()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun refreshDynamic() {
        if (firstRefresh) {
            dynamicList = ArrayList()
        } else {
            offset = 0
            bottomReached = false
            dynamicList.clear()
            dynamicAdapter.notifyDataSetChanged()
        }
        addDynamic(type, true)
    }

    private fun addDynamic(type: String) = addDynamic(type, false)

    @SuppressLint("NotifyDataSetChanged")
    private fun addDynamic(type: String, refresh: Boolean) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val list = ArrayList<Dynamic>()
                offset = DynamicApi.getDynamicList(list, offset, 0, type)
                bottomReached = offset == -1L
                
                withContext(Dispatchers.Main) {
                    setRefreshing(false)
                    val originalSize = dynamicList.size
                    dynamicList.addAll(list)
                    
                    when {
                        firstRefresh -> {
                            firstRefresh = false
                            dynamicAdapter = DynamicAdapter(this@DynamicActivity, dynamicList, recyclerView)
                            setAdapter(dynamicAdapter)
                        }
                        refresh -> dynamicAdapter.notifyDataSetChanged()
                        else -> dynamicAdapter.safeNotifyChanges(originalSize, list.size)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loadFail(e)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DynamicHolder.GO_TO_INFO_REQUEST && resultCode == RESULT_OK) {
            try {
                if (data != null && !isRefreshing) {
                    DynamicHolder.removeDynamicFromList(
                        dynamicList,
                        data.getIntExtra("position", 0) - 1,
                        dynamicAdapter
                    )
                }
            } catch (e: Throwable) {
                Log.w("DynamicActivity", "动态删除异常", e)
                MsgUtil.showMsg("操作失败，请重试")
            }
        }
    }

    companion object {
        fun getRelayDynamicLauncher(activity: BaseActivity): ActivityResultLauncher<Intent> {
            return activity.registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == RESULT_OK) {
                    val safeData = result.data ?: return@registerForActivityResult
                    val text = safeData.getStringExtra("text").orEmpty()
                    val dynamicId = safeData.getLongExtra("dynamicId", -1L)
                    
                    activity.lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            val atUids = text.extractMentions()
                            val dynId = DynamicApi.relayDynamic(
                                if (text.isEmpty()) "转发动态" else text,
                                atUids.ifEmpty { null },
                                dynamicId
                            )
                            
                            withContext(Dispatchers.Main) {
                                activity.handlePublishResult(dynId != -1L)
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                MsgUtil.error(e)
                            }
                        }
                    }
                }
            }
        }
    }
}
