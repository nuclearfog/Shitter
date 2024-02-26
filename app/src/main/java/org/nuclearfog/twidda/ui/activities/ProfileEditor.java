package org.nuclearfog.twidda.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.kyleduo.switchbutton.SwitchButton;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.CredentialsLoader;
import org.nuclearfog.twidda.backend.async.CredentialsUpdater;
import org.nuclearfog.twidda.backend.helper.MediaStatus;
import org.nuclearfog.twidda.backend.helper.update.StatusPreferenceUpdate;
import org.nuclearfog.twidda.backend.helper.update.UserUpdate;
import org.nuclearfog.twidda.backend.image.PicassoBuilder;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.backend.utils.ToolbarUpdater;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.OnConfirmListener;
import org.nuclearfog.twidda.ui.dialogs.ProgressDialog;
import org.nuclearfog.twidda.ui.dialogs.StatusPreferenceDialog;
import org.nuclearfog.twidda.ui.dialogs.StatusPreferenceDialog.PreferenceSetCallback;
import org.nuclearfog.twidda.ui.views.InputView;
import org.nuclearfog.twidda.ui.views.InputView.OnTextChangeListener;

import java.io.FileNotFoundException;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * Activity for profile editor
 *
 * @author nuclearfog
 */
public class ProfileEditor extends MediaActivity implements OnClickListener, OnConfirmListener, OnTextChangeListener, OnCheckedChangeListener, PreferenceSetCallback, Callback {

	/**
	 * key to set/restore user information
	 * value is {@link User} or {@link UserUpdate}
	 */
	public static final String KEY_USER = "profile-editor-user-data";

	/**
	 * return code used if profile information has changed
	 */
	public static final int RETURN_PROFILE_UPDATED = 0xF5C0E570;

	private CredentialsUpdater credentialUpdater;
	private CredentialsLoader credentialsLoader;
	private GlobalSettings settings;
	private Picasso picasso;

	private ImageView profile_image, profile_banner, toolbar_background, changeBannerBtn;
	private SwitchButton privacy, indexable, hideCollections;
	private InputView username, profileUrl;
	private Button addBannerBtn;

	private UserUpdate userUpdate = new UserUpdate();
	private boolean profileModified = false;

