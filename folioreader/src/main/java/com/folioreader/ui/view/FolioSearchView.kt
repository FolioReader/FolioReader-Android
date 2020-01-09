package com.folioreader.ui.view

import android.app.SearchManager
import android.content.ComponentName
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.folioreader.Config
import com.folioreader.R
import com.folioreader.util.UiUtil

class FolioSearchView : SearchView {

    companion object {
        @JvmField
        val LOG_TAG: String = FolioSearchView::class.java.simpleName
    }

    private lateinit var searchAutoComplete: SearchView.SearchAutoComplete

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun init(componentName: ComponentName, config: Config) {
        Log.v(LOG_TAG, "-> init")

        val searchManager: SearchManager = context.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        setSearchableInfo(searchManager.getSearchableInfo(componentName))
        setIconifiedByDefault(false)

        adjustLayout()
        applyTheme(config)
    }

    private fun adjustLayout() {
        Log.v(LOG_TAG, "-> adjustLayout")

        // Hide searchHintIcon
        val searchMagIcon: View = findViewById(R.id.search_mag_icon)
        searchMagIcon.layoutParams = LinearLayout.LayoutParams(0, 0)

        // Remove left margin of search_edit_frame
        val searchEditFrame: View = findViewById(R.id.search_edit_frame)
        (searchEditFrame.layoutParams as ViewGroup.MarginLayoutParams).leftMargin = 0
    }

    private fun applyTheme(config: Config) {
        Log.v(LOG_TAG, "-> applyTheme")

        val searchCloseButton: ImageView = findViewById(R.id.search_close_btn)
        UiUtil.setColorIntToDrawable(config.themeColor, searchCloseButton.drawable)

        searchAutoComplete = findViewById(R.id.search_src_text)
        UiUtil.setEditTextCursorColor(searchAutoComplete, config.themeColor)
        UiUtil.setEditTextHandleColor(searchAutoComplete, config.themeColor)
        searchAutoComplete.highlightColor = ColorUtils.setAlphaComponent(config.themeColor, 85)
        if (config.isNightMode) {
            searchAutoComplete.setTextColor(ContextCompat.getColor(context, R.color.night_title_text_color))
            searchAutoComplete.setHintTextColor(ContextCompat.getColor(context, R.color.night_text_color))
        } else {
            searchAutoComplete.setHintTextColor(ContextCompat.getColor(context, R.color.edit_text_hint_color))
        }
    }

    fun setDayMode() {
        searchAutoComplete.setTextColor(ContextCompat.getColor(context, R.color.black))
    }

    fun setNightMode() {
        searchAutoComplete.setTextColor(ContextCompat.getColor(context, R.color.white))
    }
}