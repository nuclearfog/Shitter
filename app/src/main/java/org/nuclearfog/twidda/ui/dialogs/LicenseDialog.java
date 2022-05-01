package org.nuclearfog.twidda.ui.dialogs;

import android.app.Dialog;
import android.content.Context;

import com.larswerkman.licenseview.LicenseView;

import org.nuclearfog.twidda.R;

/**
 * Dialog class to show 3rd party licenses
 *
 * @author nuclearfog
 */
public class LicenseDialog extends Dialog {

    /**
     *
     */
    public LicenseDialog(Context context) {
        super(context, R.style.LicenseDialog);
        setContentView(R.layout.dialog_licenses);
        LicenseView licenseView = findViewById(R.id.license_view);
        try {
            licenseView.setLicenses(R.xml.licenses);
        } catch (Exception err) {
            err.printStackTrace();
            dismiss();
        }
    }


    @Override
    public void show() {
        if (!isShowing()) {
            super.show();
        }
    }
}