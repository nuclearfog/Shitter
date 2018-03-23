package org.nuclearfog.twidda.window;

import org.nuclearfog.twidda.R;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.widget.Button;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

public class ColorPreferences implements OnColorChangedListener, DialogInterface.OnDismissListener {

    public static final int BACKGROUND = 0x0;
    public static final int FONT_COLOR = 0x1;
    public static final int HIGHLIGHTING = 0x2;
    public static final int TWEET_COLOR = 0x3;

    private int background = 0;
    private int highlight = 0;
    private int font = 0;
    private int tweet = 0;
    private int mode;

    private static ColorPreferences ourInstance;
    private SharedPreferences settings;
    private Context context;
    private Dialog d;
    private boolean imageload;

    private ColorPreferences(Context context) {
        settings = context.getSharedPreferences("settings", 0);
        imageload = settings.getBoolean("image_load", true);
        background = settings.getInt("background_color", 0xff0f114a);
        font = settings.getInt("font_color", 0xffffffff);
        tweet = settings.getInt("tweet_color", 0xff19aae8);
        highlight = settings.getInt("highlight_color", 0xffff00ff);
    }

    @Override
    public void onColorChanged(int newColor) {
        switch(mode) {
            case BACKGROUND:
                background = newColor;
                break;
            case FONT_COLOR:
                font = newColor;
                break;
            case HIGHLIGHTING:
                highlight = newColor;
                break;
            case TWEET_COLOR:
                tweet = newColor;
                break;
        }
    }

    @Override
    public void onDismiss(DialogInterface i) {
        Button colorButton1 = (Button)((AppSettings)context).findViewById(R.id.color_background);
        Button colorButton2 = (Button)((AppSettings)context).findViewById(R.id.color_font);
        Button colorButton3 = (Button)((AppSettings)context).findViewById(R.id.color_tweet);
        Button colorButton4 = (Button)((AppSettings)context).findViewById(R.id.highlight_color);
        colorButton1.setBackgroundColor(background);
        colorButton2.setBackgroundColor(font);
        colorButton3.setBackgroundColor(tweet);
        colorButton4.setBackgroundColor(highlight);
        d.dismiss();
    }

    public int getColor(final int Mode){
        switch (Mode) {
            case BACKGROUND:
                return background;
            case FONT_COLOR:
                return font;
            case TWEET_COLOR:
               return tweet;
            case HIGHLIGHTING:
                return highlight;
            default:
                return 0xFFFFFFFF;
        }
    }

    public void setColor(final int MODE) {
        int preColor;
        mode = MODE;
        switch(MODE) {
            case(BACKGROUND):
                preColor = background;
                break;
            case(FONT_COLOR):
                preColor = font;
                break;
            case(TWEET_COLOR):
                preColor = tweet;
                break;
            case HIGHLIGHTING:
                preColor = highlight;
                break;
            default:
                preColor = 0xFFFFFFFF;
        }
        d = ColorPickerDialogBuilder.with(context)
                .showAlphaSlider(false).initialColor(preColor)
                .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE).density(20)
                .setOnColorChangedListener(this).build();
        d.setOnDismissListener(this);
        d.show();
    }

    public boolean loadImage() {
        return imageload;
    }

    public void commit() {
        SharedPreferences.Editor e = settings.edit();
        e.putInt("background_color", background);
        e.putInt("font_color", font);
        e.putInt("tweet_color", tweet);
        e.putInt("highlight_color", highlight);
        e.apply();
    }

    private void setContext(Context context){
        this.context = context;
    }

    public static ColorPreferences getInstance(Context c) {
        if(ourInstance == null)
            ourInstance = new ColorPreferences(c);
        ourInstance.setContext(c);
        return ourInstance;
    }
}