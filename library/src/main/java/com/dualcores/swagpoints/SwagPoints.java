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

public class SwagPoints extends View {

	public static int INVALID_VALUE = -1;
	public static final int MAX = 100;
	public static final int MIN = 0;

	/**
	 * Offset = -90 indicates that the progress starts from 12 o'clock.
	 */
	private static final int ANGLE_OFFSET = -90;

	/**
	 * The current points value.
	 */
	private int mPoints = MIN;

	/**
	 * The min value of progress value.
	 */
	private int mMin = MIN;

	/**
	 * The Maximum value that this SeekArc can be set to
	 */
	private int mMax = MAX;

	/**
	 * The increment/decrement value for each movement of progress.
	 */
	private int mStep = 10;

	/**
	 * The Drawable for the seek arc thumbnail
	 */
	private Drawable mIndicatorIcon;


	private int mProgressWidth = 4;
	private int mArcWidth = 4;
	private boolean mClockwise = true;
	private boolean mEnabled = true;

	//
	// internal variables
	//
	/**
	 * The counts of point update to determine whether to change previous progress.
	 */
	private int mUpdateTimes = 0;
	private float mPreviousProgress = -1;
	private float mCurrentProgress = 0;

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
	private OnSwagPointsChangeListener mOnSwagPointsChangeListener;

	public SwagPoints(Context context) {
		super(context);
		init(context, null, 0);
	}

