package org.nuclearfog.twidda.ui.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.BlurMaskFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.NotificationAction;
import org.nuclearfog.twidda.backend.async.PollAction;
import org.nuclearfog.twidda.backend.async.StatusAction;
import org.nuclearfog.twidda.backend.async.TextEmojiLoader;
import org.nuclearfog.twidda.backend.async.TranslationLoader;
import org.nuclearfog.twidda.backend.image.PicassoBuilder;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.EmojiUtils;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.backend.utils.LinkAndScrollMovement;
import org.nuclearfog.twidda.backend.utils.LinkUtils;
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.backend.utils.Tagger;
import org.nuclearfog.twidda.backend.utils.Tagger.OnTagClickListener;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Card;
import org.nuclearfog.twidda.model.Location;
import org.nuclearfog.twidda.model.Media;
import org.nuclearfog.twidda.model.Notification;
import org.nuclearfog.twidda.model.Poll;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.ui.adapter.recyclerview.PreviewAdapter;
import org.nuclearfog.twidda.ui.adapter.recyclerview.PreviewAdapter.OnCardClickListener;
import org.nuclearfog.twidda.ui.dialogs.AudioPlayerDialog;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.OnConfirmListener;
import org.nuclearfog.twidda.ui.dialogs.ReportDialog;
import org.nuclearfog.twidda.ui.fragments.StatusFragment;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Locale;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * Status Activity to show status and user information
 *
 * @author nuclearfog
 */
