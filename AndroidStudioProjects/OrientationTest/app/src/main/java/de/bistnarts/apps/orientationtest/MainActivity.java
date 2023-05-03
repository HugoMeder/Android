package de.bistnarts.apps.orientationtest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Vector;

import de.bistnarts.apps.orientationtest.tools.ContinousQuaternionFilter;
import de.bistnarts.apps.orientationtest.tools.GNSSOneShoot;


public class MainActivity extends AppCompatActivity {

    private long lastTime;
    private ViewPager2 viewPager;
    private MyAdapter adapter;
    private ContinousQuaternionFilter qf = new ContinousQuaternionFilter () ;


    enum InitAttrs {

        TEXT ( R.layout.text_view, TextViewHolder.class, "Text" ),
        AXIS ( R.layout.axis_view, AxisViewHolder.class, "Mangetfield" ),
        SPIRIT_LEVEL ( R.layout.spiritlevel_view, SpiritLevelViewHolder.class, "Wasserwaage" ),
        INTEGRATOR ( R.layout.integrator_view, IntegratorViewHolder.class, "Integrator" ) ;
        InitAttrs (int layout, java.lang.Class viewHolderClass, String tabName ) {
            this.layout = layout ;
            this.viewHolderClass = viewHolderClass ;
            this.tabName = tabName ;
        }


        final int layout;
        final Class viewHolderClass;
        final String tabName;
    }

    InitAttrs[] initAttrs = { InitAttrs.INTEGRATOR, InitAttrs.TEXT, InitAttrs.AXIS, InitAttrs.SPIRIT_LEVEL } ;
    //InitAttrs[] initAttrs = { InitAttrs.SPIRIT_LEVEL } ;

    int numViews = initAttrs.length ;

        class MyAdapter extends  RecyclerView.Adapter implements SensorEventListener {
            private MyTabConfigurationStrategy strat = new MyTabConfigurationStrategy () ;

            //AbstractViewHolder views[] = new AbstractViewHolder[numViews] ;
            private Vector<AbstractViewHolder> views = new Vector<AbstractViewHolder>() ;

            public TabLayoutMediator.TabConfigurationStrategy getStrategy() {
                return strat ;
            }

            class MyTabConfigurationStrategy implements TabLayoutMediator.TabConfigurationStrategy {

                @Override
                public void onConfigureTab( TabLayout.Tab tab, int position) {
                    View v = tab.getCustomView();
                    tab.setText( initAttrs[position].tabName ) ;
                }
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                LayoutInflater infl = getLayoutInflater();
                //int cnt = parent.getChildCount() ;
                int cnt = views.size() ;
                System.out.println( "onCreateViewHolder "+cnt+" "+initAttrs[cnt].tabName );
                View w =infl.inflate( initAttrs[cnt].layout, parent, false ) ;
                try {
                    Constructor constr = initAttrs[cnt].viewHolderClass.getConstructor(View.class);
                    RecyclerView.ViewHolder rv = null;
                    rv = (RecyclerView.ViewHolder) constr.newInstance( w );
                    views.add((AbstractViewHolder) rv) ;
                    return rv;
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List payloads) {
                super.onBindViewHolder(holder, position, payloads);
                //views[position] = (AbstractViewHolder) holder;
                System.out.println ( "onBindViewHolder "+position ) ;
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
                super.onViewDetachedFromWindow(holder);
                ((AbstractViewHolder)holder).detach();
                System.out.println ( "onViewDetachedFromWindow "+holder.getAdapterPosition() ) ;
            }

            @Override
            public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
                super.onViewAttachedToWindow(holder);
                ((AbstractViewHolder)holder).attach();
                System.out.println ( "onViewAttachedToWindow "+holder.getAdapterPosition() ) ;
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                System.out.println( "onBindViewHolder");
            }

            @Override
            public int getItemCount() {
                return numViews;
            }

            @Override
            public void onSensorChanged(SensorEvent event) {

                if ( event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR ) {
                    qf.map ( event.values ); ;
                }
                for (AbstractViewHolder view : views) {
                    if ( view != null && view.isAttached() ) {
                        view.onSensorChanged( event );
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                for (AbstractViewHolder view : views) {
                    if ( view != null && view.isAttached() ) {
                        view.onAccuracyChanged( sensor, accuracy );
                    }
                }

            }

        }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new MyAdapter();
        int view = R.layout.pager;
        setContentView(view);
        viewPager = (ViewPager2) findViewById(R.id.pager);
        viewPager.setAdapter(adapter);
        TabLayout tl = findViewById(R.id.tab_layout);
        //adapter

        TabLayoutMediator med = new TabLayoutMediator(tl, viewPager, adapter.getStrategy());
        med.attach();

        SensorManager sensorManager;
        Sensor sensor;

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (sensor != null) {
            sensorManager.registerListener(adapter, sensor, 1000000000);
            System.out.println("registered");
        }
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (sensor != null) {
            sensorManager.registerListener(adapter, sensor, 1000000000);
            System.out.println("registered");
        }

        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (sensor != null) {
            sensorManager.registerListener(adapter, sensor, 1000000000);
            System.out.println("registered");
        }
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (sensor != null) {
            sensorManager.registerListener(adapter, sensor, 1000000000);
            System.out.println("registered");
        }
    }
}