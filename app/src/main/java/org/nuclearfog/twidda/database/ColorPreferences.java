package org.nuclearfog.twidda.database;

import android.content.Context;
import android.content.SharedPreferences;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

public class ColorPreferences implements OnColorSelectedListener {

    public static final int BACKGROUND = 0x0;
    public static final int FONT_COLOR = 0x1;
    public static final int TWEET_COLOR = 0x3;

    private int background = 0;
    private int font = 0;
    private int tweet = 0;
    private int mode;

    private static ColorPreferences ourInstance;
    private SharedPreferences settings;
    private static Context context;


    private ColorPreferences(Context context) {
        ColorPreferences.context = context;
        settings = context.getSharedPreferences("settings", 0);
        background = settings.getInt("background_color", 0xff061a22);
        font = settings.getInt("font_color", 0xffffffff);
        tweet = settings.getInt("tweet_color", 0xff19aae8);
    }

    @Override
    public void onColorSelected(int i) {
        switch(mode) {
            case BACKGROUND:
                background = i;
                break;
            case FONT_COLOR:
                font = i;
                break;
            case TWEET_COLOR:
                tweet = i;
                break;
        }
    }

    public void setColor(final int MODE) {
        int preColor = 0x0;
        mode = MODE;
        if(MODE == BACKGROUND)
            preColor = background;
        else if(MODE == FONT_COLOR)
            preColor = font;
        else if(MODE == TWEET_COLOR)
            preColor = tweet;
        ColorPickerDialogBuilder.with(context)
                .showAlphaSlider(false).initialColor(preColor)
                .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE).density(20)
                .setOnColorSelectedListener(this).build().show();
    }

    public void commit() {
        SharedPreferences.Editor e = settings.edit();
        e.putInt("background_color", background);
        e.putInt("font_color", font);
        e.putInt("tweet_color", tweet);
        e.apply();
    }

    public int getBackgroundColor(){return background;}
    public int getFontColor(){return font;}
    public int getTweetColor(){return tweet;}

    public static ColorPreferences getInstance(Context c) {
        if(ourInstance==null)
            ourInstance = new ColorPreferences(c);
        context = c;
        return ourInstance;
    }
}