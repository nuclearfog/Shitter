package org.nuclearfog.twidda.database;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.widget.Button;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.window.AppSettings;

public class ColorPreferences implements OnColorChangedListener, DialogInterface.OnDismissListener {

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
    public void onColorChanged(int i) {
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

    @Override
    public void onDismiss(DialogInterface i) {
        Button colorButton1 = (Button)((AppSettings)context).findViewById(R.id.color_background);
        Button colorButton2 = (Button)((AppSettings)context).findViewById(R.id.color_font);
        Button colorButton3 = (Button)((AppSettings)context).findViewById(R.id.color_tweet);
        String color1Str = "#"+Integer.toHexString(background);
        String color2Str = "#"+Integer.toHexString(font);
        String color3Str = "#"+Integer.toHexString(tweet);
        colorButton1.setBackgroundColor(background);
        colorButton2.setBackgroundColor(font);
        colorButton3.setBackgroundColor(tweet);
        colorButton1.setText(color1Str);
        colorButton2.setText(color2Str);
        colorButton3.setText(color3Str);
    }

    public int getColor(final int Mode){
        switch (Mode) {
            case BACKGROUND:
                return background;
            case FONT_COLOR:
                return font;
            case TWEET_COLOR:
               return tweet;
            default:
                return -1;
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
        Dialog d = ColorPickerDialogBuilder.with(context)
                .showAlphaSlider(false).initialColor(preColor)
                .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE).density(20)
                .setOnColorChangedListener(this).build();
        d.setOnDismissListener(this);
        d.show();
    }

    public void commit() {
        SharedPreferences.Editor e = settings.edit();
        e.putInt("background_color", background);
        e.putInt("font_color", font);
        e.putInt("tweet_color", tweet);
        e.apply();
    }

    public static ColorPreferences getInstance(Context c) {
        if(ourInstance == null)
            ourInstance = new ColorPreferences(c);
        context = c;
        return ourInstance;
    }
}