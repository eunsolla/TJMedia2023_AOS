package com.verse.app.base.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.verse.app.base.activity.ActivityResult
import com.verse.app.base.viewmodel.BaseViewModel
import com.verse.app.base.viewmodel.FragmentViewModel
import com.verse.app.contants.LoadingDialogState
import com.verse.app.ui.dialog.LoadingDialog

/**
 * Description : BaseFragment Class
 *
 * Created by jhlee on 2023-01-01
 */
abstract class BaseFragment<Binding : ViewDataBinding, VM : BaseViewModel> : Fragment(),
    LifecycleOwner {

    private var _binding: Binding? = null
    val binding get() = _binding!!

    abstract val layoutId: Int
    abstract val viewModel: VM
    abstract val bindingVariable: Int
    private var isActivityViewModel = false
    private val loadingDialog: LoadingDialog by lazy { LoadingDialog(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return DataBindingUtil.inflate<Binding>(
            inflater,
            layoutId,
            container,
            false
        ).apply {
            lifecycleOwner = viewLifecycleOwner
            setVariable(bindingVariable, viewModel)
            _binding = this

            runCatching {
                ViewModelProvider(requireActivity())[viewModel::class.java]
            }.onFailure {
                isActivityViewModel = false
            }.onSuccess {
                isActivityViewModel = true
            }
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // BaseFragment 상속 받는 ViewModel 이 전부다 FragmentViewModel 로 넘어 가면
        // BaseFragment<Binding : ViewDataBinding, VM : FragmentViewModel> 변경 해야함
        if (viewModel is FragmentViewModel) {
            with(viewModel) {
                startLoadingDialog.observe(viewLifecycleOwner) { state ->
                    performLoadingDialog(state)
                }

                showToastStringMsg.observe(viewLifecycleOwner) {
                    showToast(it)
                }

                showToastIntMsg.observe(viewLifecycleOwner) {
                    showToast(it)
                }

                initView()
            }

            (viewModel as FragmentViewModel).startActivityPage.observe(viewLifecycleOwner) {
                startActivityAndAnimation(it)
            }
        } else {
            if (!isActivityViewModel) {
                with(viewModel) {
                    startLoadingDialog.observe(viewLifecycleOwner) { state ->
                        performLoadingDialog(state)
                    }

                    showToastStringMsg.observe(viewLifecycleOwner) {
                        showToast(it)
                    }

                    showToastIntMsg.observe(viewLifecycleOwner) {
                        showToast(it)
                    }

                    initView()
                }
            }
        }
    }

    protected open fun initView() {}

    /**
     * 로딩 프로그래스 바 노출 유무
     */
    protected fun performLoadingDialog(state: LoadingDialogState) {
        loadingDialog.setState(state)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadingDialog.setState(LoadingDialogState.DISMISS)
        // Binding Memory Leak 방어 코드.
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()

        if (viewModel is FragmentViewModel) {
            viewModel.clearDisposable()
        } else {
            // Activity ViewModel 아닌경우에만 ClearDisposable
            if (!isActivityViewModel) {
                viewModel.clearDisposable()
            }
        }
    }

    /**
     *  Toast Msg
     */
    fun showToast(@StringRes strId: Int) {
        showToast(getString(strId))
    }

    @MainThread
    fun showToast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    /**
     * Start Activity And Animation
     * @param result 페이지 이동할 데이터
     * @see ActivityResult
     */
    private fun startActivityAndAnimation(result: ActivityResult) {
        startActivity(result.getIntent(requireContext()))
        if (result.enterAni != -1 && result.exitAni != -1) {
            activity?.overridePendingTransition(result.enterAni, result.exitAni)
        }
    }
}