public class StatusActivity extends AppCompatActivity implements OnClickListener, OnLongClickListener, OnTagClickListener, OnConfirmListener,
		OnCardClickListener, ActivityResultCallback<ActivityResult> {

	/**
	 * Activity result code to update existing status information
	 */
	public static final int RETURN_STATUS_UPDATE = 0x789CD38B;

	/**
	 * Activity result code if a status was not found or removed
	 */
	public static final int RETURN_STATUS_REMOVED = 0x8B03DB84;

	/**
	 * Activity return code to update a notification
	 */
	public static final int RETURN_NOTIFICATION_UPDATE = 0x30BC261D;

	/**
	 * Activity return code if a notification was not found
	 */
	public static final int RETURN_NOTIFICATION_REMOVED = 0x99BB4149;

	/**
	 * key used for status information
	 * value type is {@link Status}
	 * If no status object exists, {@link #KEY_STATUS_ID} and {@link #KEY_NAME} will be used instead
	 */
	public static final String KEY_DATA = "status_data";

	/**
	 * key for the status author's name. alternative to {@link #KEY_DATA}
	 * value type is String
	 */
	public static final String KEY_NAME = "status_author";

	/**
	 * key for the status ID value, alternative to {@link #KEY_DATA}
	 * value type is Long
	 */
	public static final String KEY_STATUS_ID = "status_id";

	/**
	 * key for the notification ID value
	 * value type is long
	 */
	public static final String KEY_NOTIFICATION_ID = "notification_id";

	/**
	 * toolbar menu group ID for copy options
	 */
	private static final int MENU_GROUP_COPY = 0x157426;

	/**
	 * color of the profile image placeholder
	 */
	private static final int IMAGE_PLACEHOLDER_COLOR = 0x2F000000;

	private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this);
	private AsyncCallback<StatusAction.Result> statusCallback = this::onStatusResult;
	private AsyncCallback<PollAction.Result> pollResult = this::onPollResult;
	private AsyncCallback<TranslationLoader.Result> translationResult = this::onTranslationResult;
	private AsyncCallback<NotificationAction.Result> notificationCallback = this::onNotificationResult;
	private AsyncCallback<TextEmojiLoader.Result> statusTextUpdate = this::onStatusTextUpdate;
	private AsyncCallback<TextEmojiLoader.Result> usernameUpdate = this::onUsernameUpdate;

	private StatusAction statusLoader;
	private NotificationAction notificationLoader;
	private TranslationLoader translationLoader;
	private PollAction pollLoader;
	private TextEmojiLoader emojiLoader;

	@Nullable
	private ClipboardManager clip;
	private GlobalSettings settings;
	private Picasso picasso;
	private PreviewAdapter adapter;

	private TextView status_source, created_at, status_text, screen_name, username, edited;
	private TextView location_name, sensitive, visibility, spoiler, spoiler_hint, translate_text;
	private Button reply_button, repost_button, like_button, reply_name, repost_name_button;
	private View verifiedIcon, lockedIcon, groupIcon, botIcon;
	private ImageView profile_image;
	private Toolbar toolbar;
	private RecyclerView card_list;

	@Nullable
	private Status status;
	@Nullable
	private Notification notification;
	private boolean hidden, translated;


	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(AppStyles.setFontScale(newBase));
	}


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.page_status);
		ViewGroup root = findViewById(R.id.page_status_root);
		card_list = findViewById(R.id.page_status_cards);
		toolbar = findViewById(R.id.page_status_toolbar);
		reply_button = findViewById(R.id.page_status_reply);
		repost_button = findViewById(R.id.page_status_repost);
		like_button = findViewById(R.id.page_status_favorite);
		username = findViewById(R.id.page_status_username);
		screen_name = findViewById(R.id.page_status_screenname);
		profile_image = findViewById(R.id.page_status_profile);
		reply_name = findViewById(R.id.page_status_reply_reference);
		status_text = findViewById(R.id.page_status_text);
		created_at = findViewById(R.id.page_status_date);
		status_source = findViewById(R.id.page_status_api);
		location_name = findViewById(R.id.page_status_location_name);
		sensitive = findViewById(R.id.page_status_sensitive);
		spoiler = findViewById(R.id.page_status_spoiler);
		visibility = findViewById(R.id.page_status_visibility);
		edited = findViewById(R.id.page_status_edited);
		repost_name_button = findViewById(R.id.page_status_reposter_reference);
		translate_text = findViewById(R.id.page_status_text_translate);
		spoiler_hint = findViewById(R.id.page_status_text_sensitive_hint);
		verifiedIcon = findViewById(R.id.page_status_verified);
		lockedIcon = findViewById(R.id.page_status_private);
		groupIcon = findViewById(R.id.page_status_group);
		botIcon = findViewById(R.id.page_status_bot);

		clip = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		statusLoader = new StatusAction(this);
		pollLoader = new PollAction(this);
		notificationLoader = new NotificationAction(this);
		translationLoader = new TranslationLoader(this);
		emojiLoader = new TextEmojiLoader(this);
		picasso = PicassoBuilder.get(this);
		settings = GlobalSettings.get(this);
		adapter = new PreviewAdapter(this);

		reply_button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.answer, 0, 0, 0);
		repost_button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.repost, 0, 0, 0);
		location_name.setCompoundDrawablesWithIntrinsicBounds(R.drawable.location, 0, 0, 0);
		sensitive.setCompoundDrawablesWithIntrinsicBounds(R.drawable.sensitive, 0, 0, 0);
		spoiler.setCompoundDrawablesWithIntrinsicBounds(R.drawable.exclamation, 0, 0, 0);
		visibility.setCompoundDrawablesWithIntrinsicBounds(R.drawable.global, 0, 0, 0);
		reply_name.setCompoundDrawablesWithIntrinsicBounds(R.drawable.back, 0, 0, 0);
		repost_name_button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.repost, 0, 0, 0);
		edited.setCompoundDrawablesWithIntrinsicBounds(R.drawable.edit, 0, 0, 0);
		status_text.setMovementMethod(LinkAndScrollMovement.getInstance());
		status_text.setLinkTextColor(settings.getHighlightColor());
		card_list.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
		card_list.setAdapter(adapter);
		if (settings.likeEnabled()) {
			like_button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.like, 0, 0, 0);
		} else {
			like_button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.favorite, 0, 0, 0);
		}
		toolbar.setTitle("");
		setSupportActionBar(toolbar);
		AppStyles.setTheme(root);
		location_name.setTextColor(settings.getHighlightColor());
		translate_text.setTextColor(settings.getHighlightColor());

		// get parameters
		if (savedInstanceState == null) {
			savedInstanceState = getIntent().getExtras();
		}
		if (savedInstanceState != null) {
			// get data
			String replyUsername = "";
			Serializable serialized = savedInstanceState.getSerializable(KEY_DATA);
			long statusId = savedInstanceState.getLong(KEY_STATUS_ID, 0L);
			long notificationId = savedInstanceState.getLong(KEY_NOTIFICATION_ID, 0L);

			// set status data
			if (serialized instanceof Status) {
				Status status = (Status) serialized;
				Status embeddedStatus = status.getEmbeddedStatus();
				setStatus(status);
				StatusAction.Param param = new StatusAction.Param(StatusAction.Param.ONLINE, status.getId());
				statusLoader.execute(param, statusCallback);
				if (embeddedStatus != null) {
					statusId = embeddedStatus.getId();
					replyUsername = embeddedStatus.getAuthor().getScreenname();
					hidden = embeddedStatus.isHidden();
				} else {
					statusId = status.getId();
					replyUsername = status.getAuthor().getScreenname();
					hidden = status.isHidden();
				}
			}
			// set notification data
			else if (serialized instanceof Notification) {
				Notification notification = (Notification) serialized;
				NotificationAction.Param notificationParam = new NotificationAction.Param(NotificationAction.Param.LOAD_ONLINE, notification.getId());
				notificationLoader.execute(notificationParam, notificationCallback);
				if (notification.getStatus() != null) {
					setNotification(notification);
					statusId = notification.getStatus().getId();
					replyUsername = notification.getStatus().getAuthor().getScreenname();
				}
			}
			// get status data using status ID
			else if (statusId != 0L) {
				replyUsername = savedInstanceState.getString(KEY_NAME);
				StatusAction.Param param = new StatusAction.Param(StatusAction.Param.DATABASE, statusId);
				statusLoader.execute(param, statusCallback);
			}
			// get notification data using notification ID
			else if (notificationId != 0L) {
				replyUsername = savedInstanceState.getString(KEY_NAME);
				NotificationAction.Param notificationParam = new NotificationAction.Param(NotificationAction.Param.LOAD_ONLINE, notificationId);
				notificationLoader.execute(notificationParam, notificationCallback);
			}
			String tag = replyUsername + ":" + statusId;
			if (getSupportFragmentManager().findFragmentByTag(tag) == null) {
				// initialize status reply list
				Bundle param = new Bundle();
				param.putInt(StatusFragment.KEY_MODE, StatusFragment.MODE_REPLY);
				param.putString(StatusFragment.KEY_SEARCH, replyUsername);
				param.putLong(StatusFragment.KEY_ID, statusId);
				FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
				fragmentTransaction.replace(R.id.page_status_reply_fragment, StatusFragment.class, param, tag);
				fragmentTransaction.commit();
			}
		}

		repost_name_button.setOnClickListener(this);
		reply_name.setOnClickListener(this);
		translate_text.setOnClickListener(this);
		reply_button.setOnClickListener(this);
		repost_button.setOnClickListener(this);
		like_button.setOnClickListener(this);
		profile_image.setOnClickListener(this);
		location_name.setOnClickListener(this);
		repost_button.setOnLongClickListener(this);
		like_button.setOnLongClickListener(this);
		repost_name_button.setOnLongClickListener(this);
		location_name.setOnLongClickListener(this);
		status_text.setOnClickListener(this);
	}


	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putSerializable(KEY_DATA, status);
		super.onSaveInstanceState(outState);
	}


	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
		if (notification != null) {
			intent.putExtra(KEY_DATA, notification);
			setResult(RETURN_NOTIFICATION_UPDATE, intent);
		} else {
			intent.putExtra(KEY_DATA, status);
			setResult(RETURN_STATUS_UPDATE, intent);
		}
		super.onBackPressed();
	}


	@Override
	protected void onDestroy() {
		statusLoader.cancel();
		pollLoader.cancel();
		notificationLoader.cancel();
		translationLoader.cancel();
		emojiLoader.cancel();
		super.onDestroy();
	}


	@Override
	public boolean onCreateOptionsMenu(@NonNull Menu m) {
		getMenuInflater().inflate(R.menu.status, m);
		AppStyles.setOverflowIcon(toolbar, settings.getIconColor());
		return true;
	}


	@Override
	public boolean onPrepareOptionsMenu(@NonNull Menu m) {
		MenuItem optDelete = m.findItem(R.id.menu_status_delete);
		MenuItem optHide = m.findItem(R.id.menu_status_hide);
		MenuItem optCopy = m.findItem(R.id.menu_status_copy);
		MenuItem optReport = m.findItem(R.id.menu_status_report);
		MenuItem optPin = m.findItem(R.id.menu_status_pin);
		MenuItem menuBookmark = m.findItem(R.id.menu_status_bookmark);
		MenuItem editStatus = m.findItem(R.id.menu_status_edit);
		MenuItem editHistory = m.findItem(R.id.menu_status_history);
		SubMenu copyMenu = optCopy.getSubMenu();
		// set status options
		if (status != null) {
			Status currentStatus = status;
			if (currentStatus.getEmbeddedStatus() != null) {
				currentStatus = currentStatus.getEmbeddedStatus();
			}
			if (currentStatus.getAuthor().getId() == settings.getLogin().getId()) {
				optPin.setTitle(status.isPinned() ? R.string.menu_status_unpin : R.string.menu_status_pin);
				optPin.setVisible(true);
			} else if (currentStatus.getRepliedUserId() == settings.getLogin().getId()) {
				optHide.setTitle(hidden ? R.string.menu_status_unhide : R.string.menu_status_hide);
				optHide.setVisible(true);
			}
			// enable/disable bookmark
			if (currentStatus.isBookmarked()) {
				menuBookmark.setTitle(R.string.menu_bookmark_remove);
			} else {
				menuBookmark.setTitle(R.string.menu_bookmark_add);
			}
			// enable/disable status hide option
			if (currentStatus.getAuthor().isCurrentUser()) {
				optDelete.setVisible(true);
				if (settings.getLogin().getConfiguration().isStatusEditSupported()) {
					editStatus.setVisible(true);
				}
			} else {
				optReport.setVisible(true);
			}
			if (currentStatus.editedAt() != 0L) {
				editHistory.setVisible(true);
			}
			// add media link items
			// check if menu doesn't contain media links already
			if (copyMenu.size() == 2) {
				for (int i = 0; i < currentStatus.getMedia().length; i++) {
					// create sub menu entry and use array index as item ID
					String text = getString(R.string.menu_media_link) + ' ' + (i + 1);
					copyMenu.add(MENU_GROUP_COPY, i, Menu.NONE, text);
				}
			}
			return true;
		}
		return false;
	}


	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		Status currentStatus = status;
		if (currentStatus != null && currentStatus.getEmbeddedStatus() != null)
			currentStatus = currentStatus.getEmbeddedStatus();
		// add/remove bookmark
		if (item.getItemId() == R.id.menu_status_bookmark) {
			if (currentStatus != null && statusLoader.isIdle()) {
				Toast.makeText(getApplicationContext(), R.string.info_loading, Toast.LENGTH_SHORT).show();
				int mode = currentStatus.isBookmarked() ? StatusAction.Param.UNBOOKMARK : StatusAction.Param.BOOKMARK;
				StatusAction.Param param = new StatusAction.Param(mode, currentStatus.getId());
				statusLoader.execute(param, statusCallback);
			}
			return true;
		}
		// hide status
		else if (item.getItemId() == R.id.menu_status_hide) {
			if (currentStatus != null && statusLoader.isIdle()) {
				Toast.makeText(getApplicationContext(), R.string.info_loading, Toast.LENGTH_SHORT).show();
				int mode = hidden ? StatusAction.Param.UNHIDE : StatusAction.Param.HIDE;
				StatusAction.Param param = new StatusAction.Param(mode, currentStatus.getId());
				statusLoader.execute(param, statusCallback);
			}
			return true;
		}
		// pin/unpin status
		else if (item.getItemId() == R.id.menu_status_pin) {
			if (currentStatus != null && statusLoader.isIdle()) {
				Toast.makeText(getApplicationContext(), R.string.info_loading, Toast.LENGTH_SHORT).show();
				int mode = currentStatus.isPinned() ? StatusAction.Param.UNPIN : StatusAction.Param.PIN;
				StatusAction.Param param = new StatusAction.Param(mode, currentStatus.getId());
				statusLoader.execute(param, statusCallback);
			}
			return true;
		}
		// get status link
		else if (item.getItemId() == R.id.menu_status_browser) {
			if (currentStatus != null && !currentStatus.getUrl().isEmpty()) {
				LinkUtils.redirectToBrowser(this, currentStatus.getUrl());
			}
			return true;
		}
		// copy status link to clipboard
		else if (item.getItemId() == R.id.menu_status_copy_text) {
			if (currentStatus != null && clip != null) {
				ClipData linkClip = ClipData.newPlainText("status text", currentStatus.getText());
				clip.setPrimaryClip(linkClip);
				Toast.makeText(getApplicationContext(), R.string.info_status_text_copied, Toast.LENGTH_SHORT).show();
			}
			return true;
		}
		// copy status link to clipboard
		else if (item.getItemId() == R.id.menu_status_copy_link) {
			if (currentStatus != null && clip != null) {
				ClipData linkClip = ClipData.newPlainText("status link", currentStatus.getUrl());
				clip.setPrimaryClip(linkClip);
				Toast.makeText(getApplicationContext(), R.string.info_status_link_copied, Toast.LENGTH_SHORT).show();
			}
			return true;
		}
		// copy media links
		else if (item.getGroupId() == MENU_GROUP_COPY) {
			if (currentStatus != null) {
				int index = item.getItemId();
				Media[] medias = currentStatus.getMedia();
				if (index >= 0 && index < medias.length) {
					if (clip != null) {
						ClipData linkClip = ClipData.newPlainText("status media link", medias[index].getUrl());
						clip.setPrimaryClip(linkClip);
						Toast.makeText(getApplicationContext(), R.string.info_status_medialink_copied, Toast.LENGTH_SHORT).show();
					}
				}
			}
			return true;
		}
		// edit status
		else if (item.getItemId() == R.id.menu_status_edit) {
			if (currentStatus != null) {
				Intent intent = new Intent(this, StatusEditor.class);
				intent.putExtra(StatusEditor.KEY_EDIT_DATA, currentStatus);
				activityResultLauncher.launch(intent);
			}
			return true;
		}
		// report status
		else if (item.getItemId() == R.id.menu_status_report) {
			if (currentStatus != null) {
				ReportDialog.show(this, currentStatus.getAuthor().getId(), currentStatus.getId());
			}
			return true;
		}
		// get edit history
		else if (item.getItemId() == R.id.menu_status_history) {
			if (currentStatus != null) {
				Intent intent = new Intent(this, EditHistoryActivity.class);
				intent.putExtra(EditHistoryActivity.KEY_ID, currentStatus.getId());
				startActivity(intent);
			}
			return true;
		}
		// Delete status option
		else if (item.getItemId() == R.id.menu_status_delete) {
			ConfirmDialog.show(this, ConfirmDialog.DELETE_STATUS, null);
			return true;
		}
		return false;
	}


	@Override
	public void onActivityResult(ActivityResult result) {
		if (result.getData() != null) {
			if (result.getResultCode() == StatusEditor.RETURN_STATUS_UPDATE) {
				Serializable data = result.getData().getSerializableExtra(StatusEditor.KEY_REPLY_DATA);
				if (data instanceof Status) {
					setStatus((Status) data);
				}
			}
		}
	}


	@Override
	public void onClick(View v) {
		if (status != null) {
			Status status = this.status;
			if (status.getEmbeddedStatus() != null)
				status = status.getEmbeddedStatus();
			// answer to the status
			if (v.getId() == R.id.page_status_reply) {
				Intent intent = new Intent(this, StatusEditor.class);
				intent.putExtra(StatusEditor.KEY_REPLY_DATA, status);
				startActivity(intent);
			}
			// show user reposting this status
			else if (v.getId() == R.id.page_status_repost) {
				Intent intent = new Intent(this, UsersActivity.class);
				intent.putExtra(UsersActivity.KEY_ID, status.getId());
				intent.putExtra(UsersActivity.KEY_MODE, UsersActivity.USERS_REPOST);
				startActivity(intent);
			}
			// show user favoriting this status
			else if (v.getId() == R.id.page_status_favorite) {
				Intent intent = new Intent(this, UsersActivity.class);
				intent.putExtra(UsersActivity.KEY_ID, status.getId());
				intent.putExtra(UsersActivity.KEY_MODE, UsersActivity.USERS_FAVORIT);
				startActivity(intent);
			}
			// open profile of the status author
			else if (v.getId() == R.id.page_status_profile) {
				Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
				intent.putExtra(ProfileActivity.KEY_USER, status.getAuthor());
				startActivity(intent);
			}
			// open replied status
			else if (v.getId() == R.id.page_status_reply_reference) {
				Intent intent = new Intent(getApplicationContext(), StatusActivity.class);
				intent.putExtra(KEY_STATUS_ID, status.getRepliedStatusId());
				intent.putExtra(KEY_NAME, status.getReplyName());
				startActivity(intent);
			}
			// open status location coordinates
			else if (v.getId() == R.id.page_status_location_name) {
				Location location;
				if (status.getEmbeddedStatus() != null) {
					location = status.getEmbeddedStatus().getLocation();
				} else {
					location = status.getLocation();
				}
				if (location != null && !location.getCoordinates().trim().isEmpty()) {
					LinkUtils.openCoordinates(this, location.getCoordinates());
				}
			}
			// go to user reposting this status
			else if (v.getId() == R.id.page_status_reposter_reference) {
				Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
				intent.putExtra(ProfileActivity.KEY_USER, this.status.getAuthor());
				startActivity(intent);
			}
			// unblur text on click
			else if (v.getId() == R.id.page_status_text) {
				// remove blur if any
				if (status_text.getPaint().getMaskFilter() != null) {
					status_text.getPaint().setMaskFilter(null);
					spoiler_hint.setVisibility(View.INVISIBLE);
				}
			}
			// translate status text
			else if (v.getId() == R.id.page_status_text_translate) {
				if (translated) {
					Spannable spannableText = Tagger.makeTextWithLinks(status.getText(), settings.getHighlightColor(), this);
					translate_text.setText(R.string.status_translate_text);
					status_text.setText(spannableText);
					translated = false;
				} else if (translationLoader.isIdle()) {
					TranslationLoader.Param param = new TranslationLoader.Param(status.getId());
					translationLoader.execute(param, translationResult);
				}
			}
		}
	}


	@Override
	public boolean onLongClick(View v) {
		if (status != null && statusLoader.isIdle()) {
			// repost this status
			if (v.getId() == R.id.page_status_repost) {
				Toast.makeText(getApplicationContext(), R.string.info_loading, Toast.LENGTH_SHORT).show();
				int mode = status.isReposted() ? StatusAction.Param.UNREPOST : StatusAction.Param.REPOST;
				StatusAction.Param param = new StatusAction.Param(mode, status.getId());
				statusLoader.execute(param, statusCallback);
				return true;
			}
			// favorite this status
			else if (v.getId() == R.id.page_status_favorite) {
				Toast.makeText(getApplicationContext(), R.string.info_loading, Toast.LENGTH_SHORT).show();
				int mode = status.isFavorited() ? StatusAction.Param.UNFAVORITE : StatusAction.Param.FAVORITE;
				StatusAction.Param param = new StatusAction.Param(mode, status.getId());
				statusLoader.execute(param, statusCallback);
				return true;
			}
			// go to original status
			else if (v.getId() == R.id.page_status_reposter_reference) {
				Status embeddedStatus = status.getEmbeddedStatus();
				if (embeddedStatus != null) {
					Intent intent = new Intent(this, StatusActivity.class);
					intent.putExtra(KEY_DATA, embeddedStatus);
					startActivity(intent);
				}
				return true;
			}
			// copy status coordinates
			else if (v.getId() == R.id.page_status_location_name) {
				Location location;
				if (status.getEmbeddedStatus() != null) {
					location = status.getEmbeddedStatus().getLocation();
				} else {
					location = status.getLocation();
				}
				if (clip != null && location != null) {
					ClipData linkClip = ClipData.newPlainText("Status location coordinates", location.getCoordinates());
					clip.setPrimaryClip(linkClip);
					Toast.makeText(getApplicationContext(), R.string.info_status_location_copied, Toast.LENGTH_SHORT).show();
				}
				return true;
			}
		}
		return false;
	}


	@Override
	public void onConfirm(int type) {
		if (type == ConfirmDialog.DELETE_STATUS) {
			if (status != null) {
				long id = status.getId();
				if (status.getEmbeddedStatus() != null) {
					id = status.getEmbeddedStatus().getId();
				}
				StatusAction.Param param = new StatusAction.Param(StatusAction.Param.DELETE, id);
				statusLoader.execute(param, statusCallback);
			}
		}
	}


	@Override
	public void onCardClick(Card card, int type) {
		if (type == OnCardClickListener.TYPE_LINK) {
			if (!card.getUrl().isEmpty()) {
				LinkUtils.redirectToBrowser(this, card.getUrl());
			}
		} else if (type == OnCardClickListener.TYPE_IMAGE) {
			String imageUrl = card.getImageUrl();
			if (!imageUrl.isEmpty()) {
				Intent intent = new Intent(this, ImageViewer.class);
				intent.putExtra(ImageViewer.KEY_IMAGE_DATA, card.getImageUrl());
				startActivity(intent);
			}
		}
	}


	@Override
	public void onMediaClick(Media media) {
		Uri uri = Uri.parse(media.getUrl());
		switch (media.getMediaType()) {
			case Media.PHOTO:
				Intent intent = new Intent(this, ImageViewer.class);
				intent.putExtra(ImageViewer.KEY_IMAGE_DATA, media);
				startActivity(intent);
				break;

			case Media.AUDIO:
				AudioPlayerDialog.show(this, uri);
				break;

			case Media.GIF:
			case Media.VIDEO:
				intent = new Intent(this, VideoViewer.class);
				intent.putExtra(VideoViewer.KEY_VIDEO_DATA, media);
				startActivity(intent);
				break;
		}
	}


	@Override
	public void onTagClick(String tag) {
		// proceed click when there is no text blur
		if (status_text.getPaint().getMaskFilter() == null) {
			Intent intent = new Intent(this, SearchActivity.class);
			intent.putExtra(SearchActivity.KEY_QUERY, tag);
			startActivity(intent);
		}
	}

	/**
	 * called when a link is clicked
	 *
	 * @param tag link string
	 */
	@Override
	public void onLinkClick(String tag) {
		// proceed click when there is no text blur
		if (status_text.getPaint().getMaskFilter() == null) {
			LinkUtils.redirectToBrowser(this, tag);
		}
	}


	@Override
	public void onVoteClick(Poll poll, int[] selection) {
		if (pollLoader.isIdle()) {
			PollAction.Param param = new PollAction.Param(PollAction.Param.VOTE, poll.getId(), selection);
			pollLoader.execute(param, pollResult);
		}
	}

	/**
	 * load status into UI
	 *
	 * @param status status information
	 */
	private void setStatus(@NonNull Status status) {
		this.status = status;
		translated = false;
		Drawable placeholder = new ColorDrawable(IMAGE_PLACEHOLDER_COLOR);
		if (status.getEmbeddedStatus() != null) {
			repost_name_button.setVisibility(View.VISIBLE);
			repost_name_button.setText(status.getAuthor().getScreenname());
			status = status.getEmbeddedStatus();
		} else {
			repost_name_button.setVisibility(View.GONE);
		}
		User author = status.getAuthor();
		Location location = status.getLocation();
		invalidateOptionsMenu();

		repost_button.setVisibility(View.VISIBLE);
		like_button.setVisibility(View.VISIBLE);
		reply_button.setVisibility(View.VISIBLE);
		screen_name.setText(author.getScreenname());
		created_at.setText(SimpleDateFormat.getDateTimeInstance().format(status.getTimestamp()));
		reply_button.setText(StringUtils.NUMBER_FORMAT.format(status.getReplyCount()));
		like_button.setText(StringUtils.NUMBER_FORMAT.format(status.getFavoriteCount()));
		repost_button.setText(StringUtils.NUMBER_FORMAT.format(status.getRepostCount()));
		// set repost icon
		if (status.isReposted()) {
			AppStyles.setDrawableColor(repost_button, settings.getRepostIconColor());
		} else {
			AppStyles.setDrawableColor(repost_button, settings.getIconColor());
		}
		// set favorite/like icon
		if (status.isFavorited()) {
			AppStyles.setDrawableColor(like_button, settings.getFavoriteIconColor());
		} else {
			AppStyles.setDrawableColor(like_button, settings.getIconColor());
		}
		// set user icons
		if (author.isVerified()) {
			verifiedIcon.setVisibility(View.VISIBLE);
		} else {
			verifiedIcon.setVisibility(View.GONE);
		}
		if (author.isProtected()) {
			lockedIcon.setVisibility(View.VISIBLE);
		} else {
			lockedIcon.setVisibility(View.GONE);
		}
		if (author.isGroup()) {
			groupIcon.setVisibility(View.VISIBLE);
		} else {
			groupIcon.setVisibility(View.GONE);
		}
		if (author.isBot()) {
			botIcon.setVisibility(View.VISIBLE);
		} else {
			botIcon.setVisibility(View.GONE);
		}
		// add 'translate' label
		if (!status.getText().isEmpty() && !status.getLanguage().isEmpty() && !status.getLanguage().equals(Locale.getDefault().getLanguage())) {
			translate_text.setVisibility(View.VISIBLE);
		} else {
			translate_text.setVisibility(View.GONE);
		}
		// set username
		if (status.getAuthor().getEmojis().length > 0) {
			Spannable usernameSpan = new SpannableString(author.getUsername());
			if (settings.imagesEnabled()) {
				TextEmojiLoader.Param param = new TextEmojiLoader.Param(author.getEmojis(), usernameSpan, getResources().getDimensionPixelSize(R.dimen.page_status_icon_size));
				emojiLoader.execute(param, usernameUpdate);
				usernameSpan = EmojiUtils.removeTags(usernameSpan);
			}
			username.setText(usernameSpan);
		} else {
			username.setText(author.getUsername());
		}
		// set status source
		if (!status.getSource().isEmpty()) {
			status_source.setText(R.string.status_sent_from);
			status_source.append(status.getSource());
			status_source.setVisibility(View.VISIBLE);
		} else {
			status_source.setVisibility(View.GONE);
		}
		// set status text
		if (!status.getText().isEmpty()) {
			Spannable spannableText = Tagger.makeTextWithLinks(status.getText(), settings.getHighlightColor(), this);
			if (status.getEmojis().length > 0 && settings.imagesEnabled()) {
				TextEmojiLoader.Param param = new TextEmojiLoader.Param(status.getEmojis(), spannableText, getResources().getDimensionPixelSize(R.dimen.page_status_icon_size));
				emojiLoader.execute(param, statusTextUpdate);
				spannableText = EmojiUtils.removeTags(spannableText);
			}
			status_text.setVisibility(View.VISIBLE);
			status_text.setText(spannableText);
		} else {
			status_text.setVisibility(View.GONE);
		}
		// setup button to replied status
		if (status.getRepliedStatusId() > 0) {
			if (!status.getReplyName().isEmpty()) {
				reply_name.setText(status.getReplyName());
			} else {
				reply_name.setText(R.string.status_replyname_empty);
			}
			reply_name.setVisibility(View.VISIBLE);
		} else {
			reply_name.setVisibility(View.GONE);
		}
		// set status sensible warining
		if (status.isSensitive()) {
			sensitive.setVisibility(View.VISIBLE);
		} else {
			sensitive.setVisibility(View.GONE);
		}
		if (status.editedAt() != 0L) {
			edited.setVisibility(View.VISIBLE);
		} else {
			edited.setVisibility(View.GONE);
		}
		// set status spoiler warning
		if (status.isSpoiler()) {
			spoiler.setVisibility(View.VISIBLE);
			if (settings.hideSensitiveEnabled()) {
				spoiler_hint.setVisibility(View.VISIBLE);
				status_text.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
				float radius = status_text.getTextSize() / 3;
				BlurMaskFilter filter = new BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL);
				status_text.getPaint().setMaskFilter(filter);
			} else {
				spoiler_hint.setVisibility(View.INVISIBLE);
			}
		} else {
			spoiler.setVisibility(View.GONE);
			spoiler_hint.setVisibility(View.INVISIBLE);
		}
		if (status.getVisibility() == Status.VISIBLE_UNLISTED) {
			visibility.setText(R.string.status_visibility_unlisted);
			visibility.setVisibility(View.VISIBLE);
		} else if (status.getVisibility() == Status.VISIBLE_PRIVATE) {
			visibility.setText(R.string.status_visibility_private);
			visibility.setVisibility(View.VISIBLE);
		} else if (status.getVisibility() == Status.VISIBLE_DIRECT) {
			visibility.setText(R.string.status_visibility_direct);
			visibility.setVisibility(View.VISIBLE);
		} else {
			visibility.setVisibility(View.GONE);
		}
		// set profile image url
		if (settings.imagesEnabled() && !author.getProfileImageThumbnailUrl().isEmpty()) {
			Transformation roundCorner = new RoundedCornersTransformation(4, 0);
			picasso.load(author.getProfileImageThumbnailUrl()).transform(roundCorner).placeholder(placeholder).error(R.drawable.no_image).into(profile_image);
		} else {
			profile_image.setImageDrawable(placeholder);
		}
		// set location information
		if (location != null) {
			location_name.setVisibility(View.VISIBLE);
			if (!location.getPlace().isEmpty()) {
				location_name.setText(location.getFullName());
			} else {
				location_name.setText("");
			}
			if (!location.getCoordinates().isEmpty()) {
				location_name.append(" " + location.getCoordinates());
			}
		} else {
			location_name.setVisibility(View.GONE);
		}
		// set status attachment preview
		if ((status.getCards().length > 0 || status.getMedia().length > 0) || status.getPoll() != null) {
			card_list.setVisibility(View.VISIBLE);
			adapter.replaceAll(status, settings.hideSensitiveEnabled());
			if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
				status_text.setMaxLines(5);
			}
		} else {
			card_list.setVisibility(View.GONE);
			if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
				status_text.setMaxLines(10);
			}
		}
	}

	/**
	 * set notification containing a status
	 *
	 * @param notification notification with status
	 */
	private void setNotification(@NonNull Notification notification) {
		this.notification = notification;
		if (notification.getStatus() != null) {
			setStatus(notification.getStatus());
		}
	}

	/**
	 *
	 */
	private void onStatusResult(@NonNull StatusAction.Result result) {
		if (result.status != null) {
			setStatus(result.status);
		}
		switch (result.action) {
			case StatusAction.Result.DATABASE:
				if (result.status != null) {
					StatusAction.Param param = new StatusAction.Param(StatusAction.Param.ONLINE, result.status.getId());
					statusLoader.execute(param, statusCallback);
				}
				break;

			case StatusAction.Result.REPOST:
				Toast.makeText(getApplicationContext(), R.string.info_status_reposted, Toast.LENGTH_SHORT).show();
				break;

			case StatusAction.Result.UNREPOST:
				Toast.makeText(getApplicationContext(), R.string.info_status_unreposted, Toast.LENGTH_SHORT).show();
				break;

			case StatusAction.Result.FAVORITE:
				if (settings.likeEnabled())
					Toast.makeText(getApplicationContext(), R.string.info_status_liked, Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(getApplicationContext(), R.string.info_status_favored, Toast.LENGTH_SHORT).show();
				break;

			case StatusAction.Result.UNFAVORITE:
				if (settings.likeEnabled())
					Toast.makeText(getApplicationContext(), R.string.info_status_unliked, Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(getApplicationContext(), R.string.info_status_unfavored, Toast.LENGTH_SHORT).show();
				break;

			case StatusAction.Result.BOOKMARK:
				Toast.makeText(getApplicationContext(), R.string.info_status_bookmarked, Toast.LENGTH_SHORT).show();
				break;

			case StatusAction.Result.UNBOOKMARK:
				Toast.makeText(getApplicationContext(), R.string.info_status_unbookmarked, Toast.LENGTH_SHORT).show();
				break;

			case StatusAction.Result.HIDE:
				hidden = true;
				Toast.makeText(getApplicationContext(), R.string.info_reply_hidden, Toast.LENGTH_SHORT).show();
				break;

			case StatusAction.Result.UNHIDE:
				hidden = false;
				Toast.makeText(getApplicationContext(), R.string.info_reply_unhidden, Toast.LENGTH_SHORT).show();
				break;

			case StatusAction.Result.PIN:
				Toast.makeText(getApplicationContext(), R.string.info_status_pinned, Toast.LENGTH_SHORT).show();
				break;

			case StatusAction.Result.UNPIN:
				Toast.makeText(getApplicationContext(), R.string.info_status_unpinned, Toast.LENGTH_SHORT).show();
				break;

			case StatusAction.Result.DELETE:
				if (notification != null) {
					Toast.makeText(getApplicationContext(), R.string.info_status_removed, Toast.LENGTH_SHORT).show();
					Intent intent = new Intent();
					intent.putExtra(KEY_NOTIFICATION_ID, notification.getId());
					setResult(RETURN_NOTIFICATION_REMOVED, intent);
					finish();
				} else if (status != null) {
					Toast.makeText(getApplicationContext(), R.string.info_status_removed, Toast.LENGTH_SHORT).show();
					Intent intent = new Intent();
					intent.putExtra(KEY_STATUS_ID, status.getId());
					setResult(RETURN_STATUS_REMOVED, intent);
					finish();
				}
				break;

			case StatusAction.Result.ERROR:
				ErrorUtils.showErrorMessage(this, result.exception);
				if (status == null) {
					finish();
				} else if (result.exception != null && result.exception.getErrorCode() == ConnectionException.RESOURCE_NOT_FOUND) {
					// Mark status as removed, so it can be removed from the list
					Intent intent = new Intent();
					intent.putExtra(KEY_STATUS_ID, status.getId());
					setResult(RETURN_STATUS_REMOVED, intent);
					finish();
				}
				break;
		}
	}

	/**
	 * update notification
	 *
	 * @param result notification containing status information
	 */
	private void onNotificationResult(@NonNull NotificationAction.Result result) {
		switch (result.action) {
			case NotificationAction.Result.LOAD_LOCAL:
				if (result.notification != null) {
					NotificationAction.Param param = new NotificationAction.Param(NotificationAction.Param.LOAD_ONLINE, result.notification.getId());
					notificationLoader.execute(param, notificationCallback);
				}
				// fall through

			case NotificationAction.Result.LOAD_ONLINE:
				if (result.notification != null && result.notification.getStatus() != null) {
					notification = result.notification;
					setStatus(result.notification.getStatus());
				}
				break;

			case NotificationAction.Result.DISMISS:
				if (notification != null) {
					Intent intent = new Intent();
					intent.putExtra(KEY_NOTIFICATION_ID, notification.getId());
					setResult(RETURN_NOTIFICATION_REMOVED, intent);
				}
				Toast.makeText(getApplicationContext(), R.string.info_notification_dismiss, Toast.LENGTH_SHORT).show();
				finish();
				break;

			case NotificationAction.Result.ERROR:
				ErrorUtils.showErrorMessage(this, result.exception);
				if (notification == null) {
					finish();
				} else if (result.exception != null && result.exception.getErrorCode() == ConnectionException.RESOURCE_NOT_FOUND) {
					Intent intent = new Intent();
					intent.putExtra(KEY_NOTIFICATION_ID, notification.getId());
					setResult(RETURN_NOTIFICATION_REMOVED, intent);
					finish();
				}
				break;
		}
	}

	/**
	 * set poll result
	 *
	 * @param result poll result
	 */
	private void onPollResult(@NonNull PollAction.Result result) {
		switch (result.action) {
			case PollAction.Result.LOAD:
				if (result.poll != null) {
					adapter.updatePoll(result.poll);
				}
				break;

			case PollAction.Result.VOTE:
				if (result.poll != null) {
					adapter.updatePoll(result.poll);
					Toast.makeText(getApplicationContext(), R.string.info_poll_voted, Toast.LENGTH_SHORT).show();
				}
				break;

			case PollAction.Result.ERROR:
				ErrorUtils.showErrorMessage(this, result.exception);
				break;
		}
	}

	/**
	 * @param result status translation result
	 */
	private void onTranslationResult(@NonNull TranslationLoader.Result result) {
		if (result.translation != null) {
			Spannable textSpan = Tagger.makeTextWithLinks(result.translation.getText(), settings.getHighlightColor(), this);
			// append translation
			status_text.setText(textSpan);
			translate_text.setText(R.string.status_translate_source);
			translate_text.append(result.translation.getSource() + ", ");
			translate_text.append(getString(R.string.status_translate_source_language));
			translate_text.append(result.translation.getOriginalLanguage());
			translated = true;
		} else {
			ErrorUtils.showErrorMessage(getApplicationContext(), result.exception);
		}
	}

	/**
	 * set emojis, replace emoji tags with images
	 */
	private void onStatusTextUpdate(@NonNull TextEmojiLoader.Result result) {
		if (result.images != null) {
			Spannable spannable = EmojiUtils.addEmojis(getApplicationContext(), result.spannable, result.images);
			status_text.setText(spannable);
		}
	}

	/**
	 * set emojis, replace emoji tags with images
	 */
	private void onUsernameUpdate(@NonNull TextEmojiLoader.Result result) {
		if (result.images != null) {
			Spannable spannable = EmojiUtils.addEmojis(getApplicationContext(), result.spannable, result.images);
			username.setText(spannable);
		}
	}
}