package com.verse.app.utility.provider

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.annotation.ArrayRes
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.verse.app.repository.preferences.AccountPref
import dagger.hilt.android.qualifiers.ApplicationContext
import okio.IOException
import java.io.InputStream
import java.util.Locale
import javax.inject.Inject

/**
 * Description : Resource Provider Class
 *
 * Created by jhlee on 2023-01-01
 */
interface ResourceProvider {
    fun getContext(): Context
    fun getRes(): Resources
    fun getDrawable(@DrawableRes resId: Int): Drawable?
    fun getDimen(@DimenRes resId: Int): Int
    fun getDimenFloat(@DimenRes resId: Int): Float
    fun getColor(@ColorRes color: Int): Int
    fun getString(@StringRes resId: Int): String
    fun getStringArray(@ArrayRes resId: Int): Array<String>
    fun getAsset(fileName: String): InputStream?
}

class ResourceProviderImpl @Inject constructor(
    @ApplicationContext private val ctx: Context,
    private val accountPref: AccountPref
) : ResourceProvider {

    private var currentLanguage = ""

    private var currentResource: Resources? = null

    override fun getRes(): Resources = ctx.resources

    override fun getContext() = ctx

    override fun getDrawable(resId: Int) = AppCompatResources.getDrawable(ctx, resId)

    override fun getDimen(resId: Int) = getRes().getDimensionPixelSize(resId)

    override fun getDimenFloat(resId: Int) = getRes().getDimension(resId)

    override fun getColor(color: Int) = ContextCompat.getColor(ctx, color)

    override fun getString(resId: Int): String {
        // ko, en
        val language = accountPref.getPreferenceLocaleLanguage().lowercase()

        return if (language == currentLanguage) {
            val res = currentResource ?: ctx.resources
            res.getString(resId)

        } else {
            currentLanguage = language
            val config = Configuration(ctx.resources.configuration)
            config.setLocale(Locale(language))
            currentResource = ctx.createConfigurationContext(config).resources
            val res = currentResource ?: ctx.resources
            res.getString(resId)
        }
    }

    override fun getStringArray(resId: Int): Array<String> {
        // ko, en
        val language = accountPref.getPreferenceLocaleLanguage().lowercase()

        return if (language == currentLanguage) {
            val res = currentResource ?: ctx.resources
            res.getStringArray(resId)

        } else {
            currentLanguage = language
            val config = Configuration(ctx.resources.configuration)
            config.setLocale(Locale(language))
            currentResource = ctx.createConfigurationContext(config).resources
            val res = currentResource ?: ctx.resources
            res.getStringArray(resId)
        }
    }

    override fun getAsset(fileName: String): InputStream? {
        return try {
            getRes().assets.open(fileName)
        } catch (ex: IOException) {
            null
        }
    }
}