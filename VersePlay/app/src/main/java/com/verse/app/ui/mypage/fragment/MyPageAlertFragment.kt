import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.base.fragment.BaseFragment
import com.verse.app.databinding.FragmentMypageAlertBinding
import com.verse.app.ui.mypage.viewmodel.MypageAlartViewModel

class MyPageAlertFragment : BaseFragment<FragmentMypageAlertBinding, MypageAlartViewModel>() {

    override val layoutId: Int = R.layout.fragment_mypage_alert
    override val viewModel: MypageAlartViewModel by activityViewModels()
    override val bindingVariable: Int = BR.viewModel

    private lateinit var callback: OnBackPressedCallback

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewModel) {
            onLoadingShow()
        }
    }

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    }

}