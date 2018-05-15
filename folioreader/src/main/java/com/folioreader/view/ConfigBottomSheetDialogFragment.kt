package com.folioreader.view

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.Activity
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.SeekBar
import com.folioreader.Config
import com.folioreader.Constants
import com.folioreader.R
import com.folioreader.model.event.ReloadDataEvent
import com.folioreader.util.AppUtil
import com.folioreader.util.UiUtil
import kotlinx.android.synthetic.main.view_config.*
import org.greenrobot.eventbus.EventBus

/**
 * Created by mobisys2 on 11/16/2016.
 */
class ConfigBottomSheetDialogFragment : BottomSheetDialogFragment() {
    private lateinit var callback: ConfigDialogCallback
    private lateinit var config: Config
    private var isNightMode = false

    interface ConfigDialogCallback {
        fun onOrientationChange(orientation: Int)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.view_config, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.viewTreeObserver.addOnGlobalLayoutListener {
            val dialog = dialog as BottomSheetDialog
            val bottomSheet = dialog.findViewById<View>(android.support.design.R.id.design_bottom_sheet) as FrameLayout?
            val behavior = BottomSheetBehavior.from(bottomSheet!!)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.peekHeight = 0
        }

        config = AppUtil.getSavedConfig(activity)
        initViews()
    }

    override fun onDestroy() {
        super.onDestroy()
        view?.viewTreeObserver?.addOnGlobalLayoutListener(null)
    }

    private fun initViews() {
        inflateView()
        configFonts()
        view_config_font_size_seek_bar.progress = config.fontSize
        configSeekBar()
        selectFont(config.font, false)
        isNightMode = config.isNightMode
        if (isNightMode) {
            container.setBackgroundColor(ContextCompat.getColor(context!!, R.color.night))
        } else {
            container.setBackgroundColor(ContextCompat.getColor(context!!, R.color.white))
        }

        if (isNightMode) {
            view_config_ib_day_mode.isSelected = false
            view_config_ib_night_mode.isSelected = true
            UiUtil.setColorToImage(activity, config.themeColor, view_config_ib_night_mode.drawable)
            UiUtil.setColorToImage(activity, R.color.app_gray, view_config_ib_day_mode.drawable)
        } else {
            view_config_ib_day_mode.isSelected = true
            view_config_ib_night_mode.isSelected = false
            UiUtil.setColorToImage(activity, config.themeColor, view_config_ib_day_mode!!.drawable)
            UiUtil.setColorToImage(activity, R.color.app_gray, view_config_ib_night_mode.drawable)
        }

        callback = activity as ConfigDialogCallback
    }

    private fun inflateView() {
        view_config_ib_day_mode.setOnClickListener {
            isNightMode = true
            toggleBlackTheme()
            view_config_ib_day_mode.isSelected = true
            view_config_ib_night_mode.isSelected = false
            setToolBarColor()
            setAudioPlayerBackground()
            UiUtil.setColorToImage(activity, R.color.app_gray, view_config_ib_night_mode.drawable)
            UiUtil.setColorToImage(activity, config.themeColor, view_config_ib_day_mode.drawable)
        }
        view_config_ib_night_mode.setOnClickListener {
            isNightMode = false
            toggleBlackTheme()
            view_config_ib_day_mode.isSelected = false
            view_config_ib_night_mode.isSelected = true
            UiUtil.setColorToImage(activity, config.themeColor, view_config_ib_day_mode.drawable)
            UiUtil.setColorToImage(activity, R.color.app_gray, view_config_ib_night_mode.drawable)
            setToolBarColor()
            setAudioPlayerBackground()
        }
        view_config_btn_vertical_orientation.isSelected = true
    }

