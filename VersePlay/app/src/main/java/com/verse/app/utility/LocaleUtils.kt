package com.verse.app.utility

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.LocaleList
import com.facebook.FacebookSdk.getApplicationContext
import com.verse.app.contants.NationLanType
import com.verse.app.utility.manager.LoginManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class LocaleUtils @Inject constructor(
    @ApplicationContext
    val ctx: Context,
) : ContextWrapper(ctx) {
    companion object {
        const val DEFAULT_TIME_ZONE = "GMT-0:00"
        const val DTE_FORMAT_SHORT = "yyyy-MM-dd"
        const val DTE_FORMAT = "yyyy-MM-dd HH:mm"

        fun wrap(context: Context): LocaleUtils {
            var context = context
            val locale = Locale(getCurrentSetLanguage(context))
            val res = context.resources
            val configuration = res.configuration
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                configuration.setLocale(locale)
                val localeList = LocaleList(locale)
                LocaleList.setDefault(localeList)
                configuration.setLocales(localeList)

                DLogger.d("N locale : " + locale.getLanguage());
                context = context.createConfigurationContext(configuration)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {

                DLogger.d("JELLY_BEAN_MR1 locale : " + locale.getLanguage());
                configuration.setLocale(locale)
                context = context.createConfigurationContext(configuration)
            } else {

                DLogger.d("else locale " + locale.getLanguage());
                configuration.locale = locale
                res.updateConfiguration(configuration, res.displayMetrics)
            }
            return LocaleUtils(context)
        }

        fun getCurrentSetLanguage(context: Context): String? {
            val pref = context.getSharedPreferences("pref", MODE_PRIVATE)

            val result = pref.getString("PREFERENCE_APP_LOCALE_LANGUAGE", Locale.getDefault().language)

            DLogger.d("getCurrentSetLanguage : ${result}")

            return result
        }

        private fun getCurrentSetCountry(): String? {
            val pref = getApplicationContext().getSharedPreferences("pref", MODE_PRIVATE)

            val result = pref.getString("PREFERENCE_APP_LOCALE_COUNTRY", Locale.getDefault().language)

            DLogger.d("getCurrentSetLanguage : ${result}")

            return result
        }

        fun getLoginDeviceName(authTpCd: String): String? {

            val pref = getApplicationContext().getSharedPreferences("pref", MODE_PRIVATE)
            val locale = pref.getString("PREFERENCE_APP_LOCALE_LANGUAGE", Locale.getDefault().language)

            var result = ""

            when (authTpCd) {
                LoginManager.LoginType.facebook.code -> {
                    result = if (locale == NationLanType.KO.code) {
                        "페이스북 로그인"
                    } else {
                        "Facebook Login"
                    }
                }
                LoginManager.LoginType.google.code -> {
                    result = if (locale == NationLanType.KO.code) {
                        "구글 로그인"
                    } else {
                        "Google Login"
                    }
                }
                LoginManager.LoginType.kakao.code -> {
                    result = if (locale == NationLanType.KO.code) {
                        "카카오 로그인"
                    } else {
                        "Kakao Login"
                    }
                }
                LoginManager.LoginType.naver.code -> {
                    result = if (locale == NationLanType.KO.code) {
                        "네이버 로그인"
                    } else {
                        "Naver Login"
                    }
                }
                LoginManager.LoginType.twitter.code -> {
                    result = if (locale == NationLanType.KO.code) {
                        "트위터 로그인"
                    } else {
                        "Twitter Login"
                    }
                }
                else -> {
                    result = if (locale == NationLanType.KO.code) {
                        "스냅챗 로그인"
                    } else {
                        "Snapchat Login"
                    }
                }
            }

            DLogger.d("getLoginDeviceName : $result")
            return result
        }

        fun getTimeZone(): String? {
            val timeZone: TimeZone = TimeZone.getDefault()
            val calendar: Calendar = GregorianCalendar.getInstance(timeZone)
            val offsetInMillis: Int = timeZone.getOffset(calendar.getTimeInMillis())
            DLogger.d("gmt offset(sec)", offsetInMillis.toString())

            //초에서 분으로 환산
            var offset = String.format(
                Locale.getDefault(), "%02d",
                Math.abs(offsetInMillis / 60000)
            )
            DLogger.d("gmt offset(min)", offset)
            offset = (if (offsetInMillis >= 0) "+" else "-") + offset
            val strGmt: String = timeZone.getDisplayName(false, TimeZone.SHORT)
            val strId: String = timeZone.getID()

            DLogger.d("Local : ${offset} / ${strGmt} Local Time Zone Id : ${strId}")
            return strId
        }

        private fun getLocale(): Locale {
            val locale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                getApplicationContext().resources.configuration.locales.get(0)
            } else {
                getApplicationContext().resources.configuration.locale
            }

            val displayCountry: String = locale.displayCountry
            val country: String = locale.country
            val language: String = locale.language

            DLogger.d("[TEST] Locale.displayCountry : ${displayCountry}")
            DLogger.d("[TEST] Locale.country : ${country}")
            DLogger.d("[TEST] Locale.language : ${language}")

            return locale
        }

        /**
         * isCompareDate (true : 같은 날짜는 시:분까지 표기 / false : 모두 시간정보까지 표기)
         */
        fun getLocalizationTime(timeString: String, isCompareDate: Boolean): String {
            var resultTimeString: String = ""
            var isConvertTime: Boolean = false

            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
            var currentTime = System.currentTimeMillis()
            var toDay = Date(currentTime)

            if (getCurrentSetCountry() != getLocale().country) {
                isConvertTime = true
                format.timeZone = TimeZone.getTimeZone("GMT-0:00")
            }

            val timeZoneZero = format.parse(timeString)

            val date1 = SimpleDateFormat(DTE_FORMAT_SHORT, Locale.getDefault()).format(timeZoneZero!!)
            val date2 = SimpleDateFormat(DTE_FORMAT_SHORT, Locale.getDefault()).format(toDay)

            DLogger.d("[TEST] Setting Locale : ${getCurrentSetCountry()}")

            if (isCompareDate) {
                val compResult = date1.compareTo(date2)

                if (compResult == 0) {
                    //val datePattern: String = DateFormat.getBestDateTimePattern(Locale.getDefault(), DTE_FORMAT)
                    //val resultFormat = SimpleDateFormat(datePattern, Locale.getDefault())
                    //resultFormat.timeZone = TimeZone.getDefault()
                    //resultFormat.applyLocalizedPattern(datePattern)
                    if (isConvertTime) {
                        resultTimeString = SimpleDateFormat(DTE_FORMAT, getLocale()).format(timeZoneZero)
                    } else {
                        val defaultFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
                        resultTimeString =
                            SimpleDateFormat(DTE_FORMAT, Locale.getDefault()).format(defaultFormat.parse(timeString)!!)
                    }

                    // 국가 별 선호 패턴 사용
                    //resultTimeString = resultFormat.format(timeZoneZero)
                } else {
                    // 국가 별 선호 패턴 사용
                    //val datePattern: String = DateFormat.getBestDateTimePattern(Locale.getDefault(), DTE_FORMAT_SHORT)
                    //val resultFormat = SimpleDateFormat(datePattern, Locale.getDefault())
                    //resultFormat.timeZone = TimeZone.getDefault()
                    //resultFormat.applyLocalizedPattern(datePattern)
                    if (isConvertTime) {
                        resultTimeString = SimpleDateFormat(DTE_FORMAT_SHORT, getLocale()).format(timeZoneZero)
                    } else {
                        val defaultFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
                        resultTimeString =
                            SimpleDateFormat(DTE_FORMAT_SHORT, Locale.getDefault()).format(defaultFormat.parse(timeString)!!)
                    }

                    // 국가 별 선호 패턴 사용
                    //resultTimeString = resultFormat.format(timeZoneZero)
                }
            } else {
                //val datePattern: String = DateFormat.getBestDateTimePattern(Locale.getDefault(), DTE_FORMAT)
                //val resultFormat = SimpleDateFormat(datePattern, Locale.getDefault())
                //resultFormat.timeZone = TimeZone.getDefault()
                //resultFormat.applyLocalizedPattern(datePattern)

                if (isConvertTime) {
                    resultTimeString = SimpleDateFormat(DTE_FORMAT, getLocale()).format(timeZoneZero)
                } else {
                    val defaultFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
                    resultTimeString =
                        SimpleDateFormat(DTE_FORMAT, Locale.getDefault()).format(defaultFormat.parse(timeString)!!)
                }

                // 국가 별 선호 패턴 사용
                //resultTimeString = resultFormat.format(timeZoneZero)
            }

            return resultTimeString
        }
    }
}