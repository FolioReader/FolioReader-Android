package com.folioreader.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.RelativeLayout
import com.folioreader.Config
import com.folioreader.R
import com.folioreader.util.AppUtil
import kotlinx.android.synthetic.main.folio_search_bar.view.*

class FolioSearchBar : RelativeLayout {
    private lateinit var config: Config
    var isForSearch: Boolean = true
    lateinit var callback: FolioSearchBarCallback

    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attributes: AttributeSet?) : this(context, attributes, 0)
    constructor(context: Context, attributes: AttributeSet?, defStyle: Int) : super(context, attributes, defStyle) {
        LayoutInflater.from(context).inflate(R.layout.folio_search_bar, this)
        init()
    }

    private fun init() {
        config = AppUtil.getSavedConfig(context)
        if (config.isNightMode) setNightMode() else setDayMode()
        initColors()
        initListeners()
        edit_query.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                btn_query_search.performClick()
                true
            } else
                false
        }
    }

    private fun initColors() {
//        UiUtil.setColorToImage(context, config.themeColor, cancel_img.drawable)
//        UiUtil.setColorToImage(context, config.themeColor, search_img.drawable)
    }

    private fun initListeners() {
        btn_disable.setOnClickListener {
            callback.disableSearch()
        }
        btn_query_search.setOnClickListener {
            if (isForSearch)
                callback.showSearch(getSearchQuery())
            else
                callback.goNextResult()
        }
    }

    fun setListeners(callback: FolioSearchBarCallback) {
        this.callback = callback
    }

    fun changeSearchIcon(doSearch: Boolean) {
        if (doSearch) {
            btn_query_search.setImageResource(R.drawable.ic_search_white_24px)
        } else {
            btn_query_search.setImageResource(R.drawable.ic_keyboard_arrow_down_white_24)
        }
    }

    fun clearSearchSection() {
        if (edit_query != null)
            edit_query.text.clear()
        changeSearchIcon(true)

    }

    fun show(isShow: Boolean) {
        if (isShow) this.visibility = View.VISIBLE
        this.animate().translationY((-this.height).toFloat())
                .setInterpolator(DecelerateInterpolator(2f))
                .start()

    }

    private fun setNightMode() {
//        search_bar_container.setBackgroundColor(ContextCompat.getColor(context, R.color.black))
    }

    private fun setDayMode() {
//        search_bar_container.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
    }

    fun hide(isHide: Boolean) {
        if (isHide) this.visibility = View.GONE
        this.animate().translationY((0f)
                .toFloat())
                .setInterpolator(AccelerateInterpolator(2f))
                .start()
    }

    private fun getSearchQuery(): String? {
        if (edit_query.text == null) {
            return null
        } else {
            var searchQuery: String = edit_query.text.toString().trim()
            return if (!searchQuery.isEmpty()) {
                if (searchQuery.contains(" ")) {
                    searchQuery = searchQuery.replace(" ", "%20")
                }
                if (searchQuery.isNotEmpty()) {
                    searchQuery
                } else {
                    null
                }
            } else {
                null
            }
        }
    }
}
