package com.folioreader.ui.view

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.*
import android.view.ActionMode.Callback
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.PopupWindow
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import com.folioreader.Config
import com.folioreader.Constants
import com.folioreader.R
import com.folioreader.model.DisplayUnit
import com.folioreader.model.HighLight
import com.folioreader.model.HighlightImpl.HighlightStyle
import com.folioreader.model.sqlite.HighLightTable
import com.folioreader.ui.activity.FolioActivity
import com.folioreader.ui.activity.FolioActivityCallback
import com.folioreader.ui.fragment.DictionaryFragment
import com.folioreader.ui.fragment.FolioPageFragment
import com.folioreader.util.AppUtil
import com.folioreader.util.HighlightUtil
import com.folioreader.util.UiUtil
import dalvik.system.PathClassLoader
import kotlinx.android.synthetic.main.text_selection.view.*
import org.json.JSONObject
import org.springframework.util.ReflectionUtils
import timber.log.Timber
import java.lang.ref.WeakReference

/**
 * @author by mahavir on 3/31/16.
 */
class FolioWebView : WebView {

    companion object {
        private const val IS_SCROLLING_CHECK_TIMER = 100
        private const val IS_SCROLLING_CHECK_MAX_DURATION = 10000

        @JvmStatic
        fun onWebViewConsoleMessage(cm: ConsoleMessage, LOG_TAG: String, msg: String): Boolean {
            when (cm.messageLevel()) {
                ConsoleMessage.MessageLevel.LOG -> {
                    Timber.tag(LOG_TAG).v(msg)
                    return true
                }
                ConsoleMessage.MessageLevel.DEBUG, ConsoleMessage.MessageLevel.TIP -> {
                    Timber.tag(LOG_TAG).d(msg)
                    return true
                }
                ConsoleMessage.MessageLevel.WARNING -> {
                    Timber.tag(LOG_TAG).w(msg)
                    return true
                }
                ConsoleMessage.MessageLevel.ERROR -> {
                    Timber.tag(LOG_TAG).e(msg)
                    return true
                }
                else -> return false
            }
        }
    }

    private var horizontalPageCount = 0
    private var displayMetrics: DisplayMetrics? = null
    private var density: Float = 0.toFloat()
    private var mScrollListener: ScrollListener? = null
    private var mSeekBarListener: SeekBarListener? = null
    private lateinit var gestureDetector: GestureDetectorCompat
    private var eventActionDown: MotionEvent? = null
    private var pageWidthCssDp: Int = 0
    private var pageWidthCssPixels: Float = 0.toFloat()
    private lateinit var webViewPager: WebViewPager
    private lateinit var uiHandler: Handler
    private lateinit var folioActivityCallback: FolioActivityCallback
    private lateinit var parentFragment: FolioPageFragment

    private var actionMode: ActionMode? = null
    private var textSelectionCb: TextSelectionCb? = null
    private var textSelectionCb2: TextSelectionCb2? = null
    private var selectionRect = Rect()
    private val popupRect = Rect()
    private var popupWindow = PopupWindow()
    private lateinit var viewTextSelection: View
    private var isScrollingCheckDuration: Int = 0
    private var isScrollingRunnable: Runnable? = null
    private var oldScrollX: Int = 0
    private var oldScrollY: Int = 0
    private var lastTouchAction: Int = 0
    private var destroyed: Boolean = false
    private var handleHeight: Int = 0

    private var lastScrollType: LastScrollType? = null

    val contentHeightVal: Int
        get() = Math.floor((this.contentHeight * this.scale).toDouble()).toInt()

    val webViewHeight: Int
        get() = this.measuredHeight

    private enum class LastScrollType {
        USER, PROGRAMMATIC
    }

    @JavascriptInterface
    fun getDirection(): String {
        return folioActivityCallback.direction.toString()
    }

    @JavascriptInterface
    fun getTopDistraction(unitString: String): Int {
        val unit = DisplayUnit.valueOf(unitString)
        return folioActivityCallback.getTopDistraction(unit)
    }

