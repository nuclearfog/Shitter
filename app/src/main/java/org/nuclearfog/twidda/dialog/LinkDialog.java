package org.nuclearfog.twidda.dialog;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.method.ScrollingMovementMethod;
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
import static com.squareup.picasso.NetworkPolicy.NO_STORE;

/**
 * dialog class to show link preview
 *
 * @author nuclearfog
 */
public class LinkDialog extends Dialog implements LinkPreviewCallback, OnClickListener {

    private TextView title, description;
    private ImageView preview;
    private ProgressBar loading;

    private TextCrawler textCrawler;
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
        description.setMovementMethod(ScrollingMovementMethod.getInstance());

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
        // load preview
        textCrawler.makePreview(this, url);
        this.url = url;
    }


    @Override
    public void dismiss() {
        super.dismiss();
        textCrawler.cancel();
        Picasso.get().cancelRequest(preview);
    }


    @Override
    public void onPre() {
        // reset views
        title.setText("");
        description.setText("");
        preview.setImageResource(0);
        loading.setVisibility(View.VISIBLE);
    }


    @Override
    public void onPos(SourceContent sourceContent, boolean b) {
        loading.setVisibility(View.INVISIBLE);
        if (sourceContent.isSuccess()) {
            // set website title
            title.setText(sourceContent.getTitle());
            description.setText(sourceContent.getDescription());
            // check for image
            if (!sourceContent.getImages().isEmpty()) {
                // load first image as preview
                String link = sourceContent.getImages().get(0);
                if (link != null && link.startsWith("https://")) {
                    // load image without caching
                    Picasso.get().load(link).networkPolicy(NO_STORE).into(preview);
                }
            } else {
                // no image preview
                preview.setVisibility(View.GONE);
            }
        } else {
            // no valid title means lack of information
            // so open link directly in browser
            title.performClick();
            dismiss();
        }
    }


    @Override
    public void onClick(View v) {
        // close icon
        if (v.getId() == R.id.link_preview_close) {
            dismiss();
        }
        // title
        else if (v.getId() == R.id.link_preview_title) {
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