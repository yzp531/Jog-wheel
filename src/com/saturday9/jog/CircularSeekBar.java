/**
 * @author Raghav Sood
 * @version 1
 * @date 26 January, 2013
 */
package com.saturday9.jog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Paint.Align;
import android.graphics.Shader.TileMode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * The Class CircularSeekBar.
 */
public class CircularSeekBar extends View {

	/** The context */
	private Context mContext;

	/** The listener to listen for changes */
	private OnSeekChangeListener mListener;

	/** The color of the progress ring */
	private Paint circleColor;

	/** the color of the inside circle. Acts as background color */
	private Paint innerColor;

	/** The progress circle ring background */
	private Paint circleRing;

	/** The text paint */
	private Paint progressText;

	/** The text percentegy paint */
	private Paint progressTextPercent;

	/** The angle of progress */
	private float angle = 0;

	/** The start angle (12 O'clock */
	private int startAngle = 270;

	/** The width of the progress ring */
	private int barWidth = 5;

	/** The width of the view */
	private int width;

	/** The height of the view */
	private int height;

	/** The maximum progress amount */
	private int maxProgress = 100;

	/** The current progress */
	private int progress;

	/** The progress percent */
	private int progressPercent;

	/** The radius of the inner circle */
	private float innerRadius;

	/** The radius of the outer circle */
	private float outerRadius;

	/** The circle's center X coordinate */
	private float cx;

	/** The circle's center Y coordinate */
	private float cy;

	/** The progress guide point */
	private float gx, gy;
	
	/** The perenct text point */
	private float ptx, pty;

	/** The left bound for the circle RectF */
	private float left;

	/** The right bound for the circle RectF */
	private float right;

	/** The top bound for the circle RectF */
	private float top;

	/** The bottom bound for the circle RectF */
	private float bottom;

	/** The X coordinate for the top left corner of the marking drawable */
	private float dx;

	/** The Y coordinate for the top left corner of the marking drawable */
	private float dy;

	/** The X coordinate for 12 O'Clock */
	private float startPointX;

	/** The Y coordinate for 12 O'Clock */
	private float startPointY;

	/**
	 * The X coordinate for the current position of the marker, pre adjustment
	 * to center
	 */
	private float markPointX;

	/**
	 * The Y coordinate for the current position of the marker, pre adjustment
	 * to center
	 */
	private float markPointY;

	/**
	 * The adjustment factor. This adds an adjustment of the specified size to
	 * both sides of the progress bar, allowing touch events to be processed
	 * more user friendlily (yes, I know that's not a word)
	 */
	private float adjustmentFactor = 3;

	/** The progress mark when the view isn't being progress modified */
	private Bitmap progressMark;

	/** The progress mark when the view is being progress modified. */
	private Bitmap progressMarkPressed;

	/** The flag to see if view is pressed */
	private boolean IS_PRESSED = false;

	/**
	 * The flag to see if the setProgress() method was called from our own
	 * View's setAngle() method, or externally by a user.
	 */
	private boolean CALLED_FROM_ANGLE = false;

	/** The rectangle containing our circles and arcs. */
	private RectF rect = new RectF();

