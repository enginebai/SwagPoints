package com.dualcores.swagpoints;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by enginebai on 2016/8/7.
 */
public class SwagPoints extends View {

	public static final int MAX = 100;
	public static final int MIN = 0;

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
		mProgressWidth = (int)(mProgressWidth * density);

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
			mProgressWidth = (int)array.getDimension(
					R.styleable.SwagPoints_progressWidth, mProgressWidth);
			mProgressColor = array.getColor(R.styleable.SwagPoints_progressColor, mProgressColor);

			mArcWidth = (int)array.getDimension(R.styleable.SwagPoints_arcWidth, mArcWidth);
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
		mProgressPaint.setColor(mProgressColor);

		mSweepAngle = mSweepAngle > 360 ? 360 : mSweepAngle;
		mSweepAngle = mSweepAngle < 0 ? 0 : mSweepAngle;

		mStartAngle = mStartAngle > 360 ? 360 : mStartAngle;
		mStartAngle = mStartAngle < 0 ? 0 : mStartAngle;

		mProgressSweep = (float)mPoints / mMax * mSweepAngle;

		if (mRoundEdges) {
			mArcPaint.setStrokeCap(Paint.Cap.ROUND);
			mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
		}
	}

	public interface OnSwagPointsChangeListener {

		/**
		 * Notification that the point value has changed.
		 *
		 * @param swagPoints    The SwagPoints view whose value has changed
		 * @param point         The current point value.
		 * @param fromUser      True if the point change was triggered by the user.
		 */
		void onPointsChanged(SwagPoints swagPoints, int point, boolean fromUser);

		void onStartTackingTouch(SwagPoints swagPoints);

		void onStopTrackingTouch(SwagPoints swagPoints);
	}
}
