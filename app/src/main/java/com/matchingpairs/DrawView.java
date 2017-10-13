package com.matchingpairs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by next on 13/10/17.
 */
public class DrawView extends View
{
    Paint paint = new Paint();
    List<LineCoordinate> mLineCoordinates;

    public DrawView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mLineCoordinates = new ArrayList<>();
        paint.setColor(context.getResources().getColor(R.color.colorGreen));
        paint.setStrokeWidth(5);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        for(LineCoordinate lineCoordinate : mLineCoordinates)
        {
            canvas.drawLine(lineCoordinate.startX, lineCoordinate.startY, lineCoordinate.endX,
                    lineCoordinate.endY, paint);
        }
    }

    public void drawLine(LineCoordinate lineCoordinate)
    {
        mLineCoordinates.add(lineCoordinate);
        invalidate();
    }
}
