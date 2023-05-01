package de.bistnarts.apps.orientationtest.tools;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.Vector;

public class TextDrawer {
    private final Paint paint;
    private Vector<String> lines;

    public TextDrawer (Paint paint ) {
        this.paint = paint ;
    }

    public void setText ( Vector<String> lines ) {
        this.lines = lines ;
    }

    public void drawOnto (Canvas canvas, int x, int y ) {
        float h = paint.getTextSize();

        for ( String l : lines ) {
            y += h ;
            String l2 = l.replace("\t", "       ");
            canvas.drawText( l2, x, y, paint );
        }

    }

    public float getFullTextHeight() {
        return paint.getTextSize()*lines.size() ;
    }
}
