package com.verse.app.ui.scheme

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.verse.app.R
import com.verse.app.utility.DLogger

class SchemeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scheme)
        DLogger.d("SchemeActivitySchemeActivitySchemeActivitySchemeActivity")

    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }
}