package org.nuclearfog.twidda.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.CredentialsLoader;
import org.nuclearfog.twidda.backend.async.CredentialsUpdater;
import org.nuclearfog.twidda.backend.helper.MediaStatus;
import org.nuclearfog.twidda.backend.helper.update.UserUpdate;
import org.nuclearfog.twidda.backend.image.PicassoBuilder;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.backend.utils.ToolbarUpdater;
import org.nuclearfog.twidda.config.Configuration;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Credentials;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.OnConfirmListener;
import org.nuclearfog.twidda.ui.dialogs.ProgressDialog;
import org.nuclearfog.twidda.ui.dialogs.StatusPreferenceDialog;

import java.io.FileNotFoundException;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * Activity for profile editor
 *
 * @author nuclearfog
 */
public class ProfileEditor extends MediaActivity implements OnClickListener, OnConfirmListener, TextWatcher, Callback {

	/**
	 * key to preload user data
	 * value is {@link User}
	 */
	public static final String KEY_USER = "profile-editor-data";

	/**
	 * return code used if profile information has changed
	 */
	public static final int RETURN_PROFILE_UPDATED = 0xF5C0E570;

	private CredentialsUpdater credentialUpdater;
	private CredentialsLoader credentialsLoader;
	private GlobalSettings settings;
	private Picasso picasso;

	private ProgressDialog progressDialog;
	private StatusPreferenceDialog prefDialog;
	private ConfirmDialog confirmDialog;

	private ImageView profile_image, profile_banner, toolbar_background, changeBannerBtn;
	private EditText username, profileUrl, profileLocation, userDescription;
	private Button addBannerBtn;
	private CompoundButton privacy;

	@Nullable
	private User user;
	@Nullable
	private Credentials credentials;
	private UserUpdate userUpdate = new UserUpdate();
	private boolean changed = false;

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
		profile_image = findViewById(R.id.edit_profile_image);
		profile_banner = findViewById(R.id.profile_edit_banner);
		addBannerBtn = findViewById(R.id.profile_edit_add_banner);
		changeBannerBtn = findViewById(R.id.profile_edit_change_banner);
		toolbar_background = findViewById(R.id.profile_edit_toolbar_background);
		username = findViewById(R.id.profile_edit_change_name);
		profileUrl = findViewById(R.id.profile_edit_change_url);
		profileLocation = findViewById(R.id.profile_edit_change_location);
		userDescription = findViewById(R.id.profile_edit_change_description);
		privacy = findViewById(R.id.profile_edit_privacy);

		prefDialog = new StatusPreferenceDialog(this, userUpdate);
		progressDialog = new ProgressDialog(this, null);
		confirmDialog = new ConfirmDialog(this, this);
		credentialUpdater = new CredentialsUpdater(this);
		credentialsLoader = new CredentialsLoader(this);
		settings = GlobalSettings.get(this);
		picasso = PicassoBuilder.get(this);

		toolbar.setTitle(R.string.menu_edit_profile);
		setSupportActionBar(toolbar);

		if (!settings.toolbarOverlapEnabled()) {
			ConstraintSet constraints = new ConstraintSet();
			constraints.clone(root);
			constraints.connect(R.id.profile_edit_banner, ConstraintSet.TOP, R.id.edit_profile_toolbar, ConstraintSet.BOTTOM);
			constraints.connect(R.id.profile_edit_add_banner, ConstraintSet.TOP, R.id.profile_edit_banner, ConstraintSet.TOP);
			constraints.applyTo(root);
		}

		Configuration config = settings.getLogin().getConfiguration();
		if (!config.profileUrlEnabled()) {
			profileUrl.setVisibility(View.GONE);
			profileUrlLabel.setVisibility(View.GONE);
		}
		if (!config.profileLocationEnabled()) {
			profileLocation.setVisibility(View.GONE);
			profileLocationLabel.setVisibility(View.GONE);
		}
		toolbar.setBackgroundColor(settings.getBackgroundColor() & ProfileActivity.TOOLBAR_TRANSPARENCY);
		profile_banner.setDrawingCacheEnabled(true);
		AppStyles.setTheme(root);

