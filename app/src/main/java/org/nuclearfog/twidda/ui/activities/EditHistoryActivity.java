package org.nuclearfog.twidda.ui.activities;

import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.ui.fragments.EditHistoryFragment;

/**
 * @author nuclearfog
 */
public class EditHistoryActivity extends AppCompatActivity {

	public static final String KEY_ID = EditHistoryFragment.KEY_ID;


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.page_fragment);
		ViewGroup root = findViewById(R.id.page_fragment_root);
		Toolbar toolbar = findViewById(R.id.page_fragment_toolbar);

		if (savedInstanceState == null) {
			FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
			fragmentTransaction.replace(R.id.page_fragment_container, EditHistoryFragment.class, getIntent().getExtras());
			fragmentTransaction.commit();
		}
		toolbar.setTitle(R.string.menu_status_history);
		setSupportActionBar(toolbar);
		AppStyles.setTheme(root);
	}
}