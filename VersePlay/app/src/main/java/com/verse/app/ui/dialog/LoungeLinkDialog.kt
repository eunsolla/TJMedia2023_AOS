package com.verse.app.ui.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.verse.app.R
import com.verse.app.databinding.DialogLoungeLinkBinding
import com.verse.app.extension.changeVisible
import com.verse.app.extension.dp
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.DeviceProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/18
 */
@AndroidEntryPoint
class LoungeLinkDialog : DialogFragment() {

    @Inject
    lateinit var deviceProvider: DeviceProvider

    private lateinit var binding: DialogLoungeLinkBinding

    interface Listener {
        fun onLoungeLinkConfirm(url: String)
    }

    private var listener: Listener? = null
    private var linkUrl: String = ""

    fun setListener(url: String, listener: Listener): LoungeLinkDialog {
        this.listener = listener

        if (url.isNotEmpty()) {
            this.linkUrl = url
        }

        return this
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.runCatching {
            setLayout(311.dp, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            requestFeature(Window.FEATURE_NO_TITLE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return DataBindingUtil.inflate<DialogLoungeLinkBinding>(
            inflater,
            R.layout.dialog_lounge_link,
            container,
            false
        ).run {
            binding = this
            this.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (linkUrl.isNotEmpty()) {
            binding.etLink.setText(linkUrl)
        } else {
            binding.etLink.setText("http://")
        }

        binding.etLink.addTextChangedListener(onTextChanged = { str, _, _, _ ->
            DLogger.d("LinkText $str")
            linkUrl = str.toString()
            if (!str.isNullOrEmpty()) {
                binding.etInvalidLink.changeVisible(View.INVISIBLE)
            }
        })

        binding.tvConfirm.setOnClickListener {
            if (linkUrl.startsWith("http://") || linkUrl.startsWith("https://")) {
                listener?.onLoungeLinkConfirm(linkUrl)
                dismiss()
            } else {
                binding.etInvalidLink.changeVisible(View.VISIBLE)
            }
        }

        binding.tvCancel.setOnClickListener {
            dismiss()
        }

        binding.ivRemove.setOnClickListener {
            binding.etLink.text?.clear()
        }
    }

    fun simpleShow(fm: FragmentManager) {
        try {
            // 이미 보여지고 있는 Dialog 인경우 스킵
            if (!isAdded) {
                super.show(fm, "LoungeLinkDialog")
            }
        } catch (ex: Exception) {
            // ignore
        }
    }
}
