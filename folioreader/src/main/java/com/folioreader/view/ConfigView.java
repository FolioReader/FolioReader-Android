package com.folioreader.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import com.folioreader.Font;
import com.folioreader.R;
import com.folioreader.adapter.FontAdapter;
import com.folioreader.util.Tags;
import java.util.ArrayList;

public class ConfigView extends FrameLayout implements View.OnClickListener {

  private static final float SENSITIVITY = 1.0f;
  private static final float DEFAULT_DRAG_LIMIT = 0.5f;
  private static final int INVALID_POINTER = -1;
  private static final int FADE_DAY_NIGHT_MODE = 500;

  private int activePointerId = INVALID_POINTER;

  private boolean isNightMode = false;

  private float verticalDragRange;

  private RelativeLayout container;
  private RecyclerView recyclerViewFonts;
  private ImageButton dayButton;
  private ImageButton nightButton;
  private ViewDragHelper viewDragHelper;
  private ConfigViewCallback configViewCallback;

  public ConfigView(Context context) {
    this(context, null);
  }

  public ConfigView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ConfigView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  private void inflateView() {
    inflate(getContext(), R.layout.view_config, this);
    container = (RelativeLayout) findViewById(R.id.container);
    recyclerViewFonts = (RecyclerView) findViewById(R.id.recycler_view_fonts);
    dayButton = (ImageButton) findViewById(R.id.day_button);
    nightButton = (ImageButton) findViewById(R.id.night_button);
    dayButton.setTag(Tags.DAY_BUTTON);
    nightButton.setTag(Tags.NIGHT_BUTTON);
    dayButton.setOnClickListener(this);
    nightButton.setOnClickListener(this);
  }

