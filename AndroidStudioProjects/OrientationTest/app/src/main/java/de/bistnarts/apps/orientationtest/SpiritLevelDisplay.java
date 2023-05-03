package de.bistnarts.apps.orientationtest;

import static android.media.AudioDeviceInfo.TYPE_BUILTIN_SPEAKER;
import static android.media.AudioManager.FX_KEY_CLICK;
import static android.media.AudioManager.GET_DEVICES_OUTPUTS;
import static android.provider.Settings.System.DEFAULT_RINGTONE_URI;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.graphics.Xfermode;
import android.media.AsyncPlayer;
import android.media.AudioAttributes;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiManager;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import de.bistnarts.apps.orientationtest.tools.LowPassFilter;
import de.bistnarts.apps.orientationtest.tools.Quaternion;
import de.bistnarts.apps.orientationtest.tools.TextDrawer;

public class SpiritLevelDisplay extends View implements AttachDetach, View.OnLongClickListener {
    private Quaternion orientation;
    double[][]matrix = new double[3][3] ;
    float[]vec = new float[3] ;
    private Paint red;
    private Paint green;
    private Paint blue;
    private Paint[] rgb;
    private LowPassFilter axisFilter = new LowPassFilter ( 0.3f ) ;
    private Paint whiteFill;
    private Paint whiteStroke;
    private AudioManager audioManager;

    private int clickCount = 0 ;
    private TextDrawer td;

    public SpiritLevelDisplay(Context context) {
        super(context);
        init () ;
    }

    public SpiritLevelDisplay(Context context, AttributeSet attrs ) {
        super(context, attrs );
        init () ;
    }

    private void init() {

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

        //whiteStroke.setXfermode( new PorterDuffXfermode( PorterDuff.Mode.)) ;
        rgb = new Paint[3];
        rgb[0] = red ;
        rgb[1] = green ;
        rgb[2] = blue ;
        setOnLongClickListener( this );

        //List<MidiDeviceInfo> midi = getMidiDevices(true);
        checkAudio () ;

        td = new TextDrawer ( whiteStroke ) ;
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

    public void setOrientation(float[] ori) {
        float[] values = axisFilter.filter(ori);
        orientation = new Quaternion ( values[3], values[0], values[1], values[2] ).normalize() ;
        orientation.getMatrix33( matrix );
        invalidate () ;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw( canvas );

        Vector<String> txt = new Vector<String>();
        td.setText( txt ) ;
        txt.add( String.format( Locale.ENGLISH, "click count "+clickCount ) ) ;
        td.drawOnto( canvas, 100, 100 );
        if ( matrix == null )
            return ;
        int w = getWidth();
        int h = getHeight();
        float r = w > h ? h : w;
        float cx = w / 2;
        float cy = h / 2;
        float scale = r / 3;

        double degreePerUnit = 5 ;
        double f = 1.0 / (degreePerUnit*Math.PI / 180.0);
        float dx = (float) (matrix[2][0] * scale * f);
        float dy = (float) (matrix[2][1] * scale * f);

        canvas.drawCircle( cx+dx, cy-dy, (float) (scale*0.2), whiteFill );

        for ( int i = 0 ; i < 2 ; i++ ) {
            for ( int j = 0 ; j < 3 ; j++ ) {
                vec[j] = (float)matrix[i][j] ;
            }
            drawAxis( vec, canvas, cx, cy, scale, whiteStroke ) ;
        }
    }

    private void drawAxis(float[] vector, Canvas canvas, float cx, float cy, float scale, Paint p) {
        canvas.drawLine( -vector[0]*scale+cx, cy+vector[1]*scale, vector[0]*scale+cx, cy-vector[1]*scale, p );
    }


    @Override
    public void attach() {
        axisFilter.reset();
    }

    @Override
    public void detach() {

    }

    @Override
    public boolean isAttached() {
        return false;
    }

    @Override
    public boolean onLongClick(View v) {
        /*if ( audioManager != null ) {
            audioManager.playSoundEffect( FX_KEY_CLICK, 1 );
            clickCount++ ;
            invalidate();
        }*/
        AsyncPlayer p = new AsyncPlayer( ("ping") ) ;
        AudioAttributes attr = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        Uri uri = DEFAULT_RINGTONE_URI;
        p.play( getContext(), uri, false, attr );
        clickCount++ ;
        return false;
    }
}
