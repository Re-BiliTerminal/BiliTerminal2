package com.huanli233.biliterminal2.activity.settings.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.activity.SplashActivity
import com.huanli233.biliterminal2.databinding.FragmentQrLoginBinding

class QRLoginFragment : Fragment() {
    private var _binding: FragmentQrLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: QRLoginViewModel by viewModels()
    private var fromSetup: Boolean = false
    private var qrScale: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fromSetup = arguments?.getBoolean("from_setup", false) ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQrLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeViewModel()
        viewModel.refreshQrCode()
    }

    private fun setupClickListeners() {
        binding.jump.setOnClickListener {
            if (fromSetup) {
                startActivity(Intent(requireContext(), SplashActivity::class.java))
            }
            activity?.finish()
        }

        binding.special.setOnClickListener {
            val intent = Intent(requireContext(), SpecialLoginActivity::class.java).apply {
                putExtra("from_setup", fromSetup)
            }
            startActivity(intent)
            activity?.finish()
        }

        binding.qrImage.setOnClickListener {
            handleQrImageClick()
        }
    }

    private fun handleQrImageClick() {
        val uiState = viewModel.uiState.value
        if (uiState?.isRefreshEnabled == true) {
            binding.qrImage.setImageResource(R.mipmap.loading)
            binding.qrImage.isEnabled = false
            viewModel.refreshQrCode()
        } else {
            adjustQrCodeSize()
        }
    }

    private fun adjustQrCodeSize() {
        when (qrScale) {
            0 -> {
                binding.guideline33.setGuidelinePercent(0.00f)
                binding.guideline34.setGuidelinePercent(1.00f)
                qrScale = 1
            }
            1 -> {
                binding.guideline33.setGuidelinePercent(0.30f)
                binding.guideline34.setGuidelinePercent(0.70f)
                qrScale = 2
            }
            2 -> {
                binding.guideline33.setGuidelinePercent(0.15f)
                binding.guideline34.setGuidelinePercent(0.85f)
                qrScale = 0
            }
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner, Observer { state ->
            binding.qrImage.isEnabled = true
            
            // Update QR code image
            state.qrBitmap?.let {
                binding.qrImage.setImageBitmap(it)
            }

            // Update status text
            binding.scanStat.text = when (state.state) {
                QRLoginState.NONE -> getString(R.string.login_qrcode_reuesting)
                QRLoginState.WAITING -> getString(R.string.login_qrcode_wating)
                QRLoginState.EXPIRED -> getString(R.string.login_qrcode_expired)
                QRLoginState.SCANNED -> getString(R.string.login_qrcode_scanned)
                QRLoginState.LOGGED_IN -> getString(R.string.login_qrcode_logining)
                QRLoginState.ERROR_NETWORK -> getString(R.string.login_qrcode_network_error)
                QRLoginState.ERROR_API -> getString(R.string.login_qrcode_api_error)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
