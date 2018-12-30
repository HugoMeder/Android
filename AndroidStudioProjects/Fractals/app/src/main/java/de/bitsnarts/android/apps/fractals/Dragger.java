package de.bitsnarts.android.apps.fractals;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;

import de.bitsnarts.android.tools.logger.Logger;
import de.bitsnarts.shared.math.transforms.ConformalAffineTransform2D;

import static de.bitsnarts.android.apps.fractals.Dragger.DragMode.IDLE;

public class Dragger {

    private DragMode lastApply;

    enum DragMode {
        IDLE, POINT, LINE ;
    }

    private ConformalAffineTransform2D tr0, tr;
    private DragMode dragMode = IDLE ;
    private float p0x_start, p0y_start, p1x_start, p1y_start;
    private float p0x, p0y, p1x, p1y;
    private int id0, id1 ;

    private static float pointRadius = 100 ;

    public void drawState(Canvas canvas) {
        if ( dragMode == IDLE ) {
            /*Paint idlePaint = new Paint () ;
            idlePaint.setColor( 0xff007fff );
            drawPoint ( canvas, idlePaint, canvas.getWidth()/2, canvas.getHeight()/2 ) ;
            */
            return;
        }
        Paint startPaint = new Paint () ;
        startPaint.setColor ( 0xff00ff00 ) ;
        Paint endPaint = new Paint () ;
        endPaint.setColor ( 0xffff0000 ) ;
        if ( dragMode == DragMode.LINE ) {
            drawLine ( canvas, startPaint, p0x_start, p0y_start, p1x_start, p1y_start) ;
            drawLine ( canvas, endPaint, p0x, p0y, p1x, p1y) ;
        } else {
            drawPoint ( canvas, startPaint, p0x_start, p0y_start) ;
            drawPoint ( canvas, endPaint, p0x, p0y) ;
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


    public Dragger (ConformalAffineTransform2D tr ) {
        this.tr0 = tr ;
        this.tr = tr ;
    }

    public ConformalAffineTransform2D getTransform() {
        return tr ;
    }

    public boolean onTouchEvent(MotionEvent event) {
        int pc = event.getPointerCount() ;
        if ( pc < 1 || pc > 2 )
            return false ;
        float[] x = new float[pc] ;
        float[] y = new float[pc] ;
        int[] id = new int[pc] ;
        for ( int i = 0 ; i < pc ; i++ ) {
            x[i] = event.getX(i) ;
            y[i] = event.getY(i) ;
            id[i] = event.getPointerId(i) ;
        }
        onToutchEvent ( event.getEventTime(), event.getAction(), x, y, id ) ;
        return true;
    }

    private void onToutchEvent(long eventTime, int action, float[] x, float[] y, int[] id) {
        logEvent ( eventTime, action, x, y, id ) ;
        int pc = x.length ;
        switch ( action ) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                switch ( dragMode ) {
                    case IDLE: {
                        if (pc == 1) {
                            //Logger.log().println ( "start POINT" ) ;
                            dragMode = DragMode.POINT;
                            p0x_start = x[0];
                            p0y_start = y[0];
                            id0 = id[0];
                        } else {
                            //Logger.log().println ( "start LINE 1" ) ;
                            dragMode = DragMode.LINE;
                            p0x_start = x[0];
                            p0y_start = y[0];
                            p1x_start = x[1];
                            p1y_start = y[1];
                            id0 = id[0];
                            id1 = id[1];
                        }
                        break;
                    }
                    case POINT: {
                        if (pc == 2) {
                            applyTransform0();
                            //Logger.log().println ( "start LINE 2" ) ;
                            dragMode = DragMode.LINE;
                            p0x_start = x[0];
                            p0y_start = y[0];
                            p1x_start = x[1];
                            p1y_start = y[1];
                            id0 = id[0];
                            id1 = id[1];
                        } else {
                            //Logger.log().println ( "POINT" ) ;
                            if (id0 != id[0]) {
                                applyTransform0();
                                p0x_start = x[0];
                                p0y_start = y[0];
                                id0 = id[0];
                                Logger.log().println("Pointer Id mismatch 1");
                            } else {
                                p0x = x[0];
                                p0y = y[0];
                                applyTransform();
                            }
                        }
                        break;
                    }
                    case LINE: {
                        if (pc == 2) {
                            //Logger.log().println ( "LINE" ) ;
                            if (id0 != id[0] || id1 != id[1]) {
                                applyTransform0();
                                //Logger.log().println ( "start LINE 2" ) ;
                                p0x_start = x[0];
                                p0y_start = y[0];
                                p1x_start = x[1];
                                p1y_start = y[1];
                                id0 = id[0];
                                id1 = id[1];
                                Logger.log().println("Pointer Id mismatch 2");
                            } else {
                                p0x = x[0];
                                p0y = y[0];
                                p1x = x[1];
                                p1y = y[1];
                                applyTransform();
                            }
                        }
                        break;
                    }
                }
                break;
                case MotionEvent.ACTION_UP:
                    applyTransform0();
                    dragMode = IDLE;
                    lastApply = IDLE;
                    break;
                }
        }

    private void logEvent(long eventTime, int action, float[] x, float[] y, int[] id) {
        int pc = x.length ;
        Logger.log().print ( ""+eventTime+" "+action+" "+x[0]+" "+y[0]+" "+id[0] ) ;
        if ( pc == 2 ) {
            Logger.log().println ( " "+x[1]+" "+y[1]+" "+id[1] ) ;
        } else {
            Logger.log().println () ;
        }
    }


    private void applyTransform() {
        lastApply = dragMode ;
        //Logger.log().println ( "apply "+dragMode ) ;
        ConformalAffineTransform2D ptr = getPixelTransform();
        if ( ptr != null )
            tr = tr0.times(ptr.inverse());
    }

    private void applyTransform0() {
        applyTransform() ;
        tr0 = tr ;
    }

    private ConformalAffineTransform2D getPixelTransform() {
        if ( dragMode == DragMode.POINT ) {
            return new ConformalAffineTransform2D ( 1, 0, p0x - p0x_start, p0y - p0y_start) ;
        } else if ( dragMode == DragMode.LINE){
            return ConformalAffineTransform2D.fromLinePair (p0x_start, p0y_start, p1x_start, p1y_start, p0x, p0y, p1x, p1y) ;
        } else {
            return null ;
        }
    }
}
