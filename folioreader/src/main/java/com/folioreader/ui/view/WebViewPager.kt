package com.folioreader.ui.view

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.webkit.JavascriptInterface
import androidx.core.view.GestureDetectorCompat
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.folioreader.R

class WebViewPager : ViewPager {

    companion object {
        @JvmField
        val LOG_TAG: String = WebViewPager::class.java.simpleName
    }

    private var horizontalPageCount: Int = 0
    private var folioWebView: FolioWebView? = null
    private var takeOverScrolling: Boolean = false
    var isScrolling: Boolean = false
        private set
    private var uiHandler: Handler? = null
    private var gestureDetector: GestureDetectorCompat? = null

    private var lastGestureType: LastGestureType? = null

    private enum class LastGestureType {
        OnSingleTapUp, OnLongPress, OnFling, OnScroll
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {

        uiHandler = Handler()
        gestureDetector = GestureDetectorCompat(context, GestureListener())

        addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                // Log.d(LOG_TAG, "-> onPageScrolled -> position = " + position +
                // ", positionOffset = " + positionOffset + ", positionOffsetPixels = " + positionOffsetPixels);

                isScrolling = true

                if (takeOverScrolling && folioWebView != null) {
                    val scrollX = folioWebView!!.getScrollXPixelsForPage(position) + positionOffsetPixels
                    //Log.d(LOG_TAG, "-> onPageScrolled -> scrollX = " + scrollX);
                    folioWebView!!.scrollTo(scrollX, 0)
                }

                if (positionOffsetPixels == 0) {
                    //Log.d(LOG_TAG, "-> onPageScrolled -> takeOverScrolling = false");
                    takeOverScrolling = false
                    isScrolling = false
                }
            }

            override fun onPageSelected(position: Int) {
                Log.v(LOG_TAG, "-> onPageSelected -> $position")
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    private fun getScrollStateString(state: Int): String {
        return when (state) {
            ViewPager.SCROLL_STATE_IDLE -> "SCROLL_STATE_IDLE"
            ViewPager.SCROLL_STATE_DRAGGING -> "SCROLL_STATE_DRAGGING"
            ViewPager.SCROLL_STATE_SETTLING -> "SCROLL_STATE_SETTLING"
            else -> "UNKNOWN_STATE"
        }
    }

    fun setHorizontalPageCount(horizontalPageCount: Int) {
        //Log.d(LOG_TAG, "-> horizontalPageCount = " + horizontalPageCount);

        this.horizontalPageCount = horizontalPageCount
        adapter = WebViewPagerAdapter()
        currentItem = 0

        if (folioWebView == null)
            folioWebView = (parent as View).findViewById(R.id.folioWebView)
    }

    @JavascriptInterface
    fun setCurrentPage(pageIndex: Int) {
        Log.v(LOG_TAG, "-> setCurrentItem -> pageIndex = $pageIndex")

        uiHandler!!.post { setCurrentItem(pageIndex, false) }
    }

    @JavascriptInterface
    fun setPageToLast() {

        uiHandler!!.post { currentItem = horizontalPageCount - 1 }
    }

    @JavascriptInterface
    fun setPageToFirst() {

        uiHandler!!.post { currentItem = 0 }
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent?): Boolean {
            super@WebViewPager.onTouchEvent(e)
            return true
        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            //Log.d(LOG_TAG, "-> onSingleTapUp");
            lastGestureType = LastGestureType.OnSingleTapUp
            return false
        }

        override fun onLongPress(e: MotionEvent?) {
            super.onLongPress(e)
            //Log.d(LOG_TAG, "-> onLongPress -> " + e);
            lastGestureType = LastGestureType.OnLongPress
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            //Log.v(LOG_TAG, "-> onScroll -> e1 = " + e1 + ", e2 = " + e2 + ", distanceX = " + distanceX + ", distanceY = " + distanceY);
            lastGestureType = LastGestureType.OnScroll
            return false
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            //Log.d(LOG_TAG, "-> onFling -> e1 = " + e1 + ", e2 = " + e2 + ", velocityX = " + velocityX + ", velocityY = " + velocityY);
            lastGestureType = LastGestureType.OnFling
            return false
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        //Log.d(LOG_TAG, "-> onTouchEvent -> " + AppUtil.actionToString(event.getAction()));

        if (event == null)
            return false

        // Rare condition in fast scrolling
        if (gestureDetector == null)
            return false

        val gestureReturn = gestureDetector!!.onTouchEvent(event)
        if (gestureReturn)
            return true

        val superReturn = super.onTouchEvent(event)

        if (event.action == MotionEvent.ACTION_UP) {
            if (lastGestureType == LastGestureType.OnScroll || lastGestureType == LastGestureType.OnFling) {
                //Log.d(LOG_TAG, "-> onTouchEvent -> takeOverScrolling = true, " + "lastGestureType = " + lastGestureType);
                takeOverScrolling = true
            }
            lastGestureType = null
        }

        return superReturn
    }

    private inner class WebViewPagerAdapter : PagerAdapter() {

        override fun getCount(): Int {
            return horizontalPageCount
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {

            val view = LayoutInflater.from(container.context)
                .inflate(R.layout.view_webview_pager, container, false)

            // Debug code
            // Set alpha for folioWebView in folio_page_fragment.xml to 0.5 also.
            /*if (position % 2 == 0) {
                view.setBackgroundResource(R.drawable.green_border_background)
            } else {
                view.setBackgroundResource(R.drawable.blue_border_background)
            }

            val textView = view.findViewById<TextView>(R.id.textView)
            textView.text = Integer.toString(position)*/

            container.addView(view)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }
    }
}
