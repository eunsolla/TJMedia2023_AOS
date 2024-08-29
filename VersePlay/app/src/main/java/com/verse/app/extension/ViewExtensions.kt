package com.verse.app.extension

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.activity.ComponentActivity
import androidx.annotation.AnimRes
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.findFragment
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.verse.app.utility.DLogger
import com.verse.app.utility.provider.DeviceProvider
import dagger.hilt.android.internal.managers.ViewComponentManager
import java.text.DecimalFormat
import java.util.*
import kotlin.math.roundToInt

/**
 * CustomView initBinding Function.
 * Invalid LayoutId or Not LifecycleOwner Extension
 * T -> Not Support Class
 * @param layoutId View Layout Id
 * @param lifecycleOwner View LifecycleOwner
 * @param isAdd 이 함수 내에서 View 를 추가 할건지? Default true,
 * @param apply 고차 함수. (Optional)
 */
inline fun <reified T : ViewDataBinding> ViewGroup.initBinding(
    @LayoutRes layoutId: Int,
    lifecycleOwner: LifecycleOwner,
    isAdd: Boolean = true,
    apply: T.() -> Unit = {},
): T {

    val viewRoot = LayoutInflater.from(context).inflate(layoutId, this, false)

    val binding: T =
        DataBindingUtil.bind<T>(viewRoot) ?: throw NullPointerException("Invalid LayoutId ...")

    binding.lifecycleOwner = lifecycleOwner

    if (isAdd) {
        addView(binding.root)
    }

    binding.apply(apply)

    return binding
}

/**
 * RecyclerView Clear Function
 */
fun RecyclerView.clear() {
    this.adapter = null
    for (i in itemDecorationCount - 1 downTo 0) {
        removeItemDecorationAt(i)
    }
    removeAllViewsInLayout()
}

/**
 * Convert Dp to Int
 * ex. 5.dp
 */
val String.Unit: String
    get() = convert(this, 10000, 1)

val String.Comma: String
    get() = DecimalFormat("#,###").format(this)

/**
 * Convert Dp to Int
 * ex. 5.dp
 */
val Int.dp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()

/**
 * Convert Dp to Float
 * ex. 5F.dp
 */
val Float.dp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    )

/**
 * RecyclerView notifyItemChanged Func
 * @param pos Adapter Position
 * @param item Object Item.
 */
fun <T : Any> ViewParent.recyclerViewNotifyPayload(pos: Int, item: T?) {
    if (this is RecyclerView) {
        if (item == null) {
            this.adapter?.notifyItemChanged(pos)
        } else {
            this.adapter?.notifyItemChanged(pos, item)
        }
    }
}

/**
 * RecyclerView notifyItemChanged Func
 * @param pos Adapter Position
 * @param item Object Item.
 */
fun ViewParent.recyclerViewNotifyRemoved(pos: Int, itemCount: Int? = -1) {
    if (this is RecyclerView) {
        if (itemCount == null || itemCount <= -1) {
            this.adapter?.notifyItemRemoved(pos)
        } else {
            this.adapter?.notifyItemRangeRemoved(pos, itemCount)
        }
    }
}

/**
 * RecyclerView notifyDataSetChanged Func
 * @param pos Adapter Position
 * @param item Object Item.
 */
fun ViewParent.recyclerViewNotifyAll() {
    if (this is RecyclerView) {
        this.adapter?.notifyDataSetChanged()
    }
}

/**
 * ViewPager  notifyItemChanged Func
 * @param pos Adapter Position
 * @param item Object Item.
 */
fun <T : Any> ViewParent.viewPagerNotifyPayload(pos: Int, item: T?) {
    if (this is ViewPager2) {
        if (item == null) {
            this.adapter?.notifyItemChanged(pos)
        } else {
            this.adapter?.notifyItemChanged(pos, item)
        }
    }
}

/**
 * ViewPager RecyclerView notifyDataSetChanged Func
 * @param pos Adapter Position
 * @param item Object Item.
 */
