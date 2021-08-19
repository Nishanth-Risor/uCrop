package com.yalantis.ucrop

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import com.uvstudio.him.photofilterlibrary.PhotoFilter

object UCropFilterAlgoUtils {
    enum class FilterType {
        NONE,
        INVERT,
        BW,
        HIGHLIGHT,
        OIL,
        LIGHT
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    fun applyFilter(
        context: Context,
        bitmap: Bitmap,
        filterType:FilterType
    ): Bitmap {
        return when (filterType) {
               FilterType.INVERT -> {
                    applyInvertEffect(context, bitmap)
                }
                FilterType.BW -> {
                    applyGrayEffect(context, bitmap)
                }
                FilterType.HIGHLIGHT -> {
                    applyHighlightEffect(context, bitmap)
                }
                FilterType.LIGHT -> {
                    applyLightEffect(context, bitmap)
                }
                FilterType.OIL -> {
                    applyOilEffect(context, bitmap)
                }
                else -> {
                    bitmap
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun applyOilEffect(context: Context, source: Bitmap): Bitmap {
        return PhotoFilter().thirteen(context, source)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun applyLightEffect(context: Context, source: Bitmap): Bitmap {
        return PhotoFilter().twelve(context, source)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun applyHighlightEffect(context: Context, src: Bitmap): Bitmap {
        return PhotoFilter().three(context, src)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun applyInvertEffect(context: Context, src: Bitmap): Bitmap {
        return PhotoFilter().one(context, src)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun applyGrayEffect(context: Context, bm: Bitmap): Bitmap {
        return PhotoFilter().two(context, bm)
    }

}
