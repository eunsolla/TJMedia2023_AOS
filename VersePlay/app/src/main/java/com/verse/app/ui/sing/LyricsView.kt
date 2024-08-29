package com.verse.app.ui.sing

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import com.verse.app.BR
import com.verse.app.R
import com.verse.app.databinding.ViewLyricsBinding
import com.verse.app.extension.initBinding
import com.verse.app.utility.DLogger
import dagger.hilt.android.AndroidEntryPoint

/**
 * Description : DrawerLayout 기반의 HeaderView
 *
 * Created by jhlee on 2023-01-01
 */
@AndroidEntryPoint
class LyricsView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(ctx, attrs, defStyleAttr), LifecycleOwner, LifecycleEventObserver {

    private val lifecycleRegistry: LifecycleRegistry by lazy { LifecycleRegistry(this) }

    private val activity: FragmentActivity by lazy { context as FragmentActivity }

    private var _binding: ViewLyricsBinding? = null
    val binding: ViewLyricsBinding get() = _binding!!

    init {
        if (!isInEditMode) {
            _binding = initBinding(R.layout.view_lyrics, this)
        } else {
            val tempView = LayoutInflater.from(context).inflate(R.layout.view_lyrics, this, false)
            addView(tempView)
        }

        activity.lifecycle.addObserver(this)
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
                binding.rvSing?.adapter = null
            }

            else -> {
            }
        }
    }


    override fun getLifecycle() = lifecycleRegistry
}