package com.folioreader.ui.folio.fragment

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.text.Html
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.folioreader.Config
import com.folioreader.R
import com.folioreader.model.event.MediaOverlayHighlightStyleEvent
import com.folioreader.model.event.MediaOverlaySpeedEvent
import com.folioreader.util.AppUtil
import com.folioreader.util.UiUtil
import com.folioreader.view.MediaControllerCallback
import com.folioreader.view.StyleableTextView
import org.greenrobot.eventbus.EventBus

class MediaControllerFragment : BottomSheetDialogFragment() {

    companion object {
        @JvmField
        val LOG_TAG: String = MediaControllerFragment::class.java.simpleName
        const val BUNDLE_IS_VISIBLE = "isVisible"

        @JvmStatic
        fun getInstance(supportFragmentManager: FragmentManager,
                        callback: MediaControllerCallback): MediaControllerFragment {

            var mediaControllerFragment = supportFragmentManager.findFragmentByTag(LOG_TAG)
                    as MediaControllerFragment?
            if (mediaControllerFragment == null)
                mediaControllerFragment = MediaControllerFragment()
            mediaControllerFragment.callback = callback
            return mediaControllerFragment
        }
    }

    private lateinit var config: Config
    lateinit var callback: MediaControllerCallback
    private var isPlaying: Boolean = false
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var mTouchOutsideView: View
    var visible: Boolean = false

    private var container: RelativeLayout? = null
    private var prev_button: ImageButton? = null
    private var play_button: ImageButton? = null
    private var next_button: ImageButton? = null
    private var playback_speed_Layout: LinearLayout? = null
    private var btn_half_speed: StyleableTextView? = null
    private var btn_one_x_speed: StyleableTextView? = null
    private var btn_one_and_half_speed: StyleableTextView? = null
    private var btn_twox_speed: StyleableTextView? = null
    private var btn_backcolor_style: StyleableTextView? = null
    private var btn_text_undeline_style: StyleableTextView? = null
    private var btn_text_color_style: StyleableTextView? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.v(LOG_TAG, "-> onCreateDialog")

