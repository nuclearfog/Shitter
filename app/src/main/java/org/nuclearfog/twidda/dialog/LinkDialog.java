package org.nuclearfog.twidda.dialog;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.leocardz.link.preview.library.LinkPreviewCallback;
import com.leocardz.link.preview.library.SourceContent;
import com.leocardz.link.preview.library.TextCrawler;
import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.widget.Toast.LENGTH_SHORT;

/**
 * dialog class to show link preview
 *
 * @author nuclearfog
 */
public class LinkDialog extends Dialog implements LinkPreviewCallback, OnClickListener {

    private TextCrawler textCrawler;
    private ProgressBar loading;

    private TextView title, description;
    private ImageView preview;

    private String url;

    /**
     *
     */
    public LinkDialog(Context context) {
        super(context, R.style.AppInfoDialog);

        setContentView(R.layout.dialog_link_preview);
        ImageView close = findViewById(R.id.link_preview_close);
        loading = findViewById(R.id.link_preview_progress);
        title = findViewById(R.id.link_preview_title);
        description = findViewById(R.id.link_preview_description);
        preview = findViewById(R.id.link_preview_image);

        close.setImageResource(R.drawable.cross);
        GlobalSettings settings = GlobalSettings.getInstance(context);
        AppStyles.setProgressColor(loading, settings.getHighlightColor());
        AppStyles.setDrawableColor(close, Color.BLACK);
        title.setTextColor(Color.BLUE);

        textCrawler = new TextCrawler();
        close.setOnClickListener(this);
        title.setOnClickListener(this);
    }

    /**
     * show dialog and generate link preview
     *
     * @param url link url to show preview
     */
    public void show(String url) {
        super.show();
        if (title.getText().length() == 0)
            textCrawler.makePreview(this, url);
        this.url = url;
    }


    @Override
    public void dismiss() {
        super.dismiss();
        textCrawler.cancel();
    }


    @Override
    public void onPre() {
        loading.setVisibility(View.VISIBLE);
    }


    @Override
    public void onPos(SourceContent sourceContent, boolean b) {
        loading.setVisibility(View.INVISIBLE);
        if (sourceContent.isSuccess()) {
            title.setText(sourceContent.getTitle());
        } else {
            if (url.startsWith("https://")) {
                title.setText(url.substring(8));
            } else {
                title.setText(url);
            }
        }
        description.setText(sourceContent.getDescription());
        if (!sourceContent.getImages().isEmpty()) {
            Picasso.get().load(sourceContent.getImages().get(0)).into(preview);
        } else {
            preview.setVisibility(View.GONE);
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.link_preview_close) {
            dismiss();
        } else if (v.getId() == R.id.link_preview_title) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            try {
                getContext().startActivity(intent);
            } catch (ActivityNotFoundException err) {
                Toast.makeText(getContext(), R.string.error_connection_failed, LENGTH_SHORT).show();
            }
        }
    }
}