		Object data = getIntent().getSerializableExtra(KEY_USER);
		if (data instanceof User) {
			user = (User) data;
			setUser();
		}
		username.addTextChangedListener(this);
		profileUrl.addTextChangedListener(this);
		profileLocation.addTextChangedListener(this);
		userDescription.addTextChangedListener(this);
		profile_image.setOnClickListener(this);
		profile_banner.setOnClickListener(this);
		addBannerBtn.setOnClickListener(this);
		statusPrefBtn.setOnClickListener(this);
	}


	@Override
	protected void onStart() {
		super.onStart();
		if (credentials == null) {
			credentialsLoader.execute(null, credentialsLoaderCallback);
		}
	}


	@Override
	protected void onDestroy() {
		progressDialog.dismiss();
		credentialUpdater.cancel();
		credentialsLoader.cancel();
		super.onDestroy();
	}


	@Override
	public void onBackPressed() {
		if (changed) {
			confirmDialog.show(ConfirmDialog.PROFILE_EDITOR_LEAVE);
		} else {
			super.onBackPressed();
		}
	}


	@Override
	public boolean onCreateOptionsMenu(@NonNull Menu m) {
		getMenuInflater().inflate(R.menu.edit, m);
		AppStyles.setMenuIconColor(m, settings.getIconColor());
		return super.onCreateOptionsMenu(m);
	}


	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if (item.getItemId() == R.id.action_save) {
			updateUser();
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
			}
			// Add image as banner image
			else if (resultType == REQUEST_BANNER) {
				userUpdate.setBannerImage(mediaStatus);
				int widthPixels = Resources.getSystem().getDisplayMetrics().widthPixels;
				picasso.load(uri).resize(widthPixels, widthPixels / 3).centerCrop(Gravity.TOP).into(profile_banner, this);
				addBannerBtn.setVisibility(View.INVISIBLE);
				changeBannerBtn.setVisibility(View.VISIBLE);
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
			prefDialog.show();
		}
	}


	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}


	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}


	@Override
	public void afterTextChanged(Editable s) {
		changed = true;
	}


	@Override
	public void onConfirm(int type, boolean remember) {
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
			String username = this.username.getText().toString();
			String userLoc = profileLocation.getText().toString();
			String userBio = userDescription.getText().toString();
			if (username.trim().isEmpty()) {
				String errMsg = getString(R.string.error_empty_name);
				this.username.setError(errMsg);
			} else {
				userUpdate.setProfile(username, userBio, userLoc);
				userUpdate.setPrivacy(privacy.isChecked());
				if (userUpdate.prepare(getContentResolver())) {
					credentialUpdater.execute(userUpdate, credentialsUpdateCallback);
					progressDialog.show();
				} else {
					Toast.makeText(getApplicationContext(), R.string.error_media_init, Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	/**
	 * Set current user's information
	 */
	private void setUser() {
		if (user != null) {
			String profileImageUrl = user.getProfileImageThumbnailUrl();
			String bannerImageUrl = user.getBannerImageThumbnailUrl();
			if (!profileImageUrl.isEmpty()) {
				Transformation roundCorner = new RoundedCornersTransformation(5, 0);
				picasso.load(profileImageUrl).transform(roundCorner).into(profile_image);
			}
			if (!bannerImageUrl.isEmpty()) {
				picasso.load(bannerImageUrl).into(profile_banner, this);
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
		}
	}

	/**
	 * set current user's credentials
	 */
	private void setCredentials() {
		if (credentials != null) {
			privacy.setChecked(credentials.isLocked());
			userUpdate.setPrivacy(credentials.isLocked());
			userUpdate.setLanguageCode(credentials.getLanguage());
			userUpdate.setStatusVisibility(credentials.getVisibility());
			userUpdate.setContentSensitive(credentials.isSensitive());
		}
	}

	/**
	 *
	 */
	private void onCredentialsLoaderResult(CredentialsLoader.Result result) {
		if (result.credentials != null) {
			credentials = result.credentials;
			setCredentials();
		} else if (result.exception != null) {
			String message = ErrorUtils.getErrorMessage(this, result.exception);
			confirmDialog.show(ConfirmDialog.PROFILE_EDITOR_ERROR, message);
		}
		progressDialog.dismiss();
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
			confirmDialog.show(ConfirmDialog.PROFILE_EDITOR_ERROR, message);
		}
		progressDialog.dismiss();
	}
}