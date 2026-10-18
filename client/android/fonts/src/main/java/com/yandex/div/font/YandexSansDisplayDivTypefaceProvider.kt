package com.yandex.div.font

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.yandex.div.core.font.DivTypefaceProvider
import com.yandex.div.font.typeface.R
import javax.inject.Inject

class YandexSansDisplayDivTypefaceProvider @Inject constructor(
    private val context: Context
) : DivTypefaceProvider {

    override fun getRegular(): Typeface {
        return ResourcesCompat.getFont(context, R.font.irsans) ?: Typeface.DEFAULT
    }

    override fun getMedium(): Typeface {
        return ResourcesCompat.getFont(context, R.font.irsans) ?: Typeface.DEFAULT
    }

    override fun getLight(): Typeface {
        return ResourcesCompat.getFont(context, R.font.irsans) ?: Typeface.DEFAULT
    }

    override fun getBold(): Typeface {
        return ResourcesCompat.getFont(context, R.font.irsans) ?: Typeface.DEFAULT
    }
}