fun ViewParent.viewPagerRecyclerViewNotifyAll() {
    if (this is ViewPager2) {
        DLogger.d("Navi - viewPagerRecyclerViewNotifyAll")
        this.adapter?.notifyDataSetChanged()
    }
}


/**
 * ViewPager2 Get Fragment
 * @param viewPager : ViewPager2
 */
fun Fragment.getFragment(viewPager: ViewPager2): Fragment? {
    val pos = viewPager.currentItem
    return getFragment(pos, viewPager)
}

/**
 * TargetFragment Get 함수
 * @return T Nullable
 */
inline fun <reified T : Fragment> Fragment.getCurrentFragment(viewPager: ViewPager2): T? {
    val pos = viewPager.currentItem
    val fragment = getFragment(pos, viewPager)
    if (fragment is T) {
        return fragment
    }
    return null
}

/**
 * TargetFragment Get 함수
 * @return T Nullable
 */
inline fun <reified T : Fragment> Fragment.getFragment(tagName: String): T? {
    val fragment = childFragmentManager.findFragmentByTag(tagName)
    if (fragment is T) {
        return fragment
    }
    return null
}

/**
 * ViewPager2 Get Child Fragment
 * @param pos : Current Pos
 * @param viewPager : ViewPager2
 */
fun Fragment.getFragment(pos: Int, viewPager: ViewPager2): Fragment? {
    return childFragmentManager.findFragmentByTag("f${viewPager.adapter?.getItemId(pos)}")
}

/**
 * ViewPager2 Get Fragment
 * @param viewPager : ViewPager2
 */
fun AppCompatActivity.getFragment(viewPager: ViewPager2): Fragment? {
    val pos = viewPager.currentItem
    return getFragment(pos, viewPager)
}

/**
 * ViewPager2 Get Fragment
 * @param pos : Current Pos
 * @param viewPager : ViewPager2
 */
fun AppCompatActivity.getFragment(pos: Int, viewPager: ViewPager2): Fragment? {
    return supportFragmentManager.findFragmentByTag("f${viewPager.adapter?.getItemId(pos)}")
}

/**
 * ViewPager2 Request Layout
 */
fun ViewPager2.requestLayout(view: View?) {
    if (view == null) return
    if (view is ViewGroup) {
        view.post {
            DLogger.d("Child Height ${view.height}")
            val widthSpec =
                View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY)
            val heightSpec =
                View.MeasureSpec.makeMeasureSpec(view.height, View.MeasureSpec.UNSPECIFIED)
            view.measure(widthSpec, heightSpec)
            DLogger.d("ViewPager2 RequestLayout ${this.layoutParams.height}   ${view.measuredHeight}")
            if (this.layoutParams.height != view.measuredHeight) {
                this.layoutParams = (this.layoutParams).also { lp ->
                    lp.height = view.measuredHeight
                }
            }
        }
    }
}

/**
 * reduce scroll sensitivity of ViewPager2
 */
fun ViewPager2.reduceDragSensitivity() {
    val recyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
    recyclerViewField.isAccessible = true
    val recyclerView = recyclerViewField.get(this) as RecyclerView

    val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
    touchSlopField.isAccessible = true
    val touchSlop = touchSlopField.get(recyclerView) as Int
    touchSlopField.set(recyclerView, touchSlop * 3)
}

/**
 * isFakeDragging Check Current Item Func
 */
fun ViewPager2.currentItem(pos: Int, smoothScroll: Boolean = true) {
    if (isFakeDragging) {
        endFakeDrag()
    }
    setCurrentItem(pos, smoothScroll)
}

@ColorInt
fun String.alphaColor(alpha: Float): Int {
    val rounded = (((alpha * 100).roundToInt() / 100.0) * 255).roundToInt()
    var hex = Integer.toHexString(rounded).toUpperCase(Locale.ROOT)
    if (hex.length == 1) {
        hex = "0$hex"
    }
    return Color.parseColor("#$hex$this")
}

/**
 * fragment 생성
 */
inline fun <reified T : Fragment> initFragment(args: Bundle.() -> Unit = {}): T {
    val fragment = T::class.java.newInstance()
    val bundle = Bundle()
    bundle.args()
    fragment.arguments = bundle
    return fragment
}

