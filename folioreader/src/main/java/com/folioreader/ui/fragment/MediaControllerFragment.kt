package com.folioreader.ui.fragment

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.folioreader.Config
import com.folioreader.R
import com.folioreader.model.event.MediaOverlayHighlightStyleEvent
import com.folioreader.model.event.MediaOverlaySpeedEvent
import com.folioreader.ui.view.MediaControllerCallback
import com.folioreader.ui.view.StyleableTextView
import com.folioreader.util.AppUtil
import com.folioreader.util.UiUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.greenrobot.eventbus.EventBus

class MediaControllerFragment : BottomSheetDialogFragment() {

    companion object {
        @JvmField
        val LOG_TAG: String = MediaControllerFragment::class.java.simpleName
        private const val BUNDLE_IS_VISIBLE = "BUNDLE_IS_VISIBLE"

        @JvmStatic
        fun getInstance(
            supportFragmentManager: FragmentManager,
            callback: MediaControllerCallback
        ): MediaControllerFragment {

            var mediaControllerFragment = supportFragmentManager.findFragmentByTag(LOG_TAG)
                    as MediaControllerFragment?
            if (mediaControllerFragment == null)
                mediaControllerFragment = MediaControllerFragment()
            mediaControllerFragment.callback = callback
            return mediaControllerFragment
        }
    }

    private lateinit var config: Config
    private lateinit var callback: MediaControllerCallback
    private var isPlaying: Boolean = false
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var mTouchOutsideView: View
    var visible: Boolean = false

    private var container: RelativeLayout? = null
    private var prevButton: ImageButton? = null
    private var playPauseButton: ImageButton? = null
    private var nextButton: ImageButton? = null
    private var playbackSpeedLayout: LinearLayout? = null
    private var btnHalfSpeed: StyleableTextView? = null
    private var btnOneXSpeed: StyleableTextView? = null
    private var btnOneAndHalfSpeed: StyleableTextView? = null
    private var btnTwoXSpeed: StyleableTextView? = null
    private var btnBackColorStyle: StyleableTextView? = null
    private var btnTextUnderlineStyle: StyleableTextView? = null
    private var btnTextColorStyle: StyleableTextView? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.v(LOG_TAG, "-> onCreateDialog")

