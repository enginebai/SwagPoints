package com.dualcores.swagpoints;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by enginebai on 2016/8/7.
 */
public class SwagPoints extends View {

	public static final int INVALID_VALUE = -1;
	public static final int MAX = 100;
	public static final int MIN = 0;
	/**
	 * Offset = -90 indicates that the progress starts from 12 o'clock.
	 */
	private static final int ANGLE_OFFSET = -90;

	/**
	 * The current progress value.
	 */
	private int mPoints = MIN;

	/**
	 * The max value of progress value.
	 */
	private int mMax = MAX;

	/**
	 * The min value of progress value.
	 */
	private int mMin = MIN;

	/**
	 * The increment/decrement value for each movement of progress.
	 */
	private int mStep = 1;

	/**
	 * The indicator icon drawable.
	 */
	private Drawable mIndicatorIcon;

	/**
	 * The line width of progress.
	 */
	private int mProgressWidth = 4;
	private int mProgressColor;

	private int mArcColor;
	private int mArcWidth = 4;

	private boolean mClosewise = true;
	private boolean mEnabled = true;

	//
	// deprecated
	//
	/**
	 * @deprecated
	 */
	private boolean mRoundEdges = false;
	/**
	 * @deprecated
	 */
	private int mSweepAngle = 360;
	/**
	 * @deprecated
	 */
	private int mRotation = 0;
	/**
	 * @deprecated
	 */
	private int mStartAngle = 0;
	/**
	 * @deprecated
	 */
	private boolean mTouchInside = true;

	//
	// internal variables
	//
	/**
	 * The counts of point update to determine whether to change previous progress.
	 */
	private int mUpdateTimes = 0;
	private int mPreviousProgress = -1;
	private int mCurrentProgress = 0;
	/**
	 * Determine whether reach max of point.
	 */
	private boolean isMax = false;
	/**
	 * Determine whether reach min of point.
	 */
	private boolean isMin = false;

	private int mArcRadius = 0;
	private RectF mArcRect = new RectF();
	private Paint mArcPaint;
	/**
	 * @deprecated
	 */
	private float mProgressSweep = 0;
	private Paint mProgressPaint;

	private int mTranslateX;
	private int mTranslateY;

	// the (x, y) coordinator of indicator icon
	private int mIndicatorIconX;
	private int mIndicatorIconY;

	/**
	 * The current touch angle of arc.
	 */
	private double mTouchAngle;
	private float mTouchIgnoreRadius;
	private OnSwagPointsChangeListener mOnSwagPointsChangeListener;

	public SwagPoints(Context context) {
		super(context);
		initView(context, null, 0);
	}

