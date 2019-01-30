package d.running.newtoncradle;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;

import android.graphics.RectF;

import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by PirateHat on 2019/1/29.
 */

public class NewtonCradle extends View {


    private static int MAX_BALL_COUNT; // 最大的球的数量
    private static final int MIN_BALL_COUNT = 3;
    private static final int DEFAULT_BALL_COUNT = 6;
    private static final int DEFAULT_BALL_COLOR = 0xff_ff_ff_ff;
    private static final int DEFAULT_CRADLE_COLOR = 0xff_ff_ff_ff;
    private static final int DEFAULT_BASE_COLOR = 0xff_ff_ff_ff;
    private static final int DEFAULT_LINE_COLOR = 0xff_ff_ff_ff;

    private int mBallCount;
    private int mBallColor;
    private int mCradleColor;
    private int mBaseColor;
    private int mLineColor;


    private Paint mBallPaint;
    private Paint mLinePaint;
    private Paint mBasePaint;
    private Paint mCradlePaint;


    private Path mDst;
    private RectF mCradleRect;
    private RectF mOvalRect;
    private Path mCradlePath;
    private Path mOvalPath;
    private Path mLinePath;
    private RectF mBaseRect;

    private PathMeasure mCradleMeasure;
    private PathMeasure mOvalMeasure;


    private float mWidth;
    private float mHeight;

    private float mBaseWidth;
    private float mBaseHeight;

    private float[][] mCircles; //记录禁止的时候的点
    private float[][] mLines;  //记录禁止的时候的点


    private float mLineLength;
    private int mRadius;

    private boolean isCw;
    private double mSweepAngle;
    private double mMaxAngle;


    private int mCurrentBall;


    private static final String TAG = "NewtonCradle";


    public NewtonCradle(Context context) {
        this(context, null);
    }

    public NewtonCradle(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NewtonCradle(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs);
        initPaint();
        initPath();
        initData();
    }

