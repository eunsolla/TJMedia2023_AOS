package com.verse.app.tracking.ui

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.verse.app.R
import com.verse.app.tracking.interceptor.TrackingDataManager
import com.verse.app.tracking.interceptor.model.TrackingHttpEntity
import com.verse.app.tracking.ui.detail.TrackingDetailRootFragment
import com.verse.app.tracking.ui.list.TrackingListFragment
import java.lang.ref.WeakReference

/**
 * Description : HTTP 로그 정보 보여주는 BottomSheetDialog
 * @see <a href="https://github.com/sieunju/httptracking">참고</a>
 * Created by juhongmin on 2022/03/29
 */
internal class TrackingBottomSheetDialog : BottomSheetDialogFragment() {

    companion object {
        var IS_SHOW_WIFI_SHARE_MSG = false
    }

    internal interface DismissListener {
        fun onDismiss()
    }

    private val windowManager: WindowManager by lazy { requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager }
    private var listener: DismissListener? = null

    private var detailData: WeakReference<TrackingHttpEntity>? = null

    // [s] View
    private lateinit var tvTitle: AppCompatTextView
    private lateinit var ivBack: AppCompatImageView
    private lateinit var ivClear: AppCompatTextView
    private lateinit var rootContainer: FrameLayout
    // [e] View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetStyle)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            setupRatio(bottomSheetDialog)
        }
        return dialog
    }

    override fun onStart() {
        super.onStart()
        if (dialog is BottomSheetDialog) {
            (dialog as BottomSheetDialog).runCatching {
                behavior.skipCollapsed = true
                behavior.isDraggable = true
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.d_tracking_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(view) {
            tvTitle = findViewById(R.id.tvTitle)
            ivBack = findViewById(R.id.ivBack)
            rootContainer = findViewById(R.id.fragment)
            ivClear = findViewById(R.id.ivClear)

            ivClear.setOnClickListener { handleClear() }
            ivBack.setOnClickListener { handleBack() }
        }

        moveToListFragment()
    }

    private fun moveToListFragment() {
        setHeaderTitle("List")
        ivBack.visibility = View.GONE
        ivClear.visibility = View.VISIBLE

        val findFragment = childFragmentManager.fragments.firstOrNull { it is TrackingListFragment }
        if (findFragment is TrackingListFragment) {
            childFragmentManager.popBackStack()
        } else {
            childFragmentManager.beginTransaction().apply {
                replace(R.id.fragment, TrackingListFragment.newInstance())
                addToBackStack(null)
                commit()
            }
        }
    }

    /**
     * HTTP Tracking 상세 진입
     */
    fun moveToDetailFragment(clickData: TrackingHttpEntity) {
        detailData?.clear()
        detailData = null

        detailData = WeakReference(clickData)

        ivBack.visibility = View.VISIBLE
        ivClear.visibility = View.GONE

        childFragmentManager.beginTransaction().apply {
            add(R.id.fragment, TrackingDetailRootFragment.newInstance())
            addToBackStack(null)
            commit()
        }
    }

    /**
     * set Header Title
     */
    fun setHeaderTitle(title: CharSequence) {
        tvTitle.text = title
    }

    fun setListener(listener: DismissListener): TrackingBottomSheetDialog {
        this.listener = listener
        return this
    }

    fun getTempDetailData(): TrackingHttpEntity? {
        return detailData?.get()
    }

    override fun dismiss() {
        super.dismiss()
        this.listener?.onDismiss()
    }

    /**
     * 목록 화면으로 돌아가는 처리
     */
    private fun handleBack() {
        moveToListFragment()
    }

    /**
     * 데이터 리스트 클리어
     */
    private fun handleClear() {
        TrackingDataManager.getInstance().clear()
        moveToListFragment()
    }

    /**
     * BottomSheet Device 비율에 맞게 높이값 조정 하는 함수
     */
    private fun setupRatio(bottomSheetDialog: BottomSheetDialog) {
        val bottomSheet =
            bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as View
        val behavior = BottomSheetBehavior.from(bottomSheet)
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = getBottomSheetHeight()
        bottomSheet.layoutParams = layoutParams
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    /**
     * Height 85%
     */
    private fun getBottomSheetHeight(): Int {
        val realContentsHeight = getDeviceHeight()
            .minus(getNavigationBarHeight())
            .minus(getStatusBarHeight())
        return (realContentsHeight * 0.9F).toInt()
    }

    private fun getDeviceHeight(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowManager.currentWindowMetrics.bounds.height()
        } else {
            val displayMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.heightPixels
        }
    }

    private fun getNavigationBarHeight(): Int {
        val id: Int = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (id > 0) {
            resources.getDimensionPixelSize(id)
        } else 0
    }

    private fun getStatusBarHeight(): Int {
        val id: Int = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (id > 0) {
            resources.getDimensionPixelSize(id)
        } else 0
    }
}
