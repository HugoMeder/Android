package de.bistnarts.apps.orientationtest;

import static android.media.AudioDeviceInfo.TYPE_BUILTIN_SPEAKER;
import static android.media.AudioManager.GET_DEVICES_OUTPUTS;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.AsyncPlayer;
import android.media.AudioAttributes;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiManager;
import android.net.Uri;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

import de.bistnarts.apps.orientationtest.tools.GyrosopicAxisListener;
import de.bistnarts.apps.orientationtest.tools.GyrosopicIntegrator;
import de.bistnarts.apps.orientationtest.tools.LowPassFilter;
import de.bistnarts.apps.orientationtest.tools.MatrixFromVectorPair;
import de.bistnarts.apps.orientationtest.tools.PropertyAccess;
import de.bistnarts.apps.orientationtest.tools.Quaternion;
import de.bistnarts.apps.orientationtest.tools.TextDrawer;
import de.bistnarts.apps.orientationtest.tools.TiltCalibration;
import de.bistnarts.apps.orientationtest.tools.VectorOps;

public class SpiritLevelDisplay extends View implements AttachDetach, ScaleGestureDetector.OnScaleGestureListener, GyrosopicAxisListener {

    private LowPassFilter accFilter;
    private LowPassFilter omegaFilter;
    private boolean doSample;
    private long startSampleTimeMs;
    private float[] omega;
    private double omegaAbsDeg;
    private boolean waitForRest;
    private long scaleTimeMS;
    private boolean scaled;
    private LowPassFilter omegaAbsFilter;
    private float omegaAbsDeg2;
    private double [] zAxis = {0.0, 0.0, 1.0} ;
    private double[] calibAxis = zAxis ;
    private MatrixFromVectorPair mfvp;
    private boolean doShow = true ;
    private Activity activity;
    private boolean scaling;
    static Vector<MenuEntry> entries = new Vector<MenuEntry>() ;
    private MenuEntry menuEntryStartOnRest;
    private GyrosopicIntegrator gyro;
    private double[] rotationAxis;
    private double rotationAxisError;
    private TiltCalibration axisAndAngle;
    private long axisAndAngleSTartTimeMS;
    private double[] tiltAxis;
    private double tiltAxisToGravityAngle;
    private double tiltAxisToGravityAngleError;

    private PropertyAccess propertyAccess ;
    private Paint wheelStroke;
    private Paint wheelText;
    private boolean textOn;

    enum MenuEntry {
        CALIB_AXIS ("Ache kalibrieren" ),
        CALIB_ACCELERATION ( "Lot kalibrieren" ),
        CALIB_AXIS_AND_ANGLE ( "Wasserwaage kalibrieren"),
        TEXT_ON_OFF( "Text Ein/Aus" ) ;

        private final String title;
        private final int id;

        MenuEntry(String title ) {
            this.title = title ;
            entries.add( this ) ;
            id = entries.size() ;
        }

