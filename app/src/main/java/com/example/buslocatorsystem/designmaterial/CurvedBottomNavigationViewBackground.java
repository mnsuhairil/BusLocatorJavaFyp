package com.example.buslocatorsystem.designmaterial;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

public class CurvedBottomNavigationViewBackground extends GradientDrawable {

    private int backgroundColor;
    private int curveHeight;

    public CurvedBottomNavigationViewBackground(int backgroundColor, int curveHeight) {
        super(Orientation.BOTTOM_TOP, new int[]{backgroundColor, Color.TRANSPARENT});
        this.backgroundColor = backgroundColor;
        this.curveHeight = curveHeight;
    }

    @Override
    public void draw(Canvas canvas) {
        Path path = createCurvedPath(canvas.getWidth(), canvas.getHeight(), curveHeight);
        canvas.clipPath(path);
        super.draw(canvas);
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        invalidateSelf();
    }

    private Path createCurvedPath(int width, int height, int curveHeight) {
        Path path = new Path();
        path.moveTo(0, 0);
        path.lineTo(0, height - curveHeight);
        path.quadTo(width / 2, height, width, height - curveHeight);
        path.lineTo(width, 0);
        path.close();
        return path;
    }
}
