package com.verse.app.ui.dialogfragment

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.verse.app.R
import com.verse.app.base.fragment.BaseDialogFragment
import com.verse.app.databinding.DialogFragmentFilterBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description : 필터 공통 Fragment Dialog
 *
 * Created by jhlee on 2023-01-31
 */
@AndroidEntryPoint
class FilterDialogFragment : BaseDialogFragment<DialogFragmentFilterBinding>() {

    override val layoutId: Int = R.layout.dialog_fragment_filter
    private var listener: Listener? = null

    var btnOneText: String? = null
    var btnTwoText: String? = null
    var btnThreeText: String? = null
    var btnFourText: String? = null
    var btnFiveText: String? = null

    var isSelectedOne: Boolean = false
    var isSelectedTwo: Boolean = false
    var isSelectedThree: Boolean = false


    companion object {
        const val POSITION_1: Int = 1
        const val POSITION_2: Int = 2
        const val POSITION_3: Int = 3
        const val POSITION_4: Int = 4
        const val POSITION_5: Int = 5
    }

    interface Listener {
        fun onClick(which: Int)
    }

    /**
     * 버튼명_1
     */
    fun setBtnOneName(btnText: String, isSelected: Boolean): FilterDialogFragment {
        if (!btnText.isNullOrEmpty()) {
            btnOneText = btnText
            isSelectedOne = isSelected
        }
        return this
    }

    /**
     * 버튼명_2
     */
    fun setBtnTwoName(btnText: String, isSelected: Boolean): FilterDialogFragment {
        if (!btnText.isNullOrEmpty()) {
            btnTwoText = btnText
            isSelectedTwo = isSelected
        }
        return this
    }

    /**
     * 버튼명_3
     */
    fun setBtnThreeName(btnText: String, isSelected: Boolean): FilterDialogFragment {
        if (!btnText.isNullOrEmpty()) {
            btnThreeText = btnText
            isSelectedThree = isSelected
        }
        return this
    }

    /**
     * 버튼명_4
     */
    fun setBtnFourName(btnText: String): FilterDialogFragment {
        if (!btnText.isNullOrEmpty()) {
            btnFourText = btnText
        }
        return this
    }

    /**
     * 버튼명_5
     */
    fun setBtnFiveName(btnText: String): FilterDialogFragment {
        if (!btnText.isNullOrEmpty()) {
            btnFiveText = btnText
        }
        return this
    }

    fun setListener(listener: Listener): FilterDialogFragment {
        this.listener = listener
        return this
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.let {
            btnOneText?.let { txt ->
                setBtn(it.btnOneTextView, txt, POSITION_1)
            }
            btnTwoText?.let { txt ->
                setBtn(it.btnTwoTextView, txt, POSITION_2)
            }
            btnThreeText?.let { txt ->
                setBtn(it.btnThreeTextView, txt, POSITION_3)
            }
            btnFourText?.let { txt ->
                setBtn(it.btnFourTextView, txt, POSITION_4)
            }
            btnFiveText?.let { txt ->
                setBtn(it.btnFiveTextView, txt, POSITION_5)
            }
        }
    }

