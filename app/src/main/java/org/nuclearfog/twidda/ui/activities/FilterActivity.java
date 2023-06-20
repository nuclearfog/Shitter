package org.nuclearfog.twidda.ui.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;

/**
 * Status filter viewer activity
 *
 * @author nuclearfog
 */
public class FilterActivity extends AppCompatActivity {


	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(AppStyles.setFontScale(newBase));
	}


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.page_filter);
		ViewGroup root = findViewById(R.id.page_filter_root);
		Toolbar toolbar = findViewById(R.id.page_filter_toolbar);
		toolbar.setTitle(R.string.toolbar_title_filter);
		setSupportActionBar(toolbar);

		AppStyles.setTheme(root);
	}
}