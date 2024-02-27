package de.bitsnarts.gnsstracker;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DataView extends androidx.appcompat.widget.AppCompatTextView {

    public DataView(@NonNull Context context) {
        super(context);
        init ( context ) ;
    }

    public DataView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init ( context ) ;
    }

    public DataView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init ( context ) ;
    }

    private void init(Context context) {
       ((MainActivity)context).getGlobals().setDataView ( this ) ;
    }


}