    @JavascriptInterface
    fun getBottomDistraction(unitString: String): Int {
        val unit = DisplayUnit.valueOf(unitString)
        return folioActivityCallback.getBottomDistraction(unit)
    }

    @JavascriptInterface
    fun getViewportRect(unitString: String): String {
        val unit = DisplayUnit.valueOf(unitString)
        val rect = folioActivityCallback.getViewportRect(unit)
        return UiUtil.rectToDOMRectJson(rect)
    }

    @JavascriptInterface
    fun toggleSystemUI() {
        uiHandler.post {
            folioActivityCallback.toggleSystemUI()
        }
    }

    @JavascriptInterface
    fun isPopupShowing(): Boolean {
        return popupWindow.isShowing
    }

    private inner class HorizontalGestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            //Timber.d("-> onScroll -> e1 = " + e1 + ", e2 = " + e2 + ", distanceX = " + distanceX + ", distanceY = " + distanceY);
            lastScrollType = LastScrollType.USER
            return false
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            //Timber.d("-> onFling -> e1 = " + e1 + ", e2 = " + e2 + ", velocityX = " + velocityX + ", velocityY = " + velocityY);

            if (!webViewPager.isScrolling) {
                // Need to complete the scroll as ViewPager thinks these touch events should not
                // scroll it's pages.
                //Timber.d("-> onFling -> completing scroll");
                uiHandler.postDelayed({
                    // Delayed to avoid inconsistency of scrolling in WebView
                    scrollTo(getScrollXPixelsForPage(webViewPager!!.currentItem), 0)
                }, 100)
            }

