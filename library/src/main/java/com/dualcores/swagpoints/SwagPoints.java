package com.dualcores.swagpoints;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
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
	private int mProgressWidth = 8;
	private int mProgressColor;

	private int mArcColor;
	private int mArcWidth = 8;

	private boolean mClosewise;
	private boolean mEnabled;

	//
	// deprecated
	//
	private boolean mRoundEdges = false;
	private int mSweepAngle = 360;
	private int mRotation = 0;
	private int mStartAngle = 0;
	private boolean mTouchIndide = true;

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


	public SwagPoints(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private void initView() {

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
