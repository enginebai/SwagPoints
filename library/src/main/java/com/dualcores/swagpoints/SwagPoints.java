package com.dualcores.swagpoints;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class SwagPoints extends View {

	private static final String TAG = SwagPoints.class.getSimpleName();
	private static int INVALID_PROGRESS_VALUE = -1;
	// The initial rotational offset -90 means we start at 12 o'clock
	private final int mAngleOffset = -90;

	/**
	 * The Drawable for the seek arc thumbnail
	 */
	private Drawable mThumb;

	private int mMin = 0;

	/**
	 * The Maximum value that this SeekArc can be set to
	 */
	private int mMax = 100;

	/**
	 * The Current value that the SeekArc is set to
	 */
	private int mProgress = 0;

	/**
	 * The width of the progress line for this SeekArc
	 */
	private int mProgressWidth = 4;

	/**
	 * The Width of the background arc for the SeekArc
	 */
	private int mArcWidth = 2;

	/**
	 * The Angle to start drawing this Arc from
	 */
	private int mStartAngle = 0;

	/**
	 * The Angle through which to draw the arc (Max is 360)
	 */
	private int mSweepAngle = 360;

	/**
	 * The rotation of the SeekArc- 0 is twelve o'clock
	 */
	private int mRotation = 0;

	/**
	 * Give the SeekArc rounded edges
	 */
	private boolean mRoundedEdges = false;

	/**
	 * Enable touch inside the SeekArc
	 */
	private boolean mTouchInside = true;

	/**
	 * Will the progress increase clockwise or anti-clockwise
	 */
	private boolean mClockwise = true;


	/**
	 * is the control enabled/touchable
	 */
	private boolean mEnabled = true;

	/**
	 * 滑動後，增減的數值變化。
	 */
	private int mStep = 10;

	// Internal variables
	private int mArcRadius = 0;
	private float mProgressSweep = 0;
	private RectF mArcRect = new RectF();
	private Paint mArcPaint;
	private Paint mProgressPaint;
	private int mTranslateX;
	private int mTranslateY;
	private int mThumbXPos;
	private int mThumbYPos;
	private double mTouchAngle;
	private float mTouchIgnoreRadius;
	private OnSeekArcChangeListener mOnSeekArcChangeListener;

	// 計算progress更新的次數
	private int mUpdateTimes = 0;
	private float mPreviousProgress = -1;
	private float mCurrentProgress = 0;

	private boolean isMax = false;
	private boolean isMin = false;

	public interface OnSeekArcChangeListener {

		/**
		 * Notification that the progress level has changed. Clients can use the
		 * fromUser parameter to distinguish user-initiated changes from those
		 * that occurred programmatically.
		 *
		 * @param swagPoints  The SeekArc whose progress has changed
		 * @param progress The current progress level. This will be in the range
		 *                 0..max where max was set by
		 * @param fromUser True if the progress change was initiated by the user.
		 */
		void onProgressChanged(SwagPoints swagPoints, int progress, boolean fromUser);

		/**
		 * Notification that the user has started a touch gesture. Clients may
		 * want to use this to disable advancing the seekbar.
		 *
		 * @param swagPoints The SeekArc in which the touch gesture began
		 */
		void onStartTrackingTouch(SwagPoints swagPoints);

		/**
		 * Notification that the user has finished a touch gesture. Clients may
		 * want to use this to re-enable advancing the seekarc.
		 *
		 * @param swagPoints The SeekArc in which the touch gesture began
		 */
		void onStopTrackingTouch(SwagPoints swagPoints);
	}

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

		Log.d(TAG, "Initialising SeekArc");
		final Resources res = getResources();
		float density = context.getResources().getDisplayMetrics().density;

		// Defaults, may need to link this into theme settings
		int arcColor = res.getColor(R.color.color_arc);
		int progressColor = res.getColor(R.color.color_progress);
		int thumbHalfheight = 0;
		int thumbHalfWidth = 0;
		mThumb = res.getDrawable(R.drawable.indicator);
		// Convert progress width to pixels for current density
		mProgressWidth = (int) (mProgressWidth * density);


		if (attrs != null) {
			// Attribute initialization
			final TypedArray a = context.obtainStyledAttributes(attrs,
					R.styleable.SwagPoints, defStyle, 0);

			Drawable thumb = a.getDrawable(R.styleable.SwagPoints_indicatorIcon);
			if (thumb != null) {
				mThumb = thumb;
			}

			thumbHalfheight = (int) mThumb.getIntrinsicHeight() / 2;
			thumbHalfWidth = (int) mThumb.getIntrinsicWidth() / 2;
			mThumb.setBounds(-thumbHalfWidth, -thumbHalfheight, thumbHalfWidth,
					thumbHalfheight);

			mMin = a.getInteger(R.styleable.SwagPoints_min, mMin);
			mMax = a.getInteger(R.styleable.SwagPoints_max, mMax);
			mProgress = a.getInteger(R.styleable.SwagPoints_points, mProgress);
			mProgressWidth = (int) a.getDimension(
					R.styleable.SwagPoints_progressWidth, mProgressWidth);
			mArcWidth = (int) a.getDimension(R.styleable.SwagPoints_arcWidth,
					mArcWidth);
			mStartAngle = a.getInt(R.styleable.SwagPoints_startAngle, mStartAngle);
			mSweepAngle = a.getInt(R.styleable.SwagPoints_sweepAngle, mSweepAngle);
			mRotation = a.getInt(R.styleable.SwagPoints_rotation, mRotation);
			mRoundedEdges = a.getBoolean(R.styleable.SwagPoints_roundEdges,
					mRoundedEdges);
			mTouchInside = a.getBoolean(R.styleable.SwagPoints_touchInside,
					mTouchInside);
			mClockwise = a.getBoolean(R.styleable.SwagPoints_clockwise,
					mClockwise);
			mEnabled = a.getBoolean(R.styleable.SwagPoints_enabled, mEnabled);

			arcColor = a.getColor(R.styleable.SwagPoints_arcColor, arcColor);
			progressColor = a.getColor(R.styleable.SwagPoints_progressColor,
					progressColor);

			mStep = a.getInteger(R.styleable.SwagPoints_step, mStep);
			a.recycle();
		}

		mSweepAngle = (mSweepAngle > 360) ? 360 : mSweepAngle;
		mSweepAngle = (mSweepAngle < 0) ? 0 : mSweepAngle;

		mProgressSweep = (float) mProgress / mMax * mSweepAngle;
		mProgress = (mProgress > mMax) ? mMax : mProgress;
		mProgress = (mProgress < mMin) ? mMin : mProgress;

		mStartAngle = (mStartAngle > 360) ? 0 : mStartAngle;
		mStartAngle = (mStartAngle < 0) ? 0 : mStartAngle;

		mArcPaint = new Paint();
		mArcPaint.setColor(arcColor);
		mArcPaint.setAntiAlias(true);
		mArcPaint.setStyle(Paint.Style.STROKE);
		mArcPaint.setStrokeWidth(mArcWidth);
		//mArcPaint.setAlpha(45);

		mProgressPaint = new Paint();
		mProgressPaint.setColor(progressColor);
		mProgressPaint.setAntiAlias(true);
		mProgressPaint.setStyle(Paint.Style.STROKE);
		mProgressPaint.setStrokeWidth(mProgressWidth);

		if (mRoundedEdges) {
			mArcPaint.setStrokeCap(Paint.Cap.ROUND);
			mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (!mClockwise) {
			canvas.scale(-1, 1, mArcRect.centerX(), mArcRect.centerY());
		}

		// Draw the arcs
		final int arcStart = mStartAngle + mAngleOffset + mRotation;
		final int arcSweep = mSweepAngle;
		canvas.drawArc(mArcRect, arcStart, arcSweep, false, mArcPaint);
		canvas.drawArc(mArcRect, arcStart, mProgressSweep, false,
				mProgressPaint);

		if (mEnabled) {
			// Draw the thumb nail
			canvas.translate(mTranslateX - mThumbXPos, mTranslateY - mThumbYPos);
			mThumb.draw(canvas);
		}
	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		final int height = getDefaultSize(getSuggestedMinimumHeight(),
				heightMeasureSpec);
		final int width = getDefaultSize(getSuggestedMinimumWidth(),
				widthMeasureSpec);
		final int min = Math.min(width, height);

		mTranslateX = (int) (width * 0.5f);
		mTranslateY = (int) (height * 0.5f);

		int arcDiameter = min - getPaddingLeft();
		mArcRadius = arcDiameter / 2;
		float top = height / 2 - (arcDiameter / 2);
		float left = width / 2 - (arcDiameter / 2);
		mArcRect.set(left, top, left + arcDiameter, top + arcDiameter);

		int arcStart = (int) mProgressSweep + mStartAngle + mRotation + 90;
		mThumbXPos = (int) (mArcRadius * Math.cos(Math.toRadians(arcStart)));
		mThumbYPos = (int) (mArcRadius * Math.sin(Math.toRadians(arcStart)));

		setTouchInSide(mTouchInside);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mEnabled) {
			this.getParent().requestDisallowInterceptTouchEvent(true);

			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					onStartTrackingTouch();
					updateOnTouch(event);
					break;
				case MotionEvent.ACTION_MOVE:
					updateOnTouch(event);
					break;
				case MotionEvent.ACTION_UP:
					onStopTrackingTouch();
					setPressed(false);
					this.getParent().requestDisallowInterceptTouchEvent(false);
					break;
				case MotionEvent.ACTION_CANCEL:
					onStopTrackingTouch();
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
		if (mThumb != null && mThumb.isStateful()) {
			int[] state = getDrawableState();
			mThumb.setState(state);
		}
		invalidate();
	}

	private void onStartTrackingTouch() {
		if (mOnSeekArcChangeListener != null) {
			mOnSeekArcChangeListener.onStartTrackingTouch(this);
		}
	}

	private void onStopTrackingTouch() {
		if (mOnSeekArcChangeListener != null) {
			mOnSeekArcChangeListener.onStopTrackingTouch(this);
		}
	}

	private void updateOnTouch(MotionEvent event) {
		boolean ignoreTouch = ignoreTouch(event.getX(), event.getY());
		if (ignoreTouch) {
			return;
		}
		setPressed(true);
		mTouchAngle = getTouchDegrees(event.getX(), event.getY());
		int progress = getProgressForAngle(mTouchAngle);
		onProgressRefresh(progress, true);
	}

	private boolean ignoreTouch(float xPos, float yPos) {
		boolean ignore = false;
		float x = xPos - mTranslateX;
		float y = yPos - mTranslateY;

		float touchRadius = (float) Math.sqrt(((x * x) + (y * y)));
		if (touchRadius < mTouchIgnoreRadius) {
			ignore = true;
		}
		return ignore;
	}

	private double getTouchDegrees(float xPos, float yPos) {
		float x = xPos - mTranslateX;
		float y = yPos - mTranslateY;
		//invert the x-coord if we are rotating anti-clockwise
		x = (mClockwise) ? x : -x;
		// convert to arc Angle
		double angle = Math.toDegrees(Math.atan2(y, x)
				+ (Math.PI / 2)
				- Math.toRadians(mRotation));
		if (angle < 0) {
			angle = 360 + angle;
		}
		angle -= mStartAngle;
//		System.out.printf("(%f, %f) %f\n", x, y, angle);
		return angle;
	}

	private int getProgressForAngle(double angle) {
		int touchProgress = (int) Math.round(valuePerDegree() * angle);

		touchProgress = (touchProgress < mMin) ? INVALID_PROGRESS_VALUE
				: touchProgress;
		touchProgress = (touchProgress > mMax) ? INVALID_PROGRESS_VALUE
				: touchProgress;
		return touchProgress;
	}

	private float valuePerDegree() {
		return (float) (mMax - mMin) / mSweepAngle;
	}

	private void onProgressRefresh(int progress, boolean fromUser) {
		updateProgress(progress, fromUser);
	}

	private void updateThumbPosition() {
		int thumbAngle = (int) (mStartAngle + mProgressSweep + mRotation + 90);
		mThumbXPos = (int) (mArcRadius * Math.cos(Math.toRadians(thumbAngle)));
		mThumbYPos = (int) (mArcRadius * Math.sin(Math.toRadians(thumbAngle)));
	}

	private void updateProgress(int progress, boolean fromUser) {

		// 要偵測的區域
		final int maxDetectValue = (int) ((double) mMax * 0.95);
		final int minDetectValue = (int) ((double) mMax * 0.05) + mMin;
//		System.out.printf("(%d, %d) / (%d, %d)\n", mMax, mMin, maxDetectValue, minDetectValue);

		mUpdateTimes++;
		if (progress == INVALID_PROGRESS_VALUE) {
			return;
		}

		// 預防在靠近原點點到直接變成最大值
		if (progress > maxDetectValue && mPreviousProgress == INVALID_PROGRESS_VALUE) {
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

		// 紀錄目前和前一個進度變化
		if (mUpdateTimes == 1) {
			mCurrentProgress = progress;
		} else {
			mPreviousProgress = mCurrentProgress;
			mCurrentProgress = progress;
		}

//		System.out.printf("New value (%.0f, %.0f)\n", mPreviousProgress, mCurrentProgress);

		// 判斷超過最大值或最小值，最大最小值不重複判斷
		// 用數值範圍判斷預防轉太快直接略過最大最小值。
		// progress變化可能從98 -> 0/1 or 0/1 -> 98/97，而不會過0或100
		if (mUpdateTimes > 1 && !isMin && !isMax) {
			if (mPreviousProgress >= maxDetectValue && mCurrentProgress <= minDetectValue &&
					mPreviousProgress > mCurrentProgress) {
				isMax = true;
				progress = mMax;
//				Logger.d("Reach Max " + progress);
				if (mOnSeekArcChangeListener != null) {
					mOnSeekArcChangeListener
							.onProgressChanged(this, progress, fromUser);
					return;
				}
			} else if (mCurrentProgress >= maxDetectValue && mPreviousProgress <= minDetectValue &&
					mCurrentProgress > mPreviousProgress) {
				isMin = true;
				progress = mMin;
//				Logger.d("Reach Min " + progress);
				if (mOnSeekArcChangeListener != null) {
					mOnSeekArcChangeListener
							.onProgressChanged(this, progress, fromUser);
					return;
				}
			}
		}

		// 到達最大值後，從最大值往回轉，就可以解除鎖定
		if (isMax & (mCurrentProgress < mPreviousProgress) && mCurrentProgress >= maxDetectValue) {
//			Logger.d("Unlock max");
			isMax = false;
		}

		// 到達最小值後，從最小值往前轉，就可以解除鎖定
		if (isMin && (mPreviousProgress < mCurrentProgress) && mPreviousProgress <= minDetectValue) {
//			Logger.d("Unlock min");
			isMin = false;
		}

		if (!isMax && !isMin) {
			progress = (progress > mMax) ? mMax : progress;
			progress = (progress < mMin) ? mMin : progress;
			mProgress = progress;

			if (mOnSeekArcChangeListener != null) {
				progress = progress - (progress % mStep);

				mOnSeekArcChangeListener
						.onProgressChanged(this, progress, fromUser);
			}

			mProgressSweep = (float) progress / mMax * mSweepAngle;
//			System.out.printf("%d, %f\n", progress, mProgressSweep);
			updateThumbPosition();
			invalidate();
		}
	}

	/**
	 * Sets a listener to receive notifications of changes to the SeekArc's
	 * progress level. Also provides notifications of when the user starts and
	 * stops a touch gesture within the SeekArc.
	 *
	 * @param l The seek bar notification listener
	 */
	public void setOnSeekArcChangeListener(OnSeekArcChangeListener l) {
		mOnSeekArcChangeListener = l;
	}

	public void setProgress(int progress) {
		updateProgress(progress, false);
	}

	public int getProgress() {
		return mProgress;
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

	public int getArcRotation() {
		return mRotation;
	}

	public void setArcRotation(int mRotation) {
		this.mRotation = mRotation;
		updateThumbPosition();
	}

	public int getStartAngle() {
		return mStartAngle;
	}

	public void setStartAngle(int mStartAngle) {
		this.mStartAngle = mStartAngle;
		updateThumbPosition();
	}

	public int getSweepAngle() {
		return mSweepAngle;
	}

	public void setSweepAngle(int mSweepAngle) {
		this.mSweepAngle = mSweepAngle;
		updateThumbPosition();
	}

	public void setRoundedEdges(boolean isEnabled) {
		mRoundedEdges = isEnabled;
		if (mRoundedEdges) {
			mArcPaint.setStrokeCap(Paint.Cap.ROUND);
			mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
		} else {
			mArcPaint.setStrokeCap(Paint.Cap.SQUARE);
			mProgressPaint.setStrokeCap(Paint.Cap.SQUARE);
		}
	}

	public void setTouchInSide(boolean isEnabled) {
		int thumbHalfheight = (int) mThumb.getIntrinsicHeight() / 2;
		int thumbHalfWidth = (int) mThumb.getIntrinsicWidth() / 2;
		mTouchInside = isEnabled;
		if (mTouchInside) {
			mTouchIgnoreRadius = (float) mArcRadius / 4;
		} else {
			// Don't use the exact radius makes interaction too tricky
			mTouchIgnoreRadius = mArcRadius
					- Math.min(thumbHalfWidth, thumbHalfheight);
		}
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
}
