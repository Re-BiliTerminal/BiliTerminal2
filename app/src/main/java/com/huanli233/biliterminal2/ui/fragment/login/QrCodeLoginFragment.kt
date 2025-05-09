package com.huanli233.biliterminal2.ui.fragment.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.ethanhua.skeleton.SkeletonScreen
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.data.menu.MenuConfigManager
import com.huanli233.biliterminal2.databinding.FragmentLoginQrcodeBinding
import com.huanli233.biliterminal2.ui.activity.login.EXTRA_NAME_FROM_SETUP
import com.huanli233.biliterminal2.ui.activity.login.LoginActivity
import com.huanli233.biliterminal2.ui.fragment.base.BaseFragment
import com.huanli233.biliterminal2.ui.utils.crossFadeSetText
import com.huanli233.biliterminal2.ui.utils.image.transition
import com.huanli233.biliterminal2.ui.utils.showSkeleton
import com.huanli233.biliterminal2.utils.MsgUtil
import com.huanli233.biliterminal2.utils.QRCodeUtil
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QrCodeLoginFragment(): BaseFragment() {

    private lateinit var binding: FragmentLoginQrcodeBinding
    private val viewModel: QrCodeLoginViewModel by viewModels()

    private var qrScale: Int = 0
    private var skeletonScreen: SkeletonScreen? = null

    private val fromSetup by lazy { arguments?.getBoolean(EXTRA_NAME_FROM_SETUP) == true }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginQrcodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.qrcodeState.observe(viewLifecycleOwner) {
            it.onLoading {
                skeletonScreen = binding.qrcodeImage.showSkeleton(R.layout.layout_skeleton_qrcode)
                binding.qrcodeStatus.crossFadeSetText(getString(R.string.requesting_qrcode))
            }.onSuccess {
                skeletonScreen?.hide()
                Glide.with(this)
                    .load(QRCodeUtil.createQRCodeBitmap(it, 320, 320))
                    .transition()
                    .into(binding.qrcodeImage)
            }.onApiError {
                skeletonScreen?.hide()
                binding.qrcodeImage.setImageResource(R.mipmap.loading_2233_error)
                binding.qrcodeStatus.crossFadeSetText("$it")
            }.onNonApiError {
                skeletonScreen?.hide()
                binding.qrcodeImage.setImageResource(R.mipmap.loading_2233_error)
                binding.qrcodeStatus.crossFadeSetText(getString(R.string.login_qrcode_network_error))
            }
        }
        viewModel.qrCodeLoginState.observe(viewLifecycleOwner) {
            it.onSuccess {
                if (it.finished) {
                    MsgUtil.showMsg(getString(R.string.login_success))
                    callAfterLoggedIn()
                } else {
                    binding.qrcodeStatus.crossFadeSetText(
                        when (it.code) {
                            0 -> getString(R.string.login_qrcode_logining)
                            86090 -> getString(R.string.login_qrcode_scanned)
                            86101 -> getString(R.string.login_qrcode_wating)
                            86038 -> getString(R.string.login_qrcode_expired)
                            else -> getString(R.string.login_qrcode_api_error)
                        }
                    )
                }
            }
        }

        binding.qrcodeImage.setOnClickListener {
            if (viewModel.needRefresh.value == true) {
                viewModel.loadQrcode()
            } else if (viewModel.qrcodeState.value?.isSuccess == true) {
                val guidelineLeft = binding.guideline33
                val guidelineRight = binding.guideline34

                when (qrScale) {
                    0 -> {
                        guidelineLeft.setGuidelinePercent(0.00f)
                        guidelineRight.setGuidelinePercent(1.00f)
                        qrScale = 1
                    }
                    1 -> {
                        guidelineLeft.setGuidelinePercent(0.30f)
                        guidelineRight.setGuidelinePercent(0.70f)
                        qrScale = 2
                    }
                    2 -> {
                        guidelineLeft.setGuidelinePercent(0.15f)
                        guidelineRight.setGuidelinePercent(0.85f)
                        qrScale = 0
                    }
                }
            }
        }
        binding.skip.apply {
            visibility = if (fromSetup) View.VISIBLE else View.GONE
            setOnClickListener { callAfterLoggedIn() }
        }
        binding.loginImport.setOnClickListener {
            (requireActivity() as? LoginActivity)?.navigateToImport()
        }
    }

    private fun callAfterLoggedIn() {
        if (fromSetup) {
            startActivity(Intent(context, MenuConfigManager.readMenuConfig().firstActivityClass))
        }
        activity?.finish()
    }

}