	public SwagPoints(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context, attrs, R.attr.swagPointsStyle);
	}

	public SwagPoints(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initView(context, attrs, defStyleAttr);
	}

	private void initView(Context context, AttributeSet attrs, int defStyle) {
		float density = context.getResources().getDisplayMetrics().density;

		mArcColor = ContextCompat.getColor(context, R.color.color_arc);
		mProgressColor = ContextCompat.getColor(context, R.color.color_progress);
		mProgressWidth = (int) (mProgressWidth * density);

		mIndicatorIcon = ContextCompat.getDrawable(context, R.drawable.indicator);

		// read in the style attributes
		if (attrs != null) {
			final TypedArray array = context.obtainStyledAttributes(attrs,
					R.styleable.SwagPoints, defStyle, 0);

			Drawable indicatorIcon = array.getDrawable(R.styleable.SwagPoints_indicatorIcon);
			if (indicatorIcon != null)
				mIndicatorIcon = indicatorIcon;

			int indicatorIconHalfWidth = mIndicatorIcon.getIntrinsicWidth() / 2;
			int indicatorIconHalfHeight = mIndicatorIcon.getIntrinsicHeight() / 2;
			mIndicatorIcon.setBounds(-indicatorIconHalfWidth, -indicatorIconHalfHeight,
					indicatorIconHalfWidth, indicatorIconHalfHeight);

			mMin = array.getInteger(R.styleable.SwagPoints_min, mMin);
			mMax = array.getInteger(R.styleable.SwagPoints_max, mMax);
			mStep = array.getInteger(R.styleable.SwagPoints_step, mStep);
			mPoints = array.getInteger(R.styleable.SwagPoints_points, mPoints);
			mProgressWidth = (int) array.getDimension(
					R.styleable.SwagPoints_progressWidth, mProgressWidth);
			mProgressColor = array.getColor(R.styleable.SwagPoints_progressColor, mProgressColor);

			mArcWidth = (int) array.getDimension(R.styleable.SwagPoints_arcWidth, mArcWidth);
			mArcColor = array.getColor(R.styleable.SwagPoints_arcColor, mArcColor);

			mEnabled = array.getBoolean(R.styleable.SwagPoints_enabled, mEnabled);

			mStartAngle = array.getInteger(R.styleable.SwagPoints_startAngle, mStartAngle);
			mSweepAngle = array.getInteger(R.styleable.SwagPoints_sweepAngle, mSweepAngle);
			mRotation = array.getInteger(R.styleable.SwagPoints_rotation, mRotation);
			mRoundEdges = array.getBoolean(R.styleable.SwagPoints_roundEdges, mRoundEdges);
			mTouchInside = array.getBoolean(R.styleable.SwagPoints_touchInside, mTouchInside);

			array.recycle();
		}

		// range check
		mPoints = mPoints > mMax ? mMax : mPoints;
		mPoints = mPoints < mMin ? mMin : mPoints;

		mArcPaint = new Paint();
		mArcPaint.setColor(mArcColor);
		mArcPaint.setAntiAlias(true);
		mArcPaint.setStyle(Paint.Style.STROKE);
		mArcPaint.setStrokeWidth(mArcWidth);

		mProgressPaint = new Paint();
		mProgressPaint.setColor(mProgressColor);
		mProgressPaint.setAntiAlias(true);
		mProgressPaint.setStyle(Paint.Style.STROKE);
		mProgressPaint.setStrokeWidth(mProgressWidth);

		mSweepAngle = mSweepAngle > 360 ? 360 : mSweepAngle;
		mSweepAngle = mSweepAngle < 0 ? 0 : mSweepAngle;

		mStartAngle = mStartAngle > 360 ? 360 : mStartAngle;
		mStartAngle = mStartAngle < 0 ? 0 : mStartAngle;

		mProgressSweep = (float) mPoints / mMax * mSweepAngle;

		if (mRoundEdges) {
			mArcPaint.setStrokeCap(Paint.Cap.ROUND);
			mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
		}
	}

	public interface OnSwagPointsChangeListener {

		/**
		 * Notification that the point value has changed.
		 *
		 * @param swagPoints The SwagPoints view whose value has changed
		 * @param point      The current point value.
		 * @param fromUser   True if the point change was triggered by the user.
		 */
		void onPointsChanged(SwagPoints swagPoints, int point, boolean fromUser);

		void onStartTackingTouch(SwagPoints swagPoints);

		void onStopTrackingTouch(SwagPoints swagPoints);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
		final int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
		final int min = Math.min(width, height);

		mTranslateX = (int) (width * 0.5f);
		mTranslateY = (int) (height * 0.5f);

		int arcDiameter = min - getPaddingLeft();
		mArcRadius = arcDiameter / 2;
		float top = height / 2 - mArcRadius;
		float left = width / 2 - mArcRadius;
		mArcRect.set(left, top, left + arcDiameter, top + arcDiameter);

		updateIndicatorIconPosition();

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (!mClosewise)
			canvas.scale(-1, 1, mArcRect.centerX(), mArcRect.centerY());

		// draw the arc and progress
		final int arcStart = mStartAngle + mRotation + ANGLE_OFFSET;
		final int arcSweep = mSweepAngle;
		canvas.drawArc(mArcRect, arcStart, arcSweep, false, mArcPaint);
		canvas.drawArc(mArcRect, arcStart, mProgressSweep, false, mProgressPaint);

		// draw the indicator icon
		if (mEnabled) {
			canvas.translate(mTranslateX - mIndicatorIconX, mTranslateY - mIndicatorIconY);
			mIndicatorIcon.draw(canvas);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mEnabled) {
			// 阻止父View去攔截onTouchEvent()事件，確保touch事件可以正確傳遞到此層View。
			this.getParent().requestDisallowInterceptTouchEvent(true);
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if (mOnSwagPointsChangeListener != null)
						mOnSwagPointsChangeListener.onStartTackingTouch(this);
					updateOnTouch(event);
					break;

				case MotionEvent.ACTION_MOVE:
					updateOnTouch(event);
					break;

				case MotionEvent.ACTION_UP:
					if (mOnSwagPointsChangeListener != null)
						mOnSwagPointsChangeListener.onStopTrackingTouch(this);
					updateOnTouch(event);
					this.getParent().requestDisallowInterceptTouchEvent(false);
					break;

				case MotionEvent.ACTION_CANCEL:
					if (mOnSwagPointsChangeListener != null)
						mOnSwagPointsChangeListener.onStartTackingTouch(this);
					setPressed(false);
					this.getParent().requestDisallowInterceptTouchEvent(false);
					break;
			}
			return true;
		}
		return false;
	}

	/**
	 * Update all the UI components on touch.
	 *
	 * @param event MotionEvent
	 */
	private void updateOnTouch(MotionEvent event) {
		setPressed(true);
		mTouchAngle = convertTouchEventPointToAngle(event.getX(), event.getY());
//		Log.d(this.getClass().getSimpleName(), "Touch angle = " + mTouchAngle);
		int progress = convertAngleToProgress(mTouchAngle);
		updatePoints(progress, true);
	}

	/**
	 * Convert coordinates to degree of arc.
	 *
	 * @param eventX touch point x
	 * @param eventY touch point y
	 * @return degree of arc.
	 */
	private double convertTouchEventPointToAngle(float eventX, float eventY) {

		// transform touch coordinate into component coordinate
		float x = eventX - mTranslateY;
		float y = eventY - mTranslateY;
//		System.out.printf("%.2f, %.2f\n", x, y);

		x = mClosewise ? x : -x;
		double angle = Math.toDegrees(Math.atan2(y, x) + (Math.PI / 2) - Math.toRadians(mRotation));
		angle = (angle < 0) ? (angle + 360) : angle;
		angle -= mStartAngle;
		return angle;
	}

	private int convertAngleToProgress(double touchAngle) {
		int touchProgress = (int) (Math.round(getPointsPerDegree() * touchAngle));
		touchProgress = (touchProgress < mMin) ? INVALID_VALUE : touchProgress;
		touchProgress = (touchProgress > mMax) ? INVALID_VALUE : touchProgress;
		return touchProgress;
	}

	/**
	 * Get the points difference per degree of arc.
	 *
	 * @return points per degree
	 */
	private float getPointsPerDegree() {
		return (float) (mMax - mMin) / mSweepAngle;
	}

	private void updatePoints(int progress, boolean isFromUser) {

		// detect points change closed to max or min
		final int maxDetectPoints = (int) ((double) mMax * 0.95);
		final int minDetectPoints = (int) ((double) mMin * 0.05) + mMin;

		mUpdateTimes++;
//		System.out.printf("Progress = %d\n", progress);
		if (progress == INVALID_VALUE)
			return;

		// avoid accidentally touch to become max from original point
		// 避免在靠近原點點到直接變成最大值
		if (progress > maxDetectPoints && mPreviousProgress == INVALID_VALUE)
			return;

		if (mPreviousProgress != mCurrentProgress)
			System.out.printf("Progress (%d)(%f) %d -> %d (%s, %s)\n",
					progress, mTouchAngle,
					mPreviousProgress, mCurrentProgress,
					isMax ? "Max" : "",
					isMin ? "Min" : "");

		// record previous and current progress change
		if (mUpdateTimes == 1)
			mCurrentProgress = progress;
		else {
			mPreviousProgress = mCurrentProgress;
			mCurrentProgress = progress;
		}

		if (mPreviousProgress == mCurrentProgress)
			return;

		System.out.printf("New value (%d -> %d)\n", mPreviousProgress, mCurrentProgress);

		/**
		 * Determine whether reach max or min to lock point update event.
		 *
		 * When reaching max, the progress will drop from max (or maxDetectPoints ~ max
		 * to min (or min ~ minDetectPoints) and vice versa.
		 *
		 * If reach max or min, stop increasing / decreasing to avoid exceeding the max / min.
		 */
		// 判斷超過最大值或最小值，最大最小值不重複判斷
		// 用數值範圍判斷預防轉太快直接略過最大最小值。
		// progress變化可能從98 -> 0/1 or 0/1 -> 98/97，而不會過0或100
		if (mUpdateTimes > 1 && !isMin && !isMax) {
			if (mPreviousProgress >= maxDetectPoints && mCurrentProgress <= minDetectPoints &&
					mPreviousProgress > mCurrentProgress) {
				isMax = true;
				progress = mMax;
				if (mOnSwagPointsChangeListener != null) {
					mOnSwagPointsChangeListener.onPointsChanged(this, progress, isFromUser);
					return;
				}
			} else if (mCurrentProgress >= maxDetectPoints && mPreviousProgress <= minDetectPoints &&
					mCurrentProgress > mPreviousProgress) {
				isMin = true;
				progress = mMin;
				if (mOnSwagPointsChangeListener != null) {
					mOnSwagPointsChangeListener.onPointsChanged(this, progress, isFromUser);
					return;
				}
			}
		}

		// Detect whether decreasing from max or increasing from min, to unlock the update event.
		// Make sure to check in detect range only.
		if (isMax && (mCurrentProgress < mPreviousProgress) && mCurrentProgress >= maxDetectPoints)
			isMax = false;
		if (isMin && (mPreviousProgress < mCurrentProgress) && mPreviousProgress <= minDetectPoints)
			isMin = false;

		if (!isMax && !isMin) {
			progress = (progress > mMax) ? mMax : progress;
			progress = (progress < mMin) ? mMin : progress;
			mPoints = progress;

			if (mOnSwagPointsChangeListener != null) {
				progress = progress - (progress % mStep);
				mOnSwagPointsChangeListener.onPointsChanged(this, progress, isFromUser);
			}
			mProgressSweep = (float)progress / mMax * mSweepAngle;
			updateIndicatorIconPosition();
			invalidate();
		}
	}

	private void updateIndicatorIconPosition() {
		int arcStart = (int) mProgressSweep + mStartAngle + mRotation + 90;
		mIndicatorIconX = (int) (mArcRadius * Math.cos(Math.toRadians(arcStart)));
		mIndicatorIconY = (int) (mArcRadius * Math.sin(Math.toRadians(arcStart)));
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		if (mIndicatorIcon != null && mIndicatorIcon.isStateful()) {
			int[] states = getDrawableState();
			mIndicatorIcon.setState(states);
		}
		invalidate();
	}

	public int getPoints() {
		return mPoints;
	}

	public void setPoints(int points) {
		mPoints = points;
	}

	public int getMax() {
		return mMax;
	}

	public void setMax(int max) {
		mMax = max;
	}

	public int getMin() {
		return mMin;
	}

	public void setMin(int min) {
		mMin = min;
	}

	public int getStep() {
		return mStep;
	}

	public void setStep(int step) {
		mStep = step;
	}

	public Drawable getIndicatorIcon() {
		return mIndicatorIcon;
	}

	public void setIndicatorIcon(Drawable indicatorIcon) {
		mIndicatorIcon = indicatorIcon;
	}

	public int getProgressWidth() {
		return mProgressWidth;
	}

	public void setProgressWidth(int progressWidth) {
		mProgressWidth = progressWidth;
	}

	public int getProgressColor() {
		return mProgressColor;
	}

	public void setProgressColor(int progressColor) {
		mProgressColor = progressColor;
	}

	public int getArcColor() {
		return mArcColor;
	}

	public void setArcColor(int arcColor) {
		mArcColor = arcColor;
	}

	public int getArcWidth() {
		return mArcWidth;
	}

	public void setArcWidth(int arcWidth) {
		mArcWidth = arcWidth;
	}

	public boolean isClosewise() {
		return mClosewise;
	}

	public void setClosewise(boolean closewise) {
		mClosewise = closewise;
	}

	@Override
	public boolean isEnabled() {
		return mEnabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		mEnabled = enabled;
	}

	public void setOnSwagPointsChangeListener(OnSwagPointsChangeListener onSwagPointsChangeListener) {
		mOnSwagPointsChangeListener = onSwagPointsChangeListener;
	}
}