	{
		mListener = new OnSeekChangeListener() {

			@Override
			public void onProgressChange(CircularSeekBar view, int newProgress) {

			}
		};
		
		circleColor = new Paint();
		innerColor = new Paint();
		circleRing = new Paint();
		progressText = new Paint();
		progressTextPercent = new Paint();

		circleColor.setColor(Color.parseColor("#ff33b5e5")); // Set default
		// progress
		// color to holo
		// blue.
		innerColor.setColor(0xfff1f1f1); // Set default background color to
		// black
		circleRing.setColor(0xfff7f7f7);// Set default background color to Gray
//		LinearGradient gradation = new LinearGradient(0, 0, 500, 500, 0x80a9bb, 0x50b5e3, Shader.TileMode.REPEAT);
//		RadialGradient gradation = new RadialGradient(0, 0, 100, Color.WHITE, Color.BLACK, TileMode.MIRROR);
//		circleRing.setShader(gradation);
		circleColor.setAntiAlias(true);
		innerColor.setAntiAlias(true);
		circleRing.setAntiAlias(true);

		circleColor.setStrokeWidth(50);
		circleRing.setStrokeWidth(5);

		circleColor.setStyle(Paint.Style.FILL);

		// inner circle
		progressText.setAntiAlias(true);
		progressText.setColor(Color.BLACK);
		progressText.setTextSize(40);
		progressText.setTextAlign(Align.CENTER);

		// text percent
		progressTextPercent.setAntiAlias(true);
		progressTextPercent.setColor(Color.BLACK);
		progressTextPercent.setTextSize(20);
	}

	/**
	 * Instantiates a new circular seek bar.
	 * 
	 * @param context
	 *            the context
	 * @param attrs
	 *            the attrs
	 * @param defStyle
	 *            the def style
	 */
	public CircularSeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initDrawable();
	}

	/**
	 * Instantiates a new circular seek bar.
	 * 
	 * @param context
	 *            the context
	 * @param attrs
	 *            the attrs
	 */
	public CircularSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initDrawable();
	}

	/**
	 * Instantiates a new circular seek bar.
	 * 
	 * @param context
	 *            the context
	 */
	public CircularSeekBar(Context context) {
		super(context);
		mContext = context;
		initDrawable();
	}

	/**
	 * Inits the drawable.
	 */
	public void initDrawable() {
		progressMark = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.triangle);
		progressMarkPressed = BitmapFactory.decodeResource(mContext.getResources(),
				R.drawable.scrubber_control_pressed_holo);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		width = getWidth(); // Get View Width
		height = getHeight();// Get View Height

		int size = (width > height) ? height : width; 
		// Choose the smaller between width and height to make a square

		float smallersize = 30;
		cx = width / 2; // Center X for circle
		cy = height / 2; // Center Y for circle
		outerRadius = size / 2 - smallersize; // Radius of the outer circle

		innerRadius = outerRadius - barWidth - 30; // Radius of the inner circle

		left = cx - outerRadius + 8; // Calculate left bound of our rect
		right = cx + outerRadius - 8;// Calculate right bound of our rect
		top = cy - outerRadius + 8;// Calculate top bound of our rect
		bottom = cy + outerRadius - 8;// Calculate bottom bound of our rect

		startPointX = cx; // 12 O'clock X coordinate
		startPointY = cy - outerRadius;// 12 O'clock Y coordinate
		markPointX = startPointX;// Initial locatino of the marker X coordinate
		markPointY = startPointY;// Initial locatino of the marker Y coordinate

		rect.set(left, top, right, bottom); // assign size to rect
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		dx = getXFromAngle();
		dy = getYFromAngle();

		canvas.drawCircle(cx, cy, outerRadius, circleRing);
		canvas.drawArc(rect, startAngle, angle, true, circleColor);
		canvas.drawCircle(cx, cy, innerRadius, innerColor);
		drawMarkerAtProgress(canvas);