            lastScrollType = LastScrollType.USER
            return true
        }

        override fun onDown(event: MotionEvent?): Boolean {
            //Timber.v("-> onDown -> " + event.toString());

            eventActionDown = MotionEvent.obtain(event)
            super@FolioWebView.onTouchEvent(event)
            return true
        }
    }

    @JavascriptInterface
    fun dismissPopupWindow(): Boolean {
        Timber.d("-> dismissPopupWindow -> %s", parentFragment.spineItem?.href)
        val wasShowing = popupWindow.isShowing
        if (Looper.getMainLooper().thread == Thread.currentThread()) {
            popupWindow.dismiss()
        } else {
            uiHandler.post { popupWindow.dismiss() }
        }
        selectionRect = Rect()
        uiHandler.removeCallbacks(isScrollingRunnable)
        isScrollingCheckDuration = 0
        return wasShowing
    }

    override fun destroy() {
        super.destroy()
        Timber.d("-> destroy")
        dismissPopupWindow()
        destroyed = true
    }

    private inner class VerticalGestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            //Timber.v("-> onScroll -> e1 = " + e1 + ", e2 = " + e2 + ", distanceX = " + distanceX + ", distanceY = " + distanceY);
            lastScrollType = LastScrollType.USER
            return false
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            //Timber.v("-> onFling -> e1 = " + e1 + ", e2 = " + e2 + ", velocityX = " + velocityX + ", velocityY = " + velocityY);
            lastScrollType = LastScrollType.USER
            return false
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private fun init() {
        Timber.v("-> init")

        uiHandler = Handler()
        displayMetrics = resources.displayMetrics
        density = displayMetrics!!.density

        gestureDetector = if (folioActivityCallback.direction == Config.Direction.HORIZONTAL) {
            GestureDetectorCompat(context, HorizontalGestureListener())
        } else {
            GestureDetectorCompat(context, VerticalGestureListener())
        }

        initViewTextSelection()
    }

    fun initViewTextSelection() {
        Timber.v("-> initViewTextSelection")

        val textSelectionMiddleDrawable = ContextCompat.getDrawable(
            context,
            R.drawable.abc_text_select_handle_middle_mtrl_dark
        )
        handleHeight = textSelectionMiddleDrawable?.intrinsicHeight ?: (24 * density).toInt()

        val config = AppUtil.getSavedConfig(context)!!
        val ctw = if (config.isNightMode) {
            ContextThemeWrapper(context, R.style.FolioNightTheme)
        } else {
            ContextThemeWrapper(context, R.style.FolioDayTheme)
        }

        viewTextSelection = LayoutInflater.from(ctw).inflate(R.layout.text_selection, null)
        viewTextSelection.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

        viewTextSelection.yellowHighlight.setOnClickListener {
            Timber.v("-> onClick -> yellowHighlight")
            onHighlightColorItemsClicked(HighlightStyle.Yellow, false)
        }
        viewTextSelection.greenHighlight.setOnClickListener {
            Timber.v("-> onClick -> greenHighlight")
            onHighlightColorItemsClicked(HighlightStyle.Green, false)
        }
        viewTextSelection.blueHighlight.setOnClickListener {
            Timber.v("-> onClick -> blueHighlight")
            onHighlightColorItemsClicked(HighlightStyle.Blue, false)
        }
        viewTextSelection.pinkHighlight.setOnClickListener {
            Timber.v("-> onClick -> pinkHighlight")
            onHighlightColorItemsClicked(HighlightStyle.Pink, false)
        }
        viewTextSelection.underlineHighlight.setOnClickListener {
            Timber.v("-> onClick -> underlineHighlight")
            onHighlightColorItemsClicked(HighlightStyle.Underline, false)
        }

        viewTextSelection.deleteHighlight.setOnClickListener {
            Timber.v("-> onClick -> deleteHighlight")
            dismissPopupWindow()
            loadUrl("javascript:clearSelection()")
            loadUrl("javascript:deleteThisHighlight()")
        }

        viewTextSelection.copySelection.setOnClickListener {
            dismissPopupWindow()
            loadUrl("javascript:onTextSelectionItemClicked(${it.id})")
        }
        viewTextSelection.shareSelection.setOnClickListener {
            dismissPopupWindow()
            loadUrl("javascript:onTextSelectionItemClicked(${it.id})")
        }
        viewTextSelection.defineSelection.setOnClickListener {
            dismissPopupWindow()
            loadUrl("javascript:onTextSelectionItemClicked(${it.id})")
        }
    }

    @JavascriptInterface
    fun onTextSelectionItemClicked(id: Int, selectedText: String?) {

        uiHandler.post { loadUrl("javascript:clearSelection()") }

        when (id) {
            R.id.copySelection -> {
                Timber.v("-> onTextSelectionItemClicked -> copySelection -> $selectedText")
                UiUtil.copyToClipboard(context, selectedText)
                Toast.makeText(context, context.getString(R.string.copied), Toast.LENGTH_SHORT).show()
            }
            R.id.shareSelection -> {
                Timber.v("-> onTextSelectionItemClicked -> shareSelection -> $selectedText")
                UiUtil.share(context, selectedText)
            }
            R.id.defineSelection -> {
                Timber.v("-> onTextSelectionItemClicked -> defineSelection -> $selectedText")
                uiHandler.post { showDictDialog(selectedText) }
            }
            else -> {
                Timber.w("-> onTextSelectionItemClicked -> unknown id = $id")
            }
        }
    }

    private fun showDictDialog(selectedText: String?) {
        val dictionaryFragment = DictionaryFragment()
        val bundle = Bundle()
        bundle.putString(Constants.SELECTED_WORD, selectedText?.trim())
        dictionaryFragment.arguments = bundle
        dictionaryFragment.show(parentFragment.fragmentManager, DictionaryFragment::class.java.name)
    }

    private fun onHighlightColorItemsClicked(style: HighlightStyle, isAlreadyCreated: Boolean) {
        parentFragment.highlight(style, isAlreadyCreated)
        dismissPopupWindow()
    }

    @JavascriptInterface
    fun deleteThisHighlight(id: String?) {
        Timber.d("-> deleteThisHighlight")

        if (id.isNullOrEmpty())
            return

        val highlightImpl = HighLightTable.getHighlightForRangy(id)
        if (HighLightTable.deleteHighlight(id)) {
            val rangy = HighlightUtil.generateRangyString(parentFragment.pageName)
            uiHandler.post { parentFragment.loadRangy(rangy) }
            if (highlightImpl != null) {
                HighlightUtil.sendHighlightBroadcastEvent(
                    context, highlightImpl,
                    HighLight.HighLightAction.DELETE
                )
            }
        }
    }

    fun setParentFragment(parentFragment: FolioPageFragment) {
        this.parentFragment = parentFragment
    }

    fun setFolioActivityCallback(folioActivityCallback: FolioActivityCallback) {
        this.folioActivityCallback = folioActivityCallback
        init()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)

        pageWidthCssDp = Math.ceil((measuredWidth / density).toDouble()).toInt()
        pageWidthCssPixels = pageWidthCssDp * density
    }

    fun setScrollListener(listener: ScrollListener) {
        mScrollListener = listener
    }

    fun setSeekBarListener(listener: SeekBarListener) {
        mSeekBarListener = listener
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        //Timber.v("-> onTouchEvent -> " + AppUtil.actionToString(event.getAction()));

        if (event == null)
            return false

        lastTouchAction = event.action

        return if (folioActivityCallback.direction == Config.Direction.HORIZONTAL) {
            computeHorizontalScroll(event)
        } else {
            computeVerticalScroll(event)
        }
    }

    private fun computeVerticalScroll(event: MotionEvent): Boolean {

        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    private fun computeHorizontalScroll(event: MotionEvent): Boolean {
        //Timber.v("-> computeHorizontalScroll");

        // Rare condition in fast scrolling
        if (!::webViewPager.isInitialized)
            return super.onTouchEvent(event)

        webViewPager.dispatchTouchEvent(event)
        val gestureReturn = gestureDetector.onTouchEvent(event)
        return if (gestureReturn) true else super.onTouchEvent(event)
    }

    fun getScrollXDpForPage(page: Int): Int {
        //Timber.v("-> getScrollXDpForPage -> page = " + page);
        return page * pageWidthCssDp
    }

    fun getScrollXPixelsForPage(page: Int): Int {
        //Timber.v("-> getScrollXPixelsForPage -> page = " + page);
        return Math.ceil((page * pageWidthCssPixels).toDouble()).toInt()
    }

    fun setHorizontalPageCount(horizontalPageCount: Int) {
        this.horizontalPageCount = horizontalPageCount

        uiHandler.post {
            webViewPager = (parent as View).findViewById(R.id.webViewPager)
            webViewPager.setHorizontalPageCount(this@FolioWebView.horizontalPageCount)
        }
    }

    override fun scrollTo(x: Int, y: Int) {
        super.scrollTo(x, y)
        //Timber.d("-> scrollTo -> x = " + x);
        lastScrollType = LastScrollType.PROGRAMMATIC
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        if (mScrollListener != null) mScrollListener!!.onScrollChange(t)
        super.onScrollChanged(l, t, oldl, oldt)

        if (lastScrollType == LastScrollType.USER) {
            //Timber.d("-> onScrollChanged -> scroll initiated by user");
            parentFragment.searchLocatorVisible = null
        }

        lastScrollType = null
    }

    interface ScrollListener {
        fun onScrollChange(percent: Int)
    }

    interface SeekBarListener {
        fun fadeInSeekBarIfInvisible()
    }

    private inner class TextSelectionCb : ActionMode.Callback {

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            Timber.d("-> onCreateActionMode")
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            Timber.d("-> onPrepareActionMode")

            evaluateJavascript("javascript:getSelectionRect()") { value ->
                val rectJson = JSONObject(value)
                setSelectionRect(
                    rectJson.getInt("left"), rectJson.getInt("top"),
                    rectJson.getInt("right"), rectJson.getInt("bottom")
                )
            }
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            Timber.d("-> onActionItemClicked")
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            Timber.d("-> onDestroyActionMode")
            dismissPopupWindow()
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private inner class TextSelectionCb2 : ActionMode.Callback2() {

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            Timber.d("-> onCreateActionMode")
            menu.clear()
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            Timber.d("-> onPrepareActionMode")
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            Timber.d("-> onActionItemClicked")
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            Timber.d("-> onDestroyActionMode")
            dismissPopupWindow()
        }

        override fun onGetContentRect(mode: ActionMode, view: View, outRect: Rect) {
            Timber.d("-> onGetContentRect")

            evaluateJavascript("javascript:getSelectionRect()") { value ->
                val rectJson = JSONObject(value)
                setSelectionRect(
                    rectJson.getInt("left"), rectJson.getInt("top"),
                    rectJson.getInt("right"), rectJson.getInt("bottom")
                )
            }
        }
    }

    override fun startActionMode(callback: Callback): ActionMode {
        Timber.d("-> startActionMode")

        textSelectionCb = TextSelectionCb()
        actionMode = super.startActionMode(textSelectionCb)
        actionMode?.finish()

        /*try {
            applyThemeColorToHandles()
        } catch (e: Exception) {
            Timber.w(e, "-> startActionMode -> Failed to apply theme colors to selection handles")
        }*/

        return actionMode as ActionMode

        //Comment above code and uncomment below line for stock text selection
        //return super.startActionMode(callback)
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun startActionMode(callback: Callback, type: Int): ActionMode {
        Timber.d("-> startActionMode")

        textSelectionCb2 = TextSelectionCb2()
        actionMode = super.startActionMode(textSelectionCb2, type)
        actionMode?.finish()

        /*try {
            applyThemeColorToHandles()
        } catch (e: Exception) {
            Timber.w(e, "-> startActionMode -> Failed to apply theme colors to selection handles")
        }*/

        return actionMode as ActionMode

        //Comment above code and uncomment below line for stock text selection
        //return super.startActionMode(callback, type)
    }

    private fun applyThemeColorToHandles() {
        Timber.v("-> applyThemeColorToHandles")

        if (Build.VERSION.SDK_INT < 23) {
            val folioActivityRef: WeakReference<FolioActivity> = folioActivityCallback.activity
            val mWindowManagerField = ReflectionUtils.findField(FolioActivity::class.java, "mWindowManager")
            mWindowManagerField.isAccessible = true
            val mWindowManager = mWindowManagerField.get(folioActivityRef.get())

            val windowManagerImplClass = Class.forName("android.view.WindowManagerImpl")
            val mGlobalField = ReflectionUtils.findField(windowManagerImplClass, "mGlobal")
            mGlobalField.isAccessible = true
            val mGlobal = mGlobalField.get(mWindowManager)

            val windowManagerGlobalClass = Class.forName("android.view.WindowManagerGlobal")
            val mViewsField = ReflectionUtils.findField(windowManagerGlobalClass, "mViews")
            mViewsField.isAccessible = true
            val mViews = mViewsField.get(mGlobal) as ArrayList<View>
            val config = AppUtil.getSavedConfig(context)!!

            for (view in mViews) {
                val handleViewClass = Class.forName("com.android.org.chromium.content.browser.input.HandleView")

                if (handleViewClass.isInstance(view)) {
                    val mDrawableField = ReflectionUtils.findField(handleViewClass, "mDrawable")
                    mDrawableField.isAccessible = true
                    val mDrawable = mDrawableField.get(view) as BitmapDrawable
                    UiUtil.setColorIntToDrawable(config.themeColor, mDrawable)
                }
            }

        } else {
            val folioActivityRef: WeakReference<FolioActivity> = folioActivityCallback.activity
            val mWindowManagerField = ReflectionUtils.findField(FolioActivity::class.java, "mWindowManager")
            mWindowManagerField.isAccessible = true
            val mWindowManager = mWindowManagerField.get(folioActivityRef.get())

            val windowManagerImplClass = Class.forName("android.view.WindowManagerImpl")
            val mGlobalField = ReflectionUtils.findField(windowManagerImplClass, "mGlobal")
            mGlobalField.isAccessible = true
            val mGlobal = mGlobalField.get(mWindowManager)

            val windowManagerGlobalClass = Class.forName("android.view.WindowManagerGlobal")
            val mViewsField = ReflectionUtils.findField(windowManagerGlobalClass, "mViews")
            mViewsField.isAccessible = true
            val mViews = mViewsField.get(mGlobal) as ArrayList<View>
            val config = AppUtil.getSavedConfig(context)!!

            for (view in mViews) {
                val popupDecorViewClass = Class.forName("android.widget.PopupWindow\$PopupDecorView")

                if (!popupDecorViewClass.isInstance(view))
                    continue

                val mChildrenField = ReflectionUtils.findField(popupDecorViewClass, "mChildren")
                mChildrenField.isAccessible = true
                val mChildren = mChildrenField.get(view) as kotlin.Array<View>

                //val pathClassLoader = PathClassLoader("/system/app/Chrome/Chrome.apk", ClassLoader.getSystemClassLoader())

                val pathClassLoader =
                    PathClassLoader("/system/app/Chrome/Chrome.apk", folioActivityRef.get()?.classLoader)

                val popupTouchHandleDrawableClass = Class.forName(
                    "org.chromium.android_webview.PopupTouchHandleDrawable",
                    true, pathClassLoader
                )

                //if (!popupTouchHandleDrawableClass.isInstance(mChildren[0]))
                //    continue

                val mDrawableField = ReflectionUtils.findField(popupTouchHandleDrawableClass, "mDrawable")
                mDrawableField.isAccessible = true
                val mDrawable = mDrawableField.get(mChildren[0]) as Drawable
                UiUtil.setColorIntToDrawable(config.themeColor, mDrawable)
            }
        }
    }

    @JavascriptInterface
    fun setSelectionRect(left: Int, top: Int, right: Int, bottom: Int) {

        val currentSelectionRect = Rect()
        currentSelectionRect.left = (left * density).toInt()
        currentSelectionRect.top = (top * density).toInt()
        currentSelectionRect.right = (right * density).toInt()
        currentSelectionRect.bottom = (bottom * density).toInt()
        Timber.d("-> setSelectionRect -> $currentSelectionRect")

        computeTextSelectionRect(currentSelectionRect)
        uiHandler.post { showTextSelectionPopup() }
    }

    private fun computeTextSelectionRect(currentSelectionRect: Rect) {
        Timber.v("-> computeTextSelectionRect")

        val viewportRect = folioActivityCallback.getViewportRect(DisplayUnit.PX)
        Timber.d("-> viewportRect -> $viewportRect")

        if (!Rect.intersects(viewportRect, currentSelectionRect)) {
            Timber.i("-> currentSelectionRect doesn't intersects viewportRect")
            uiHandler.post {
                popupWindow.dismiss()
                uiHandler.removeCallbacks(isScrollingRunnable)
            }
            return
        }
        Timber.i("-> currentSelectionRect intersects viewportRect")

        if (selectionRect == currentSelectionRect) {
            Timber.i("-> setSelectionRect -> currentSelectionRect is equal to previous " +
                    "selectionRect so no need to computeTextSelectionRect and show popupWindow again")
            return
        }

        Timber.i("-> setSelectionRect -> currentSelectionRect is not equal to previous " +
                    "selectionRect so computeTextSelectionRect and show popupWindow"
        )
        selectionRect = currentSelectionRect

        val aboveSelectionRect = Rect(viewportRect)
        aboveSelectionRect.bottom = selectionRect.top - (8 * density).toInt()
        val belowSelectionRect = Rect(viewportRect)
        belowSelectionRect.top = selectionRect.bottom + handleHeight

        //Timber.d("-> aboveSelectionRect -> " + aboveSelectionRect);
        //Timber.d("-> belowSelectionRect -> " + belowSelectionRect);

        // Priority to show popupWindow will be as following -
        // 1. Show popupWindow below selectionRect, if space available
        // 2. Show popupWindow above selectionRect, if space available
        // 3. Show popupWindow in the middle of selectionRect

        //popupRect initialisation for belowSelectionRect
        popupRect.left = viewportRect.left
        popupRect.top = belowSelectionRect.top
        popupRect.right = popupRect.left + viewTextSelection.measuredWidth
        popupRect.bottom = popupRect.top + viewTextSelection.measuredHeight
        //Timber.d("-> Pre decision popupRect -> " + popupRect);

        val popupY: Int
        if (belowSelectionRect.contains(popupRect)) {
            Timber.i("-> show below")
            popupY = belowSelectionRect.top

        } else {

            // popupRect initialisation for aboveSelectionRect
            popupRect.top = aboveSelectionRect.top
            popupRect.bottom = popupRect.top + viewTextSelection.measuredHeight

            if (aboveSelectionRect.contains(popupRect)) {
                Timber.i("-> show above")
                popupY = aboveSelectionRect.bottom - popupRect.height()

            } else {

                Timber.i("-> show in middle")
                val popupYDiff = (viewTextSelection.measuredHeight - selectionRect.height()) / 2
                popupY = selectionRect.top - popupYDiff
            }
        }

        val popupXDiff = (viewTextSelection.measuredWidth - selectionRect.width()) / 2
        val popupX = selectionRect.left - popupXDiff

        popupRect.offsetTo(popupX, popupY)
        //Timber.d("-> Post decision popupRect -> " + popupRect);

        // Check if popupRect left side is going outside of the viewportRect
        if (popupRect.left < viewportRect.left) {
            popupRect.right += 0 - popupRect.left
            popupRect.left = 0
        }

        // Check if popupRect right side is going outside of the viewportRect
        if (popupRect.right > viewportRect.right) {
            val dx = popupRect.right - viewportRect.right
            popupRect.left -= dx
            popupRect.right -= dx
        }
    }

    private fun showTextSelectionPopup() {
        Timber.v("-> showTextSelectionPopup")
        Timber.d("-> showTextSelectionPopup -> To be laid out popupRect -> %s", popupRect)

        popupWindow.dismiss()
        oldScrollX = scrollX
        oldScrollY = scrollY

        isScrollingRunnable = Runnable {
            uiHandler.removeCallbacks(isScrollingRunnable)
            val currentScrollX = scrollX
            val currentScrollY = scrollY
            val inTouchMode = lastTouchAction == MotionEvent.ACTION_DOWN ||
                    lastTouchAction == MotionEvent.ACTION_MOVE

            if (oldScrollX == currentScrollX && oldScrollY == currentScrollY && !inTouchMode) {
                Timber.i("-> Stopped scrolling, show Popup")
                popupWindow.dismiss()
                popupWindow = PopupWindow(viewTextSelection, WRAP_CONTENT, WRAP_CONTENT)
                popupWindow.isClippingEnabled = false
                popupWindow.showAtLocation(
                    this@FolioWebView, Gravity.NO_GRAVITY,
                    popupRect.left, popupRect.top
                )
            } else {
                Timber.i("-> Still scrolling, don't show Popup")
                oldScrollX = currentScrollX
                oldScrollY = currentScrollY
                isScrollingCheckDuration += IS_SCROLLING_CHECK_TIMER
                if (isScrollingCheckDuration < IS_SCROLLING_CHECK_MAX_DURATION && !destroyed)
                    uiHandler.postDelayed(isScrollingRunnable, IS_SCROLLING_CHECK_TIMER.toLong())
            }
        }

        uiHandler.removeCallbacks(isScrollingRunnable)
        isScrollingCheckDuration = 0
        if (!destroyed)
            uiHandler.postDelayed(isScrollingRunnable, IS_SCROLLING_CHECK_TIMER.toLong())
    }
}
