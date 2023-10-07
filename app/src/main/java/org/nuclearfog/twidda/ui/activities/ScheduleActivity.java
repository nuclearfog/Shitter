package org.nuclearfog.twidda.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.ui.fragments.ScheduleFragment;

/**
 * Activity class used to show a list of scheduled posts
 *
 * @author nuclearfog
 */
public class ScheduleActivity extends AppCompatActivity implements OnClickListener {


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.page_fragment);
		ViewGroup root = findViewById(R.id.page_fragment_root);
		View floatingButton = findViewById(R.id.page_fragment_floating_button);
		Toolbar toolbar = findViewById(R.id.page_fragment_toolbar);
		GlobalSettings settings = GlobalSettings.get(this);

		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.page_fragment_container, ScheduleFragment.class, null);
		fragmentTransaction.commit();

		if (settings.floatingButtonEnabled()) {
			floatingButton.setVisibility(View.VISIBLE);
		}
		toolbar.setTitle(R.string.toolbar_schedule_title);
		setSupportActionBar(toolbar);
		AppStyles.setTheme(root);

		floatingButton.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.page_tab_view_post_button) {
			Intent intent = new Intent(this, StatusEditor.class);
			startActivity(intent);
		}
	}
}