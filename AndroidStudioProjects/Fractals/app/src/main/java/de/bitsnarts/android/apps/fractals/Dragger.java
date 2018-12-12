package de.bitsnarts.android.apps.fractals;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;

import de.bitsnarts.shared.math.transforms.ConformalAffineTransform2D;

public class Dragger {

    enum DragMode {
        IDLE, POINT, LINE ;
    }

    private ConformalAffineTransform2D tr;
    private DragMode dragMode = DragMode.IDLE ;
    private float p1x_start, p1y_start, p2x_start, p2y_start ;
    private float p1x, p1y, p2x, p2y ;
    private static float pointRadius = 100 ;

    public void drawState(Canvas canvas) {
        if ( dragMode == DragMode.IDLE ) {
            Paint idlePaint = new Paint () ;
            idlePaint.setColor( 0xff007fff );
            drawPoint ( canvas, idlePaint, canvas.getWidth()/2, canvas.getHeight()/2 ) ;
            return;
        }
        Paint startPaint = new Paint () ;
        startPaint.setColor ( 0xff00ff00 ) ;
        Paint endPaint = new Paint () ;
        endPaint.setColor ( 0xffff0000 ) ;
        if ( dragMode == DragMode.LINE ) {
            drawLine ( canvas, startPaint, p1x_start, p1y_start, p2x_start, p2y_start ) ;
            drawLine ( canvas, endPaint, p1x, p1y, p2x, p2y ) ;
        } else {
            drawPoint ( canvas, startPaint, p1x_start, p1y_start ) ;
            drawPoint ( canvas, endPaint, p1x, p1y ) ;
        }
    }

    private void drawPoint(Canvas canvas, Paint paint, double px1, double py1  ) {
        canvas.drawLine( (float)px1-pointRadius, (float)py1, (float)px1+pointRadius, (float)py1, paint );
        canvas.drawLine( (float)px1, (float)py1-pointRadius, (float)px1, (float)py1+pointRadius, paint );
    }

    private void drawLine(Canvas canvas, Paint paint, double px1, double py1, double px2, double py2  ) {
        canvas.drawLine( (float)px1, (float)py1, (float)px2, (float)py2, paint );
        double dx = (py1-py2)/2 ;
        double dy =-(px1-px2)/2 ;
        canvas.drawLine( (float)(px1+dx), (float)(py1+dy), (float)(px1-dx), (float)(py1-dy), paint );
    }


    DragMode mode = DragMode.IDLE ;

    public Dragger (ConformalAffineTransform2D tr ) {
        this.tr = tr ;
    }

    public ConformalAffineTransform2D getTransform() {
        return tr ;
    }

    public boolean onTouchEvent(MotionEvent event) {
        int pc = event.getPointerCount() ;
        if ( pc < 1 || pc > 2 )
            return false ;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                if ( dragMode == DragMode.IDLE ) {
                    if ( pc == 1 ) {
                        dragMode = DragMode.POINT;
                        p1x_start = event.getX() ;
                        p1y_start = event.getY() ;
                    } else {
                        dragMode = DragMode.LINE ;
                        p1x_start = event.getX(0 ) ;
                        p1y_start = event.getY(0 ) ;
                        p2x_start = event.getX(1 ) ;
                        p2y_start = event.getY(1 ) ;
                    }
                } else { //
                    if ( pc == 1 ) {
                        if ( dragMode != DragMode.POINT ) {
                            applyTransform();
                            dragMode = DragMode.POINT ;
                            p1x_start = event.getX() ;
                            p1y_start = event.getY() ;
                        } else {
                            p1x = event.getX() ;
                            p1y = event.getY() ;
                        }
                    } else {
                        if ( dragMode != DragMode.LINE ) {
                            applyTransform();
                            dragMode = DragMode.LINE ;
                            p1x_start = event.getX(0 ) ;
                            p1y_start = event.getY(0 ) ;
                            p2x_start = event.getX(1 ) ;
                            p2y_start = event.getY(1 ) ;
                        } else {
                            p1x = event.getX(0 ) ;
                            p1y = event.getY(0 ) ;
                            p2x = event.getX(1 ) ;
                            p2y = event.getY(1 ) ;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                applyTransform () ;
                dragMode = DragMode.IDLE ;
                break;
        }
        return true;
    }

    private void applyTransform() {
        ConformalAffineTransform2D ptr = getPixelTransform();
        tr = tr.times( ptr.inverse() ) ;
    }

    private ConformalAffineTransform2D getPixelTransform() {
        if ( dragMode == DragMode.POINT ) {
            return new ConformalAffineTransform2D ( 1, 0, p1x-p1x_start, p1y-p1y_start ) ;
        } else {
            return ConformalAffineTransform2D.fromLinePair ( p1x_start, p1y_start, p2x_start,p2y_start, p1x, p1y, p2x, p2y ) ;
        }
    }
}
