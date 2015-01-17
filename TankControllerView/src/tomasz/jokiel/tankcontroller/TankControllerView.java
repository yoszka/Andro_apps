package tomasz.jokiel.tankcontroller;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class TankControllerView extends View implements Callback{
    
    private float mViewWidth;
    private float mViewHeigth;
    private Paint mCirclePaint;
    private Paint mCrossPaint;
    private Paint mTextPaint;
    private PointFext[] mPoints = new PointFext[]{new PointFext(), new PointFext()};
    private float mRadius;
    private int mLeftPointId = PointFext.PointSide.LEFT.asInt();
    private int mRightPointId = PointFext.PointSide.RIGHT.asInt();
    private int mMarginBetween = 50;
    private OnPositionChangedByDistanceListener mOnPositionChangedByDistanceListener;

    public TankControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initVars();
    }

    public void setOnPositionChangedByDistanceListener(OnPositionChangedByDistanceListener listener) {
        mOnPositionChangedByDistanceListener = listener;
    }

    private void initVars() {
        mCirclePaint = new Paint();
        mCirclePaint.setColor(Color.BLUE);
        mCirclePaint.setStyle(Paint.Style.FILL);

        mCrossPaint = new Paint();
        mCrossPaint.setColor(Color.GRAY);
        mCrossPaint.setStrokeWidth(10.0f);

        mTextPaint = new Paint();
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(20.0f);

        mRadius = 50.0f;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        boolean initStartpos = (mViewWidth == 0) ? true : false;

        mViewWidth = getWidth();
        mViewHeigth = getHeight();

        if(initStartpos) {
            mPoints[0].x = mViewWidth / 4;
            mPoints[0].y = mViewHeigth / 2;
            mPoints[1].x = (mViewWidth / 4) * 3;
            mPoints[1].y = mViewHeigth / 2;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int size = event.getPointerCount();

        if (size > 2) {
            return false;
        }

        int pointerIndex = event.getActionIndex();
        event.getPointerId(pointerIndex);
        int actionMasked = event.getActionMasked();
        PointFext.PointSide pointerSide = PointFext.PointSide.NONE;
        
        if(isOnLeftSide(event)) {
            pointerSide = PointFext.PointSide.LEFT;
        } else {
            pointerSide = PointFext.PointSide.RIGHT;
        }
        
        switch (actionMasked) {
        case MotionEvent.ACTION_DOWN:
        case MotionEvent.ACTION_POINTER_DOWN:
            if(pointerIndex < 2) {
                PointFext point = new PointFext(pointerSide, pointerIndex);
                point.x = getXPosBounded(pointerSide.asInt(), pointerIndex, event);
                point.y = getYPosBounded(pointerIndex, event);
                mPoints[pointerSide.asInt()] = point;
            }
            break;

        case MotionEvent.ACTION_MOVE:

            if(size == 1) {
                PointFext pointTmp = mPoints[pointerSide.asInt()];
                if (pointTmp != null) {
                    if(isSinglePointerExceedBorder(pointTmp, event)) {
                        bringPointToBegining(pointerSide);
                    } else {
                        pointTmp.x = getXPosBounded(pointerSide.asInt(), 0, event);
                        pointTmp.y = getYPosBounded(0, event);
                    }
                    notifyPositionChangedByDistance();
                }
            } else {
                for (int i = 0; i < size; i++) {
                    int pointerIdIndex = event.getPointerId(i);

                    boolean pointerNotInPointerTables = pointerIdIndex >= mPoints.length;

                    if(pointerNotInPointerTables) {
                        continue;
                    }

                    PointFext pointTmp = mPoints[pointerIdIndex];
                    if (pointTmp != null) {
//                        if(event.getX(i) > (mViewWidth /2 - mMarginBetween / 2) && event.getX(i) < (mViewWidth /2 + mMarginBetween / 2)) {
//                            bringPointToBegining(pointTmp.side);
//                        } else {
                            pointTmp.x = getXPosBounded(pointerIdIndex, pointTmp.index, event);
                            pointTmp.y = getYPosBounded(pointTmp.index, event);
//                        }
                            notifyPositionChangedByDistance();
                    }
                }
            }

            break;

        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_POINTER_UP:
        case MotionEvent.ACTION_CANCEL:
            bringPointToBegining(pointerSide);
            notifyPositionChanged();
            break;

        default:
            break;
        }

        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // cross
        canvas.drawLine(0, mViewHeigth / 2, mViewWidth, mViewHeigth / 2,
                mCrossPaint);
        canvas.drawLine(mViewWidth / 2, 0, mViewWidth / 2, mViewHeigth,
                mCrossPaint);

        // pointers
        for (int i = 0; i < mPoints.length; i++) {
            if (mPoints[i] != null) {
                String posText = (mPoints[i].side == PointFext.PointSide.LEFT) ? "L" : "P";
                canvas.drawCircle(mPoints[i].x, mPoints[i].y, mRadius, mCirclePaint);
                canvas.drawText(String.valueOf(i) + ":"+i+posText, mPoints[i].x - mRadius - 50, mPoints[i].y - mRadius - 50, mTextPaint);
            }
          }
    }
    private boolean isSinglePointerExceedBorder(PointFext pointer, MotionEvent event) {
        if(pointer.side == PointFext.PointSide.LEFT) {
            return event.getX(0) > (mViewWidth /2 - mMarginBetween / 2);
        } else {
            return event.getX(0) < (mViewWidth /2 + mMarginBetween / 2);
        }
    }

    private boolean isOnLeftSide(MotionEvent event) {
        return event.getX(event.getActionIndex()) < mViewWidth /2;
    }


    private float getXPosBounded(int pointerId, int pointerIndex, MotionEvent event) {
        float xPos = event.getX(pointerIndex);
        
        if(pointerId == mLeftPointId) {
            if(xPos < mRadius) {
                xPos = mRadius;
            }
            if(xPos > ((mViewWidth / 2) - mRadius)) {
                xPos = ((mViewWidth / 2) - mRadius);
            }
        } else if(pointerId == mRightPointId) {
            if(xPos > (mViewWidth - mRadius)) {
                xPos = (mViewWidth - mRadius);
            }
            if(xPos < ((mViewWidth / 2) + mRadius)) {
                xPos = ((mViewWidth / 2) + mRadius);
            }
        }
        
        return xPos;
    }

    private float getYPosBounded(int pointerIndex, MotionEvent event) {
        float yPos = event.getY(pointerIndex);

        if(yPos < mRadius) {
            yPos = mRadius;
        }
        if(yPos > (mViewHeigth - mRadius)) {
            yPos = (mViewHeigth - mRadius);
        }

        return yPos;
    }

    private void bringPointToBegining(PointFext.PointSide pointSide) {
        PointFext point = mPoints[pointSide.asInt()];

        if(point != null) {
            if(pointSide == PointFext.PointSide.LEFT) {
                point.x = mViewWidth / 4;
                point.y = mViewHeigth / 2;
            } 
            if(pointSide == PointFext.PointSide.RIGHT) {
                point.x = (mViewWidth / 4) * 3;
                point.y = mViewHeigth / 2;
            }
        }
    }

    @Override
    public boolean handleMessage(Message arg0) {
        if(mOnPositionChangedByDistanceListener != null) {
            mOnPositionChangedByDistanceListener.updatePosition(mPoints);
            return true;
        }
        return false;
    }

    public String getCurrentPositionFormatted() {
        int deltaLeft = getCalculateRelativeCurrentPosition(PointFext.PointSide.LEFT, 255);
        int deltaRight = getCalculateRelativeCurrentPosition(PointFext.PointSide.RIGHT, 255);

        String leftSign = (deltaLeft >= 0) ? "+" : "-";
        String rightSign = (deltaRight >= 0) ? "+" : "-";
        deltaLeft = Math.abs(deltaLeft);
        deltaRight = Math.abs(deltaRight);

        return String.format(leftSign + "%03d&" + rightSign + "%03d", deltaLeft, deltaRight);
    }

    private int getCalculateRelativeCurrentPosition(PointFext.PointSide side, int maxValue) {
        PointFext point = (mPoints[0].side == side) ? mPoints[0] : mPoints[1];
        int dY = (int) ((mViewHeigth / 2) - point.y);
        dY = (int) ((dY / (mViewHeigth / 2 - mRadius)) * maxValue);
        dY = (dY > maxValue) ? maxValue : dY;
        return dY;
    }
    
    private void notifyPositionChangedByDistance() {
        if(mOnPositionChangedByDistanceListener != null) {
            mOnPositionChangedByDistanceListener.updatePosition(mPoints);
        }
    }

    private void notifyPositionChanged() {
        if(mOnPositionChangedByDistanceListener != null) {
            mOnPositionChangedByDistanceListener.onPositionChanged();
        }
    }

    private static class PointFext extends PointF {
        enum PointSide {
            LEFT, RIGHT, NONE;

            int asInt(){
                return ordinal();
            }
        }
        
        PointFext() {
            super();
        }

        PointSide side = PointSide.NONE;
        int index;

        PointFext(PointSide side, int index) {
            this();
            this.side = side;
            this.index = index;
        }
    }

}
