package com.folioreader.view

import android.content.Context
import android.support.v4.content.ContextCompat
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
import com.folioreader.util.UiUtil
import kotlinx.android.synthetic.main.folio_search_bar.view.*
import kotlinx.android.synthetic.main.folio_toolbar.view.*


class FolioSearchBar : RelativeLayout {
    private lateinit var config: Config
    private var visible: Boolean = false
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
        search_query.setOnEditorActionListener() { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search_img.performClick()
                true
            }
            false
        }
    }

    private fun initColors() {
//        UiUtil.setColorToImage(context, config.themeColor, cancel_img.drawable)
//        UiUtil.setColorToImage(context, config.themeColor, search_img.drawable)
    }

    private fun initListeners() {
        cancel_img.setOnClickListener {
            callback.disableSearch()
        }
        search_img.setOnClickListener {
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
            search_img.setImageResource(R.drawable.ic_search_white_24px)
        } else {
            search_img.setImageResource(R.drawable.ic_keyboard_arrow_down_white_24)
        }
    }

    fun clearSearchSection() {
        if (search_query != null)
            search_query.text.clear()
        changeSearchIcon(true)

    }

    fun show(isShow: Boolean) {
        if (isShow) this.visibility = View.VISIBLE
        this.animate().translationY((-this.height).toFloat())
                .setInterpolator(DecelerateInterpolator(2f))
                .start()

    }

    fun setNightMode() {
//        search_bar_container.setBackgroundColor(ContextCompat.getColor(context, R.color.black))
    }

    fun setDayMode() {
//        search_bar_container.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
    }

    fun hide(isHide: Boolean) {
        if (isHide) this.visibility = View.GONE
        this.animate().translationY((0f)
                .toFloat())
                .setInterpolator(AccelerateInterpolator(2f))
                .start()
    }

    fun getSearchQuery(): String? {
        if (search_query.text == null) {
            return null
        } else {
            var searchQuery: String = search_query.text.toString().trim()
            if (!searchQuery.isEmpty()) {
                if (searchQuery.contains(" ")) {
                    searchQuery = searchQuery.replace(" ", "%20")
                }
                if (searchQuery.isNotEmpty()) {
                    return searchQuery
                } else {
                    return null
                }
            } else {
                return null
            }
        }
    }

}
