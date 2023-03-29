package org.nuclearfog.twidda.ui.activities;

import static org.nuclearfog.twidda.ui.activities.SearchActivity.KEY_SEARCH_QUERY;
import static org.nuclearfog.twidda.ui.activities.StatusEditor.KEY_STATUS_EDITOR_REPLYID;
import static org.nuclearfog.twidda.ui.activities.StatusEditor.KEY_STATUS_EDITOR_TEXT;
import static org.nuclearfog.twidda.ui.activities.UsersActivity.KEY_USERS_ID;
import static org.nuclearfog.twidda.ui.activities.UsersActivity.KEY_USERS_MODE;
import static org.nuclearfog.twidda.ui.activities.UsersActivity.USERS_FAVORIT;
import static org.nuclearfog.twidda.ui.activities.UsersActivity.USERS_REPOST;
import static org.nuclearfog.twidda.ui.fragments.StatusFragment.KEY_STATUS_FRAGMENT_ID;
import static org.nuclearfog.twidda.ui.fragments.StatusFragment.KEY_STATUS_FRAGMENT_MODE;
import static org.nuclearfog.twidda.ui.fragments.StatusFragment.KEY_STATUS_FRAGMENT_SEARCH;
import static org.nuclearfog.twidda.ui.fragments.StatusFragment.STATUS_FRAGMENT_REPLY;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BlurMaskFilter;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
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
import org.nuclearfog.twidda.backend.async.EmojiLoader;
import org.nuclearfog.twidda.backend.async.EmojiLoader.EmojiParam;
import org.nuclearfog.twidda.backend.async.EmojiLoader.EmojiResult;
import org.nuclearfog.twidda.backend.async.NotificationAction;
import org.nuclearfog.twidda.backend.async.NotificationAction.NotificationActionParam;
import org.nuclearfog.twidda.backend.async.NotificationAction.NotificationActionResult;
import org.nuclearfog.twidda.backend.async.StatusAction;
import org.nuclearfog.twidda.backend.async.StatusAction.StatusParam;
import org.nuclearfog.twidda.backend.async.StatusAction.StatusResult;
import org.nuclearfog.twidda.backend.async.PollAction;
import org.nuclearfog.twidda.backend.async.PollAction.PollActionParam;
import org.nuclearfog.twidda.backend.async.PollAction.PollActionResult;
import org.nuclearfog.twidda.backend.async.TranslationLoader;
import org.nuclearfog.twidda.backend.async.TranslationLoader.TranslationResult;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.backend.utils.PicassoBuilder;
import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.backend.utils.TextWithEmoji;
import org.nuclearfog.twidda.config.Configuration;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.database.impl.DatabaseNotification;
import org.nuclearfog.twidda.database.impl.DatabaseStatus;
import org.nuclearfog.twidda.model.Card;
import org.nuclearfog.twidda.model.Location;
import org.nuclearfog.twidda.model.Media;
import org.nuclearfog.twidda.model.Notification;
import org.nuclearfog.twidda.model.Poll;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.model.Translation;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.ui.adapter.PreviewAdapter;
import org.nuclearfog.twidda.ui.adapter.PreviewAdapter.OnCardClickListener;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.OnConfirmListener;
import org.nuclearfog.twidda.ui.dialogs.MetricsDialog;
import org.nuclearfog.twidda.ui.fragments.StatusFragment;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * Status Activity to show status and user information
 *
 * @author nuclearfog
 */
