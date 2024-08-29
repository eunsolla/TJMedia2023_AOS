package com.verse.app.ui.header

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.contants.HeaderType
import com.verse.app.extension.getFragmentAct
import com.verse.app.extension.initBinding
import com.verse.app.extension.startAct
import com.verse.app.livedata.NonNullLiveData
import com.verse.app.ui.search.activity.SearchMainActivity
import com.verse.app.ui.song.activity.SongListActivity
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.DeviceProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Description : DrawerLayout 기반의 HeaderView
 *
 * Created by jhlee on 2023-01-01
 */
@AndroidEntryPoint
class HeaderView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : CoordinatorLayout(ctx, attrs, defStyleAttr), LifecycleOwner, LifecycleEventObserver {

    @Inject
    lateinit var deviceProvider: DeviceProvider

    private var type: HeaderType = HeaderType.NONE

    private val lifecycleRegistry: LifecycleRegistry by lazy { LifecycleRegistry(this) }

    lateinit var activity: FragmentActivity

    private var _binding: ViewDataBinding? = null
    val binding: ViewDataBinding get() = _binding!!

    private val _headerTitle: NonNullLiveData<String> by lazy { NonNullLiveData("") }
    val headerTitle: LiveData<String> get() = _headerTitle

    private val _contentsCount: NonNullLiveData<Int> by lazy { NonNullLiveData(0) } //헤더 영역에  컨텐츠 수가 필요할 경우 사용. (ex : 댓글)
    val contentsCount: LiveData<Int> get() = _contentsCount

    init {

//        fitsSystemWindows = true

        if (!isInEditMode) {

            context.obtainStyledAttributes(attrs, R.styleable.HeaderView).run {
                try {
                    type = HeaderType.values()[getInt(R.styleable.HeaderView_headerType, 0)]
                } finally {
                }
                recycle()
            }

            _binding = initBinding(type.id, this) {
                setVariable(BR.headerView, this@HeaderView)
            }

            if (context is FragmentActivity) {
                activity = context as FragmentActivity
                activity.lifecycle.addObserver(this)
            }

            //top padding
//            setPadding(0, deviceProvider.getStatusBarHeight(), 0, 0)

        } else {
            val tempView = LayoutInflater.from(context).inflate(HeaderType.NONE.id, this, false)
            addView(tempView)
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        lifecycleRegistry.handleLifecycleEvent(event)

        when (event) {
            Lifecycle.Event.ON_CREATE -> {
            }

            Lifecycle.Event.ON_RESUME -> {
            }

            Lifecycle.Event.ON_PAUSE -> {
            }

            Lifecycle.Event.ON_STOP -> {
            }

            Lifecycle.Event.ON_DESTROY -> {
            }

            else -> {
            }
        }
    }


    override fun getLifecycle() = lifecycleRegistry

    fun setHeaderUserName(userName: String?) {
        if (!userName.isNullOrEmpty()) {
            DLogger.d("UserName $userName")
//            _userName.value = userName
        }
    }

    /**
     * set Header Title
     * @param title
     */
    fun setHeaderTitle(title: String?) {
        if (!title.isNullOrEmpty()) {
            _headerTitle.value = title
        }
    }

    /**
     * set Contents Count
     * @param title
     */
    fun setContentsCount(count: Int?) {
        _contentsCount.value = count ?: 0
    }

    /**
     * Back
     * @param title
     */
    fun onBack() {
        if (::activity.isInitialized) {
            if (activity != null) {
                activity.onBackPressed()
            }
        } else {
            context.getFragmentAct()?.let {
                it.onBackPressed()
            }
        }
    }

    fun doSearch() {
        activity.startAct<SearchMainActivity>()
    }

    /**
     * 헤더 필터
     * 1.노래 목록
     */
    fun onShowFilter() {
        if (activity is SongListActivity) {
            (activity as SongListActivity).showFilter()
        }
    }

}