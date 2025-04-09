package com.huanli233.biliterminal2.activity.article

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.activity.base.BaseActivity
import com.huanli233.biliterminal2.activity.opus.OpusActivity
import com.huanli233.biliterminal2.activity.opus.OpusViewModel
import com.huanli233.biliterminal2.api.apiResultNonNull
import com.huanli233.biliterminal2.api.bilibiliApi
import com.huanli233.biliterminal2.api.toResultNonNull
import com.huanli233.biliterminal2.databinding.ActivitySimpleViewpagerBinding
import com.huanli233.biliterminal2.util.extensions.LoadState
import com.huanli233.biliterminal2.util.extensions.invisible
import com.huanli233.biliterminal2.util.extensions.showError
import com.huanli233.biliterminal2.util.extensions.visible
import com.huanli233.biliwebapi.api.interfaces.IArticleApi
import com.huanli233.biliwebapi.bean.article.ArticleInfo
import com.huanli233.biliwebapi.bean.opus.Opus
import kotlinx.coroutines.launch
import kotlin.getValue

class ArticleInfoActivity : BaseActivity() {

    val viewModel: ArticleInfoViewModel by viewModels {
        ArticleInfoViewModelFactory(intent.getLongExtra("cvid", -1))
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySimpleViewpagerBinding.inflate(layoutInflater)

        setPageName(getString(R.string.article_detail))
        binding.loading.visible()
        binding.viewPager.invisible()

        viewModel.data.observe(this) {
            when (it) {
                is LoadState.Success -> {
                    startActivity(
                        Intent(this, OpusActivity::class.java).apply {
                            putExtra("opusId", it.data.dynIdStr)
                            intent.extras?.let { extras -> putExtras(extras) }
                        }
                    )
                    finish()
                }

                is LoadState.Error -> {
                    binding.loading.showError()
                }

                else -> Unit
            }
        }
    }
}

@Suppress("DEPRECATION")
class ArticleInfoViewModel(val cvid: Long): ViewModel() {

    private val _data = MutableLiveData<LoadState<ArticleInfo>>()
    val data: LiveData<LoadState<ArticleInfo>> get() = _data

    init {
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch {
            bilibiliApi.api(IArticleApi::class) { getArticle(cvid) }
                .apiResultNonNull()
                .onSuccess {
                    _data.postValue(LoadState.Success(it))
                }.onFailure {
                    _data.postValue(LoadState.Error(it))
                }
        }
    }

}

class ArticleInfoViewModelFactory(private val cvid: Long) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OpusViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ArticleInfoViewModel(cvid) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}