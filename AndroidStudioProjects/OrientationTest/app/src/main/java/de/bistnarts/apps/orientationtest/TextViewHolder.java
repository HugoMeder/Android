package de.bistnarts.apps.orientationtest;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

public class TextViewHolder extends AbstractViewHolder {

    TextView textView ;
    long lastTime ;
    private float[] magneticField;
    private float[] orientation;
    private Float temperature;
    private float[] accelleration;

    public TextViewHolder(View itemView ) {
        super( itemView );
        ConstraintLayout view = (ConstraintLayout) itemView;
        textView = (TextView) view.getViewById(R.id.textView);
        textView.setText("pos " + getAdapterPosition());
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();
        switch ( type ) {
            case Sensor.TYPE_MAGNETIC_FIELD :
                magneticField = event.values ;
                break ;
            case Sensor.TYPE_ROTATION_VECTOR:
                orientation = event.values;
                break ;
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                temperature = (Float)event.values[0];
                break ;
            case Sensor.TYPE_ACCELEROMETER:
                accelleration = event.values;
                break ;
        }
        long now = System.currentTimeMillis();
        if ( now-lastTime > 1000 ) {
            render () ;
            lastTime = now ;
        }
    }

    private void render() {
        //System.out.println( "tick");
        StringBuffer sb =new StringBuffer();
        if ( magneticField != null ) {
            float[] vals = magneticField;
            double phi = Math.sqrt(vals[0] * vals[0] + vals[1] * vals[1] + vals[2] * vals[2]);
            sb.append( "magetic field\nx "+vals[0]+"\ny "+vals[1]+"\nz "+vals[2]+"\nphi "+phi+" grad\n" );

        }
        if ( orientation != null ) {
            float[] vals = orientation;
            double len = Math.sqrt(vals[0] * vals[0] + vals[1] * vals[1] + vals[2] * vals[2] + vals[3] * vals[3]);
            double imabs = Math.sqrt(vals[0] * vals[0] + vals[1] * vals[1] + vals[2] * vals[2]);
            double reabs = Math.abs( vals[3] ) ;
            double phi = Math.atan2(imabs, reabs)*360/Math.PI;

            sb.append( "orientation quat\ni "+vals[0]+"\nj "+vals[1]+"\nk"+vals[2]+"\nr "+vals[3]+"\nwinkel "+phi+" grad\n" );
        }
        if ( temperature != null )
            sb.append( "temperatur "+temperature ) ;
        if ( accelleration != null ) {
            float[] vals = accelleration;
            double len = Math.sqrt(vals[0] * vals[0] + vals[1] * vals[1] + vals[2] * vals[2] );
            sb.append( "accelleration\nx "+vals[0]+"\ny "+vals[1]+"\nz"+vals[2]+"\nabs "+len+"\n" );
        }
        textView.setText( sb );
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}