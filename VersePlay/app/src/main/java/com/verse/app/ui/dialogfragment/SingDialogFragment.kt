package com.verse.app.ui.dialogfragment

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import com.verse.app.R
import com.verse.app.base.fragment.BaseBottomSheetDialogFragment
import com.verse.app.databinding.DialogFragmentSingBinding
import com.verse.app.extension.startAct
import com.verse.app.permissions.SPermissions
import com.verse.app.ui.dialog.CommonDialog
import com.verse.app.ui.main.viewmodel.MainViewModel
import com.verse.app.ui.song.activity.SongMainActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description :  메인 -> (+) 콘텐츠/앨범 업로드 Dialog
 *
 * Created by jhlee on 2023-01-31
 */
@AndroidEntryPoint
class SingDialogFragment : BaseBottomSheetDialogFragment<DialogFragmentSingBinding, MainViewModel>() {

    override val layoutId: Int = R.layout.dialog_fragment_sing
    override val viewModel: MainViewModel by activityViewModels()
    override val bindingVariable: Int = BR.viewModel
    private var singDialogListener: Listener? = null
    private var isMoveToAlbum = false
    private var isMoveToSongMain = false
    interface Listener {
        fun onDismiss()
        fun onMoveToSing()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewModel) {
            //부르기 목록 이동
            moveToSingContentsUpload.observe(viewLifecycleOwner) {
                isMoveToSongMain = true
                requireActivity().startAct<SongMainActivity>()
                dismiss()
            }
            moveToAlbumContentsUpload.observe(viewLifecycleOwner) {
                isMoveToAlbum = true
                dismiss()
                showAlbumContentsUploadPage()
            }
        }
    }

    fun setListener(listener: Listener): SingDialogFragment {
        singDialogListener = listener
        return this
    }

    private fun handlePermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        SPermissions(this)
            .requestPermissions(*permissions.toTypedArray())
            .build { b, _ ->
                if (b) {
                    viewModel.showNetWorkErrorDialog
                } else {
                    // 권한 거부 팝업
                    CommonDialog(requireContext())
                        .setContents(resources.getString(R.string.str_permission_denied))
                        .setPositiveButton(R.string.str_confirm)
                        .show()
                }
            }
    }

    fun show(fm: FragmentManager) {
        try {
            // 이미 보여지고 있는 Dialog 인경우 스킵
            if (!isAdded) {
                super.show(fm, "SingDialogFragment")
            }
        } catch (ex: Exception) {
            // ignore
        }
    }

    override fun onClick(p0: View?) {
        TODO("Not yet implemented")
    }

    override fun onDetach() {
        super.onDetach()
        if(!isMoveToAlbum && !isMoveToSongMain){
            singDialogListener?.onDismiss()
        }else{
            if(isMoveToSongMain){
                singDialogListener?.onMoveToSing()
            }
        }
        viewModel.onLoadingDismiss()
    }
    override fun onDestroy() {
        super.onDestroy()
        viewModel.onLoadingDismiss()
    }
}