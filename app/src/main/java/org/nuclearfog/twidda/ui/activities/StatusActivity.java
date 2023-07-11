package org.nuclearfog.twidda.ui.activities;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
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
import android.view.ViewTreeObserver.OnPreDrawListener;
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
import androidx.core.widget.NestedScrollView;
import androidx.core.widget.NestedScrollView.OnScrollChangeListener;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.nuclearfog.tag.Tagger;
import org.nuclearfog.tag.Tagger.OnTagClickListener;
import org.nuclearfog.textviewtool.LinkAndScrollMovement;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.NotificationAction;
import org.nuclearfog.twidda.backend.async.NotificationAction.NotificationActionParam;
import org.nuclearfog.twidda.backend.async.NotificationAction.NotificationActionResult;
import org.nuclearfog.twidda.backend.async.PollAction;
import org.nuclearfog.twidda.backend.async.PollAction.PollActionParam;
import org.nuclearfog.twidda.backend.async.PollAction.PollActionResult;
import org.nuclearfog.twidda.backend.async.StatusAction;
import org.nuclearfog.twidda.backend.async.StatusAction.StatusParam;
import org.nuclearfog.twidda.backend.async.StatusAction.StatusResult;
import org.nuclearfog.twidda.backend.async.TextEmojiLoader;
import org.nuclearfog.twidda.backend.async.TextEmojiLoader.EmojiParam;
import org.nuclearfog.twidda.backend.async.TextEmojiLoader.EmojiResult;
import org.nuclearfog.twidda.backend.async.TranslationLoader;
import org.nuclearfog.twidda.backend.async.TranslationLoader.TranslationResult;
import org.nuclearfog.twidda.backend.image.PicassoBuilder;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.EmojiUtils;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.backend.utils.LinkUtils;
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.config.Configuration;
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
import org.nuclearfog.twidda.ui.dialogs.MetricsDialog;
import org.nuclearfog.twidda.ui.dialogs.ReportDialog;
import org.nuclearfog.twidda.ui.fragments.StatusFragment;
import org.nuclearfog.twidda.ui.views.LockableConstraintLayout;
import org.nuclearfog.twidda.ui.views.LockableConstraintLayout.LockCallback;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Locale;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * Status Activity to show status and user information
 *
 * @author nuclearfog
 */