//		canvas.drawText(String.format("%dp", getProgress()), cx, cy, progressText);
//		canvas.drawText(String.format("angle: %d", angle), cx, cy, progressText);
//		canvas.drawText(String.format("marker: %d, %d", (int)gx, (int)gy), cx, cy + 50, progressText);
//		canvas.drawText(String.format("center: %d, %d", (int)cx, (int)cy), 300, 100, progressText);
//		canvas.drawText(String.format("outerradius: %f", outerRadius), 300, 150, progressText);
		
		getPercentPosition();
		canvas.drawText(String.format("%dp", progress), cx, cy + (progressText.getTextSize() / 2), progressText);
		canvas.drawText(String.format("%d %%", getProgressPercent()), ptx, pty, progressTextPercent);

		super.onDraw(canvas);
	}

	/**
	 * Draw marker at the current progress point onto the given canvas.
	 * 
	 * @param canvas
	 *            the canvas
	 */
	public void drawMarkerAtProgress(Canvas canvas) {
		getGuidePosition();
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		matrix.postTranslate(gx, gy);
		canvas.drawBitmap(progressMark, matrix, null);
	}

	/**
	 * Gets the X coordinate of the arc's end arm's point of intersection with
	 * the circle
	 * 
	 * @return the X coordinate
	 */
	public float getXFromAngle() {
		int size1 = progressMark.getWidth();
		int size2 = progressMarkPressed.getWidth();
		int adjust = (size1 > size2) ? size1 : size2;
		float x = markPointX - (adjust / 2);
		return x;
	}

	/**
	 * Gets the Y coordinate of the arc's end arm's point of intersection with
	 * the circle
	 * 
	 * @return the Y coordinate
	 */
	public float getYFromAngle() {
		int size1 = progressMark.getHeight();
		int size2 = progressMarkPressed.getHeight();
		int adjust = (size1 > size2) ? size1 : size2;
		float y = markPointY - (adjust / 2);
		return y;
	}

	/**
	 * Get the angle.
	 * 
	 * @return the angle
	 */
	public float getAngle() {
		return angle;
	}

	/**
	 * Set the angle.
	 * 
	 * @param angle
	 *            the new angle
	 */
	public void setAngle(float angle) {
		this.angle = angle;
		float donePercent = (((float) this.angle) / 360) * 100;
		float progress = (donePercent / 100) * getMaxProgress();
		setProgressPercent(Math.round(donePercent));
		CALLED_FROM_ANGLE = true;
		setProgress(Math.round(progress));
	}

	private void getGuidePosition() {
		float pointRadius = outerRadius + 30;
		float triangle_size = 40;
		double triangle_angle = Math.atan((triangle_size*0.5)/pointRadius) * 180 / Math.PI;
		//angle = triangle_angle;
		gx = cx + (float)((pointRadius * Math.sin((angle - triangle_angle)*(Math.PI)/180))) ;
		gy = cy - (float)((pointRadius * Math.cos((angle - triangle_angle)*(Math.PI)/180))) ;
	}
	
	private void getPercentPosition() {
		float pointRadius = outerRadius - 20;
		float triangle_size = 40;
		double triangle_angle = Math.atan((triangle_size*0.5)/pointRadius) * 180 / Math.PI;
		//angle = triangle_angle;
		ptx = cx + (float)((pointRadius * Math.sin((angle)*(Math.PI)/180))) ;
		pty = cy - (float)((pointRadius * Math.cos((angle)*(Math.PI)/180))) ;
	}

	/**
	 * Sets the seek bar change listener.
	 * 
	 * @param listener
	 *            the new seek bar change listener
	 */
	public void setSeekBarChangeListener(OnSeekChangeListener listener) {
		mListener = listener;
	}

	/**
	 * Gets the seek bar change listener.
	 * 
	 * @return the seek bar change listener
	 */
	public OnSeekChangeListener getSeekBarChangeListener() {
		return mListener;
	}

	/**
	 * Gets the bar width.
	 * 
	 * @return the bar width
	 */
	public int getBarWidth() {
		return barWidth;
	}

	/**
	 * Sets the bar width.
	 * 
	 * @param barWidth
	 *            the new bar width
	 */
	public void setBarWidth(int barWidth) {
		this.barWidth = barWidth;
	}

	/**
	 * The listener interface for receiving onSeekChange events. The class that
	 * is interested in processing a onSeekChange event implements this
	 * interface, and the object created with that class is registered with a
	 * component using the component's
	 * <code>setSeekBarChangeListener(OnSeekChangeListener)<code> method. When
	 * the onSeekChange event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see OnSeekChangeEvent
	 */
	public interface OnSeekChangeListener {

		/**
		 * On progress change.
		 * 
		 * @param view
		 *            the view
		 * @param newProgress
		 *            the new progress
		 */
		public void onProgressChange(CircularSeekBar view, int newProgress);
	}

	/**
	 * Gets the max progress.
	 * 
	 * @return the max progress
	 */
	public int getMaxProgress() {
		return maxProgress;
	}

	/**
	 * Sets the max progress.
	 * 
	 * @param maxProgress
	 *            the new max progress
	 */
	public void setMaxProgress(int maxProgress) {
		this.maxProgress = maxProgress;
	}

	/**
	 * Gets the progress.
	 * 
	 * @return the progress
	 */
	public int getProgress() {
		return progress;
	}

	/**
	 * Sets the progress.
	 * 
	 * @param progress
	 *            the new progress
	 */
	public void setProgress(int progress) {
		if (this.progress != progress) {
			this.progress = progress;
			if (!CALLED_FROM_ANGLE) {
				int newPercent = (this.progress / this.maxProgress) * 100;
				int newAngle = (newPercent / 100) * 360;
				this.setAngle(newAngle);
				this.setProgressPercent(newPercent);
			}
			mListener.onProgressChange(this, this.getProgress());
			CALLED_FROM_ANGLE = false;
		}
	}
	
	public void setProgress(float progress) {
		progress = progress > maxProgress ? 0 : progress;
		float percent = progress / (float)maxProgress;
		float angle = 360.0f * percent;
		setAngle(angle);
		invalidate();
	}

	/**
	 * Gets the progress percent.
	 * 
	 * @return the progress percent
	 */
	public int getProgressPercent() {
		return progressPercent;
	}

	/**
	 * Sets the progress percent.
	 * 
	 * @param progressPercent
	 *            the new progress percent
	 */
	public void setProgressPercent(int progressPercent) {
		this.progressPercent = progressPercent;
	}

	/**
	 * Sets the ring background color.
	 * 
	 * @param color
	 *            the new ring background color
	 */
	public void setRingBackgroundColor(int color) {
		circleRing.setColor(color);
	}

	/**
	 * Sets the back ground color.
	 * 
	 * @param color
	 *            the new back ground color
	 */
	public void setBackGroundColor(int color) {
		innerColor.setColor(color);
	}

	/**
	 * Sets the progress color.
	 * 
	 * @param color
	 *            the new progress color
	 */
	public void setProgressColor(int color) {
		circleColor.setColor(color);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		boolean up = false;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			moved(x, y, up);
			break;
		case MotionEvent.ACTION_MOVE:
			moved(x, y, up);
			break;
		case MotionEvent.ACTION_UP:
			up = true;
			moved(x, y, up);
			break;
		}
		return true;
	}

	/**
	 * Moved.
	 * 
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param up
	 *            the up
	 */
	private void moved(float x, float y, boolean up) {
		//		float distance = (float) Math.sqrt(Math.pow((x - cx), 2) + Math.pow((y - cy), 2));
		IS_PRESSED = true;

		markPointX = x;
		markPointY = y;

		float degrees = (float) ((float) ((Math.toDegrees(Math.atan2(x - cx, cy - y)) + 360.0)) % 360.0);
		// and to make it count 0-360
		if (degrees < 0) {
			degrees += 2 * Math.PI;
		}

		setAngle(Math.round(degrees));
		invalidate();
	}

	/**
	 * Gets the adjustment factor.
	 * 
	 * @return the adjustment factor
	 */
	public float getAdjustmentFactor() {
		return adjustmentFactor;
	}

	/**
	 * Sets the adjustment factor.
	 * 
	 * @param adjustmentFactor
	 *            the new adjustment factor
	 */
	public void setAdjustmentFactor(float adjustmentFactor) {
		this.adjustmentFactor = adjustmentFactor;
	}
}
