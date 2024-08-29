package com.verse.app.widget.pagertablayout

import android.graphics.Color
import android.view.View
import androidx.annotation.ColorInt
import androidx.lifecycle.MutableLiveData

/**
 * Description :
 *
 * Created by juhongmin on 2023/05/17
 */
open class PagerTabItemV2(
    val title: Int
) {
    var pos: Int = 0

    @ColorInt
    var txtColor: Int = Color.BLACK

    @ColorInt
    var disableTxtColor: Int = Color.BLACK

    var txtStyle: String = BaseTabLayoutV2.Style.BOLD.value

    var disableTxtStyle: String = BaseTabLayoutV2.Style.MEDIUM.value

    var view: View? = null

    // 해당 탭 선택 유부 LiveData
    var isSelected: MutableLiveData<Boolean>? = null
        get() {
            if (field == null) {
                field = MutableLiveData(false)
            }
            return field
        }
}