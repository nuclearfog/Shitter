package org.nuclearfog.twidda.ui.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
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


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.filter, menu);
		return super.onCreateOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if (item.getItemId() == R.id.menu_filter_create) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}