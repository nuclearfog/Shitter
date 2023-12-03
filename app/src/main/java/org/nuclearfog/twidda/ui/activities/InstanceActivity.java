package org.nuclearfog.twidda.ui.activities;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AsyncExecutor;
import org.nuclearfog.twidda.backend.async.InstanceLoader;
import org.nuclearfog.twidda.backend.image.PicassoBuilder;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.LinkAndScrollMovement;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Instance;
import org.nuclearfog.twidda.ui.fragments.ListFragment;
import org.nuclearfog.twidda.ui.fragments.ListFragment.ItemViewModel;

/**
 * Activity used to show information and announcements of an instance
 *
 * @author nuclearfog
 */
public class InstanceActivity extends AppCompatActivity implements OnClickListener {

	/**
	 *
	 */
	private static final String KEY_INSTANCE_SAVE = "save-instance";

	/**
	 * color of the profile image placeholder
	 */
	private static final int IMAGE_PLACEHOLDER_COLOR = 0x2F000000;

	/**
	 * background color transparency mask for TextView backgrounds
	 */
	private static final int TEXT_TRANSPARENCY = 0xafffffff;

	private TextView description;
	private Toolbar toolbar;
	private ImageView banner;

	private Drawable placeholder;
	private GlobalSettings settings;
	private InstanceLoader instanceLoader;
	private Picasso picasso;
	private ItemViewModel viewModel;

	@Nullable
	private Instance instance;

	private AsyncExecutor.AsyncCallback<InstanceLoader.Result> instanceResult = this::onInstanceResult;


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.page_instance);
		ViewGroup root = findViewById(R.id.page_instance_root);
		settings = GlobalSettings.get(this);
		toolbar = findViewById(R.id.page_instance_toolbar);
		description = findViewById(R.id.page_instance_description);
		banner = findViewById(R.id.page_instance_banner);
		instanceLoader = new InstanceLoader(getApplicationContext());
		picasso = PicassoBuilder.get(this);
		placeholder = new ColorDrawable(IMAGE_PLACEHOLDER_COLOR);
		viewModel = new ViewModelProvider(this).get(ItemViewModel.class);

		toolbar.setTitle("");
		setSupportActionBar(toolbar);
		AppStyles.setTheme(root);
		AppStyles.setOverflowIcon(toolbar, settings.getIconColor());
		description.setMovementMethod(LinkAndScrollMovement.getInstance());
		description.setBackgroundColor(settings.getBackgroundColor() & TEXT_TRANSPARENCY);

		if (savedInstanceState != null) {
			Object data_instance = savedInstanceState.getSerializable(KEY_INSTANCE_SAVE);
			if (data_instance instanceof Instance) {
				instance = (Instance) data_instance;
			}
		}
		banner.setOnClickListener(this);
	}


	@Override
	protected void onStart() {
		super.onStart();
		if (instance == null) {
			InstanceLoader.Param param = new InstanceLoader.Param(InstanceLoader.Param.ONLINE);
			instanceLoader.execute(param, instanceResult);
		}
	}


	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putSerializable(KEY_INSTANCE_SAVE, instance);
		super.onSaveInstanceState(outState);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.instance, menu);
		return true;
	}


	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.menu_instance_show_all_announcements).setChecked(settings.showAllAnnouncements());
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if (item.getItemId() == R.id.menu_instance_info) {
			// todo implement instance information dialog
			return true;
		}
		//
		else if (item.getItemId() == R.id.menu_instance_show_all_announcements) {
			boolean isChecked = !item.isChecked();
			settings.setShowAllAnnouncements(isChecked);
			item.setChecked(isChecked);
			viewModel.notify(ListFragment.NOTIFY_CHANGED);
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	protected void onDestroy() {
		instanceLoader.cancel();
		super.onDestroy();
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.page_instance_banner) {
			if (instance != null && !instance.getBannerImageUrl().isEmpty()) {
				Intent intent = new Intent(this, ImageViewer.class);
				intent.putExtra(ImageViewer.KEY_IMAGE_DATA, instance.getBannerImageUrl());
				startActivity(intent);
			}
		}
	}

	/**
	 *
	 */
	private void onInstanceResult(InstanceLoader.Result result) {
		instance = result.instance;
		if (instance != null) {
			toolbar.setTitle(instance.getTitle());
			description.setText(instance.getDescription());
			if (settings.imagesEnabled() && !instance.getBannerImageUrl().isEmpty()) {
				picasso.load(instance.getBannerImageUrl()).placeholder(placeholder).into(banner);
			} else {
				banner.setImageDrawable(placeholder);
			}
		}
	}
}