public class StatusActivity extends AppCompatActivity implements OnClickListener, OnLongClickListener, OnTagClickListener,
		OnConfirmListener, OnCardClickListener, OnScrollChangeListener, LockCallback, ActivityResultCallback<ActivityResult>, OnPreDrawListener {

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
	 * scrollview position threshold to lock/unlock child scrolling
	 */
	private static final int SCROLL_THRESHOLD = 10;

	/**
	 * toolbar menu group ID for copy options
	 */
	private static final int MENU_GROUP_COPY = 0x157426;

	/**
	 * color of the profile image placeholder
	 */
	private static final int IMAGE_PLACEHOLDER_COLOR = 0x2F000000;

	private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this);
	private AsyncCallback<StatusResult> statusCallback = this::onStatusResult;
	private AsyncCallback<PollActionResult> pollResult = this::onPollResult;
	private AsyncCallback<TranslationResult> translationResult = this::onTranslationResult;
	private AsyncCallback<NotificationActionResult> notificationCallback = this::onNotificationResult;
	private AsyncCallback<EmojiResult> statusTextUpdate = this::onStatusTextUpdate;
	private AsyncCallback<EmojiResult> usernameUpdate = this::onUsernameUpdate;

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
	private ConfirmDialog confirmDialog;
	private MetricsDialog metricsDialog;
	private AudioPlayerDialog audioDialog;
	private ReportDialog reportDialog;

	private ViewGroup root, header;
	private NestedScrollView container;
	private LockableConstraintLayout body;
	private TextView status_source, created_at, status_text, screen_name, username, location_name, sensitive, spoiler, spoiler_hint, translate_text;
	private Button reply_button, repost_button, like_button, reply_name, repost_name_button;
	private ImageView profile_image;
	private Toolbar toolbar;
	private View card_container;

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
		root = findViewById(R.id.page_status_root);
		header = findViewById(R.id.page_status_header);
		body = findViewById(R.id.page_status_body);
		container = findViewById(R.id.page_status_scroll);
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
		repost_name_button = findViewById(R.id.page_status_reposter_reference);
		translate_text = findViewById(R.id.page_status_text_translate);
		spoiler_hint = findViewById(R.id.page_status_text_sensitive_hint);
		card_container = findViewById(R.id.page_status_cards_container);
		RecyclerView card_list = findViewById(R.id.page_status_cards);

		clip = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		statusLoader = new StatusAction(this);
		pollLoader = new PollAction(this);
		notificationLoader = new NotificationAction(this);
		translationLoader = new TranslationLoader(this);
		emojiLoader = new TextEmojiLoader(this);
		confirmDialog = new ConfirmDialog(this, this);
		metricsDialog = new MetricsDialog(this);
		audioDialog = new AudioPlayerDialog(this);
		reportDialog = new ReportDialog(this);
		picasso = PicassoBuilder.get(this);
		settings = GlobalSettings.get(this);
		adapter = new PreviewAdapter(this);

		reply_button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.answer, 0, 0, 0);
		repost_button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.repost, 0, 0, 0);
		location_name.setCompoundDrawablesWithIntrinsicBounds(R.drawable.location, 0, 0, 0);
		sensitive.setCompoundDrawablesWithIntrinsicBounds(R.drawable.sensitive, 0, 0, 0);
		spoiler.setCompoundDrawablesWithIntrinsicBounds(R.drawable.exclamation, 0, 0, 0);
		reply_name.setCompoundDrawablesWithIntrinsicBounds(R.drawable.back, 0, 0, 0);
		repost_name_button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.repost, 0, 0, 0);
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
		if (savedInstanceState == null) {
			return;
		}
		// get data
		Serializable serialized = savedInstanceState.getSerializable(KEY_DATA);
		long statusId = savedInstanceState.getLong(KEY_STATUS_ID, 0L);
		long notificationId = savedInstanceState.getLong(KEY_NOTIFICATION_ID, 0L);
		String replyUsername = "";

		// set status data
		if (serialized instanceof Status) {
			Status status = (Status) serialized;
			Status embeddedStatus = status.getEmbeddedStatus();
			setStatus(status);
			StatusParam statusParam = new StatusParam(StatusParam.ONLINE, status.getId());
			statusLoader.execute(statusParam, statusCallback);
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
			NotificationActionParam notificationParam = new NotificationActionParam(NotificationActionParam.ONLINE, notification.getId());
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
			StatusParam statusParam = new StatusParam(StatusParam.DATABASE, statusId);
			statusLoader.execute(statusParam, statusCallback);
		}
		// get notification data using notification ID
		else if (notificationId != 0L) {
			replyUsername = savedInstanceState.getString(KEY_NAME);
			NotificationActionParam notificationParam = new NotificationActionParam(NotificationActionParam.ONLINE, notificationId);
			notificationLoader.execute(notificationParam, notificationCallback);
		}
		// initialize status reply list
		Bundle param = new Bundle();
		param.putInt(StatusFragment.KEY_MODE, StatusFragment.MODE_REPLY);
		param.putString(StatusFragment.KEY_SEARCH, replyUsername);
		param.putLong(StatusFragment.KEY_ID, statusId);
		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.page_status_reply_fragment, StatusFragment.class, param);
		fragmentTransaction.commit();

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
		container.setOnScrollChangeListener(this);
		body.addLockCallback(this);
		container.getViewTreeObserver().addOnPreDrawListener(this);
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
		audioDialog.close();
		super.onDestroy();
	}


	@Override
	public boolean onPreDraw() {
		// when views are added to scrollview, scroll back to top
		container.getViewTreeObserver().removeOnPreDrawListener(this);
		body.getLayoutParams().height = root.getMeasuredHeight() - toolbar.getMeasuredHeight();
		container.scrollTo(0, 0);
		return true;
	}


	@Override
	public boolean onCreateOptionsMenu(@NonNull Menu m) {
		getMenuInflater().inflate(R.menu.status, m);
		AppStyles.setOverflowIcon(toolbar, settings.getIconColor());
		return super.onCreateOptionsMenu(m);
	}


	@Override
	public boolean onPrepareOptionsMenu(@NonNull Menu m) {
		MenuItem optDelete = m.findItem(R.id.menu_status_delete);
		MenuItem optHide = m.findItem(R.id.menu_status_hide);
		MenuItem optCopy = m.findItem(R.id.menu_status_copy);
		MenuItem optReport = m.findItem(R.id.menu_status_report);
		MenuItem optMetrics = m.findItem(R.id.menu_status_metrics);
		MenuItem menuBookmark = m.findItem(R.id.menu_status_bookmark);
		MenuItem editStatus = m.findItem(R.id.menu_status_edit);
		SubMenu copyMenu = optCopy.getSubMenu();

		// set status options
		if (status != null) {
			Status currentStatus = status;
			if (currentStatus.getEmbeddedStatus() != null) {
				currentStatus = currentStatus.getEmbeddedStatus();
			}
			// enable/disable status reply hide
			long currentUserId = settings.getLogin().getId();
			if (currentStatus.getRepliedUserId() == currentUserId && currentStatus.getAuthor().getId() != currentUserId) {
				optHide.setTitle(hidden ? R.string.menu_status_hide : R.string.menu_status_unhide);
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
			// enable/disable status metrics option
			if (currentStatus.getMetrics() != null) {
				optMetrics.setVisible(true);
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
		return super.onPrepareOptionsMenu(m);
	}


	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if (status == null)
			return super.onOptionsItemSelected(item);

		Status status = this.status;
		if (status.getEmbeddedStatus() != null)
			status = status.getEmbeddedStatus();
		// Delete status option
		if (item.getItemId() == R.id.menu_status_delete) {
			confirmDialog.show(ConfirmDialog.DELETE_STATUS);
			return true;
		}
		// add/remove bookmark
		else if (item.getItemId() == R.id.menu_status_bookmark) {
			Toast.makeText(getApplicationContext(), R.string.info_loading, Toast.LENGTH_SHORT).show();
			int mode = status.isBookmarked() ? StatusParam.UNBOOKMARK : StatusParam.BOOKMARK;
			StatusParam param = new StatusParam(mode, status.getId());
			statusLoader.execute(param, statusCallback);
			return true;
		}
		// hide status
		else if (item.getItemId() == R.id.menu_status_hide) {
			int mode = hidden ? StatusParam.UNHIDE : StatusParam.HIDE;
			StatusParam param = new StatusParam(mode, status.getId());
			statusLoader.execute(param, statusCallback);
			return true;
		}
		// get status link
		else if (item.getItemId() == R.id.menu_status_browser) {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(status.getUrl()));
			try {
				startActivity(intent);
			} catch (ActivityNotFoundException err) {
				Toast.makeText(getApplicationContext(), R.string.error_connection_failed, Toast.LENGTH_SHORT).show();
			}
			return true;
		}
		// copy status link to clipboard
		else if (item.getItemId() == R.id.menu_status_copy_text) {
			if (clip != null) {
				ClipData linkClip = ClipData.newPlainText("status text", status.getText());
				clip.setPrimaryClip(linkClip);
				Toast.makeText(getApplicationContext(), R.string.info_status_text_copied, Toast.LENGTH_SHORT).show();
			}
			return true;
		}
		// copy status link to clipboard
		else if (item.getItemId() == R.id.menu_status_copy_link) {
			if (clip != null) {
				ClipData linkClip = ClipData.newPlainText("status link", status.getUrl());
				clip.setPrimaryClip(linkClip);
				Toast.makeText(getApplicationContext(), R.string.info_status_link_copied, Toast.LENGTH_SHORT).show();
			}
			return true;
		}
		// open status metrics page
		else if (item.getItemId() == R.id.menu_status_metrics) {
			if (status.getMetrics() != null) {
				metricsDialog.show(status.getMetrics());
			}
		}
		// copy media links
		else if (item.getGroupId() == MENU_GROUP_COPY) {
			int index = item.getItemId();
			Media[] medias = status.getMedia();
			if (index >= 0 && index < medias.length) {
				if (clip != null) {
					ClipData linkClip = ClipData.newPlainText("status media link", medias[index].getUrl());
					clip.setPrimaryClip(linkClip);
					Toast.makeText(getApplicationContext(), R.string.info_status_medialink_copied, Toast.LENGTH_SHORT).show();
				}
			}
			return true;
		}
		// edit status
		else if (item.getItemId() == R.id.menu_status_edit) {
			Intent intent = new Intent(this, StatusEditor.class);
			intent.putExtra(StatusEditor.KEY_STATUS_DATA, status);
			intent.putExtra(StatusEditor.KEY_EDIT, true);
			activityResultLauncher.launch(intent);
		}
		// report status
		else if (item.getItemId() == R.id.menu_status_report) {
			reportDialog.show(status.getAuthor().getId(), status.getId());
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onActivityResult(ActivityResult result) {
		if (result.getData() != null) {
			if (result.getResultCode() == StatusEditor.RETURN_STATUS_UPDATE) {
				Serializable data = result.getData().getSerializableExtra(StatusEditor.KEY_STATUS_DATA);
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
				intent.putExtra(StatusEditor.KEY_STATUS_DATA, status);
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
					translationLoader.execute(status.getId(), translationResult);
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
				int mode = status.isReposted() ? StatusParam.UNREPOST : StatusParam.REPOST;
				StatusParam param = new StatusParam(mode, status.getId());
				statusLoader.execute(param, statusCallback);
				return true;
			}
			// favorite this status
			else if (v.getId() == R.id.page_status_favorite) {
				Toast.makeText(getApplicationContext(), R.string.info_loading, Toast.LENGTH_SHORT).show();
				int mode = status.isFavorited() ? StatusParam.UNFAVORITE : StatusParam.FAVORITE;
				StatusParam param = new StatusParam(mode, status.getId());
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
	public void onScrollChange(@NonNull NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
		body.lock(scrollY > header.getMeasuredHeight() + SCROLL_THRESHOLD && scrollY < header.getMeasuredHeight() - SCROLL_THRESHOLD);
	}


	@Override
	public boolean aquireLock() {
		return container.getScrollY() < header.getMeasuredHeight() - SCROLL_THRESHOLD;
	}


	@Override
	public void onConfirm(int type) {
		if (type == ConfirmDialog.DELETE_STATUS) {
			if (status != null) {
				long id = status.getId();
				if (status.getEmbeddedStatus() != null) {
					id = status.getEmbeddedStatus().getId();
				}
				StatusParam param = new StatusParam(StatusParam.DELETE, id);
				statusLoader.execute(param, statusCallback);
			}
		}
	}


	@Override
	public void onCardClick(Card card, int type) {
		if (type == OnCardClickListener.TYPE_LINK) {
			LinkUtils.openLink(this, card.getUrl());
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
				audioDialog.show(uri);
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
			LinkUtils.openLink(this, tag);
		}
	}


	@Override
	public void onVoteClick(Poll poll, int[] selection) {
		if (pollLoader.isIdle()) {
			PollActionParam param = new PollActionParam(PollActionParam.VOTE, poll, selection);
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
		// set user verified icon
		if (author.isVerified()) {
			username.setCompoundDrawablesWithIntrinsicBounds(R.drawable.verify, 0, 0, 0);
			AppStyles.setDrawableColor(username, settings.getIconColor());
		} else {
			username.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		}
		// set user protected icon
		if (author.isProtected()) {
			screen_name.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock, 0, 0, 0);
			AppStyles.setDrawableColor(screen_name, settings.getIconColor());
		} else {
			screen_name.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		}
		// add 'translate' label
		if (!status.getText().isEmpty() && !status.getLanguage().isEmpty() && !status.getLanguage().equals(Locale.getDefault().getLanguage())) {
			translate_text.setVisibility(View.VISIBLE); // todo add translation support check
		} else {
			translate_text.setVisibility(View.GONE);
		}
		// set username
		if (status.getAuthor().getEmojis().length > 0) {
			Spannable usernameSpan = new SpannableString(author.getUsername());
			if (settings.imagesEnabled()) {
				EmojiParam param = new EmojiParam(author.getEmojis(), usernameSpan, getResources().getDimensionPixelSize(R.dimen.page_status_icon_size));
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
				EmojiParam param = new EmojiParam(status.getEmojis(), spannableText, getResources().getDimensionPixelSize(R.dimen.page_status_icon_size));
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
			card_container.setVisibility(View.VISIBLE);
			adapter.replaceAll(status, settings.hideSensitiveEnabled());
			status_text.setMaxLines(5);
		} else {
			card_container.setVisibility(View.GONE);
			status_text.setMaxLines(10);
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
	private void onStatusResult(@NonNull StatusResult result) {
		if (result.status != null) {
			setStatus(result.status);
		}
		switch (result.mode) {
			case StatusResult.DATABASE:
				if (result.status != null) {
					StatusParam param = new StatusParam(StatusParam.ONLINE, result.status.getId());
					statusLoader.execute(param, statusCallback);
				}
				break;

			case StatusResult.REPOST:
				Toast.makeText(getApplicationContext(), R.string.info_status_reposted, Toast.LENGTH_SHORT).show();
				break;

			case StatusResult.UNREPOST:
				Toast.makeText(getApplicationContext(), R.string.info_status_unreposted, Toast.LENGTH_SHORT).show();
				break;

			case StatusResult.FAVORITE:
				if (settings.likeEnabled())
					Toast.makeText(getApplicationContext(), R.string.info_status_liked, Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(getApplicationContext(), R.string.info_status_favored, Toast.LENGTH_SHORT).show();
				break;

			case StatusResult.UNFAVORITE:
				if (settings.likeEnabled())
					Toast.makeText(getApplicationContext(), R.string.info_status_unliked, Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(getApplicationContext(), R.string.info_status_unfavored, Toast.LENGTH_SHORT).show();
				break;

			case StatusResult.BOOKMARK:
				Toast.makeText(getApplicationContext(), R.string.info_status_bookmarked, Toast.LENGTH_SHORT).show();
				break;

			case StatusResult.UNBOOKMARK:
				Toast.makeText(getApplicationContext(), R.string.info_status_unbookmarked, Toast.LENGTH_SHORT).show();
				break;

			case StatusResult.HIDE:
				hidden = true;
				invalidateOptionsMenu();
				Toast.makeText(getApplicationContext(), R.string.info_reply_hidden, Toast.LENGTH_SHORT).show();
				break;

			case StatusResult.UNHIDE:
				hidden = false;
				invalidateOptionsMenu();
				Toast.makeText(getApplicationContext(), R.string.info_reply_unhidden, Toast.LENGTH_SHORT).show();
				break;

			case StatusResult.DELETE:
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

			case StatusResult.ERROR:
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
	private void onNotificationResult(@NonNull NotificationActionResult result) {
		switch (result.mode) {
			case NotificationActionResult.DATABASE:
				if (result.notification != null) {
					NotificationActionParam param = new NotificationActionParam(NotificationActionParam.ONLINE, result.notification.getId());
					notificationLoader.execute(param, notificationCallback);
				}
				// fall through

			case NotificationActionResult.ONLINE:
				if (result.notification != null && result.notification.getStatus() != null) {
					notification = result.notification;
					setStatus(result.notification.getStatus());
				}
				break;

			case NotificationActionResult.DISMISS:
				if (notification != null) {
					Intent intent = new Intent();
					intent.putExtra(KEY_NOTIFICATION_ID, notification.getId());
					setResult(RETURN_NOTIFICATION_REMOVED, intent);
				}
				Toast.makeText(getApplicationContext(), R.string.info_notification_dismiss, Toast.LENGTH_SHORT).show();
				finish();
				break;

			case NotificationActionResult.ERROR:
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
	private void onPollResult(@NonNull PollActionResult result) {
		switch (result.mode) {
			case PollActionResult.LOAD:
				if (result.poll != null) {
					adapter.updatePoll(result.poll);
				}
				break;

			case PollActionResult.VOTE:
				if (result.poll != null) {
					adapter.updatePoll(result.poll);
					Toast.makeText(getApplicationContext(), R.string.info_poll_voted, Toast.LENGTH_SHORT).show();
				}
				break;

			case PollActionResult.ERROR:
				ErrorUtils.showErrorMessage(this, result.exception);
				break;
		}
	}

	/**
	 * @param result status translation result
	 */
	private void onTranslationResult(@NonNull TranslationResult result) {
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
			Toast.makeText(getApplicationContext(), R.string.error_translating_status, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * set emojis, replace emoji tags with images
	 */
	private void onStatusTextUpdate(@NonNull EmojiResult result) {
		if (settings.getLogin().getConfiguration() == Configuration.MASTODON && result.images != null) {
			Spannable spannable = EmojiUtils.addEmojis(getApplicationContext(), result.spannable, result.images);
			status_text.setText(spannable);
		}
	}

	/**
	 * set emojis, replace emoji tags with images
	 */
	private void onUsernameUpdate(@NonNull EmojiResult result) {
		if (settings.getLogin().getConfiguration() == Configuration.MASTODON && result.images != null) {
			Spannable spannable = EmojiUtils.addEmojis(getApplicationContext(), result.spannable, result.images);
			username.setText(spannable);
		}
	}
}