package com.folioreader.view

import android.app.Activity
import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.RelativeLayout
import com.folioreader.Config
import com.folioreader.R
import com.folioreader.util.AppUtil
import com.folioreader.util.UiUtil
import kotlinx.android.synthetic.main.folio_toolbar.view.*

/**
 * Created by gautam on 15/5/18.
 */
class FolioToolbar : RelativeLayout {
    private lateinit var config: Config
    private var visible: Boolean = false
    lateinit var callback: FolioToolbarCallback

    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attributes: AttributeSet?) : this(context, attributes, 0)
    constructor(context: Context, attributes: AttributeSet?, defStyle: Int) : super(context, attributes, defStyle) {
        LayoutInflater.from(context).inflate(R.layout.folio_toolbar, this)
        init()
    }

    private fun init() {
        config = AppUtil.getSavedConfig(context)
        if (config.isNightMode) setNightMode() else setDayMode()
        if (!config.isShowTts) btn_speaker.visibility = View.GONE
        initColors()
        initListeners()
    }

    private fun initColors() {
        UiUtil.setColorToImage(context, config.themeColor, btn_close.drawable)
        UiUtil.setColorToImage(context, config.themeColor, btn_drawer.drawable)
        UiUtil.setColorToImage(context, config.themeColor, btn_config.drawable)
        UiUtil.setColorToImage(context, config.themeColor, btn_speaker.drawable)
    }

    private fun initListeners() {
        btn_drawer.setOnClickListener {
            callback.startContentHighlightActivity()
        }
        btn_close.setOnClickListener {
            (context as Activity).finish()
        }
        btn_config.setOnClickListener {
            callback.showConfigBottomSheetDialogFragment()
        }
        btn_speaker.setOnClickListener {
            callback.showMediaController()
        }
    }

    fun setListeners(callback: FolioToolbarCallback) {
        this.callback = callback
    }

    fun setTitle(title: String?) {
        title?.let {
            label_center?.text = title
        }
    }

    fun showOrHideIfVisible() {
        if (visible) {
            hide()
        } else {
            show()
        }
        visible = !visible
    }

    private fun show() {
        this.animate().translationY(0f)
                .setInterpolator(DecelerateInterpolator(2f))
                .start()
    }

    fun setNightMode() {
        toolbar_container.setBackgroundColor(ContextCompat.getColor(context, R.color.black))
        label_center.setTextColor(ContextCompat.getColor(context, R.color.white))
    }

    fun setDayMode() {
        toolbar_container.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
        label_center.setTextColor(ContextCompat.getColor(context, R.color.black))
    }

    private fun hide() {
        this.animate().translationY((-this.height)
                .toFloat())
                .setInterpolator(AccelerateInterpolator(2f))
                .start()
    }
}
