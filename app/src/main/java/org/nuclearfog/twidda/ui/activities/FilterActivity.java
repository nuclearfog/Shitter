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
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.model.Filter;
import org.nuclearfog.twidda.ui.dialogs.FilterDialog;
import org.nuclearfog.twidda.ui.dialogs.FilterDialog.FilterDialogCallback;
import org.nuclearfog.twidda.ui.fragments.FilterFragment;
import org.nuclearfog.twidda.ui.fragments.ListFragment;

/**
 * Status filter viewer activity
 *
 * @author nuclearfog
 */
public class FilterActivity extends AppCompatActivity implements FilterDialogCallback {

	private FilterDialog filterDialog;
	private ListFragment.ItemViewModel viewModel;


	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(AppStyles.setFontScale(newBase));
	}


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.page_fragment);
		ViewGroup root = findViewById(R.id.page_fragment_root);
		Toolbar toolbar = findViewById(R.id.page_fragment_toolbar);
		filterDialog = new FilterDialog(this, this);
		viewModel = new ViewModelProvider(this).get(ListFragment.ItemViewModel.class);

		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.page_fragment_container, FilterFragment.class, null);
		fragmentTransaction.commit();

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
			filterDialog.show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onFilterUpdated(Filter filter) {
		viewModel.notify(ListFragment.NOTIFY_CHANGED);
	}
}