tailrec fun Context?.getActivity(): Activity? = when (this) {
    is Activity -> this
    else -> {
        val contextWrapper = this as? ContextWrapper
        contextWrapper?.baseContext?.getActivity()
    }
}

val View.lifecycleOwner: LifecycleOwner?
    get() = try {
        val fragment = findFragment<Fragment>()
        fragment.viewLifecycleOwner
    } catch (e: IllegalStateException) {
        when (val activity = context.getActivity()) {
            is ComponentActivity -> activity
            else -> null
        }
    }

fun Context.getLifecycleOwner(): LifecycleOwner {
    return try {
        this as LifecycleOwner
    } catch (exception: ClassCastException) {
        (this as ContextWrapper).baseContext as LifecycleOwner
    }
}

/**
 * Activity-> Fragment Set
 */
inline fun FragmentActivity.replaceFragment(
    @AnimRes enterAni: Int = -1,
    @AnimRes exitAni: Int = -1,
    @AnimRes popEnterAni: Int = -1,
    @AnimRes popExitAni: Int = -1,
    containerId: Int = -1,
    fragment: Fragment,
    tagName: String = ""
) {
    if (containerId > -1) {
        supportFragmentManager.beginTransaction().apply {
            if (enterAni != -1 && exitAni != -1 && popEnterAni != -1 && popExitAni != -1) {
                setCustomAnimations(enterAni, exitAni, popEnterAni, popExitAni)
            }
            replace(containerId, fragment, tagName)
            addToBackStack(null)
            commit()
        }
    }
}

/**
 * Activity-> Fragment Set
 */
@SuppressLint("ResourceType")
inline fun FragmentActivity.replaceFragment(
    @IdRes containerId: Int = -1,
    fragment: Fragment,
) {
    if (containerId > -1) {
        supportFragmentManager.beginTransaction().apply {
            replace(containerId, fragment)
            commit()
        }
    }
}

@SuppressLint("ResourceType")
inline fun FragmentActivity.replaceBackStackFragment(
    @IdRes containerId: Int = -1,
    fragment: Fragment,
) {
    if (containerId > -1) {
        supportFragmentManager.beginTransaction().apply {
            addToBackStack(null)
            replace(containerId, fragment)
            commit()
        }
    }
}


/**
 * Activity-> Fragment Set
 */
inline fun FragmentActivity.addFragment(
    containerId: Int = -1,
    fragment: Fragment,
    @AnimRes enterAni: Int = -1,
    @AnimRes exitAni: Int = -1,
    @AnimRes popEnterAni: Int = -1,
    @AnimRes popExitAni: Int = -1,
) {
    if (containerId > -1) {
        supportFragmentManager.beginTransaction().apply {
            if (enterAni != -1 && exitAni != -1 && popEnterAni != -1 && popExitAni != -1) {
                setCustomAnimations(enterAni, exitAni, popEnterAni, popExitAni)
            }
            add(containerId, fragment)
            addToBackStack(null)
            commit()
        }
    }
}


/**
 * Activity -> Fragment remove
 */
inline fun FragmentActivity.popBackStackFragment() {
    if (supportFragmentManager.fragments.isNotEmpty()) {
        supportFragmentManager.popBackStack()
    }
}

/**
 * Root Fragment -> child fragment remove
 */
inline fun Fragment.popBackStackParentFragment() {
    if (parentFragmentManager.fragments.isNotEmpty()) {
        parentFragmentManager.popBackStack()
    }
}

/**
 * Root Fragment -> child fragment Set
 */
inline fun Fragment.replaceChildFragment(
    containerId: Int = -1,
    fragment: Fragment,
) {
    if (containerId > -1) {

        childFragmentManager.beginTransaction()
            .replace(containerId, fragment, fragment.tag)
            .commit()
    }
}


/**
 * Root Fragment -> child fragment Set
 */
inline fun Fragment.replaceChildFragment(
    containerId: Int = -1,
    fragment: Fragment,
    tagName: String,
) {
    if (containerId > -1) {
        childFragmentManager.beginTransaction()
            .replace(containerId, fragment, tagName)
            .commit()
    }
}

