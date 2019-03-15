package com.folioreader.ui.view

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import androidx.core.view.ViewCompat
import com.google.android.material.appbar.AppBarLayout
import timber.log.Timber

class FolioAppBarLayout : AppBarLayout {
    var navigationBarHeight: Int = 0
    var insets: Rect? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {

        ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
            Timber.v("-> onApplyWindowInsets")
            // For API level 20 and above

            this.insets = Rect(
                insets.systemWindowInsetLeft, insets.systemWindowInsetTop,
                insets.systemWindowInsetRight, insets.systemWindowInsetBottom
            )

            navigationBarHeight = insets.systemWindowInsetBottom

            setMargins(
                insets.systemWindowInsetLeft, insets.systemWindowInsetTop,
                insets.systemWindowInsetRight
            )
            insets
        }
    }

    override fun fitSystemWindows(insets: Rect?): Boolean {
        Timber.v("-> fitSystemWindows")
        // For API level 19 and below

        this.insets = Rect(insets)

        navigationBarHeight = insets!!.bottom

        setMargins(insets.left, insets.top, insets.right)
        return super.fitSystemWindows(insets)
    }

    private fun setMargins(left: Int, top: Int, right: Int) {

        val marginLayoutParams = layoutParams as MarginLayoutParams
        marginLayoutParams.leftMargin = left
        marginLayoutParams.topMargin = top
        marginLayoutParams.rightMargin = right
        layoutParams = marginLayoutParams
    }

    fun setTopMargin(top: Int) {
        val marginLayoutParams = layoutParams as MarginLayoutParams
        marginLayoutParams.topMargin = top
        layoutParams = marginLayoutParams
    }
}