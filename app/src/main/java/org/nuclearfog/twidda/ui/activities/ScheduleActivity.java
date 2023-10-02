package org.nuclearfog.twidda.ui.activities;

import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.ui.fragments.ScheduleFragment;

/**
 * Activity class used to show a list of scheduled posts
 *
 * @author nuclearfog
 */
public class ScheduleActivity extends AppCompatActivity {


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.page_fragment);
		ViewGroup root = findViewById(R.id.page_fragment_root);
		Toolbar toolbar = findViewById(R.id.page_fragment_toolbar);

		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.page_fragment_container, ScheduleFragment.class, null);
		fragmentTransaction.commit();

		toolbar.setTitle(R.string.toolbar_schedule_title);
		setSupportActionBar(toolbar);
		AppStyles.setTheme(root);
	}
}