    private fun configFonts() {
        view_config_font_andada.setTextColor(UiUtil.getColorList(activity, config.themeColor, R.color.grey_color))
        view_config_font_lato.setTextColor(UiUtil.getColorList(activity, config.themeColor, R.color.grey_color))
        view_config_font_lora.setTextColor(UiUtil.getColorList(activity, config.themeColor, R.color.grey_color))
        view_config_font_raleway.setTextColor(UiUtil.getColorList(activity, config.themeColor, R.color.grey_color))
        view_config_btn_vertical_orientation.setTextColor(UiUtil.getColorList(activity, config.themeColor, R.color.grey_color))
        view_config_btn_horizontal_orientation.setTextColor(UiUtil.getColorList(activity, config.themeColor, R.color.grey_color))
        view_config_font_andada.setOnClickListener { selectFont(Constants.FONT_ANDADA, true) }
        view_config_font_lato.setOnClickListener { selectFont(Constants.FONT_LATO, true) }
        view_config_font_lora.setOnClickListener { selectFont(Constants.FONT_LORA, true) }
        view_config_font_raleway.setOnClickListener { selectFont(Constants.FONT_RALEWAY, true) }
        view_config_btn_vertical_orientation.setOnClickListener {
            callback.onOrientationChange(1)
            view_config_btn_horizontal_orientation.isSelected = false
            view_config_btn_vertical_orientation.isSelected = true
        }

        view_config_btn_horizontal_orientation.setOnClickListener {
            callback.onOrientationChange(0)
            view_config_btn_horizontal_orientation.isSelected = true
            view_config_btn_vertical_orientation.isSelected = false
        }
    }

    private fun selectFont(selectedFont: Int, isReloadNeeded: Boolean) {
        when (selectedFont) {
            Constants.FONT_ANDADA -> setSelectedFont(true, false, false, false)
            Constants.FONT_LATO -> setSelectedFont(false, true, false, false)
            Constants.FONT_LORA -> setSelectedFont(false, false, true, false)
            Constants.FONT_RALEWAY -> setSelectedFont(false, false, false, true)
        }
        config.font = selectedFont
        if (isAdded && isReloadNeeded) {
            AppUtil.saveConfig(activity, config)
            EventBus.getDefault().post(ReloadDataEvent())
        }
    }

    private fun setSelectedFont(andada: Boolean, lato: Boolean, lora: Boolean, raleway: Boolean) {
        view_config_font_andada.isSelected = andada
        view_config_font_lato.isSelected = lato
        view_config_font_lora.isSelected = lora
        view_config_font_raleway.isSelected = raleway
    }

    private fun toggleBlackTheme() {
        val day = ContextCompat.getColor(context!!, R.color.white)
        val night = ContextCompat.getColor(context!!, R.color.night)
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(),
                if (isNightMode) night else day, if (isNightMode) day else night)
        colorAnimation.duration = FADE_DAY_NIGHT_MODE.toLong()
        colorAnimation.addUpdateListener { animator ->
            val value = animator.animatedValue as Int
            container.setBackgroundColor(value)
        }

        colorAnimation.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {}

            override fun onAnimationEnd(animator: Animator) {
                isNightMode = !isNightMode
                config.isNightMode = isNightMode
                AppUtil.saveConfig(activity, config)
                EventBus.getDefault().post(ReloadDataEvent())
            }

            override fun onAnimationCancel(animator: Animator) {}

            override fun onAnimationRepeat(animator: Animator) {}
        })

        colorAnimation.duration = FADE_DAY_NIGHT_MODE.toLong()
        colorAnimation.start()
    }

    private fun configSeekBar() {
        val thumbDrawable = ContextCompat.getDrawable(activity!!, R.drawable.seekbar_thumb)
        UiUtil.setColorToImage(activity, config.themeColor, thumbDrawable)
        UiUtil.setColorToImage(activity, R.color.grey_color, view_config_font_size_seek_bar.progressDrawable)
        view_config_font_size_seek_bar.thumb = thumbDrawable

        view_config_font_size_seek_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                config.fontSize = progress
                AppUtil.saveConfig(activity, config)
                EventBus.getDefault().post(ReloadDataEvent())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun setToolBarColor() {
        if (isNightMode) {
            ((context as Activity).findViewById<View>(R.id.toolbar) as FolioToolbar).setDayMode()
        } else {
            ((context as Activity).findViewById<View>(R.id.toolbar) as FolioToolbar).setNightMode()
        }

    }

    private fun setAudioPlayerBackground() {
        if (isNightMode) {
            (context as Activity).findViewById<View>(R.id.container).setBackgroundColor(ContextCompat.getColor(context!!, R.color.white))
        } else {
            (context as Activity).findViewById<View>(R.id.container).setBackgroundColor(ContextCompat.getColor(context!!, R.color.night))
        }
    }

    companion object {
        const val FADE_DAY_NIGHT_MODE = 500
    }
}
