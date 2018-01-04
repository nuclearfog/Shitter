package org.nuclearfog.twidda.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

public class ColorPreferences implements OnColorSelectedListener{

    public static final int BACKGROUND = 0x0;
    public static final int FONT_COLOR = 0x1;

    private int current = 0;
    private static ColorPreferences ourInstance;
    private SharedPreferences settings;
    private Context context;


    private ColorPreferences(Context context) {
        this.context = context;
        settings = context.getSharedPreferences("settings", 0);
    }

    @Override
    public void onColorSelected(int i) {
        current = i;
    }


    public void setColor(final int MODE) {
        ColorPickerDialogBuilder.with(context)
                .showAlphaSlider(false)
                .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE).density(20)
                .setOnColorSelectedListener(this).build().show();
    }

    public void commmit() {


    }

    /**
     *
     * TODO
     */



    public static ColorPreferences getInstance(Context context) {
        if(ourInstance==null)
            ourInstance = new ColorPreferences(context);
        return ourInstance;
    }
}