	private AsyncCallback<CredentialsLoader.Result> credentialsLoaderCallback = this::onCredentialsLoaderResult;
	private AsyncCallback<CredentialsUpdater.Result> credentialsUpdateCallback = this::onCredentialsUpdateResult;


	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(AppStyles.setFontScale(newBase));
	}


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.page_editprofile);
		Toolbar toolbar = findViewById(R.id.edit_profile_toolbar);
		ConstraintLayout root = findViewById(R.id.page_edit);
		View profileLocationLabel = findViewById(R.id.profile_edit_change_location_label);
		View profileUrlLabel = findViewById(R.id.profile_edit_change_url_label);
		View statusPrefBtn = findViewById(R.id.profile_edit_status_pref);
		InputView profileLocation = findViewById(R.id.profile_edit_change_location);
		InputView userDescription = findViewById(R.id.profile_edit_change_description);
		hideCollections = findViewById(R.id.profile_edit_hide_collection);
		indexable = findViewById(R.id.profile_edit_indexable);
		profileUrl = findViewById(R.id.profile_edit_change_url);
		privacy = findViewById(R.id.profile_edit_privacy);
		profile_image = findViewById(R.id.edit_profile_image);
		profile_banner = findViewById(R.id.profile_edit_banner);
		addBannerBtn = findViewById(R.id.profile_edit_add_banner);
		changeBannerBtn = findViewById(R.id.profile_edit_change_banner);
		toolbar_background = findViewById(R.id.profile_edit_toolbar_background);
		username = findViewById(R.id.profile_edit_change_name);
		credentialUpdater = new CredentialsUpdater(this);
		credentialsLoader = new CredentialsLoader(this);
		settings = GlobalSettings.get(this);
		picasso = PicassoBuilder.get(this);

		if (!settings.toolbarOverlapEnabled()) {
			ConstraintSet constraints = new ConstraintSet();
			constraints.clone(root);
			constraints.connect(R.id.profile_edit_banner, ConstraintSet.TOP, R.id.edit_profile_toolbar, ConstraintSet.BOTTOM);
			constraints.connect(R.id.profile_edit_add_banner, ConstraintSet.TOP, R.id.profile_edit_banner, ConstraintSet.TOP);
			constraints.applyTo(root);
		}
		if (!settings.getLogin().getConfiguration().profileUrlEnabled()) {
			profileUrl.setVisibility(View.GONE);
			profileUrlLabel.setVisibility(View.GONE);
		}
		if (!settings.getLogin().getConfiguration().profileLocationEnabled()) {
			profileLocation.setVisibility(View.GONE);
			profileLocationLabel.setVisibility(View.GONE);
		}
		toolbar.setTitle(R.string.menu_edit_profile);
		toolbar.setBackgroundColor(settings.getBackgroundColor() & ProfileActivity.TOOLBAR_TRANSPARENCY);
		profile_banner.setDrawingCacheEnabled(true);
		setSupportActionBar(toolbar);
		AppStyles.setTheme(root);

		if (savedInstanceState == null)
			savedInstanceState = getIntent().getExtras();
		if (savedInstanceState != null) {
			Object userData = savedInstanceState.getSerializable(KEY_USER);
			if (userData instanceof UserUpdate) {
				userUpdate = (UserUpdate) userData;
				if (userUpdate.getProfileImageMedia() != null) {
					Uri uri = Uri.parse(userUpdate.getProfileImageMedia().getPath());
					profile_image.setImageURI(uri);
				} else if (!userUpdate.getProfileImageUrl().isEmpty()) {
					Transformation roundCorner = new RoundedCornersTransformation(5, 0);
					picasso.load(userUpdate.getProfileImageUrl()).transform(roundCorner).into(profile_image);
				}
				if (userUpdate.getBannerImageMedia() != null) {
					Uri uri = Uri.parse(userUpdate.getBannerImageMedia().getPath());
					profile_banner.setImageURI(uri);
				} else if (!userUpdate.getBannerImageUrl().isEmpty()) {
					picasso.load(userUpdate.getBannerImageUrl()).into(profile_banner, this);
				}
				username.setText(userUpdate.getUsername());
				profileUrl.setText(userUpdate.getUrl());
				profileLocation.setText(userUpdate.getLocation());
				userDescription.setText(userUpdate.getDescription());
				privacy.setChecked(userUpdate.isPrivate());
				hideCollections.setCheckedImmediately(userUpdate.hiddenCollections());
				indexable.setCheckedImmediately(userUpdate.isIndexable());
			} else if (userData instanceof User) {
				User user = (User) userData;
				userUpdate.updateUser(user);
				if (!user.getProfileImageThumbnailUrl().isEmpty()) {
					Transformation roundCorner = new RoundedCornersTransformation(5, 0);
					picasso.load(user.getProfileImageThumbnailUrl()).transform(roundCorner).into(profile_image);
				}
				if (!user.getBannerImageThumbnailUrl().isEmpty()) {
					picasso.load(user.getBannerImageThumbnailUrl()).into(profile_banner, this);
					addBannerBtn.setVisibility(View.INVISIBLE);
					changeBannerBtn.setVisibility(View.VISIBLE);
				} else {
					addBannerBtn.setVisibility(View.VISIBLE);
					changeBannerBtn.setVisibility(View.INVISIBLE);
				}
				username.setText(user.getUsername());
				profileUrl.setText(user.getProfileUrl());
				profileLocation.setText(user.getLocation());
				userDescription.setText(user.getDescription());
				indexable.setCheckedImmediately(user.isIndexable());
				// load user credentials
				credentialsLoader.execute(null, credentialsLoaderCallback);
			}
		}
		username.setOnTextChangeListener(this);
		profileUrl.setOnTextChangeListener(this);
		profileLocation.setOnTextChangeListener(this);
		userDescription.setOnTextChangeListener(this);
		profile_image.setOnClickListener(this);
		profile_banner.setOnClickListener(this);
		addBannerBtn.setOnClickListener(this);
		privacy.setOnCheckedChangeListener(this);
		hideCollections.setOnCheckedChangeListener(this);
		indexable.setOnCheckedChangeListener(this);
		statusPrefBtn.setOnClickListener(this);
	}


	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putSerializable(KEY_USER, userUpdate);
		super.onSaveInstanceState(outState);
	}


	@Override
	public void onBackPressed() {
		if (profileModified) {
			ConfirmDialog.show(this, ConfirmDialog.PROFILE_EDITOR_LEAVE, null);
		} else {
			super.onBackPressed();
		}
	}


	@Override
	protected void onDestroy() {
		credentialUpdater.cancel();
		credentialsLoader.cancel();
		super.onDestroy();
	}


	@Override
	public boolean onCreateOptionsMenu(@NonNull Menu m) {
		getMenuInflater().inflate(R.menu.edit, m);
		AppStyles.setMenuIconColor(m, settings.getIconColor());
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if (item.getItemId() == R.id.action_save) {
			updateUser();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	protected void onAttachLocation(@Nullable Location location) {
	}


	@Override
	protected void onMediaFetched(int resultType, @NonNull Uri uri) {
		try {
			MediaStatus mediaStatus = new MediaStatus(getApplicationContext(), uri);
			// Add image as profile image
			if (resultType == REQUEST_PROFILE) {
				userUpdate.setProfileImage(mediaStatus);
				profile_image.setImageURI(uri);
				profileModified = true;
			}
			// Add image as banner image
			else if (resultType == REQUEST_BANNER) {
				userUpdate.setBannerImage(mediaStatus);
				int widthPixels = Resources.getSystem().getDisplayMetrics().widthPixels;
				picasso.load(uri).resize(widthPixels, widthPixels / 3).centerCrop(Gravity.TOP).into(profile_banner, this);
				addBannerBtn.setVisibility(View.INVISIBLE);
				changeBannerBtn.setVisibility(View.VISIBLE);
				profileModified = true;
			}
		} catch (FileNotFoundException exception) {
			Toast.makeText(getApplicationContext(), R.string.error_adding_media, Toast.LENGTH_SHORT).show();
		}
	}


	@Override
	public void onClick(View v) {
		// select net profile image
		if (v.getId() == R.id.edit_profile_image) {
			getMedia(REQUEST_PROFILE);
		}
		// select new banner image
		else if (v.getId() == R.id.profile_edit_add_banner || v.getId() == R.id.profile_edit_banner) {
			getMedia(REQUEST_BANNER);
		}
		//
		else if (v.getId() == R.id.profile_edit_status_pref) {
			StatusPreferenceUpdate statusPref = userUpdate.getStatusPreference();
			if (statusPref == null)
				statusPref = new StatusPreferenceUpdate();
			StatusPreferenceDialog.show(this, statusPref, false);
		}
	}


	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView.isPressed()) {
			if (buttonView.getId() == R.id.profile_edit_privacy) {
				userUpdate.setPrivacy(isChecked);
			} else if (buttonView.getId() == R.id.profile_edit_indexable) {
				userUpdate.setIndexable(isChecked);
			} else if (buttonView.getId() == R.id.profile_edit_hide_collection) {
				userUpdate.hideCollections(isChecked);
			}
			profileModified = true;
		}
	}


	@Override
	public void onTextChanged(InputView inputView, String text) {
		if (inputView.getId() == R.id.profile_edit_change_name) {
			userUpdate.setUsername(text);
			profileModified = true;
		} else if (inputView.getId() == R.id.profile_edit_change_location) {
			userUpdate.setLocation(text);
			profileModified = true;
		} else if (inputView.getId() == R.id.profile_edit_change_description) {
			userUpdate.setDescription(text);
			profileModified = true;
		} else if (inputView.getId() == R.id.profile_edit_change_url) {
			userUpdate.setUrl(text);
			profileModified = true;
		}
	}


	@Override
	public void onConfirm(int type) {
		// leave without settings
		if (type == ConfirmDialog.PROFILE_EDITOR_LEAVE) {
			finish();
		}
		// retry
		else if (type == ConfirmDialog.PROFILE_EDITOR_ERROR) {
			updateUser();
		}
	}


	@Override
	public void onPreferenceSet(StatusPreferenceUpdate update) {
		userUpdate.setStatusPreference(update);
		profileModified = true;
	}


	@Override
	public void onSuccess() {
		// set toolbar background
		if (settings.toolbarOverlapEnabled()) {
			profile_banner.post(new ToolbarUpdater(profile_banner, toolbar_background));
		}
	}


	@Override
	public void onError(Exception e) {
	}

	/**
	 * update user information
	 */
	private void updateUser() {
		if (credentialUpdater.isIdle()) {
			if (userUpdate.getUsername().trim().isEmpty()) {
				username.setError(getString(R.string.error_empty_name));
			} else if (!userUpdate.getUrl().trim().isEmpty() && !Patterns.WEB_URL.matcher(userUpdate.getUrl()).matches()) {
				profileUrl.setError(getString(R.string.error_wrong_url));
			} else {
				if (userUpdate.prepare(getContentResolver())) {
					credentialUpdater.execute(userUpdate, credentialsUpdateCallback);
					ProgressDialog.show(this, false);
				} else {
					Toast.makeText(getApplicationContext(), R.string.error_media_init, Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	/**
	 *
	 */
	private void onCredentialsLoaderResult(CredentialsLoader.Result result) {
		if (result.credentials != null) {
			privacy.setChecked(result.credentials.isLocked());
			indexable.setChecked(result.credentials.isIndexable());
			hideCollections.setChecked(result.credentials.collectionHidden());
			userUpdate.updateCredentials(result.credentials);
		} else if (result.exception != null) {
			String message = ErrorUtils.getErrorMessage(this, result.exception);
			ConfirmDialog.show(this, ConfirmDialog.PROFILE_EDITOR_ERROR, message);
		}
		ProgressDialog.dismiss(this);
	}

	/**
	 *
	 */
	private void onCredentialsUpdateResult(CredentialsUpdater.Result result) {
		if (result.user != null) {
			Intent data = new Intent();
			data.putExtra(KEY_USER, result.user);
			Toast.makeText(getApplicationContext(), R.string.info_profile_updated, Toast.LENGTH_SHORT).show();
			setResult(RETURN_PROFILE_UPDATED, data);
			finish();
		} else {
			String message = ErrorUtils.getErrorMessage(this, result.exception);
			ConfirmDialog.show(this, ConfirmDialog.PROFILE_EDITOR_ERROR, message);
		}
		ProgressDialog.dismiss(this);
	}
}