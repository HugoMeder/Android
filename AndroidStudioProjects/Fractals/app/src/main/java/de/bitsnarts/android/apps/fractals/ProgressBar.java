package de.bitsnarts.android.apps.fractals;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

public class ProgressBar  extends View {
    public ProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing() {
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRGB(255, 0, 0);
    }
}