  private void configRecyclerViewFonts() {
    recyclerViewFonts.setLayoutManager(
        new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
    FontAdapter fontAdapter = new FontAdapter();
    String[] fontsText = getResources().getStringArray(R.array.fonts);
    ArrayList<Font> fonts = new ArrayList<>(fontsText.length);
    for(String font : fontsText) {
      fonts.add(new Font(font));
    }
    fontAdapter.setFonts(fonts);
    recyclerViewFonts.setAdapter(fontAdapter);
  }

  private void toggleBlackTheme() {

    AnimatorSet set = new AnimatorSet();

    int day = getResources().getColor(R.color.white);
    int night = getResources().getColor(R.color.night);
    int darkNight = getResources().getColor(R.color.dark_night);

    ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(),
        isNightMode ? night : day, isNightMode ? day : night);
    colorAnimation.setDuration(FADE_DAY_NIGHT_MODE);
    colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

      @Override public void onAnimationUpdate(ValueAnimator animator) {
        container.setBackgroundColor((int) animator.getAnimatedValue());
      }
    });

    ValueAnimator colorActivityAnimation = ValueAnimator.ofObject(new ArgbEvaluator(),
        isNightMode ? darkNight : day, isNightMode ? day : darkNight);
    colorAnimation.setDuration(FADE_DAY_NIGHT_MODE);
    colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

      @Override public void onAnimationUpdate(ValueAnimator animator) {
        if (configViewCallback != null) {
          configViewCallback.onBackgroundUpdate((int) animator.getAnimatedValue());
        }
      }
    });

    set.addListener(new Animator.AnimatorListener() {
      @Override public void onAnimationStart(Animator animation) {
      }

      @Override public void onAnimationEnd(Animator animation) {
        isNightMode = !isNightMode;
      }

      @Override public void onAnimationCancel(Animator animation) {
      }

      @Override public void onAnimationRepeat(Animator animation) {
      }
    });
    set.setDuration(FADE_DAY_NIGHT_MODE);
    set.playTogether(colorAnimation, colorActivityAnimation);
  }

  /**
   * Bind the attributes of the view and config
   * the DragView with these params.
   */
  @Override protected void onFinishInflate() {
    super.onFinishInflate();
    if (!isInEditMode()) {
      inflateView();
      configRecyclerViewFonts();
      configDragViewHelper();
    }
  }

  /**
   * Updates the view size if needed.
   * @param width The new width size.
   * @param height The new height size.
   * @param oldWidth The old width size, useful the calculate the diff.
   * @param oldHeight The old height size, useful the calculate the diff.
   */
  @Override protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
    super.onSizeChanged(width, height, oldWidth, oldHeight);
    setVerticalDragRange(height);
  }

  /**
   * Configure the width and height of the DraggerView.
   *
   * @param widthMeasureSpec Spec value of width, not represent the real width.
   * @param heightMeasureSpec Spec value of height, not represent the real height.
   */
  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    int measureWidth = MeasureSpec.makeMeasureSpec(
        getMeasuredWidth() - getPaddingLeft() - getPaddingRight(),
        MeasureSpec.EXACTLY);
    int measureHeight = MeasureSpec.makeMeasureSpec(
        getMeasuredHeight() - getPaddingTop() - getPaddingBottom(),
        MeasureSpec.EXACTLY);
    if (container != null) {
      container.measure(measureWidth, measureHeight);
    }

  }

  /**
   * Detect the type of motion event (like touch)
   * at the DragView, this can be a simple
   * detector of the touch, not the listener ifself.
   *
   * @param ev Event of MotionEvent
   * @return View is touched
   */
  @Override public boolean onInterceptTouchEvent(MotionEvent ev) {
    if (!isEnabled()) {
      return false;
    }
    final int action = MotionEventCompat.getActionMasked(ev);
    switch (action) {
      case MotionEvent.ACTION_CANCEL:
      case MotionEvent.ACTION_UP:
        viewDragHelper.cancel();
        return false;
      case MotionEvent.ACTION_DOWN:
        int index = MotionEventCompat.getActionIndex(ev);
        activePointerId = MotionEventCompat.getPointerId(ev, index);
        if (activePointerId == INVALID_POINTER) {
          return false;
        }
      default:
        return viewDragHelper.shouldInterceptTouchEvent(ev);
    }
  }

  /**
   * Handle the touch event intercepted from onInterceptTouchEvent
   * method, this method valid if the touch listener
   * is a valid pointer(like fingers) or the touch
   * is inside of the DragView.
   *
   * @param ev MotionEvent instance, can be used to detect the type of touch.
   * @return Touched area is a valid position.
   */
  @Override public boolean onTouchEvent(MotionEvent ev) {
    int actionMasked = MotionEventCompat.getActionMasked(ev);
    if ((actionMasked & MotionEventCompat.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
      activePointerId = MotionEventCompat.getPointerId(ev, actionMasked);
    }
    if (activePointerId == INVALID_POINTER) {
      return false;
    }
    viewDragHelper.processTouchEvent(ev);
    return isViewHit(container, (int) ev.getX(), (int) ev.getY());
  }

  @Override public void onClick(View v) {
    switch (((Integer) v.getTag())) {
      case Tags.DAY_BUTTON:
        if (isNightMode) {
          isNightMode = true;
          toggleBlackTheme();
        }
        break;
      case Tags.NIGHT_BUTTON:
        if (!isNightMode) {
          isNightMode = false;
          toggleBlackTheme();
        }
        break;
      default:
        break;
    }
  }

  /**
   * This method is needed to calculate the auto scroll
   * when the user slide the view to the max limit, this
   * starts a animation to finish the view.
   */
  @Override public void computeScroll() {
    if (!isInEditMode() && viewDragHelper.continueSettling(true)) {
      ViewCompat.postInvalidateOnAnimation(this);
    }
  }

  /**
   * Configure the DragViewHelper instance adding a
   * instance of ViewDragHelperCallback, useful to
   * detect the touch callbacks from dragView.
   */
  private void configDragViewHelper() {
    viewDragHelper = ViewDragHelper.create(this, SENSITIVITY,
        new ConfigViewHelperCallback(this));
  }

  private boolean smoothSlideTo(View view, int x, int y) {
    if (viewDragHelper != null && viewDragHelper.smoothSlideViewTo(view, x, y)) {
      ViewCompat.postInvalidateOnAnimation(this);
      return true;
    }
    return false;
  }

  public float getVerticalDragRange() {
    return verticalDragRange;
  }

  public void setVerticalDragRange(float verticalDragRange) {
    this.verticalDragRange = verticalDragRange;
  }

  public RelativeLayout getContainer() {
    return container;
  }

  public void setConfigViewCallback(ConfigViewCallback configViewCallback) {
    this.configViewCallback = configViewCallback;
  }

  /**
   * Detect if the touch on the screen is at the region of the view.
   * @param view Instance of the view that will be verified.
   * @param x X position of the touch.
   * @param y Y position of the touch.
   * @return Position is at the region of the view.
   */
  private boolean isViewHit(View view, int x, int y) {
    int[] viewLocation = new int[2];
    view.getLocationOnScreen(viewLocation);
    int[] parentLocation = new int[2];
    this.getLocationOnScreen(parentLocation);
    int screenX = parentLocation[0] + x;
    int screenY = parentLocation[1] + y;
    return screenX >= viewLocation[0]
        && screenX < viewLocation[0] + view.getWidth()
        && screenY >= viewLocation[1]
        && screenY < viewLocation[1] + view.getHeight();
  }

  /**
   * Detect if the container actual position is above the
   * limit determined with the @param dragLimit.
   *
   * @return Use a dimension and compare with the dragged
   * axis position.
   */
  public boolean isDragViewAboveTheLimit() {
    int parentSize = container.getHeight();
    return parentSize < ViewCompat.getY(container) + (parentSize * DEFAULT_DRAG_LIMIT);
  }

  public void moveToOriginalPosition() {
    boolean success = smoothSlideTo(container, 0, 0);
  }

  public void moveOffScreen() {
    smoothSlideTo(container, 0, (int) getVerticalDragRange());
  }

}