public class StatusActivity extends AppCompatActivity implements OnClickListener, OnLongClickListener, OnTagClickListener, OnConfirmListener, OnCardClickListener {

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
	 * If no status object exists, {@link #KEY_STATUS_ID} and {@link #KEY_STATUS_NAME} will be used instead
	 */
	public static final String KEY_STATUS_DATA = "status_data";

	/**
	 * key uused for notification information, containing a status
	 * value type is {@link Notification}
	 */
	public static final String KEY_NOTIFICATION_DATA = "notification_data";

	/**
	 * key for the status ID value, alternative to {@link #KEY_STATUS_DATA}
	 * value type is Long
	 */
	public static final String KEY_STATUS_ID = "status_id";

	/**
	 * key for the status author's name. alternative to {@link #KEY_STATUS_DATA}
	 * value type is String
	 */
	public static final String KEY_STATUS_NAME = "status_author";

	/**
	 * key for the notification ID value, alternative to {@link #KEY_NOTIFICATION_DATA}
	 * value type is long
	 */
	public static final String KEY_NOTIFICATION_ID = "notification_id";

	/**
	 * key for the (notification) status author's name. alternative to {@link #KEY_STATUS_DATA}
	 * value type is String
	 */
	public static final String KEY_NOTIFICATION_NAME = "notification_status_author";

	/**
	 * key to return updated status information
	 * value type is {@link Status}
	 */
	public static final String INTENT_STATUS_UPDATE_DATA = "status_update_data";

	/**
	 * key to return updated notification information
	 * value type is {@link Status}
	 */
	public static final String INTENT_NOTIFICATION_UPDATE_DATA = "notification_update_data";

	/**
	 * key to return an ID if status was deleted
	 * value type is Long
	 */
	public static final String INTENT_STATUS_REMOVED_ID = "status_removed_id";

	/**
	 * key to return an ID if notification was deleted
	 * value type is Long
	 */
	public static final String INTENT_NOTIFICATION_REMOVED_ID = "notification_removed_id";

	/**
	 * regex pattern of a status URL
	 */
	public static final Pattern TWITTER_LINK_PATTERN = Pattern.compile("https://twitter.com/\\w+/status/\\d+");

	private static final int MENU_GROUP_COPY = 0x157426;

	private AsyncCallback<StatusResult> statusCallback = this::onStatusResult;
	private AsyncCallback<PollActionResult> pollResult = this::onPollResult;
	private AsyncCallback<TranslationResult> translationResult = this::onTranslationResult;
	private AsyncCallback<NotificationActionResult> notificationCallback = this::onNotificationResult;
	private AsyncCallback<EmojiResult> statusTextUpdate = this::onStatusTextUpdate;
	private AsyncCallback<EmojiResult> usernameUpdate = this::onUsernameUpdate;

	@Nullable
	private ClipboardManager clip;
	private StatusAction statusAsync;
	private PollAction voteAsync;
	private NotificationAction notificationAsync;
	private TranslationLoader translationAsync;
	private EmojiLoader emojiAsync;
	private GlobalSettings settings;
	private Picasso picasso;
	private PreviewAdapter adapter;
	private ConfirmDialog confirmDialog;
	private MetricsDialog metricsDialog;

	private NestedScrollView container;
	private ViewGroup root, body;
	private TextView statusApi, createdAt, statusText, screenName, username, locationName, sensitive, spoiler, spoilerHint, translateText;
	private Button replyButton, repostButton, likeButton, replyName, repostNameButton;
	private ImageView profileImage;
	private RecyclerView cardList;
	private Toolbar toolbar;

	@Nullable
	private Status status;
	@Nullable
	private Notification notification;
	@Nullable
	private Translation translation;
	private boolean hidden;


	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(AppStyles.setFontScale(newBase));
	}


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.page_status);
		root = findViewById(R.id.page_status_root);
		body = findViewById(R.id.page_status_body);
		container = findViewById(R.id.page_status_scroll);
		toolbar = findViewById(R.id.page_status_toolbar);
		replyButton = findViewById(R.id.page_status_reply);
		repostButton = findViewById(R.id.page_status_repost);
		likeButton = findViewById(R.id.page_status_favorite);
		username = findViewById(R.id.page_status_username);
		screenName = findViewById(R.id.page_status_screenname);
		profileImage = findViewById(R.id.page_status_profile);
		replyName = findViewById(R.id.page_status_reply_reference);
		statusText = findViewById(R.id.page_status_text);
		createdAt = findViewById(R.id.page_status_date);
		statusApi = findViewById(R.id.page_status_api);
		locationName = findViewById(R.id.page_status_location_name);
		sensitive = findViewById(R.id.page_status_sensitive);
		spoiler = findViewById(R.id.page_status_spoiler);
		repostNameButton = findViewById(R.id.page_status_reposter_reference);
		translateText = findViewById(R.id.page_status_text_translate);
		spoilerHint = findViewById(R.id.page_status_text_sensitive_hint);
		cardList = findViewById(R.id.page_status_cards);

		statusAsync = new StatusAction(this);
		voteAsync = new PollAction(this);
		notificationAsync = new NotificationAction(this);
		translationAsync = new TranslationLoader(this);
		emojiAsync = new EmojiLoader(this);

		picasso = PicassoBuilder.get(this);
		settings = GlobalSettings.getInstance(this);
		clip = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		adapter = new PreviewAdapter(settings, picasso, this);

		replyButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.answer, 0, 0, 0);
		repostButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.repost, 0, 0, 0);
		locationName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.location, 0, 0, 0);
		sensitive.setCompoundDrawablesWithIntrinsicBounds(R.drawable.sensitive, 0, 0, 0);
		spoiler.setCompoundDrawablesWithIntrinsicBounds(R.drawable.exclamation, 0, 0, 0);
		replyName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.back, 0, 0, 0);
		repostNameButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.repost, 0, 0, 0);
		statusText.setMovementMethod(LinkAndScrollMovement.getInstance());
		statusText.setLinkTextColor(settings.getHighlightColor());
		cardList.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
		cardList.setAdapter(adapter);
		if (settings.likeEnabled()) {
			likeButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.like, 0, 0, 0);
		} else {
			likeButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.favorite, 0, 0, 0);
		}
		toolbar.setTitle("");
		setSupportActionBar(toolbar);
		AppStyles.setTheme(root);
		locationName.setTextColor(settings.getHighlightColor());

		// get parameter, set information and initialize loaders
		if (savedInstanceState == null)
			savedInstanceState = getIntent().getExtras();
		if (savedInstanceState == null)
			return;
		long statusId = 0L;
		String replyUsername = "";
		Object statusObject = savedInstanceState.getSerializable(KEY_STATUS_DATA);
		Object notificationObject = savedInstanceState.getSerializable(KEY_NOTIFICATION_DATA);
		if (statusObject instanceof Status) {
			Status status = (Status) statusObject;
			setStatus(status);
			Status embedded = status.getEmbeddedStatus();
			if (embedded != null) {
				statusId = embedded.getId();
				replyUsername = embedded.getAuthor().getScreenname();
			} else {
				statusId = status.getId();
				hidden = status.isHidden();
				replyUsername = status.getAuthor().getScreenname();
			}
			if (status instanceof DatabaseStatus) {
				StatusParam statusParam = new StatusParam(StatusParam.ONLINE, status.getId());
				statusAsync.execute(statusParam, statusCallback);
			}
		} else if (notificationObject instanceof Notification) {
			Notification notification = (Notification) notificationObject;
			if (notification.getStatus() != null) {
				setNotification(notification);
				statusId = notification.getStatus().getId();
				replyUsername = notification.getStatus().getAuthor().getScreenname();
			}
			if (notification instanceof DatabaseNotification) {
				NotificationActionParam notificationParam = new NotificationActionParam(NotificationActionParam.ONLINE, notification.getId());
				notificationAsync.execute(notificationParam, notificationCallback);
			}
		} else {
			statusId = savedInstanceState.getLong(KEY_STATUS_ID, 0L);
			long notificationId = savedInstanceState.getLong(KEY_NOTIFICATION_ID, 0L);
			if (statusId != 0L) {
				replyUsername = savedInstanceState.getString(KEY_STATUS_NAME);
				StatusParam statusParam = new StatusParam(StatusParam.DATABASE, statusId);
				statusAsync.execute(statusParam, statusCallback);
			} else if (notificationId != 0L) {
				replyUsername = savedInstanceState.getString(KEY_NOTIFICATION_NAME);
				NotificationActionParam notificationParam = new NotificationActionParam(NotificationActionParam.ONLINE, notificationId);
				notificationAsync.execute(notificationParam, notificationCallback);
			}
		}
		// initialize status reply list
		Bundle param = new Bundle();
		param.putInt(KEY_STATUS_FRAGMENT_MODE, STATUS_FRAGMENT_REPLY);
		param.putString(KEY_STATUS_FRAGMENT_SEARCH, replyUsername);
		param.putLong(KEY_STATUS_FRAGMENT_ID, statusId);
		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.page_status_reply_fragment, StatusFragment.class, param);
		fragmentTransaction.commit();

		confirmDialog = new ConfirmDialog(this);
		metricsDialog = new MetricsDialog(this);
		confirmDialog.setConfirmListener(this);
		repostNameButton.setOnClickListener(this);
		replyName.setOnClickListener(this);
		translateText.setOnClickListener(this);
		replyButton.setOnClickListener(this);
		repostButton.setOnClickListener(this);
		likeButton.setOnClickListener(this);
		profileImage.setOnClickListener(this);
		locationName.setOnClickListener(this);
		repostButton.setOnLongClickListener(this);
		likeButton.setOnLongClickListener(this);
		repostNameButton.setOnLongClickListener(this);
		locationName.setOnLongClickListener(this);
		statusText.setOnClickListener(this);
	}


	@Override
	protected void onDestroy() {
		statusAsync.cancel();
		super.onDestroy();
	}


	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
		if (notification != null) {
			intent.putExtra(INTENT_NOTIFICATION_UPDATE_DATA, notification);
			setResult(RETURN_NOTIFICATION_UPDATE, intent);
		} else {
			intent.putExtra(INTENT_STATUS_UPDATE_DATA, status);
			setResult(RETURN_STATUS_UPDATE, intent);
		}
		super.onBackPressed();
	}


	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putSerializable(KEY_STATUS_DATA, status);
		outState.putSerializable(KEY_NOTIFICATION_DATA, notification);
		super.onSaveInstanceState(outState);
	}


	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		body.getLayoutParams().height = root.getMeasuredHeight() - toolbar.getMeasuredHeight();
		container.scrollTo(0, 0);
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
		MenuItem optMetrics = m.findItem(R.id.menu_status_metrics);
		MenuItem menuBookmark = m.findItem(R.id.menu_status_bookmark);
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
		}
		return true;
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
		}
		// add/remove bookmark
		if (item.getItemId() == R.id.menu_status_bookmark) {
			Toast.makeText(getApplicationContext(), R.string.info_loading, Toast.LENGTH_SHORT).show();
			int mode = status.isBookmarked() ? StatusParam.UNBOOKMARK : StatusParam.BOOKMARK;
			StatusParam param = new StatusParam(mode, status.getId());
			statusAsync.execute(param, statusCallback);
		}
		// hide status
		else if (item.getItemId() == R.id.menu_status_hide) {
			int mode = hidden ? StatusParam.UNHIDE : StatusParam.HIDE;
			StatusParam param = new StatusParam(mode, status.getId());
			statusAsync.execute(param, statusCallback);
		}
		// get status link
		else if (item.getItemId() == R.id.menu_status_browser) {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(status.getUrl()));
			try {
				startActivity(intent);
			} catch (ActivityNotFoundException err) {
				Toast.makeText(getApplicationContext(), R.string.error_connection_failed, Toast.LENGTH_SHORT).show();
			}
		}
		// copy status link to clipboard
		else if (item.getItemId() == R.id.menu_status_copy_text) {
			if (clip != null) {
				ClipData linkClip = ClipData.newPlainText("status text", status.getText());
				clip.setPrimaryClip(linkClip);
				Toast.makeText(getApplicationContext(), R.string.info_status_text_copied, Toast.LENGTH_SHORT).show();
			}
		}
		// copy status link to clipboard
		else if (item.getItemId() == R.id.menu_status_copy_link) {
			if (clip != null) {
				ClipData linkClip = ClipData.newPlainText("status link", status.getUrl());
				clip.setPrimaryClip(linkClip);
				Toast.makeText(getApplicationContext(), R.string.info_status_link_copied, Toast.LENGTH_SHORT).show();
			}
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
		}
		return true;
	}


	@Override
	public void onClick(View v) {
		if (status != null) {
			Status status = this.status;
			if (status.getEmbeddedStatus() != null)
				status = status.getEmbeddedStatus();
			// answer to the status
			if (v.getId() == R.id.page_status_reply) {
				String prefix = status.getUserMentions();
				Intent intent = new Intent(this, StatusEditor.class);
				intent.putExtra(KEY_STATUS_EDITOR_REPLYID, status.getId());
				if (!prefix.isEmpty())
					intent.putExtra(KEY_STATUS_EDITOR_TEXT, prefix);
				startActivity(intent);
			}
			// show user reposting this status
			else if (v.getId() == R.id.page_status_repost) {
				Intent intent = new Intent(this, UsersActivity.class);
				intent.putExtra(KEY_USERS_ID, status.getId());
				intent.putExtra(KEY_USERS_MODE, USERS_REPOST);
				startActivity(intent);
			}
			// show user favoriting this status
			else if (v.getId() == R.id.page_status_favorite) {
				Intent intent = new Intent(this, UsersActivity.class);
				intent.putExtra(KEY_USERS_ID, status.getId());
				intent.putExtra(KEY_USERS_MODE, USERS_FAVORIT);
				startActivity(intent);
			}
			// open profile of the status author
			else if (v.getId() == R.id.page_status_profile) {
				Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
				intent.putExtra(ProfileActivity.KEY_PROFILE_USER, status.getAuthor());
				startActivity(intent);
			}
			// open replied status
			else if (v.getId() == R.id.page_status_reply_reference) {
				Intent intent = new Intent(getApplicationContext(), StatusActivity.class);
				intent.putExtra(KEY_STATUS_ID, status.getRepliedStatusId());
				intent.putExtra(KEY_STATUS_NAME, status.getReplyName());
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
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse("geo:" + location.getCoordinates() + "?z=14"));
					try {
						startActivity(intent);
					} catch (ActivityNotFoundException err) {
						Toast.makeText(getApplicationContext(), R.string.error_no_card_app, Toast.LENGTH_SHORT).show();
					}
				}
			}
			// go to user reposting this status
			else if (v.getId() == R.id.page_status_reposter_reference) {
				Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
				intent.putExtra(ProfileActivity.KEY_PROFILE_USER, this.status.getAuthor());
				startActivity(intent);
			}
			// unblur text on click
			else if (v.getId() == R.id.page_status_text) {
				// remove blur if any
				if (statusText.getPaint().getMaskFilter() != null) {
					statusText.getPaint().setMaskFilter(null);
					spoilerHint.setVisibility(View.INVISIBLE);
				}
			}
			// translate status text
			else if (v.getId() == R.id.page_status_text_translate) {
				if (translationAsync.isIdle() && translation == null) {
					translationAsync.execute(status.getId(), translationResult);
				}
			}
		}
	}


	@Override
	public boolean onLongClick(View v) {
		if (status != null && statusAsync.isIdle()) {
			// repost this status
			if (v.getId() == R.id.page_status_repost) {
				Toast.makeText(getApplicationContext(), R.string.info_loading, Toast.LENGTH_SHORT).show();
				int mode = status.isReposted() ? StatusParam.UNREPOST : StatusParam.REPOST;
				StatusParam param = new StatusParam(mode, status.getId());
				statusAsync.execute(param, statusCallback);
				return true;
			}
			// favorite this status
			else if (v.getId() == R.id.page_status_favorite) {
				Toast.makeText(getApplicationContext(), R.string.info_loading, Toast.LENGTH_SHORT).show();
				int mode = status.isFavorited() ? StatusParam.UNFAVORITE : StatusParam.FAVORITE;
				StatusParam param = new StatusParam(mode, status.getId());
				statusAsync.execute(param, statusCallback);
				return true;
			}
			// go to original status
			else if (v.getId() == R.id.page_status_reposter_reference) {
				Status embeddedStatus = status.getEmbeddedStatus();
				if (embeddedStatus != null) {
					Intent intent = new Intent(this, StatusActivity.class);
					intent.putExtra(KEY_STATUS_DATA, embeddedStatus);
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
	public void onConfirm(int type, boolean rememberChoice) {
		switch (type) {
			case ConfirmDialog.DELETE_STATUS:
				if (status != null) {
					long id = status.getId();
					if (status.getEmbeddedStatus() != null) {
						id = status.getEmbeddedStatus().getId();
					}
					StatusParam param = new StatusParam(StatusParam.DELETE, id);
					statusAsync.execute(param, statusCallback);
				}
				break;

			case ConfirmDialog.PROXY_CONFIRM:
				if (status != null) {
					settings.setIgnoreProxyWarning(rememberChoice);
					Media[] mediaItems = status.getMedia();
					if (status.getEmbeddedStatus() != null) {
						mediaItems = status.getEmbeddedStatus().getMedia();
					}
					if (mediaItems.length > 0) {
						Uri uri = Uri.parse(mediaItems[0].getUrl());
						Intent intent = new Intent(this, VideoViewer.class);
						intent.putExtra(VideoViewer.VIDEO_URI, uri);
						intent.putExtra(VideoViewer.ENABLE_VIDEO_CONTROLS, true);
						startActivity(intent);
					}
				}
				break;
		}
	}


	@Override
	public void onCardClick(Card card, int type) {
		if (type == OnCardClickListener.TYPE_LINK) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(card.getUrl()));
			try {
				startActivity(intent);
			} catch (ActivityNotFoundException err) {
				Toast.makeText(getApplicationContext(), R.string.error_connection_failed, Toast.LENGTH_SHORT).show();
			}
		} else if (type == OnCardClickListener.TYPE_IMAGE) {
			String imageUrl = card.getImageUrl();
			if (!imageUrl.isEmpty()) {
				Intent intent = new Intent(this, ImageViewer.class);
				intent.putExtra(ImageViewer.IMAGE_URI, Uri.parse(card.getImageUrl()));
				startActivity(intent);
			}
		}
	}


	@Override
	public void onMediaClick(Media media) {
		Uri uri = Uri.parse(media.getUrl());
		if (media.getMediaType() == Media.PHOTO) {
			Intent mediaIntent = new Intent(this, ImageViewer.class);
			mediaIntent.putExtra(ImageViewer.IMAGE_URI, uri);
			startActivity(mediaIntent);
		} else if (media.getMediaType() == Media.VIDEO) {
			if (!settings.isProxyEnabled() || settings.ignoreProxyWarning()) {
				Intent intent = new Intent(this, VideoViewer.class);
				intent.putExtra(VideoViewer.VIDEO_URI, uri);
				intent.putExtra(VideoViewer.ENABLE_VIDEO_CONTROLS, true);
				startActivity(intent);
			} else {
				confirmDialog.show(ConfirmDialog.PROXY_CONFIRM);
			}
		} else if (media.getMediaType() == Media.GIF) {
			if (!settings.isProxyEnabled() || settings.ignoreProxyWarning()) {
				Intent intent = new Intent(this, VideoViewer.class);
				intent.putExtra(VideoViewer.VIDEO_URI, uri);
				intent.putExtra(VideoViewer.ENABLE_VIDEO_CONTROLS, false);
				startActivity(intent);
			} else {
				confirmDialog.show(ConfirmDialog.PROXY_CONFIRM);
			}
		}
	}


	@Override
	public void onTagClick(String tag) {
		Intent intent = new Intent(this, SearchActivity.class);
		intent.putExtra(KEY_SEARCH_QUERY, tag);
		startActivity(intent);
	}

	/**
	 * called when a link is clicked
	 *
	 * @param tag link string
	 */
	@Override
	public void onLinkClick(String tag) {
		Uri link = Uri.parse(tag);
		// check if the link points to another status
		if ((settings.getLogin().getConfiguration() == Configuration.TWITTER1 || settings.getLogin().getConfiguration() == Configuration.TWITTER2)
			&& TWITTER_LINK_PATTERN.matcher(link.getScheme() + "://" + link.getHost() + link.getPath()).matches()) {
			List<String> segments = link.getPathSegments();
			Intent intent = new Intent(this, StatusActivity.class);
			intent.putExtra(KEY_STATUS_ID, Long.parseLong(segments.get(2)));
			intent.putExtra(KEY_STATUS_NAME, segments.get(0));
			startActivity(intent);
		}
		// open link in a browser
		else {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(link);
			try {
				startActivity(intent);
			} catch (ActivityNotFoundException err) {
				Toast.makeText(getApplicationContext(), R.string.error_connection_failed, Toast.LENGTH_SHORT).show();
			}
		}
	}


	@Override
	public void onVoteClick(Poll poll, int[] selection) {
		if (voteAsync.isIdle()) {
			PollActionParam param = new PollActionParam(PollActionParam.VOTE, poll, selection);
			voteAsync.execute(param, pollResult);
		}
	}

	/**
	 * load status into UI
	 *
	 * @param status Tweet information
	 */
	private void setStatus(@NonNull Status status) {
		this.status = status;
		Spannable spannableText = null;
		if (status.getEmbeddedStatus() != null) {
			repostNameButton.setVisibility(View.VISIBLE);
			repostNameButton.setText(status.getAuthor().getScreenname());
			status = status.getEmbeddedStatus();
		} else {
			repostNameButton.setVisibility(View.GONE);
		}
		User author = status.getAuthor();
		Location location = status.getLocation();
		invalidateOptionsMenu();

		if (status.isReposted()) {
			AppStyles.setDrawableColor(repostButton, settings.getRepostIconColor());
		} else {
			AppStyles.setDrawableColor(repostButton, settings.getIconColor());
		}
		if (status.isFavorited()) {
			AppStyles.setDrawableColor(likeButton, settings.getFavoriteIconColor());
		} else {
			AppStyles.setDrawableColor(likeButton, settings.getIconColor());
		}
		if (author.isVerified()) {
			username.setCompoundDrawablesWithIntrinsicBounds(R.drawable.verify, 0, 0, 0);
			AppStyles.setDrawableColor(username, settings.getIconColor());
		} else {
			username.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		}
		if (author.isProtected()) {
			screenName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock, 0, 0, 0);
			AppStyles.setDrawableColor(screenName, settings.getIconColor());
		} else {
			screenName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		}
		if (!status.getLanguage().isEmpty() && !status.getLanguage().equals(Locale.getDefault().getLanguage())) {
			translateText.setVisibility(View.VISIBLE);
			translateText.setTextColor(settings.getHighlightColor());
		}
		username.setText(author.getUsername());
		screenName.setText(author.getScreenname());
		createdAt.setText(SimpleDateFormat.getDateTimeInstance().format(status.getTimestamp()));
		replyButton.setText(StringTools.NUMBER_FORMAT.format(status.getReplyCount()));
		likeButton.setText(StringTools.NUMBER_FORMAT.format(status.getFavoriteCount()));
		repostButton.setText(StringTools.NUMBER_FORMAT.format(status.getRepostCount()));
		if (!status.getSource().isEmpty()) {
			statusApi.setText(R.string.status_sent_from);
			statusApi.append(status.getSource());
			statusApi.setVisibility(View.VISIBLE);
		} else {
			statusApi.setVisibility(View.GONE);
		}
		if (statusText.getText().length() == 0) {
			if (!status.getText().isEmpty()) {
				spannableText = Tagger.makeTextWithLinks(status.getText(), settings.getHighlightColor(), this);
				statusText.setVisibility(View.VISIBLE);
				statusText.setText(spannableText);
				setTranslation();
			} else {
				statusText.setVisibility(View.GONE);
			}
		}
		if (status.getRepliedStatusId() > 0) {
			if (!status.getReplyName().isEmpty())
				replyName.setText(status.getReplyName());
			else
				replyName.setText(R.string.status_replyname_empty);
			replyName.setVisibility(View.VISIBLE);
		} else {
			replyName.setVisibility(View.GONE);
		}
		if (status.isSensitive()) {
			sensitive.setVisibility(View.VISIBLE);
		} else {
			sensitive.setVisibility(View.GONE);
		}
		if (status.isSpoiler()) {
			spoiler.setVisibility(View.VISIBLE);
			if (settings.hideSensitiveEnabled()) {
				spoilerHint.setVisibility(View.VISIBLE);
				statusText.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
				float radius = statusText.getTextSize() / 3;
				BlurMaskFilter filter = new BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL);
				statusText.getPaint().setMaskFilter(filter);
			} else {
				spoilerHint.setVisibility(View.INVISIBLE);
			}
		} else {
			spoiler.setVisibility(View.GONE);
			spoilerHint.setVisibility(View.INVISIBLE);
		}
		String profileImageUrl = author.getProfileImageThumbnailUrl();
		if (settings.imagesEnabled() && !profileImageUrl.isEmpty()) {
			Transformation roundCorner = new RoundedCornersTransformation(4, 0);
			picasso.load(profileImageUrl).transform(roundCorner).error(R.drawable.no_image).into(profileImage);
		} else {
			profileImage.setImageResource(0);
		}
		if (location != null) {
			locationName.setVisibility(View.VISIBLE);
			if (!location.getPlace().isEmpty()) {
				locationName.setText(location.getFullName());
			} else {
				locationName.setText("");
			}
			if (!location.getCoordinates().isEmpty()) {
				locationName.append(" " + location.getCoordinates());
			}
		} else {
			locationName.setVisibility(View.GONE);
		}
		if (repostButton.getVisibility() != View.VISIBLE) {
			repostButton.setVisibility(View.VISIBLE);
			likeButton.setVisibility(View.VISIBLE);
			replyButton.setVisibility(View.VISIBLE);
		}
		if ((status.getCards().length > 0 || status.getMedia().length > 0) || status.getPoll() != null) {
			cardList.setVisibility(View.VISIBLE);
			adapter.replaceAll(status);
			statusText.setMaxLines(5);
		} else {
			cardList.setVisibility(View.GONE);
			statusText.setMaxLines(10);
		}
		if (status.getEmojis().length > 0 && spannableText != null) {
			EmojiParam param = new EmojiParam(status.getEmojis(), spannableText, getResources().getDimensionPixelSize(R.dimen.page_status_icon_size));
			emojiAsync.execute(param, statusTextUpdate);
		}
		if (author.getEmojis().length > 0 && !author.getUsername().isEmpty()) {
			SpannableString usernameSpan = new SpannableString(author.getUsername());
			EmojiParam param = new EmojiParam(author.getEmojis(), usernameSpan, getResources().getDimensionPixelSize(R.dimen.page_status_icon_size));
			emojiAsync.execute(param, usernameUpdate);
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
	 * append translation to the status text
	 */
	private void setTranslation() {
		if (translation != null) {
			if (statusText.getLineCount() > statusText.getMaxLines()) {
				int y = statusText.getLayout().getLineTop(statusText.getLineCount());
				statusText.scrollTo(0, y);
			}
			// build translation string
			String text = "\n...\n" + translation.getText() + "\n...";
			Spannable textSpan = Tagger.makeTextWithLinks(text, settings.getHighlightColor(), this);
			// append translation
			statusText.append(textSpan);
			translateText.setText(R.string.status_translate_source);
			translateText.append(translation.getSource());
			// scroll to translation

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
					statusAsync.execute(param, statusCallback);
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
				if (status != null) {
					Toast.makeText(getApplicationContext(), R.string.info_status_removed, Toast.LENGTH_SHORT).show();
					Intent intent = new Intent();
					if (status.getEmbeddedStatus() != null)
						intent.putExtra(INTENT_STATUS_REMOVED_ID, status.getEmbeddedStatus().getId());
					else
						intent.putExtra(INTENT_STATUS_REMOVED_ID, status.getId());
					setResult(RETURN_STATUS_REMOVED, intent);
					finish();
				}
				break;

			case StatusResult.ERROR:
				String message = ErrorHandler.getErrorMessage(this, result.exception);
				Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
				if (status == null) {
					finish();
				} else if (result.exception != null && result.exception.getErrorCode() == ConnectionException.RESOURCE_NOT_FOUND) {
					// Mark status as removed, so it can be removed from the list
					Intent intent = new Intent();
					intent.putExtra(INTENT_STATUS_REMOVED_ID, status.getId());
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
					notificationAsync.execute(param, notificationCallback);
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
					intent.putExtra(INTENT_NOTIFICATION_REMOVED_ID, notification.getId());
					setResult(RETURN_NOTIFICATION_REMOVED, intent);
				}
				Toast.makeText(getApplicationContext(), R.string.info_notification_dismiss, Toast.LENGTH_SHORT).show();
				finish();
				break;

			case NotificationActionResult.ERROR:
				String message = ErrorHandler.getErrorMessage(this, result.exception);
				Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
				if (notification == null) {
					finish();
				} else if (result.exception != null && result.exception.getErrorCode() == ConnectionException.RESOURCE_NOT_FOUND) {
					Intent intent = new Intent();
					intent.putExtra(INTENT_NOTIFICATION_REMOVED_ID, notification.getId());
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
				String message = ErrorHandler.getErrorMessage(this, result.exception);
				Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
				break;
		}
	}

	/**
	 *
	 * @param result status translation result
	 */
	private void onTranslationResult(@NonNull TranslationResult result) {
		if (result.translation != null) {
			translation = result.translation;
			setTranslation();
		} else {
			String message = ErrorHandler.getErrorMessage(this, result.exception);
			Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * set emojis, replace emoji tags with images
	 */
	private void onStatusTextUpdate(@NonNull EmojiResult result) {
		if (settings.getLogin().getConfiguration() == Configuration.MASTODON && result.images != null) {
			Spannable spannable = TextWithEmoji.addEmojis(getApplicationContext(), result.spannable, result.images);
			statusText.setText(spannable);
		}
	}

	/**
	 * set emojis, replace emoji tags with images
	 */
	private void onUsernameUpdate(@NonNull EmojiResult result) {
		if (settings.getLogin().getConfiguration() == Configuration.MASTODON && result.images != null) {
			Spannable spannable = TextWithEmoji.addEmojis(getApplicationContext(), result.spannable, result.images);
			username.setText(spannable);
		}
	}
}