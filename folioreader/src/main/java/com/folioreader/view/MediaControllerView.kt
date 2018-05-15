package com.folioreader.view

import android.content.Context
import android.os.Build
import android.support.v4.content.ContextCompat
import android.text.Html
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import com.folioreader.Config
import com.folioreader.R
import com.folioreader.model.event.MediaOverlayHighlightStyleEvent
import com.folioreader.model.event.MediaOverlaySpeedEvent
import com.folioreader.util.AppUtil
import com.folioreader.util.UiUtil
import kotlinx.android.synthetic.main.view_audio_player.view.*
import org.greenrobot.eventbus.EventBus

/**
 * Created by gautam on 9/5/18.
 */
class MediaControllerView : RelativeLayout {
    private lateinit var config: Config
    private var visible: Boolean = true
    lateinit var callback: MediaControllerCallback
    private var isPlaying: Boolean = false

    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attributes: AttributeSet?) : this(context, attributes, 0)
    constructor(context: Context, attributes: AttributeSet?, defStyle: Int) : super(context, attributes, defStyle) {
        LayoutInflater.from(context).inflate(R.layout.view_audio_player, this)
        init()
    }

    private fun init() {
        config = AppUtil.getSavedConfig(context)
        btn_one_and_half_speed.text = Html.fromHtml(context.getString(R.string.one_and_half_speed))
        btn_half_speed.text = Html.fromHtml(context.getString(R.string.half_speed_text))
        btn_text_undeline_style.text = Html.fromHtml(context.getString(R.string.style_underline))

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            playback_speed_Layout.visibility = View.GONE
        }
        if (config.isNightMode) setNightMode()
        initColors()
        initListeners()
    }

    fun setListeners(callback: MediaControllerCallback) {
        this.callback = callback
    }

    private fun initColors() {
        btn_half_speed.setTextColor(UiUtil.getColorList(context, config.themeColor, R.color.grey_color))
        btn_one_and_half_speed.setTextColor(UiUtil.getColorList(context, config.themeColor, R.color.grey_color))
        btn_twox_speed.setTextColor(UiUtil.getColorList(context, config.themeColor, R.color.grey_color))
        btn_one_x_speed.setTextColor(UiUtil.getColorList(context, config.themeColor, R.color.grey_color))
        btn_text_undeline_style.setTextColor(UiUtil.getColorList(context, config.themeColor, R.color.grey_color))
        btn_backcolor_style.setTextColor(UiUtil.getColorList(context, R.color.white, R.color.grey_color))
        btn_backcolor_style.setBackgroundDrawable(UiUtil.convertColorIntoStateDrawable(context, config.themeColor, android.R.color.transparent))
        btn_text_color_style.setTextColor(UiUtil.getColorList(context, config.themeColor, R.color.grey_color))
        UiUtil.setColorToImage(context, config.themeColor, play_button.drawable)
        UiUtil.setColorToImage(context, config.themeColor, next_button.drawable)
        UiUtil.setColorToImage(context, config.themeColor, prev_button.drawable)
    }

    private fun initListeners() {
        shade.setOnClickListener { show() }
        play_button.setOnClickListener {
            callback.let {
                if (isPlaying) {
                    callback.pause()
                    play_button.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play_icon))
                    UiUtil.setColorToImage(context, config.themeColor, play_button.drawable)
                } else {
                    callback.play()
                    play_button.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.pause_btn))
                    UiUtil.setColorToImage(context, config.themeColor, play_button.drawable)
                }
                isPlaying = !isPlaying
            }
        }
        btn_half_speed.setOnClickListener {
            toggleSpeedControlButtons(true, false, false, false)
            EventBus.getDefault().post(MediaOverlaySpeedEvent(MediaOverlaySpeedEvent.Speed.HALF))
        }

        btn_one_x_speed.setOnClickListener {
            toggleSpeedControlButtons(false, true, false, false)
            EventBus.getDefault().post(MediaOverlaySpeedEvent(MediaOverlaySpeedEvent.Speed.ONE))
        }
        btn_one_and_half_speed.setOnClickListener {
            toggleSpeedControlButtons(false, false, true, false)
            EventBus.getDefault().post(MediaOverlaySpeedEvent(MediaOverlaySpeedEvent.Speed.ONE_HALF))
        }
        btn_twox_speed.setOnClickListener {
            toggleSpeedControlButtons(false, false, false, true)
            EventBus.getDefault().post(MediaOverlaySpeedEvent(MediaOverlaySpeedEvent.Speed.TWO))
        }

        btn_backcolor_style.setOnClickListener {
            toggleTextStyle(true, false, false)
            EventBus.getDefault().post(MediaOverlayHighlightStyleEvent(MediaOverlayHighlightStyleEvent.Style.DEFAULT))
        }

        btn_text_undeline_style.setOnClickListener {
            toggleTextStyle(false, true, false)
            EventBus.getDefault().post(MediaOverlayHighlightStyleEvent(MediaOverlayHighlightStyleEvent.Style.UNDERLINE))

        }

        btn_text_color_style.setOnClickListener {
            toggleTextStyle(false, false, true)
            EventBus.getDefault().post(MediaOverlayHighlightStyleEvent(MediaOverlayHighlightStyleEvent.Style.BACKGROUND))
        }
    }

    private fun toggleTextStyle(backcolor: Boolean, underline: Boolean, textColor: Boolean) {
        btn_backcolor_style.isSelected = backcolor
        btn_text_undeline_style.isSelected = underline
        btn_text_color_style.isSelected = textColor
    }

    private fun toggleSpeedControlButtons(half: Boolean, one: Boolean, oneHalf: Boolean, two: Boolean) {
        btn_half_speed.isSelected = half
        btn_one_x_speed.isSelected = one
        btn_one_and_half_speed.isSelected = oneHalf
        btn_twox_speed.isSelected = two
    }

    fun setPlayButtonDrawable() {
        play_button.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play_icon))
    }

    private fun setNightMode() {
        container.setBackgroundColor(ContextCompat.getColor(context, R.color.night))
    }

    private fun open() {
        container.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_up))
        container.visibility = View.VISIBLE
        shade.visibility = View.VISIBLE
    }

    fun show() {
        if (visible) open() else close()
        visible = !visible
    }

    private fun close() {
        container.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_down))
        container.visibility = View.GONE
        shade.visibility = View.GONE
    }
}