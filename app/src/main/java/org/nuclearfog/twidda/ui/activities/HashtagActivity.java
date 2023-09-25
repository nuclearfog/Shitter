package org.nuclearfog.twidda.ui.activities;

import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.ui.adapter.viewpager.HashtagAdapter;
import org.nuclearfog.twidda.ui.views.TabSelector;

/**
 * Activity class used to show hashtag following/featuring
 *
 * @author nuclearfog
 */
public class HashtagActivity extends AppCompatActivity {


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.page_hashtag);

		ViewGroup root = findViewById(R.id.page_hashtag_root);
		TabSelector tabSelector = findViewById(R.id.page_hashtag_tab);
		ViewPager2 viewPager = findViewById(R.id.page_hashtag_pager);

		HashtagAdapter adapter = new HashtagAdapter(this);
		viewPager.setAdapter(adapter);
		viewPager.setOffscreenPageLimit(2);

		tabSelector.addTabIcons(R.array.userlist_hashtag_icons);
		tabSelector.addTabLabels(R.array.hashtag_labels);

		AppStyles.setTheme(root);
	}
}