	public SwagPoints(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, R.attr.swagPointsStyle);
	}

	public SwagPoints(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	private void init(Context context, AttributeSet attrs, int defStyle) {

		float density = getResources().getDisplayMetrics().density;

		// Defaults, may need to link this into theme settings
		int arcColor = ContextCompat.getColor(context, R.color.color_arc);
		int progressColor = ContextCompat.getColor(context, R.color.color_progress);
		mProgressWidth = (int) (mProgressWidth * density);

		mIndicatorIcon = ContextCompat.getDrawable(context, R.drawable.indicator);

		if (attrs != null) {
			// Attribute initialization
			final TypedArray a = context.obtainStyledAttributes(attrs,
					R.styleable.SwagPoints, defStyle, 0);

			Drawable indicatorIcon = a.getDrawable(R.styleable.SwagPoints_indicatorIcon);
			if (indicatorIcon != null)
				mIndicatorIcon = indicatorIcon;

			int indicatorIconHalfWidth = mIndicatorIcon.getIntrinsicWidth() / 2;
			int indicatorIconHalfHeight = mIndicatorIcon.getIntrinsicHeight() / 2;
			mIndicatorIcon.setBounds(-indicatorIconHalfWidth, -indicatorIconHalfHeight, indicatorIconHalfWidth,
					indicatorIconHalfHeight);

			mPoints = a.getInteger(R.styleable.SwagPoints_points, mPoints);
			mMin = a.getInteger(R.styleable.SwagPoints_min, mMin);
			mMax = a.getInteger(R.styleable.SwagPoints_max, mMax);
			mStep = a.getInteger(R.styleable.SwagPoints_step, mStep);

			mProgressWidth = (int) a.getDimension(R.styleable.SwagPoints_progressWidth, mProgressWidth);
			progressColor = a.getColor(R.styleable.SwagPoints_progressColor, progressColor);

			mArcWidth = (int) a.getDimension(R.styleable.SwagPoints_arcWidth, mArcWidth);
			arcColor = a.getColor(R.styleable.SwagPoints_arcColor, arcColor);

			mClockwise = a.getBoolean(R.styleable.SwagPoints_clockwise,
					mClockwise);
			mEnabled = a.getBoolean(R.styleable.SwagPoints_enabled, mEnabled);
			a.recycle();
		}

		// range check
		mPoints = (mPoints > mMax) ? mMax : mPoints;
		mPoints = (mPoints < mMin) ? mMin : mPoints;

		mProgressSweep = (float) mPoints / mMax * 360;

		mArcPaint = new Paint();
		mArcPaint.setColor(arcColor);
		mArcPaint.setAntiAlias(true);
		mArcPaint.setStyle(Paint.Style.STROKE);
		mArcPaint.setStrokeWidth(mArcWidth);

		mProgressPaint = new Paint();
		mProgressPaint.setColor(progressColor);
		mProgressPaint.setAntiAlias(true);
		mProgressPaint.setStyle(Paint.Style.STROKE);
		mProgressPaint.setStrokeWidth(mProgressWidth);

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
		float top = height / 2 - (arcDiameter / 2);
		float left = width / 2 - (arcDiameter / 2);
		mArcRect.set(left, top, left + arcDiameter, top + arcDiameter);

		updateIndicatorIconPosition();
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (!mClockwise) {
			canvas.scale(-1, 1, mArcRect.centerX(), mArcRect.centerY());
		}

		// draw the arc and progress
		canvas.drawArc(mArcRect, ANGLE_OFFSET, 360, false, mArcPaint);
		canvas.drawArc(mArcRect, ANGLE_OFFSET, mProgressSweep, false, mProgressPaint);

		if (mEnabled) {
			// draw the indicator icon
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
						mOnSwagPointsChangeListener.onStartTrackingTouch(this);
					updateOnTouch(event);
					break;
				case MotionEvent.ACTION_MOVE:
					updateOnTouch(event);
					break;
				case MotionEvent.ACTION_UP:
					if (mOnSwagPointsChangeListener != null)
						mOnSwagPointsChangeListener.onStopTrackingTouch(this);
					setPressed(false);
					this.getParent().requestDisallowInterceptTouchEvent(false);
					break;
				case MotionEvent.ACTION_CANCEL:
					if (mOnSwagPointsChangeListener != null)
						mOnSwagPointsChangeListener.onStopTrackingTouch(this);
					setPressed(false);
					this.getParent().requestDisallowInterceptTouchEvent(false);
					break;
			}
			return true;
		}
		return false;
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		if (mIndicatorIcon != null && mIndicatorIcon.isStateful()) {
			int[] state = getDrawableState();
			mIndicatorIcon.setState(state);
		}
		invalidate();
	}

	/**
	 * Update all the UI components on touch events.
	 * @param event MotionEvent
	 */
	private void updateOnTouch(MotionEvent event) {
		setPressed(true);
		mTouchAngle = convertTouchEventPointToAngle(event.getX(), event.getY());
		int progress = convertAngleToProgress(mTouchAngle);
		updatePoints(progress, true);
	}

	private double convertTouchEventPointToAngle(float xPos, float yPos) {
		// transform touch coordinate into component coordinate
		float x = xPos - mTranslateX;
		float y = yPos - mTranslateY;

		x = (mClockwise) ? x : -x;
		double angle = Math.toDegrees(Math.atan2(y, x) + (Math.PI / 2));
		angle = (angle < 0) ? (angle + 360) : angle;
//		System.out.printf("(%f, %f) %f\n", x, y, angle);
		return angle;
	}

	private int convertAngleToProgress(double angle) {
		int touchProgress = (int) Math.round(valuePerDegree() * angle);
		touchProgress = (touchProgress < mMin) ? INVALID_VALUE : touchProgress;
		touchProgress = (touchProgress > mMax) ? INVALID_VALUE : touchProgress;
		return touchProgress;
	}

	private float valuePerDegree() {
		return (float) (mMax - mMin) / 360;
	}

	private void updatePoints(int progress, boolean fromUser) {
		updateProgress(progress, fromUser);
	}

	private void updateIndicatorIconPosition() {
		int thumbAngle = (int) (mProgressSweep + 90);
		mIndicatorIconX = (int) (mArcRadius * Math.cos(Math.toRadians(thumbAngle)));
		mIndicatorIconY = (int) (mArcRadius * Math.sin(Math.toRadians(thumbAngle)));
	}

	private void updateProgress(int progress, boolean fromUser) {

		// detect points change closed to max or min
		final int maxDetectValue = (int) ((double) mMax * 0.95);
		final int minDetectValue = (int) ((double) mMax * 0.05) + mMin;
//		System.out.printf("(%d, %d) / (%d, %d)\n", mMax, mMin, maxDetectValue, minDetectValue);

		mUpdateTimes++;
		if (progress == INVALID_VALUE) {
			return;
		}

		// avoid accidentally touch to become max from original point
		// 避免在靠近原點點到直接變成最大值
		if (progress > maxDetectValue && mPreviousProgress == INVALID_VALUE) {
//			System.out.printf("Skip (%d) %.0f -> %.0f %s\n",
//					progress, mPreviousProgress, mCurrentProgress, isMax ? "Max" : "");
			return;
		}

		if (mPreviousProgress != mCurrentProgress)
			System.out.printf("Progress (%d)(%f) %.0f -> %.0f (%s, %s)\n",
					progress, mTouchAngle,
					mPreviousProgress, mCurrentProgress,
					isMax ? "Max" : "",
					isMin ? "Min" : "");

		// record previous and current progress change
		// 紀錄目前和前一個進度變化
		if (mUpdateTimes == 1) {
			mCurrentProgress = progress;
		} else {
			mPreviousProgress = mCurrentProgress;
			mCurrentProgress = progress;
		}

//		System.out.printf("New value (%.0f, %.0f)\n", mPreviousProgress, mCurrentProgress);

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
			if (mPreviousProgress >= maxDetectValue && mCurrentProgress <= minDetectValue &&
					mPreviousProgress > mCurrentProgress) {
				isMax = true;
				progress = mMax;
//				Logger.d("Reach Max " + progress);
				if (mOnSwagPointsChangeListener != null) {
					mOnSwagPointsChangeListener
							.onPointsChanged(this, progress, fromUser);
					return;
				}
			} else if (mCurrentProgress >= maxDetectValue && mPreviousProgress <= minDetectValue &&
					mCurrentProgress > mPreviousProgress) {
				isMin = true;
				progress = mMin;
//				Logger.d("Reach Min " + progress);
				if (mOnSwagPointsChangeListener != null) {
					mOnSwagPointsChangeListener
							.onPointsChanged(this, progress, fromUser);
					return;
				}
			}
		}

		// Detect whether decreasing from max or increasing from min, to unlock the update event.
		// Make sure to check in detect range only.
		if (isMax & (mCurrentProgress < mPreviousProgress) && mCurrentProgress >= maxDetectValue) {
//			Logger.d("Unlock max");
			isMax = false;
		}
		if (isMin && (mPreviousProgress < mCurrentProgress) && mPreviousProgress <= minDetectValue) {
//			Logger.d("Unlock min");
			isMin = false;
		}

		if (!isMax && !isMin) {
			progress = (progress > mMax) ? mMax : progress;
			progress = (progress < mMin) ? mMin : progress;
			mPoints = progress;

			if (mOnSwagPointsChangeListener != null) {
				progress = progress - (progress % mStep);

				mOnSwagPointsChangeListener
						.onPointsChanged(this, progress, fromUser);
			}

			mProgressSweep = (float) progress / mMax * 360;
//			System.out.printf("%d, %f\n", progress, mProgressSweep);
			updateIndicatorIconPosition();
			invalidate();
		}
	}

	public interface OnSwagPointsChangeListener {

		/**
		 * Notification that the point value has changed.
		 *
		 * @param swagPoints The SwagPoints view whose value has changed
		 * @param points      The current point value.
		 * @param fromUser   True if the point change was triggered by the user.
		 */
		void onPointsChanged(SwagPoints swagPoints, int points, boolean fromUser);

		void onStartTrackingTouch(SwagPoints swagPoints);

		void onStopTrackingTouch(SwagPoints swagPoints);
	}

	public void setPoints(int points) {
		updateProgress(points, false);
	}

	public int getPoints() {
		return mPoints;
	}

	public int getProgressWidth() {
		return mProgressWidth;
	}

	public void setProgressWidth(int mProgressWidth) {
		this.mProgressWidth = mProgressWidth;
		mProgressPaint.setStrokeWidth(mProgressWidth);
	}

	public int getArcWidth() {
		return mArcWidth;
	}

	public void setArcWidth(int mArcWidth) {
		this.mArcWidth = mArcWidth;
		mArcPaint.setStrokeWidth(mArcWidth);
	}

	public void setClockwise(boolean isClockwise) {
		mClockwise = isClockwise;
	}

	public boolean isClockwise() {
		return mClockwise;
	}

	public boolean isEnabled() {
		return mEnabled;
	}

	public void setEnabled(boolean enabled) {
		this.mEnabled = enabled;
	}

	public int getProgressColor() {
		return mProgressPaint.getColor();
	}

	public void setProgressColor(int color) {
		mProgressPaint.setColor(color);
		invalidate();
	}

	public int getArcColor() {
		return mArcPaint.getColor();
	}

	public void setArcColor(int color) {
		mArcPaint.setColor(color);
		invalidate();
	}

	public int getMax() {
		return mMax;
	}

	public void setMax(int mMax) {
		this.mMax = mMax;
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

	public void setOnSwagPointsChangeListener(OnSwagPointsChangeListener onSwagPointsChangeListener) {
		mOnSwagPointsChangeListener = onSwagPointsChangeListener;
	}
}