inline fun Fragment.addChildFragment(
    containerId: Int = -1,
    fragment: Fragment,
    tagName: String? = "",
    @AnimRes enterAni: Int = -1,
    @AnimRes exitAni: Int = -1,
    @AnimRes popEnterAni: Int = -1,
    @AnimRes popExitAni: Int = -1,
) {
    if (containerId > -1) {
        childFragmentManager.beginTransaction().apply {
            if (enterAni != -1 && exitAni != -1 && popEnterAni != -1 && popExitAni != -1) {
                setCustomAnimations(enterAni, exitAni, popEnterAni, popExitAni)
            }
            add(containerId, fragment, tagName)
            addToBackStack(null)
            commit()
        }
    }
}


/**
 * Root Fragment -> child fragment remove
 */
inline fun Fragment.popBackStackChildFragment() {
    if (childFragmentManager.fragments.isNotEmpty()) {
        childFragmentManager.popBackStack()
    }
}

/**
 * Root Fragment -> child fragment remove
 */
inline fun Fragment.clearChildFragment() {
    if (childFragmentManager.fragments.isNotEmpty()) {
        childFragmentManager.fragments.clear()
    }
}

// Fragment 에서 childFragmentManager를 사용한 FragmentResultListener Extension
fun Fragment.setChildFragmentResultListener(
    requestKey: String,
    listener: ((resultKey: String, bundle: Bundle) -> Unit),
) {
    childFragmentManager.setFragmentResultListener(requestKey, this, listener)
}

// FragmentActivity 에서 FragmentResultListener Extension
fun FragmentActivity.setFragmentResultListener(
    requestKey: String,
    listener: ((resultKey: String, bundle: Bundle) -> Unit),
) {
    supportFragmentManager.setFragmentResultListener(requestKey, this, listener)
}

/**
 * Fragment View Top margin Set  (statusbar height)
 */
inline fun Fragment.updateStatusBarMargin(
    view: View,
    deviceProvider: DeviceProvider,
) {

    if (view == null) {
        return
    }

    if (view.parent is ConstraintLayout) {
        view.updateLayoutParams<ConstraintLayout.LayoutParams> {
            topMargin = deviceProvider.getStatusBarHeight()
        }
    }

    if (view.parent is LinearLayout) {
        view.updateLayoutParams<LinearLayout.LayoutParams> {
            topMargin = deviceProvider.getStatusBarHeight()
        }
    }

    if (view.parent is RelativeLayout) {
        view.updateLayoutParams<RelativeLayout.LayoutParams> {
            topMargin = deviceProvider.getStatusBarHeight()
        }
    }

    if (view.parent is CoordinatorLayout) {
        view.updateLayoutParams<CoordinatorLayout.LayoutParams> {
            topMargin = deviceProvider.getStatusBarHeight()
        }
    }
}

/**
 * Activity 키보드 Show
 */
inline fun Activity.showKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.showSoftInput(view, 0)
}

/**
 * fragment 키보드 Show
 */
inline fun Fragment.showKeyboard(context: Context, view: View) {
    val inputMethodManager =
        context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.showSoftInput(view, 0)
}

inline fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

inline fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

inline fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    view.clearFocus()
}

inline fun convert(countString: String?, criteriaNum: Long, decimalPointSize: Int): String {
    val suffix = arrayOf("", "K", "M", "B", "T")
    val number = java.lang.Double.valueOf(countString)
    return if (number < criteriaNum) {
        DecimalFormat("#,###").format(number)
    } else {
        var r = DecimalFormat("##0.000000E0").format(number)
        val unit =
            suffix[Character.getNumericValue(r[r.length - 1]) / 3]
        r = r.replace("E[0-9]".toRegex(), unit)
        var valueString = r
        if (!TextUtils.isEmpty(unit)) {
//            if (!unit.equalsIgnoreCase("")) {
            valueString = valueString.replace(unit.toRegex(), "")
        }
        val changeValue = java.lang.Double.valueOf(valueString)
        when (decimalPointSize) {
            0 -> valueString = DecimalFormat("#").format(changeValue)
            1 -> valueString = DecimalFormat("#.0").format(changeValue)
            2 -> valueString = DecimalFormat("#.00").format(changeValue)
            3 -> valueString = DecimalFormat("#.000").format(changeValue)
            4 -> valueString = DecimalFormat("#.0000").format(changeValue)
            5 -> valueString = DecimalFormat("#.00000").format(changeValue)
            6 -> valueString = DecimalFormat("#.000000").format(changeValue)
        }

        valueString + unit
    }
}