        bottomSheetDialog = BottomSheetDialog(context!!)
        var view = View.inflate(context, R.layout.view_audio_player, null)
        bindViews(view)
        bottomSheetDialog.setContentView(view)
        bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)

        mTouchOutsideView = ((view.parent as View).parent as View).findViewById(R.id.touch_outside)
        mTouchOutsideView.setOnTouchListener { _, event ->

            if (event.action == MotionEvent.ACTION_DOWN) {
                Log.v(LOG_TAG, "-> onTouch -> touch_outside -> ${getView()}")
                dialog.hide()
                visible = false
                return@setOnTouchListener true
            }
            false
        }

        initViewStates()
        onViewStateRestored(savedInstanceState)
        return bottomSheetDialog
    }

    private fun bindViews(view: View) {

        container = view.findViewById(R.id.container)
        prev_button = view.findViewById(R.id.prev_button)
        play_button = view.findViewById(R.id.play_button)
        next_button = view.findViewById(R.id.next_button)
        playback_speed_Layout = view.findViewById(R.id.playback_speed_Layout)
        btn_half_speed = view.findViewById(R.id.btn_half_speed)
        btn_one_x_speed = view.findViewById(R.id.btn_one_x_speed)
        btn_one_and_half_speed = view.findViewById(R.id.btn_one_and_half_speed)
        btn_twox_speed = view.findViewById(R.id.btn_twox_speed)
        btn_backcolor_style = view.findViewById(R.id.btn_backcolor_style)
        btn_text_undeline_style = view.findViewById(R.id.btn_text_undeline_style)
        btn_text_color_style = view.findViewById(R.id.btn_text_color_style)
    }

    override fun onStart() {
        super.onStart()
        Log.v(LOG_TAG, "-> onStart")

        dialog.setOnKeyListener { _, keyCode, event ->
            if (event.action == MotionEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                Log.v(LOG_TAG, "-> Back button pressed")
                dialog.hide()
                visible = false
                return@setOnKeyListener true
            }
            false
        }

        bottomSheetBehavior.isHideable = false
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        if (!visible)
            dialog.hide()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.v(LOG_TAG, "-> onSaveInstanceState -> $visible")
        outState.putBoolean(BUNDLE_IS_VISIBLE, visible)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        Log.v(LOG_TAG, "-> onViewStateRestored")

        if (savedInstanceState == null)
            return

        visible = savedInstanceState.getBoolean(BUNDLE_IS_VISIBLE)
        Log.v(LOG_TAG, "-> onViewStateRestored -> $visible")
    }

    fun show(fragmentManager: FragmentManager) {
        Log.v(LOG_TAG, "-> show")

        visible = true
        if (isAdded) {
            Log.v(LOG_TAG, "-> Is already added")
            dialog.show()
        } else {
            Log.v(LOG_TAG, "-> Not added")
            show(fragmentManager, MediaControllerFragment.LOG_TAG)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.v(LOG_TAG, "-> onDestroyView")
    }

    private fun initViewStates() {
        config = AppUtil.getSavedConfig(context)

        if (Build.VERSION.SDK_INT >= 24) {
            btn_one_and_half_speed?.text = Html.fromHtml(context!!.getString(R.string.one_and_half_speed), 0)
            btn_half_speed?.text = Html.fromHtml(context!!.getString(R.string.half_speed_text), 0)
            btn_text_undeline_style?.text = Html.fromHtml(context!!.getString(R.string.style_underline), 0)
        } else {
            btn_one_and_half_speed?.text = Html.fromHtml(context!!.getString(R.string.one_and_half_speed))
            btn_half_speed?.text = Html.fromHtml(context!!.getString(R.string.half_speed_text))
            btn_text_undeline_style?.text = Html.fromHtml(context!!.getString(R.string.style_underline))
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            playback_speed_Layout?.visibility = View.GONE

        if (config.isNightMode) setNightMode()

        initColors()
        initListeners()
    }

    private fun initColors() {

        btn_half_speed?.setTextColor(UiUtil.getColorList(config.themeColor, ContextCompat.getColor(context!!, R.color.grey_color)))
        btn_one_and_half_speed?.setTextColor(UiUtil.getColorList(config.themeColor, ContextCompat.getColor(context!!, R.color.grey_color)))
        btn_twox_speed?.setTextColor(UiUtil.getColorList(config.themeColor, ContextCompat.getColor(context!!, R.color.grey_color)))
        btn_one_x_speed?.setTextColor(UiUtil.getColorList(config.themeColor, ContextCompat.getColor(context!!, R.color.grey_color)))
        btn_text_undeline_style?.setTextColor(UiUtil.getColorList(config.themeColor, ContextCompat.getColor(context!!, R.color.grey_color)))
        btn_backcolor_style?.setTextColor(UiUtil.getColorList(ContextCompat.getColor(context!!, R.color.white), ContextCompat.getColor(context!!, R.color.grey_color)))
        btn_backcolor_style?.setBackgroundDrawable(UiUtil.createStateDrawable(config.themeColor, ContextCompat.getColor(context!!, android.R.color.transparent)))
        btn_text_color_style?.setTextColor(UiUtil.getColorList(config.themeColor, ContextCompat.getColor(context!!, R.color.grey_color)))
        UiUtil.setColorIntToDrawable(config.themeColor, play_button?.drawable)
        UiUtil.setColorIntToDrawable(config.themeColor, next_button?.drawable)
        UiUtil.setColorIntToDrawable(config.themeColor, prev_button?.drawable)
    }

    private fun initListeners() {

        play_button?.setOnClickListener {
            callback.let {
                if (isPlaying) {
                    callback.pause()
                    play_button?.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.play_icon))
                    UiUtil.setColorIntToDrawable(config.themeColor, play_button?.drawable)
                } else {
                    callback.play()
                    play_button?.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.pause_btn))
                    UiUtil.setColorIntToDrawable(config.themeColor, play_button?.drawable)
                }
                isPlaying = !isPlaying
            }
        }

        btn_half_speed?.setOnClickListener {
            toggleSpeedControlButtons(true, false, false, false)
            EventBus.getDefault().post(MediaOverlaySpeedEvent(MediaOverlaySpeedEvent.Speed.HALF))
        }

        btn_one_x_speed?.setOnClickListener {
            toggleSpeedControlButtons(false, true, false, false)
            EventBus.getDefault().post(MediaOverlaySpeedEvent(MediaOverlaySpeedEvent.Speed.ONE))
        }

        btn_one_and_half_speed?.setOnClickListener {
            toggleSpeedControlButtons(false, false, true, false)
            EventBus.getDefault().post(MediaOverlaySpeedEvent(MediaOverlaySpeedEvent.Speed.ONE_HALF))
        }

        btn_twox_speed?.setOnClickListener {
            toggleSpeedControlButtons(false, false, false, true)
            EventBus.getDefault().post(MediaOverlaySpeedEvent(MediaOverlaySpeedEvent.Speed.TWO))
        }

        btn_backcolor_style?.setOnClickListener {
            toggleTextStyle(true, false, false)
            EventBus.getDefault().post(MediaOverlayHighlightStyleEvent(MediaOverlayHighlightStyleEvent.Style.DEFAULT))
        }

        btn_text_undeline_style?.setOnClickListener {
            toggleTextStyle(false, true, false)
            EventBus.getDefault().post(MediaOverlayHighlightStyleEvent(MediaOverlayHighlightStyleEvent.Style.UNDERLINE))
        }

        btn_text_color_style?.setOnClickListener {
            toggleTextStyle(false, false, true)
            EventBus.getDefault().post(MediaOverlayHighlightStyleEvent(MediaOverlayHighlightStyleEvent.Style.BACKGROUND))
        }
    }

    private fun toggleTextStyle(backColor: Boolean, underline: Boolean, textColor: Boolean) {
        btn_backcolor_style?.isSelected = backColor
        btn_text_undeline_style?.isSelected = underline
        btn_text_color_style?.isSelected = textColor
    }

    private fun toggleSpeedControlButtons(half: Boolean, one: Boolean, oneHalf: Boolean, two: Boolean) {
        btn_half_speed?.isSelected = half
        btn_one_x_speed?.isSelected = one
        btn_one_and_half_speed?.isSelected = oneHalf
        btn_twox_speed?.isSelected = two
    }

    fun setPlayButtonDrawable() {
        play_button?.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.play_icon))
    }

    fun setNightMode() {
        container?.setBackgroundColor(ContextCompat.getColor(context!!, R.color.night))
    }

    fun setDayMode() {
        container?.setBackgroundColor(ContextCompat.getColor(context!!, R.color.white))
    }
}