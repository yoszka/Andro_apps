package tomasz.jokiel.tankcontroller;

import android.graphics.PointF;

/**
 * Listener for changes points position by defined distance 
 */
public abstract class OnPositionChangedByDistanceListener {
    private static final int POINTERS_COUNT = 2;
    private final int mMinDistance;
    private final PointF[] mPoints;

    /**
     * @param pointersCount define how much pointers will be analyzed
     * @param minDistance minimum distance change which will trigger {@link #onPositionChanged()}
     */
    public OnPositionChangedByDistanceListener(int minDistance) {
        mMinDistance = minDistance;
        mPoints = new PointF[POINTERS_COUNT];

        for(int i = 0; i < mPoints.length; i++) {
            mPoints[i] = new PointF();
        }
    }

    /**
     * Update points position to analyze if changed by defined minimum distance 
     * @param newPoints array of new points positions to analyze
     */
    public void updatePosition(final PointF[] newPoints) {
        boolean isAnyPositionChangedByDistance = false;

        if (newPoints.length != mPoints.length) {
            throw new IllegalArgumentException(
                    "Imput array length differ that expected: " + mPoints.length);
        }

        for(int i = 0; i < mPoints.length; i++) {
//            float distance = (float) Math.sqrt((mPoints[i].x - newPoints[i].x) * (mPoints[i].x - newPoints[i].x) 
//                    + (mPoints[i].y - newPoints[i].y) * (mPoints[i].y - newPoints[i].y));
            float distance = Math.abs(mPoints[i].y - newPoints[i].y);

            if(distance >= mMinDistance) {
//                mPoints[i].x = newPoints[i].x;
                mPoints[i].y = newPoints[i].y;
                isAnyPositionChangedByDistance = true;
            }
        }

        if(isAnyPositionChangedByDistance) {
            onPositionChanged();
        }
    }

    /**
     * Callback triggered when {@link #updatePosition(PointF[])} detect,
     * that any of point changed by defined minimum distance
     */
    public abstract void onPositionChanged();
}