    private fun setBtn(view: TextView, btnName: String, position: Int) {
        view.apply {
            setOnClickListener {
                dismiss()
                listener?.onClick(position)
            }
            text = btnName
            visibility = View.VISIBLE
        }

        if (position == POSITION_1) {
            if (isSelectedOne) {
                binding.btnOneTextView.setEnableTxtAndBgColor(
                    ContextCompat.getColor(
                        view.context,
                        R.color.color_2fc2ff
                    )
                )
                binding.btnOneTextView.setEnableTxtColor(
                    ContextCompat.getColor(
                        view.context,
                        R.color.white
                    )
                )
            } else {
                binding.btnOneTextView.setEnableTxtAndBgColor(
                    ContextCompat.getColor(
                        view.context,
                        R.color.color_eaeaea
                    )
                )
                binding.btnOneTextView.setEnableTxtColor(
                    ContextCompat.getColor(
                        view.context,
                        R.color.color_8c8c8c
                    )
                )
            }
        }

        if (position == POSITION_2) {
            if (isSelectedTwo) {
                binding.btnTwoTextView.setEnableTxtAndBgColor(
                    ContextCompat.getColor(
                        view.context,
                        R.color.color_2fc2ff
                    )
                )
                binding.btnTwoTextView.setEnableTxtColor(
                    ContextCompat.getColor(
                        view.context,
                        R.color.white
                    )
                )
            } else {
                binding.btnTwoTextView.setEnableTxtAndBgColor(
                    ContextCompat.getColor(
                        view.context,
                        R.color.color_eaeaea
                    )
                )
                binding.btnTwoTextView.setEnableTxtColor(
                    ContextCompat.getColor(
                        view.context,
                        R.color.color_8c8c8c
                    )
                )
            }
        }

        if (position == POSITION_3) {
            if (isSelectedThree) {
                binding.btnThreeTextView.setEnableTxtAndBgColor(
                    ContextCompat.getColor(
                        view.context,
                        R.color.color_2fc2ff
                    )
                )
                binding.btnThreeTextView.setEnableTxtColor(
                    ContextCompat.getColor(
                        view.context,
                        R.color.white
                    )
                )
            } else {
                binding.btnThreeTextView.setEnableTxtAndBgColor(
                    ContextCompat.getColor(
                        view.context,
                        R.color.color_eaeaea
                    )
                )
                binding.btnThreeTextView.setEnableTxtColor(
                    ContextCompat.getColor(
                        view.context,
                        R.color.color_8c8c8c
                    )
                )
            }
        }

        if (position == POSITION_4) {
            binding.layoutFourTextView.visibility = View.VISIBLE
            binding.layoutFourTextView.setOnClickListener {
                dismiss()
                listener?.onClick(position)
                binding.layoutFourTextView.isSelected = true
            }

            if (binding.layoutFourTextView.isSelected) {
                binding.layoutFourTextView.setBackgroundColor(
                    ContextCompat.getColor(
                        view.context,
                        R.color.color_2fc2ff
                    )
                )
                binding.btnFourTextView.setTextColor(
                    ContextCompat.getColor(
                        view.context,
                        R.color.white
                    )
                )
                binding.ivFourTextView.setImageDrawable(
                    ContextCompat.getDrawable(
                        view.context,
                        R.drawable.settings_w
                    )
                )
            }
        }
        if (position == POSITION_5) {
            binding.layoutFiveTextView.visibility = View.VISIBLE
            binding.layoutFiveTextView.setOnClickListener {
                dismiss()
                listener?.onClick(position)
                binding.layoutFiveTextView.isSelected = true
            }

            if (binding.layoutFiveTextView.isSelected) {
                binding.layoutFiveTextView.setBackgroundColor(
                    ContextCompat.getColor(
                        view.context,
                        R.color.color_2fc2ff
                    )
                )
                binding.btnFiveTextView.setTextColor(
                    ContextCompat.getColor(
                        view.context,
                        R.color.white
                    )
                )
                binding.ivFiveTextView.setImageDrawable(
                    ContextCompat.getDrawable(
                        view.context,
                        R.drawable.settings_w
                    )
                )
            }
        }
    }

    override fun show(manager: FragmentManager, tag: String?) {
        super.show(manager, tag)
    }

    override fun show(transaction: FragmentTransaction, tag: String?): Int {
        return super.show(transaction, tag)
    }

    override fun showNow(manager: FragmentManager, tag: String?) {
        super.showNow(manager, tag)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }

    override fun onClick(p0: View?) {
        TODO("Not yet implemented")
    }

    fun simpleShow(fm: FragmentManager) {
        super.show(fm, "FilterDialogFragment")
    }
}