    /**
     * 初始化属性
     *
     * @param context 上下文
     * @param attrs   属性
     */
    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.NewtonCradle);

        //颜色
        mBallColor = typedArray.getColor(R.styleable.NewtonCradle_ball_color, DEFAULT_BALL_COLOR);
        mBaseColor = typedArray.getColor(R.styleable.NewtonCradle_base_color, DEFAULT_BASE_COLOR);
        mCradleColor = typedArray.getColor(R.styleable.NewtonCradle_cradle_color, DEFAULT_CRADLE_COLOR);
        mLineColor = typedArray.getColor(R.styleable.NewtonCradle_line_color, DEFAULT_LINE_COLOR);
        //球的数量
        mBallCount = typedArray.getColor(R.styleable.NewtonCradle_ball_count, DEFAULT_BALL_COUNT);

        typedArray.recycle();

    }

    /**
     * 初始化 画笔
     */

    //数字的单位是 px
    //dp和px的换算公式 ：
    //dp*ppi/160 = px。比如1dp x 320ppi/160 = 2px。
    //在320x480分辨率，像素密度为160,1dp=1px
    //在480x800分辨率，像素密度为240,1dp=1.5px
    private void initPaint() {
        mBallPaint = new Paint();
        mBallPaint.setColor(mBallColor);
        mBallPaint.setStrokeWidth(20);
        mBallPaint.setStyle(Paint.Style.FILL);
        mBallPaint.setAntiAlias(true);

        mLinePaint = new Paint();
        mLinePaint.setColor(mLineColor);
        mLinePaint.setStrokeWidth(2);
        //Path 要设置成这个
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setAntiAlias(true);


        mBasePaint = new Paint();
        mBasePaint.setColor(mBaseColor);
        mBasePaint.setStrokeWidth(2);
        mBasePaint.setStyle(Paint.Style.FILL);
        mBasePaint.setAntiAlias(true);

        mCradlePaint = new Paint();
        mCradlePaint.setColor(mCradleColor);
        mCradlePaint.setStrokeWidth(2);
        mCradlePaint.setStyle(Paint.Style.STROKE);
        mCradlePaint.setAntiAlias(true);


    }

    private void initPath() {
        mDst = new Path();
        mCradleRect = new RectF();
        mOvalRect = new RectF();
        mCradlePath = new Path();
        mOvalPath = new Path();
        mLinePath = new Path();
        mCradleMeasure = new PathMeasure();

        mOvalMeasure = new PathMeasure();

        mCircles = new float[mBallCount][2];
        mLines = new float[mBallCount][2];


        mBaseRect = new RectF();
    }

    private void initData() {
        isCw = true;


        mSweepAngle = 20;
        mMaxAngle = mSweepAngle;
        mCurrentBall = 0;
        mRadius = 20;


    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w * 1.0f;
        mHeight = h * 1.0f;

        mBaseWidth = 1f / 8 * mWidth;
        mBaseHeight = 1f / 14 * mHeight;

        mLineLength = 1f / 4 * mHeight + 1f / 16 * mHeight;

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.e(TAG, "onMeasure: ");
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.e(TAG, "onLayout: ");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.BLACK);

        drawBalls(canvas);

        drawCradle(canvas);

        drawBase(canvas);

        drawLines(canvas);

        swingAngle();
    }


    private void drawBalls(Canvas canvas) {

        //先把画布平移至中心
        //坐标轴原点就在中心
        canvas.translate(mWidth / 2, mHeight / 2);
        //单数个球
        int radius = 20;
        int x = 0;
        int y = 0;

        //偶数个球

        int half = mBallCount / 2;
        if ((mBallCount & 1) == 0) {
            x = -2 * half * radius + radius;
            for (int i = 0; i < mBallCount; i++) {
                mCircles[i][0] = x;
                mCircles[i][1] = y;
                x += radius * 2;
            }

        } else {
            x = -2 * half * radius;
            for (int i = 0; i < mBallCount; i++) {
                mCircles[i][0] = x;
                mCircles[i][1] = y;
                x += radius * 2;
            }
        }


        for (int i = 0; i < mBallCount; i++) {
            //如果是正在移动的球
            if (i == mCurrentBall) {
                //当前移动的既不是左边的，也不是右边的
                if (i != 0 && i != mBallCount - 1) {
                    //需要抖动一下
                    int delta = isCw ? -2 : 2;
                    canvas.drawCircle(mCircles[i][0] + delta, mCircles[i][1], mRadius, mBallPaint);
                    //是左边的，所以
                } else if (i == 0) {
                    float cx = (float) (mCircles[i][0] - Math.sin(Math.toRadians(mSweepAngle)) * mLineLength);
                    float cy = (float) (mCircles[i][1] - (mLineLength - Math.cos(Math.toRadians(mSweepAngle)) * mLineLength));

                    canvas.drawCircle(cx, cy, mRadius, mBallPaint);
                    //是右边的，所以
                } else if (i == mBallCount - 1) {
                    float cx = (float) (mCircles[i][0] + Math.sin(Math.toRadians(mSweepAngle)) * mLineLength);
                    float cy = (float) (mCircles[i][1] - (mLineLength - Math.cos(Math.toRadians(mSweepAngle)) * mLineLength));
                    canvas.drawCircle(cx, cy, mRadius, mBallPaint);
                }
                //如果不是正在移动的球
            } else {
                canvas.drawCircle(mCircles[i][0], mCircles[i][1], mRadius, mBallPaint);
            }
        }


    }

    // TODO: 2019/1/29 和手机显示的方向相反
    private void drawCradle(Canvas canvas) {

        //注意这里要 * 1.0f ,不然会当作整数处理
        //向下平移 1/4
        canvas.translate(0, 1f / 4 * mHeight);

        //矩形左上角的坐标和右下角的坐标
        float left = -7f / 16 * mWidth;
        float right = 7f / 16 * mWidth;
        float top = -7f / 16 * mHeight;
        float bottom = 7f / 16 * mHeight;

        float rx = 1f / 8 * mWidth;
        float ry = 1f / 8 * mHeight;

        //先画出圆角矩形
        mCradleRect.set(left, top, right, bottom);

        mCradlePath.addRoundRect(mCradleRect, rx, ry, Path.Direction.CW);

        //用于测量圆角矩形的长度
        mCradleMeasure.setPath(mCradlePath, false);


        //根据圆角的距离画出椭圆，用于计算圆角部分的长度
        mOvalRect.set(-rx, ry, rx, -ry);

        mOvalPath.addOval(mOvalRect, Path.Direction.CW);

        mOvalMeasure.setPath(mOvalPath, false);

        //得到 一个角的长度
        float length = mOvalMeasure.getLength() / 4;

        //绘制半个圆角矩形
        mCradleMeasure.getSegment(bottom - ry, (right - rx + bottom - ry + length) * 2 + bottom - ry, mDst, true);


        canvas.drawPath(mDst, mCradlePaint);

        mDst.reset();
        mOvalPath.reset();
        mCradlePath.reset();
    }

    private void drawBase(Canvas canvas) {
        canvas.translate(0, -1f / 4 * mHeight);


        float rx = mBaseWidth / 6;
        float ry = mBaseHeight / 6;
        mBaseRect.set(-4 * mBaseWidth, 3 * mBaseHeight, -3 * mBaseWidth, 4 * mBaseHeight);

        canvas.drawRoundRect(mBaseRect, rx, ry, mBasePaint);


        mBaseRect.set(3 * mBaseWidth, 3 * mBaseHeight, 4 * mBaseWidth, 4 * mBaseHeight);

        canvas.drawRoundRect(mBaseRect, rx, ry, mBasePaint);


    }

    private void drawLines(Canvas canvas) {
        //回到原来的中心位置
//        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        for (int i = 0; i < mBallCount; i++) {
            mLines[i][0] = mCircles[i][0];
            mLines[i][1] = mCircles[i][1] - 1f / 4 * mHeight + 1f / 16 * mHeight;

        }

        for (int i = 0; i < mBallCount; i++) {
            //如是移动中的线
            mLinePath.reset();
            if (i == mCurrentBall) {
                if (i != 0 && i != mBallCount - 1) {
                    int delta = isCw ? 2 : -2;

                    mLinePath.moveTo(mCircles[i][0] + delta, mCircles[i][1]);
                    mLinePath.lineTo(mLines[i][0], mLines[i][1]);
                    canvas.drawPath(mLinePath, mLinePaint);
                } else if (i == 0) {

                    float cx = (float) (mCircles[i][0] - Math.sin(Math.toRadians(mSweepAngle)) * mLineLength);
                    float cy = (float) (mCircles[i][1] - (mLineLength - Math.cos(Math.toRadians(mSweepAngle)) * mLineLength));

                    mLinePath.moveTo(cx, cy);
                    mLinePath.lineTo(mLines[i][0], mLines[i][1]);
                    canvas.drawPath(mLinePath, mLinePaint);
                } else if (i == mBallCount - 1) {

                    float cx = (float) (mCircles[i][0] + Math.sin(Math.toRadians(mSweepAngle)) * mLineLength);
                    float cy = (float) (mCircles[i][1] - (mLineLength - Math.cos(Math.toRadians(mSweepAngle)) * mLineLength));
                    mLinePath.moveTo(cx, cy);
                    mLinePath.lineTo(mLines[i][0], mLines[i][1]);
                    canvas.drawPath(mLinePath, mLinePaint);
                }

                //如果不是移动中的线
            } else {
                mLinePath.moveTo(mCircles[i][0], mCircles[i][1]);
                mLinePath.lineTo(mLines[i][0], mLines[i][1]);
                canvas.drawPath(mLinePath, mLinePaint);

            }
        }

    }

    private void swingAngle() {

        if (isCw) {
            if (mCurrentBall == mBallCount - 1 && mSweepAngle != 0) {
                mSweepAngle--;
            } else if (mCurrentBall == mBallCount - 1 && mSweepAngle == 0) {
                mCurrentBall--;
            } else if (mCurrentBall != 0) {
                mCurrentBall--;
            } else if (/*mCurrentBall == 0 && */mSweepAngle != mMaxAngle) {
                mSweepAngle++;
            } else {
                isCw = false;
            }

        } else {
            if (mCurrentBall == 0 && mSweepAngle != 0) {
                mSweepAngle--;
            } else if (mCurrentBall == 0 /*&& mSweepAngle == 0*/) {
                mCurrentBall++;
            } else if (mCurrentBall != mBallCount - 1) {
                mCurrentBall++;
            } else if (mCurrentBall == mBallCount - 1 && mSweepAngle != mMaxAngle) {
                mSweepAngle++;
            } else {
                isCw = true;
            }
        }
//        invalidate();
        postInvalidateDelayed(40);

    }
//越来越慢：path定义为全局变量,然
// 后每次ondraw都没有reset,就导致了path越来越多,
// 最后卡死.解决方法就reset


}