        public int getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        static MenuEntry fromID (int id ) {
            return entries.get( id-1 ) ;
        }
    } ;
    public void setActivity(Activity activity) {
        this.activity = activity ;
        activity.registerForContextMenu ( this ) ;
    }

    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();
        MenuEntry me = MenuEntry.fromID(id);
        switch ( me ) {
            case CALIB_AXIS:
            case CALIB_ACCELERATION:
            case CALIB_AXIS_AND_ANGLE:
                menuEntryStartOnRest = me ;
                createDialog().show(); ;
                break ;
            case TEXT_ON_OFF:
                toggleTextOnOff() ;
        }
        return false ;
    }

    private void toggleTextOnOff() {

        textOn = !textOn ;
        propertyAccess.setProperty( "textOn", ""+textOn );
    }

    private AlertDialog createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        StringBuffer txt = new StringBuffer();
        switch ( menuEntryStartOnRest ) {
            case CALIB_AXIS_AND_ANGLE:
                txt.append( "lege das Gerät aufs Gesicht\n"+
                        "und warte bis ein Ton erklingt\n"+
                        ", dann schaukle um eine horizontale Längsachse\n"+
                        "bis zum nächsten Ton"
                );
                break;
            case CALIB_ACCELERATION:
                txt.append( "lege das Gerät aufs Gesicht\n"+
                        "und warte bis ein Ton erklingt\n"+
                        ", dann warte das Ende der Axenkalibrierung ab,\n"+
                        "dann ertönt ein zweiter Ton"
                ) ;
                break ;
            case CALIB_AXIS:
                txt.append( "lege das Gerät aufs Gesicht\n"+
                        "und warte bis ein Ton erklingt)\n"+
                        ", dann drehe das Gerät um 360\u00B0\n"+
                        ", es ertönt dann ein zweiter Ton"
                ) ;
                break ;
        }
        builder.setMessage(txt.toString())
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        switch ( menuEntryStartOnRest ) {
                            case CALIB_AXIS:
                            case CALIB_ACCELERATION:
                            case CALIB_AXIS_AND_ANGLE:
                                waitForRest = true ;
                        }

                    }
                })
                .setNegativeButton("Abbruch", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    class AccelerationAccumulator {
        Vector<double[]> acc = new Vector<double[]> () ;
        double[] avg = new double[3] ;
        double sigma;
        int n ;
        void add ( float[] values ) {
            double[] normal = getNormal(values);
            acc.add ( normal ) ;
        }

        public void evaluate() {
            n = acc.size();
            for (int i = 0; i < 3; i++) {
                avg[i] = 0 ;
            }
            for (double[] doubles : acc) {
                for (int i = 0; i < 3; i++) {
                    avg[i] += doubles[i];
                }
            }
            for (int i = 0; i < 3; i++) {
                avg[i] /= n;
            }
            avg = getNormal( avg ) ;
            double deltaSqr = 0 ;
            for (double[] doubles : acc) {
                for (int i = 0; i < 3; i++) {
                    double delta = avg[i]-doubles[i] ;
                    deltaSqr += delta*delta ;
                }
            }
            sigma = Math.sqrt( deltaSqr/n ) ;
            acc.clear();
        }
    }
    private Paint red;
    private Paint green;
    private Paint blue;
    private Paint[] rgb;
    private Paint whiteFill;
    private Paint whiteStroke;
    private AudioManager audioManager;

    private int clickCount = 0 ;
    private TextDrawer td;
    private ScaleGestureDetector scaleGesture;
    private float scaleFactor;
    private double scale = 1f ;
    private float[] acceleration;
    private double unit[][] = {{1.0, 0.0, 0.0},
            {0.0, 1.0, 0.0},
            {0.0, 0.0, 1.0}} ;
    private double matrix[][] = unit ;

    private AccelerationAccumulator accelerationAccumulator ;

    public SpiritLevelDisplay(Context context) {
        super(context);
        init () ;
    }

    public SpiritLevelDisplay(Context context, AttributeSet attrs ) {
        super(context, attrs );
        init () ;
    }

    private void init() {

        propertyAccess = new PropertyAccess( getContext() ) ;
        loadData () ;
        int sw = 10 ;
        red = new Paint();
        red.setARGB( 255, 255, 0, 0 );
        red.setStrokeWidth( sw );
        green = new Paint();
        green.setARGB( 255, 0, 255, 0 );
        green.setStrokeWidth( sw );
        blue = new Paint();
        blue.setARGB( 255, 100, 100, 255 );
        blue.setStrokeWidth( sw );
        whiteFill = new Paint();
        whiteFill.setARGB( 255, 255, 255, 255 );
        whiteFill.setStyle(Paint.Style.FILL);
        whiteStroke = new Paint();
        whiteStroke.setARGB( 255, 255, 255, 255 );
        whiteStroke.setStrokeWidth( sw );
        whiteStroke.setTextSize( 50 );
        whiteStroke.setTypeface( Typeface.MONOSPACE ) ;
        whiteStroke.setTextScaleX( 0.7f );
        wheelStroke = new Paint();
        wheelStroke.setARGB( 255, 255, 255, 100 );
        wheelStroke.setStrokeWidth( sw*0.2f );
        wheelStroke.setStyle( Paint.Style.STROKE );
        wheelText = new Paint() ;
        wheelText.setARGB ( 255, 255, 255, 100 ) ;
        wheelText.setTypeface( Typeface.MONOSPACE ) ;
        //wheelText.setTextScaleX( 0.7f );
        wheelText.setTextSize( 60 );
        //whiteStroke.setStyle(Paint.Style.STROKE);

        //whiteStroke.setXfermode( new PorterDuffXfermode( PorterDuff.Mode.)) ;
        rgb = new Paint[3];
        rgb[0] = red ;
        rgb[1] = green ;
        rgb[2] = blue ;
        //setOnLongClickListener( this );

        //List<MidiDeviceInfo> midi = getMidiDevices(true);
        checkAudio () ;

        td = new TextDrawer ( whiteStroke ) ;

        scaleGesture = new ScaleGestureDetector( getContext(), this ) ;
        accFilter = new LowPassFilter( 1 ) ;
        omegaFilter = new LowPassFilter( 1 ) ;
        omegaAbsFilter = new LowPassFilter( 1 ) ;

        mfvp = new MatrixFromVectorPair() ;



    }

    private void loadData() {
        String p = propertyAccess.getProperty( "tiltAxis.X" ) ;
        if ( p == null )
            return ;
        tiltAxis = new double[3] ;
        tiltAxis[0] = Double.parseDouble( p ) ;
        p = propertyAccess.getProperty( "tiltAxis.Y" ) ;
        tiltAxis[1] = Double.parseDouble( p ) ;
        p = propertyAccess.getProperty( "tiltAxis.Z" ) ;
        tiltAxis[2] = Double.parseDouble( p ) ;
        p = propertyAccess.getProperty( "tiltAngle" ) ;
        tiltAxisToGravityAngle = Double.parseDouble( p ) ;
        p = propertyAccess.getProperty( "tiltAngleError" ) ;
        tiltAxisToGravityAngleError = Double.parseDouble( p ) ;
        textOn = "true".equals( propertyAccess.getProperty( "textOn") ) ;
    }
    private void storeData() {
        Properties p = new Properties( ) ;
        p.setProperty( "tiltAxis.X", ""+tiltAxis[0] ) ;
        p.setProperty( "tiltAxis.Y", ""+tiltAxis[1] ) ;
        p.setProperty( "tiltAxis.Z", ""+tiltAxis[2] ) ;
        p.setProperty( "tiltAngle", ""+tiltAxisToGravityAngle ) ;
        p.setProperty( "tiltAngleError", ""+tiltAxisToGravityAngleError ) ;
        propertyAccess.setProperties( p );
    }

    @Override
    protected void onCreateContextMenu(ContextMenu menu) {
        if ( !scaling ) {
            MenuEntry me = MenuEntry.CALIB_AXIS ;
            MenuItem mi = menu.add(Menu.NONE, me.getId(), Menu.NONE, me.getTitle() );
            me = MenuEntry.CALIB_ACCELERATION ;
            mi = menu.add(Menu.NONE, me.getId(), Menu.NONE, me.getTitle() );
            me = MenuEntry.CALIB_AXIS_AND_ANGLE ;
            mi = menu.add(Menu.NONE, me.getId(), Menu.NONE, me.getTitle() );
            me = MenuEntry.TEXT_ON_OFF ;
            mi = menu.add(Menu.NONE, me.getId(), Menu.NONE, me.getTitle() );

            //menu.add("zwei");
            /*SubMenu sub = menu.addSubMenu ( "sub" ) ;
            //menu.add ( sub ) ;
            sub.add( "sub1" ) ;
            sub.add( "sub2" ) ;
            */
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        scaleGesture.onTouchEvent( ev ) ;
        return super.onTouchEvent( ev ) ;
    }
    private void checkAudio() {
        audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        AudioDeviceInfo[] devices = audioManager.getDevices(GET_DEVICES_OUTPUTS);
        AudioDeviceInfo device = null ;
        for ( AudioDeviceInfo d : devices ) {
            if ( d.getType() == TYPE_BUILTIN_SPEAKER ) {
                device = d;
                break ;
            }
        }
        System.out.println(  "audio?" );
    }

    private List<MidiDeviceInfo> getMidiDevices(boolean isOutput){
        Vector<MidiDeviceInfo> filteredMidiDevices = new Vector<MidiDeviceInfo>();
        MidiManager mMidiManager = (MidiManager)getContext().getSystemService(Context.MIDI_SERVICE);

        for (MidiDeviceInfo midiDevice : mMidiManager.getDevices()){
            if (isOutput){
                if (midiDevice.getOutputPortCount() > 0) filteredMidiDevices.add(midiDevice);
            } else {
                if (midiDevice.getInputPortCount() > 0) filteredMidiDevices.add(midiDevice);
            }
        }
        return filteredMidiDevices;
    }


    @Override
    protected void onDraw(Canvas canvas) {

        this.setBackgroundColor( 0xff002040 /*100, 100, 200, 255*/ );
        if ( !doShow )
            return ;
        super.onDraw( canvas );

        Vector<String> txt = new Vector<String>();
        if ( accelerationAccumulator != null ) {
            if ( !doSample ) {
                txt.add( "samples "+accelerationAccumulator.n ) ;
                txt.add( String.format( Locale.ENGLISH, "sigma %5.3f\u00B0", (accelerationAccumulator.sigma*180.0/Math.PI ))) ;
            } else {
                txt.add( "samples "+accelerationAccumulator.acc.size() ) ;
            }
        }
        if ( omega != null && waitForRest ) {
            txt.add( String.format( Locale.ENGLISH, "Winkelgeschwindigkeit %10.5f\u00B0/sec", omegaAbsDeg2 ) ) ;
        }
        if ( calibAxis != null ) {
            txt.add ( "Lotrichtung" ) ;
            txt.add ( String.format( Locale.ENGLISH, "\tx %10.4f", calibAxis[0] ) ) ;
            txt.add ( String.format( Locale.ENGLISH, "\ty %10.4f", calibAxis[1] ) ) ;
            txt.add ( String.format( Locale.ENGLISH, "\tz %10.4f", calibAxis[2] ) ) ;
        }
        if ( rotationAxis != null ) {
            txt.add ( "Rotations-Achse" ) ;
            txt.add ( String.format( Locale.ENGLISH, "\tx %10.4f", rotationAxis[0] ) ) ;
            txt.add ( String.format( Locale.ENGLISH, "\ty %10.4f", rotationAxis[1] ) ) ;
            txt.add ( String.format( Locale.ENGLISH, "\tz %10.4f", rotationAxis[2] ) ) ;
            txt.add ( String.format( Locale.ENGLISH, "\tfehler %10.4f\u00B0", rotationAxisError*180.0/Math.PI) ) ;
        }
        if ( calibAxis != null && rotationAxis != null ) {
            double cos = VectorOps.dot(calibAxis, rotationAxis);
            double sin = VectorOps.len(VectorOps.cross(calibAxis, rotationAxis));
            double phi = Math.atan2(sin, cos);
            txt.add ( String.format( Locale.ENGLISH, "Winkel zwischen Achse und Lot %10.2f", (phi*180/Math.PI) ) ) ;
        }
        if ( tiltAxis != null ) {
            txt.add( "Wasserwaage Kalibrierung" ) ;
            txt.add( String.format( Locale.ENGLISH, "\tWinkel        %10.5f\u00B0", tiltAxisToGravityAngle*180.0/Math.PI)) ;
            txt.add( String.format(Locale.ENGLISH,  "\tWinkel Fehler %10.7f\u00B0", tiltAxisToGravityAngleError*180.0/Math.PI)) ;
            txt.add( String.format( Locale.ENGLISH, "\tAchse[x]          %10.3f", tiltAxis[0] ) ) ;
            txt.add( String.format( Locale.ENGLISH, "\tAchse[y]          %10.3f", tiltAxis[1] ) ) ;
            txt.add( String.format( Locale.ENGLISH, "\tAchse[z]          %10.3f", tiltAxis[2] ) ) ;
            if ( acceleration != null ){
                double[] acc = new double[3];
                acc[0] = acceleration[0] ;
                acc[1] = acceleration[1] ;
                acc[2] = acceleration[2] ;
                double phi = VectorOps.len( VectorOps.angleBetweenVectors(acc, tiltAxis) ) ;
                txt.add( String.format(Locale.ENGLISH,  "\tWinkel diff %10.5f\u00B0", (phi-tiltAxisToGravityAngle)*180.0/Math.PI)) ;
            }
        }
        td.setText( txt ) ;
        int w = getWidth();
        int h = getHeight();
        txt.add( String.format( Locale.ENGLISH, "scale %10.3f %10.3f", scaleFactor, scale ) ) ;
        canvas.save() ;
        canvas.rotate( 90, w/2, w/2 );
        if ( textOn )
            td.drawOnto( canvas, 100, 100 );
        canvas.restore();
        float r = w > h ? h : w;
        float cx = w / 2;
        float cy = h / 2;
        if ( tiltAxis != null && acceleration != null ) {
            double[] acc = new double[3];
            acc[0] = acceleration[0] ;
            acc[1] = acceleration[1] ;
            acc[2] = acceleration[2] ;
            VectorOps.normalize( acc );
            double[] v = VectorOps.cross(tiltAxis, acc);
            double[] v2 = VectorOps.cross(acc, v);
            if ( false ) {
                paintAxis ( canvas, cx, cy, w, h, v2, whiteStroke ) ;
                paintAxis ( canvas, cx, cy, w, h, tiltAxis, whiteFill ) ;
            }
            double[] phiV = VectorOps.angleBetweenVectors(tiltAxis, acc);
            double phi = (VectorOps.len(phiV)-tiltAxisToGravityAngle/* - Math.PI / 2*/);
            drawWheel ( canvas, cx, cy, phi ) ;
        } else {
            /*
            float scale = r / 3;
            scale *= this.scale ;
            double degreePerUnit = 5 ;
            double f = 1.0 / (degreePerUnit*Math.PI / 180.0);
            double n[] = getNormal ( acceleration ) ;
            double[] acc;
            acc = new double[3] ;
            for ( int i = 0 ; i < 3 ; i++ )
                acc[i] = acceleration[i] ;
            matrix = mfvp.getMatrixFromVectorPair( calibAxis, acc ) ;
            float dx = (float) ( matrix[2][0] * scale * f);
            float dy = (float) ( matrix[2][1] * scale * f);


            canvas.drawCircle( cx+dx, cy-dy, (float) (scale*0.2), whiteFill );
            canvas.drawLine( 0, cy, w, cy, whiteFill );
            canvas.drawLine( cx, 0, cx, h, whiteFill );
             */
        }
    }
    private void drawWheel(Canvas canvas, float cx, float cy, double phi ) {
        Paint ws = new Paint(wheelStroke);
        ws.setARGB( 255, 255, 255, 255 );
        canvas.drawLine( 0, cy, canvas.getWidth(), cy, ws );
        int r = canvas.getHeight()*5;
        cx -= r ;
        //canvas.drawCircle( cx, cy, r, wheelStroke );
        canvas.save();
        /*
        canvas.rotate(  (float) (phi*180.0/Math.PI), cx, cy );
        int n = 18 ;
        double dPhi = Math.PI / n;
        for ( int i = 0 ; i <= n ; i++ ) {
            double alpha = dPhi * i - Math.PI / 2;
            canvas.drawLine( cx, cy, (float) (cx+r*Math.cos( alpha )), (float) (cy+r*Math.sin( alpha )), wheelStroke );
        }
        canvas.drawText( " 0.00\u00B0" , cx+r, cy, wheelText );
         */
        double phiDeg0 = phi * 180.0 / Math.PI - 90;
        double phiDegDelta = 10;
        int n = (int) Math.round(180/phiDegDelta);
        canvas.rotate((float) phiDeg0, cx, cy );
        int deg = -90 ;
        double deltaStroke = 0.02;
        for ( int i = 0 ; i <= n ; i++ ) {
            canvas.drawLine( cx, cy, (float) (cx+r*(1.0+deltaStroke)), cy, wheelStroke );
            canvas.save() ;
            canvas.rotate( 90, (float) (cx+r*(1.0+deltaStroke))+10, cy );
            canvas.drawText( String.format( "%-1d\u00B0", deg  ), (float) (cx+r*(1.0+deltaStroke))+10, cy, wheelText );
            canvas.restore();
            canvas.rotate((float) phiDegDelta, cx, cy );
            deg += phiDegDelta ;
        }
        canvas.restore();
        canvas.save();
        canvas.rotate((float) phiDeg0, cx, cy );
        deg = -90 ;
        phiDegDelta = 1 ;
        n = (int) Math.round( 180/phiDegDelta );
        for ( int i = 0 ; i <= n ; i++ ) {
            canvas.drawLine((float) (cx+r*(1.0-deltaStroke)), cy, cx+r, cy, wheelStroke );
            if ( deg% 10 != 0 ) {
                canvas.save() ;
                canvas.rotate( 90, (float) (cx+r+10), cy );
                canvas.drawText( String.format( "%-1d\u00B0", deg  ), (float) (cx+r+10), cy, wheelText );
                canvas.restore();
            }
            canvas.rotate((float) phiDegDelta, cx, cy );
            deg += phiDegDelta ;
        }
        canvas.restore();
        canvas.save();
        canvas.rotate((float) phiDeg0, cx, cy );
        deg = -90 ;
        phiDegDelta = 0.1 ;
        n = (int) Math.abs( 180/phiDegDelta );
        deltaStroke /= 2 ;
        for ( int i = 0 ; i <= n ; i++ ) {
            double ds = i % 5 == 0 ? deltaStroke*1.5: deltaStroke;
            canvas.drawLine((float) (cx+r*(1.0-ds)), cy, cx+r, cy, wheelStroke );
            canvas.rotate((float) phiDegDelta, cx, cy );
        }
        canvas.restore();

    }

    private void paintAxis(Canvas canvas, double cx, double cy, double w, double h, double[] axis, Paint paint) {
        double ax = axis[0];
        double ay = axis[1];
        double deltaY = cy - h;
        double deltaX = ax * deltaY / ay;
        float x1 = (float) (deltaX + cx);
        deltaY = cy;
        deltaX = ax * deltaY / ay;
        float x2 = (float) (deltaX + cx);
        canvas.drawLine( x2, 0f, x1, (float) h, paint );
    }

    private double[] xform(double[][] matrix, double[] v) {
        double[] rv = new double[3];
        for ( int i = 0 ; i < 3 ; i++ ) {
            double sum = 0.0;
            for ( int j = 0 ; j < 3 ; j++ ) {
                sum += matrix[i][j]*v[j] ;
            }
            rv[i] = sum ;
        }
        return rv ;
    }

    private double[] getNormal(float[] v) {
        double[] rv = new double[3];
        double l = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        for ( int i = 0 ; i < 3 ; i++ ) {
            rv[i] = v[i]/l ;
        }
        return rv ;
    }
    private double[] getNormal(double[] v) {
        double[] rv = new double[3];
        double l = Math.sqrt(v[0] * v[0] + v[1] *v[1] + v[2] * v[2]);
        for ( int i = 0 ; i < 3 ; i++ ) {
            rv[i] = v[i]/l ;
        }
        return rv ;
    }


    private void drawAxis(float[] vector, Canvas canvas, float cx, float cy, float scale, Paint p) {
        canvas.drawLine( -vector[0]*scale+cx, cy+vector[1]*scale, vector[0]*scale+cx, cy-vector[1]*scale, p );
    }


    @Override
    public void attach() {
        accFilter.reset();
        omegaFilter.reset();
    }

    @Override
    public void detach() {

    }

    @Override
    public boolean isAttached() {
        return false;
    }
    void startGravityCalibration() {
        playSound ( true ) ;
        doSample = true ;
        accelerationAccumulator = new AccelerationAccumulator() ;
        startSampleTimeMs = System.currentTimeMillis() ;
        clickCount++ ;
    }

    private void endGravityCalibration() {
        accelerationAccumulator.evaluate () ;
        doSample = false ;
        playSound( false );
        calibAxis = accelerationAccumulator.avg.clone() ;
        invalidate();
    }

    private void startAxisCalibration() {
        if ( gyro != null ) {
            playSound( true );
            gyro.setListener( this );
        }
    }

    private void playSound( boolean start ) {
        AsyncPlayer p = new AsyncPlayer( ("ping") ) ;
        AudioAttributes attr = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        File dir = getContext().getExternalFilesDir(  Environment.DIRECTORY_RINGTONES ) ;
        File file = start ? new File(dir, "very_short_notif.mp3") : new File(dir, "verry_short_sms.mp3") ;

        Uri uri = Uri.fromFile(file);
        p.play( getContext(), uri, false, attr );

    }

    @Override
    public boolean onScale(@NonNull ScaleGestureDetector detector) {
        scaleFactor = detector.getScaleFactor();
        scale *= scaleFactor ;
        invalidate();
        markScaleActivity () ;
        return true;
    }

    private void markScaleActivity() {
        scaleTimeMS = System.currentTimeMillis();
        scaled = true ;
        scaling = true ;
    }

    @Override
    public boolean onScaleBegin(@NonNull ScaleGestureDetector detector) {
        markScaleActivity () ;
        return true;
    }

    @Override
    public void onScaleEnd(@NonNull ScaleGestureDetector detector) {
        scaling = false ;
    }

    public void setAcceleration(float[] values, long nanos ) {
        if ( !doSample ) {
            this.acceleration = accFilter.filter(values);
        } else {
            this.accelerationAccumulator.add( accFilter.filter(values) );
            if ( (System.currentTimeMillis()-startSampleTimeMs)>10*1000 ) {
                endGravityCalibration() ;
            }
        }
        if ( axisAndAngle != null )
            axisAndAngle.setAcceleration( values, nanos );
        invalidate();
    }
    public void setAngularVelocity(float[] values, long timestampnano ) {
        omega = omegaFilter.filter( values ) ;
        omegaAbsDeg = Math.sqrt(omega[0] * omega[0] + omega[1] * omega[1] + omega[2] * omega[2])*180.0/Math.PI ;
        omegaAbsDeg2 = omegaAbsFilter.filter(omegaAbsDeg);
        if ( waitForRest && omegaAbsDeg2 < 0.3 ) {
            switch ( this.menuEntryStartOnRest ) {
                case CALIB_AXIS:
                    startAxisCalibration();
                    break ;
                case CALIB_ACCELERATION:
                    startGravityCalibration();
                    break ;
                case CALIB_AXIS_AND_ANGLE:
                    startAxisAndAngle() ;
                    break ;
            }
            waitForRest = false ;
        }
        if ( gyro == null ) {
            gyro = new GyrosopicIntegrator(new Quaternion(1,0,0,0), timestampnano ) ;
        }
        if ( axisAndAngle != null ) {
            axisAndAngle.setAngularVelocity( values, timestampnano );
            if ( (System.currentTimeMillis()-axisAndAngleSTartTimeMS)>10*1000 ) {
                axisAndAngle.evaluate();
                tiltAxis = axisAndAngle.axis ;
                tiltAxisToGravityAngle = axisAndAngle.angle ;
                tiltAxisToGravityAngleError = axisAndAngle.angleError ;
                axisAndAngle = null ;
                playSound( false );
                storeData () ;
            }
        }
        gyro.nextSpin(values, timestampnano);
    }

    private void startAxisAndAngle() {
        axisAndAngle = new TiltCalibration();
        axisAndAngleSTartTimeMS = System.currentTimeMillis() ;
        playSound( true );
    }

    @Override
    public void axisReady(GyrosopicIntegrator.AxisMeasurementResult result) {
        playSound( false );
        rotationAxis = result.getAxis() ;
        rotationAxisError = result.getError() ;
    }
}