/**
 * 뷰 높이값 셋팅 하는 함수
 */
fun View.setReHeight(height: Int) {
    val params = layoutParams
    params.height = height
    layoutParams = params
}

/**
 * 뷰 너비값 재 셋팅 하는 함수
 */
fun View.setReWidth(width: Int) {
    val params = layoutParams
    params.width = width
    layoutParams = params
}

/**
 * 뷰 너비값, 높이값 제 샛팅하는 함수
 */
fun View.setReSize(width: Int, height: Int) {
    val params = layoutParams
    params.width = width
    params.height = height
    layoutParams = params
}

/**
 * ViewPager2 LimitSize 를 안주고 스와이프 하면서
 * 캐시 사이즈 남도록 하는 함수
 * ViewPager2 -> Fragment 에서 사용
 * onDestroy 호출 안되게 하는 함수
 * @param size 원하는 캐시 사이즈
 */
fun ViewPager2.setViewPagerCache(size: Int) {
    runCatching {
        val rv = (this.getChildAt(0) as RecyclerView)
        rv.layoutManager?.isItemPrefetchEnabled = false
        rv.setItemViewCacheSize(size)
    }
}

/**
 * FragmentActivity 가져오는 View 기반 확장 함수
 * @see FragmentActivity
 * @return FragmentActivity Nullable
 */
fun View.getFragmentActivity(): FragmentActivity? {
    if (context is FragmentActivity) {
        return context as FragmentActivity
    } else if (context is ViewComponentManager.FragmentContextWrapper) {
        // Hilt Fragment or ViewHolder Case..
        var tmpContext = this.context
        while (tmpContext is ContextWrapper &&
            tmpContext !is FragmentActivity
        ) {
            tmpContext = tmpContext.baseContext as ContextWrapper
        }

        if (tmpContext is FragmentActivity) {
            return tmpContext
        }
    } else if (context is ContextWrapper) {
        var tmpContext = this.context
        while (tmpContext is ContextWrapper) {
            if (tmpContext is FragmentActivity) {
                return tmpContext
            }
            tmpContext = tmpContext.baseContext
        }
    }
    return null
}

/**
 * TargetFragment Get 함수
 * @return T Nullable
 */
inline fun <reified T : Fragment> FragmentManager.getFragment(): T? {
    val list = fragments
    for (idx in 0 until list.size) {
        val fragment: Fragment = fragments[idx]
        if (fragment is T) {
            return fragment
        }
    }
    return null
}

/**
 * View Visible / Gone / InVisible 상태를 변경해야 하는 경우
 * Set 처리하는 함수
 * @param changeVisible Target Visible State
 */
fun View.changeVisible(changeVisible: Int) {
    if (visibility != changeVisible) {
        visibility = changeVisible
    }
}

fun View.changeVisible(isVisible: Boolean) {
    changeVisible(if (isVisible) View.VISIBLE else View.GONE)
}

fun View.changeInVisible(isVisible: Boolean) {
    changeVisible(if (isVisible) View.VISIBLE else View.INVISIBLE)
}
fun View.showSoftKeyboard(delay: Long = 0) {
    postDelayed({
        requestFocus()
        val manager = (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
        manager.showSoftInput(this, 0)
    }, delay)
}


@SuppressLint("ClickableViewAccessibility")
fun EditText.requestDisallowInterceptTouchEvent() {
    this.setOnTouchListener { v, event ->
        v.parent.requestDisallowInterceptTouchEvent(true)
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_UP -> v.parent.requestDisallowInterceptTouchEvent(false)
        }
        false
    }
}