        bottomSheetDialog = BottomSheetDialog(context!!)
        val view = View.inflate(context, R.layout.view_audio_player, null)
        bindViews(view)
        bottomSheetDialog.setContentView(view)
        bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)

        mTouchOutsideView = ((view.parent as View).parent as View).findViewById(R.id.touch_outside)
        mTouchOutsideView.setOnTouchListener { _, event ->

            if (event.action == MotionEvent.ACTION_DOWN) {
                Log.v(LOG_TAG, "-> onTouch -> touch_outside -> ${getView()}")
                dialog!!.hide()
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
        prevButton = view.findViewById(R.id.prev_button)
        playPauseButton = view.findViewById(R.id.play_pause_button)
        nextButton = view.findViewById(R.id.next_button)
        playbackSpeedLayout = view.findViewById(R.id.playback_speed_Layout)
        btnHalfSpeed = view.findViewById(R.id.btn_half_speed)
        btnOneXSpeed = view.findViewById(R.id.btn_one_x_speed)
        btnOneAndHalfSpeed = view.findViewById(R.id.btn_one_and_half_speed)
        btnTwoXSpeed = view.findViewById(R.id.btn_twox_speed)
        btnBackColorStyle = view.findViewById(R.id.btn_backcolor_style)
        btnTextUnderlineStyle = view.findViewById(R.id.btn_text_undeline_style)
        btnTextColorStyle = view.findViewById(R.id.btn_text_color_style)
    }

    override fun onStart() {
        super.onStart()
        Log.v(LOG_TAG, "-> onStart")

        dialog!!.setOnKeyListener { _, keyCode, event ->
            if (event.action == MotionEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                Log.v(LOG_TAG, "-> Back button pressed")
                dialog!!.hide()
                visible = false
                return@setOnKeyListener true
            }
            false
        }

        bottomSheetBehavior.isHideable = false
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        if (!visible)
            dialog!!.hide()
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
        //Log.v(LOG_TAG, "-> onViewStateRestored -> $visible")
    }

    fun show(fragmentManager: FragmentManager) {
        Log.v(LOG_TAG, "-> show")

        visible = true
        if (isAdded) {
            //Log.v(LOG_TAG, "-> Is already added")
            dialog!!.show()
        } else {
            //Log.v(LOG_TAG, "-> Not added")
            show(fragmentManager, MediaControllerFragment.LOG_TAG)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.v(LOG_TAG, "-> onDestroyView")
    }

    private fun initViewStates() {
        config = AppUtil.getSavedConfig(context)!!

        if (Build.VERSION.SDK_INT >= 24) {
            btnOneAndHalfSpeed?.text = Html.fromHtml(context!!.getString(R.string.one_and_half_speed), 0)
            btnHalfSpeed?.text = Html.fromHtml(context!!.getString(R.string.half_speed_text), 0)
            btnTextUnderlineStyle?.text = Html.fromHtml(context!!.getString(R.string.style_underline), 0)
        } else {
            btnOneAndHalfSpeed?.text = Html.fromHtml(context!!.getString(R.string.one_and_half_speed))
            btnHalfSpeed?.text = Html.fromHtml(context!!.getString(R.string.half_speed_text))
            btnTextUnderlineStyle?.text = Html.fromHtml(context!!.getString(R.string.style_underline))
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            playbackSpeedLayout?.visibility = View.GONE

        if (config.isNightMode) setNightMode()

        initColors()
        initListeners()
    }

    private fun initColors() {

        btnHalfSpeed?.setTextColor(
            UiUtil.getColorList(
                config.themeColor,
                ContextCompat.getColor(context!!, R.color.grey_color)
            )
        )
        btnOneAndHalfSpeed?.setTextColor(
            UiUtil.getColorList(
                config.themeColor,
                ContextCompat.getColor(context!!, R.color.grey_color)
            )
        )
        btnTwoXSpeed?.setTextColor(
            UiUtil.getColorList(
                config.themeColor,
                ContextCompat.getColor(context!!, R.color.grey_color)
            )
        )
        btnOneXSpeed?.setTextColor(
            UiUtil.getColorList(
                config.themeColor,
                ContextCompat.getColor(context!!, R.color.grey_color)
            )
        )
        btnTextUnderlineStyle?.setTextColor(
            UiUtil.getColorList(
                config.themeColor,
                ContextCompat.getColor(context!!, R.color.grey_color)
            )
        )
        btnBackColorStyle?.setTextColor(
            UiUtil.getColorList(
                ContextCompat.getColor(context!!, R.color.white),
                ContextCompat.getColor(context!!, R.color.grey_color)
            )
        )
        btnBackColorStyle?.setBackgroundDrawable(
            UiUtil.createStateDrawable(
                config.themeColor,
                ContextCompat.getColor(context!!, android.R.color.transparent)
            )
        )
        btnTextColorStyle?.setTextColor(
            UiUtil.getColorList(
                config.themeColor,
                ContextCompat.getColor(context!!, R.color.grey_color)
            )
        )
        UiUtil.setColorIntToDrawable(config.themeColor, playPauseButton?.drawable)
        UiUtil.setColorIntToDrawable(config.themeColor, nextButton?.drawable)
        UiUtil.setColorIntToDrawable(config.themeColor, prevButton?.drawable)
    }

    private fun initListeners() {

        playPauseButton?.setOnClickListener {
            if (isPlaying) {
                playPauseButton?.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_play))
                UiUtil.setColorIntToDrawable(config.themeColor, playPauseButton?.drawable)
                callback.pause()
            } else {
                playPauseButton?.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_pause))
                UiUtil.setColorIntToDrawable(config.themeColor, playPauseButton?.drawable)
                callback.play()
            }
            isPlaying = !isPlaying
        }

        btnHalfSpeed?.setOnClickListener {
            toggleSpeedControlButtons(true, false, false, false)
            EventBus.getDefault().post(MediaOverlaySpeedEvent(MediaOverlaySpeedEvent.Speed.HALF))
        }

        btnOneXSpeed?.setOnClickListener {
            toggleSpeedControlButtons(false, true, false, false)
            EventBus.getDefault().post(MediaOverlaySpeedEvent(MediaOverlaySpeedEvent.Speed.ONE))
        }

        btnOneAndHalfSpeed?.setOnClickListener {
            toggleSpeedControlButtons(false, false, true, false)
            EventBus.getDefault().post(MediaOverlaySpeedEvent(MediaOverlaySpeedEvent.Speed.ONE_HALF))
        }

        btnTwoXSpeed?.setOnClickListener {
            toggleSpeedControlButtons(false, false, false, true)
            EventBus.getDefault().post(MediaOverlaySpeedEvent(MediaOverlaySpeedEvent.Speed.TWO))
        }

        btnBackColorStyle?.setOnClickListener {
            toggleTextStyle(true, false, false)
            EventBus.getDefault().post(MediaOverlayHighlightStyleEvent(MediaOverlayHighlightStyleEvent.Style.DEFAULT))
        }

        btnTextUnderlineStyle?.setOnClickListener {
            toggleTextStyle(false, true, false)
            EventBus.getDefault().post(MediaOverlayHighlightStyleEvent(MediaOverlayHighlightStyleEvent.Style.UNDERLINE))
        }

        btnTextColorStyle?.setOnClickListener {
            toggleTextStyle(false, false, true)
            EventBus.getDefault()
                .post(MediaOverlayHighlightStyleEvent(MediaOverlayHighlightStyleEvent.Style.BACKGROUND))
        }
    }

    private fun toggleTextStyle(backColor: Boolean, underline: Boolean, textColor: Boolean) {
        btnBackColorStyle?.isSelected = backColor
        btnTextUnderlineStyle?.isSelected = underline
        btnTextColorStyle?.isSelected = textColor
    }

    private fun toggleSpeedControlButtons(half: Boolean, one: Boolean, oneHalf: Boolean, two: Boolean) {
        btnHalfSpeed?.isSelected = half
        btnOneXSpeed?.isSelected = one
        btnOneAndHalfSpeed?.isSelected = oneHalf
        btnTwoXSpeed?.isSelected = two
    }

    fun setPlayButtonDrawable() {
        playPauseButton?.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_play))
    }

    fun setNightMode() {
        container?.setBackgroundColor(ContextCompat.getColor(context!!, R.color.night))
    }

    fun setDayMode() {
        container?.setBackgroundColor(ContextCompat.getColor(context!!, R.color.white))
    }
}