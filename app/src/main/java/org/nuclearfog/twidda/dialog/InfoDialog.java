package org.nuclearfog.twidda.dialog;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;

import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.R;

/**
 * dialog window with app information and resource links
 *
 * @author nuclearfog
 */
public class InfoDialog extends Dialog {

    /**
     *
     */
    public InfoDialog(Context context) {
        super(context, R.style.AppInfoDialog);
        setContentView(R.layout.dialog_app_info);
        TextView appInfo = findViewById(R.id.settings_app_info);

        appInfo.append(" V");
        appInfo.append(BuildConfig.VERSION_NAME);
    }
}