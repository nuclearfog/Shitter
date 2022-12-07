package org.nuclearfog.twidda.ui.activities;

import static android.os.AsyncTask.Status.RUNNING;
import static android.view.View.GONE;
import static android.view.View.OnClickListener;
import static android.view.View.OnLongClickListener;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_SHORT;
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
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.nuclearfog.tag.Tagger;
import org.nuclearfog.tag.Tagger.OnTagClickListener;
import org.nuclearfog.textviewtool.LinkAndScrollMovement;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.async.StatusAction;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.backend.utils.PicassoBuilder;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.OnConfirmListener;
import org.nuclearfog.twidda.ui.dialogs.LinkDialog;
import org.nuclearfog.twidda.ui.fragments.StatusFragment;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * Status Activity to show status and user information
 *
 * @author nuclearfog
 */
public class StatusActivity extends AppCompatActivity implements OnClickListener,
		OnLongClickListener, OnTagClickListener, OnConfirmListener {

	/**
	 * Activity result code to update existing status information
	 */
	public static final int RETURN_STATUS_UPDATE = 0x789CD38B;

	/**
	 * Activity result code if a status was not found or removed
	 */
	public static final int RETURN_STATUS_REMOVED = 0x8B03DB84;

	/**
	 * key used for status information
	 * value type is {@link Status}
	 * If no status object exists, {@link #KEY_STATUS_ID} and {@link #KEY_STATUS_NAME} will be used instead
	 */
	public static final String KEY_STATUS_DATA = "status_data";

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
	 * key to return updated status information
	 * value type is {@link Status}
	 */
	public static final String INTENT_STATUS_UPDATE_DATA = "status_update_data";

	/**
	 * key to return an ID if status was deleted
	 * value type is Long
	 */
	public static final String INTENT_STATUS_REMOVED_ID = "status_removed_id";

	/**
	 * regex pattern of a status URL
	 */
	public static final Pattern LINK_PATTERN = Pattern.compile("https://twitter.com/\\w+/status/\\d+");

	private static final int MENU_GROUP_COPY = 0x157426;

	@Nullable
	private ClipboardManager clip;
	private GlobalSettings settings;
	private StatusAction statusAsync;
	private Picasso picasso;

	private LinkDialog linkPreview;
	private ConfirmDialog confirmDialog;

	private TextView statusApi, createdAt, statusText, screenName, userName, locationName, sensitive_media;
	private Button ansButton, rtwButton, favButton, replyName, coordinates, repostName;
	private ImageView profileImage, mediaButton;
	private Toolbar toolbar;

	@Nullable
	private Status status;
	private long id;
	private boolean hidden;


	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(AppStyles.setFontScale(newBase));
	}


	@Override
	protected void onCreate(@Nullable Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.page_status);
		ViewGroup root = findViewById(R.id.page_status_root);
		toolbar = findViewById(R.id.page_status_toolbar);
		ansButton = findViewById(R.id.page_status_reply);
		rtwButton = findViewById(R.id.page_status_repost);
		favButton = findViewById(R.id.page_status_favorite);
		userName = findViewById(R.id.page_status_username);
		screenName = findViewById(R.id.page_status_screenname);
		profileImage = findViewById(R.id.page_status_profile);
		replyName = findViewById(R.id.page_status_reply_reference);
		statusText = findViewById(R.id.page_status_text);
		createdAt = findViewById(R.id.page_status_date);
		statusApi = findViewById(R.id.page_status_api);
		locationName = findViewById(R.id.page_status_location_name);
		coordinates = findViewById(R.id.page_status_location_coordinates);
		mediaButton = findViewById(R.id.page_status_media_attach);
		sensitive_media = findViewById(R.id.page_status_sensitive);
		repostName = findViewById(R.id.page_status_reposter_reference);

		// get parameter
		Object data = getIntent().getSerializableExtra(KEY_STATUS_DATA);
		String username;
		if (data instanceof Status) {
			status = (Status) data;
			Status embedded = status.getEmbeddedStatus();
			if (embedded != null) {
				username = embedded.getAuthor().getScreenname();
				id = embedded.getId();
			} else {
				username = status.getAuthor().getScreenname();
				id = status.getId();
				hidden = status.isHidden();
			}
		} else {
			username = getIntent().getStringExtra(KEY_STATUS_NAME);
			id = getIntent().getLongExtra(KEY_STATUS_ID, -1);
		}

		// create list fragment for status replies
		Bundle param = new Bundle();
		param.putInt(KEY_STATUS_FRAGMENT_MODE, STATUS_FRAGMENT_REPLY);
		param.putString(KEY_STATUS_FRAGMENT_SEARCH, username);
		param.putLong(KEY_STATUS_FRAGMENT_ID, id);

		// insert fragment into view
		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.page_status_reply_fragment, StatusFragment.class, param);
		fragmentTransaction.commit();

		clip = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

		settings = GlobalSettings.getInstance(this);
		ansButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.answer, 0, 0, 0);
		rtwButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.repost, 0, 0, 0);
		coordinates.setCompoundDrawablesWithIntrinsicBounds(R.drawable.location, 0, 0, 0);
		sensitive_media.setCompoundDrawablesWithIntrinsicBounds(R.drawable.sensitive, 0, 0, 0);
		replyName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.back, 0, 0, 0);
		repostName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.repost, 0, 0, 0);
		statusText.setMovementMethod(LinkAndScrollMovement.getInstance());
		statusText.setLinkTextColor(settings.getHighlightColor());
		if (settings.likeEnabled()) {
			favButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.like, 0, 0, 0);
		} else {
			favButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.favorite, 0, 0, 0);
		}
		toolbar.setTitle("");
		setSupportActionBar(toolbar);
		AppStyles.setTheme(root, settings.getBackgroundColor());
		picasso = PicassoBuilder.get(this);

		linkPreview = new LinkDialog(this);
		confirmDialog = new ConfirmDialog(this);

		confirmDialog.setConfirmListener(this);
		repostName.setOnClickListener(this);
		replyName.setOnClickListener(this);
		ansButton.setOnClickListener(this);
		rtwButton.setOnClickListener(this);
		favButton.setOnClickListener(this);
		profileImage.setOnClickListener(this);
		coordinates.setOnClickListener(this);
		mediaButton.setOnClickListener(this);
		rtwButton.setOnLongClickListener(this);
		favButton.setOnLongClickListener(this);
		repostName.setOnLongClickListener(this);
		coordinates.setOnLongClickListener(this);
	}


	@Override
	protected void onStart() {
		super.onStart();
		if (statusAsync == null) {
			// print status object and get and update it
			if (status != null) {
				statusAsync = new StatusAction(this, StatusAction.LOAD_ONLINE);
				statusAsync.execute(status.getId());
				setStatus(status);
			}
			// Load status from database first if no status is defined
			else {
				statusAsync = new StatusAction(this, StatusAction.LOAD_DATABASE);
				statusAsync.execute(id);
			}
		}
	}


	@Override
	protected void onDestroy() {
		if (statusAsync != null && statusAsync.getStatus() == RUNNING)
			statusAsync.cancel(true);
		linkPreview.cancel();
		super.onDestroy();
	}


	@Override
	public void onBackPressed() {
		Intent returnData = new Intent();
		returnData.putExtra(INTENT_STATUS_UPDATE_DATA, status);
		setResult(RETURN_STATUS_UPDATE, returnData);
		super.onBackPressed();
	}


	@Override
	public boolean onCreateOptionsMenu(@NonNull Menu m) {
		getMenuInflater().inflate(R.menu.status, m);
		AppStyles.setOverflowIcon(toolbar, settings.getIconColor());
		return super.onCreateOptionsMenu(m);
	}


	@Override
	public boolean onPrepareOptionsMenu(@NonNull Menu m) {
		if (status == null)
			return super.onPrepareOptionsMenu(m);

		MenuItem optDelete = m.findItem(R.id.menu_status_delete);
		MenuItem optHide = m.findItem(R.id.menu_status_hide);
		MenuItem optCopy = m.findItem(R.id.menu_status_copy);
		MenuItem optMetrics = m.findItem(R.id.menu_status_metrics);
		SubMenu copyMenu = optCopy.getSubMenu();

		Status status = this.status;
		if (status.getEmbeddedStatus() != null) {
			status = status.getEmbeddedStatus();
		}
		if (status.getRepliedUserId() == settings.getLogin().getId() && status.getAuthor().getId() != settings.getLogin().getId()) {
			optHide.setVisible(true);
			if (hidden) {
				optHide.setTitle(R.string.menu_tweet_unhide);
			} else {
				optHide.setTitle(R.string.menu_tweet_hide);
			}
		}
		if (status.getAuthor().isCurrentUser()) {
			optDelete.setVisible(true);
			if (new Date().getTime() - status.getTimestamp() < 2419200000L) {
				optMetrics.setVisible(true);
			}
		}
		// add media link items
		// check if menu doesn't contain media links already
		if (copyMenu.size() == 2) {
			int mediaCount = status.getMediaUris().length;
			for (int i = 0; i < mediaCount; i++) {
				// create sub menu entry and use array index as item ID
				String text = getString(R.string.menu_media_link) + ' ' + (i + 1);
				copyMenu.add(MENU_GROUP_COPY, i, Menu.NONE, text);
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
		// hide status
		else if (item.getItemId() == R.id.menu_status_hide) {
			if (hidden) {
				statusAsync = new StatusAction(this, StatusAction.UNHIDE);
			} else {
				statusAsync = new StatusAction(this, StatusAction.HIDE);
			}
			statusAsync.execute(this.status.getId());
		}
		// get status link
		else if (item.getItemId() == R.id.menu_status_browser) {
			String link = settings.getLogin().getHostname() + status.getLinkPath();
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
			try {
				startActivity(intent);
			} catch (ActivityNotFoundException err) {
				Toast.makeText(this, R.string.error_connection_failed, LENGTH_SHORT).show();
			}
		}
		// copy status link to clipboard
		else if (item.getItemId() == R.id.menu_status_copy_text) {
			if (clip != null) {
				ClipData linkClip = ClipData.newPlainText("status text", status.getText());
				clip.setPrimaryClip(linkClip);
				Toast.makeText(this, R.string.info_tweet_text_copied, LENGTH_SHORT).show();
			}
		}
		// copy status link to clipboard
		else if (item.getItemId() == R.id.menu_status_copy_link) {
			if (clip != null) {
				String link = settings.getLogin().getHostname() + status.getLinkPath();
				ClipData linkClip = ClipData.newPlainText("status link", link);
				clip.setPrimaryClip(linkClip);
				Toast.makeText(this, R.string.info_tweet_link_copied, LENGTH_SHORT).show();
			}
		}
		// open status metrics page
		else if (item.getItemId() == R.id.menu_status_metrics) {
			Intent metricsIntent = new Intent(getApplicationContext(), MetricsActivity.class);
			metricsIntent.putExtra(MetricsActivity.KEY_METRICS_STATUS, status);
			startActivity(metricsIntent);
		}
		// copy media links
		else if (item.getGroupId() == MENU_GROUP_COPY) {
			int index = item.getItemId();
			Uri[] mediaLinks = status.getMediaUris();
			if (index >= 0 && index < mediaLinks.length) {
				if (clip != null) {
					ClipData linkClip = ClipData.newPlainText("status media link", mediaLinks[index].toString());
					clip.setPrimaryClip(linkClip);
					Toast.makeText(this, R.string.info_tweet_medialink_copied, LENGTH_SHORT).show();
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
				Intent userList = new Intent(this, UsersActivity.class);
				userList.putExtra(KEY_USERS_ID, status.getId());
				userList.putExtra(KEY_USERS_MODE, USERS_REPOST);
				startActivity(userList);
			}
			// show user favoriting this status
			else if (v.getId() == R.id.page_status_favorite) {
				Intent userList = new Intent(this, UsersActivity.class);
				userList.putExtra(KEY_USERS_ID, status.getId());
				userList.putExtra(KEY_USERS_MODE, USERS_FAVORIT);
				startActivity(userList);
			}
			// open profile of the status author
			else if (v.getId() == R.id.page_status_profile) {
				Intent profile = new Intent(getApplicationContext(), ProfileActivity.class);
				profile.putExtra(ProfileActivity.KEY_PROFILE_DATA, status.getAuthor());
				startActivity(profile);
			}
			// open replied status
			else if (v.getId() == R.id.page_status_reply_reference) {
				Intent answerIntent = new Intent(getApplicationContext(), StatusActivity.class);
				answerIntent.putExtra(KEY_STATUS_ID, status.getRepliedStatusId());
				answerIntent.putExtra(KEY_STATUS_NAME, status.getReplyName());
				startActivity(answerIntent);
			}
			// open status location coordinates
			else if (v.getId() == R.id.page_status_location_coordinates) {
				Intent locationIntent = new Intent(Intent.ACTION_VIEW);
				locationIntent.setData(Uri.parse("geo:" + status.getLocationCoordinates() + "?z=14"));
				try {
					startActivity(locationIntent);
				} catch (ActivityNotFoundException err) {
					Toast.makeText(getApplicationContext(), R.string.error_no_card_app, LENGTH_SHORT).show();
				}
			}
			// open status media
			else if (v.getId() == R.id.page_status_media_attach) {
				// open embedded image links
				if (status.getMediaType() == Status.MEDIA_PHOTO) {
					Intent mediaIntent = new Intent(this, ImageViewer.class);
					mediaIntent.putExtra(ImageViewer.IMAGE_URIS, status.getMediaUris());
					mediaIntent.putExtra(ImageViewer.IMAGE_DOWNLOAD, true);
					startActivity(mediaIntent);
				}
				// open embedded video link
				else if (status.getMediaType() == Status.MEDIA_VIDEO) {
					if (!settings.isProxyEnabled() || (settings.isProxyEnabled() && settings.ignoreProxyWarning())) {
						Uri link = status.getMediaUris()[0];
						Intent mediaIntent = new Intent(this, VideoViewer.class);
						mediaIntent.putExtra(VideoViewer.VIDEO_URI, link);
						mediaIntent.putExtra(VideoViewer.ENABLE_VIDEO_CONTROLS, true);
						startActivity(mediaIntent);
					} else {
						confirmDialog.show(ConfirmDialog.PROXY_CONFIRM);
					}
				}
				// open embedded gif link
				else if (status.getMediaType() == Status.MEDIA_GIF) {
					Uri link = status.getMediaUris()[0];
					Intent mediaIntent = new Intent(this, VideoViewer.class);
					mediaIntent.putExtra(VideoViewer.VIDEO_URI, link);
					mediaIntent.putExtra(VideoViewer.ENABLE_VIDEO_CONTROLS, false);
					startActivity(mediaIntent);
				}
			}
			// go to user reposting this status
			else if (v.getId() == R.id.page_status_reposter_reference) {
				Intent profile = new Intent(getApplicationContext(), ProfileActivity.class);
				profile.putExtra(ProfileActivity.KEY_PROFILE_DATA, this.status.getAuthor());
				startActivity(profile);
			}
		}
	}


	@Override
	public boolean onLongClick(View v) {
		if (status != null && (statusAsync == null || statusAsync.getStatus() != RUNNING)) {
			// repost this status
			if (v.getId() == R.id.page_status_repost) {
				if (status.isReposted()) {
					statusAsync = new StatusAction(this, StatusAction.REMOVE_REPOST);
				} else {
					statusAsync = new StatusAction(this, StatusAction.REPOST);
				}

				if (status.getEmbeddedStatus() != null)
					statusAsync.execute(status.getId(), status.getEmbeddedStatus().getRepostId());
				else
					statusAsync.execute(status.getId());
				Toast.makeText(this, R.string.info_loading, LENGTH_SHORT).show();
				return true;
			}
			// favorite this status
			else if (v.getId() == R.id.page_status_favorite) {
				if (status.isFavorited()) {
					statusAsync = new StatusAction(this, StatusAction.UNFAVORITE);
				} else {
					statusAsync = new StatusAction(this, StatusAction.FAVORITE);
				}
				statusAsync.execute(status.getId());
				Toast.makeText(this, R.string.info_loading, LENGTH_SHORT).show();
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
			}
			// copy status coordinates
			else if (v.getId() == R.id.page_status_location_coordinates) {
				if (clip != null) {
					ClipData linkClip;
					Status embeddedStatus = status.getEmbeddedStatus();
					if (embeddedStatus != null)
						linkClip = ClipData.newPlainText("Status location coordinates", embeddedStatus.getLocationCoordinates());
					else
						linkClip = ClipData.newPlainText("Status location coordinates", status.getLocationCoordinates());
					clip.setPrimaryClip(linkClip);
					Toast.makeText(this, R.string.info_tweet_location_copied, LENGTH_SHORT).show();
				}
			}
		}
		return false;
	}


	@Override
	public void onConfirm(int type, boolean rememberChoice) {
		if (status != null) {
			Status status = this.status;
			if (status.getEmbeddedStatus() != null) {
				status = status.getEmbeddedStatus();
			}
			// delete status
			if (type == ConfirmDialog.DELETE_STATUS) {
				statusAsync = new StatusAction(this, StatusAction.DELETE);
				statusAsync.execute(status.getId(), status.getRepostId());
			}
			// confirm playing video without proxy
			else if (type == ConfirmDialog.PROXY_CONFIRM) {
				settings.setIgnoreProxyWarning(rememberChoice);
				Uri link = status.getMediaUris()[0];
				Intent mediaIntent = new Intent(this, VideoViewer.class);
				mediaIntent.putExtra(VideoViewer.VIDEO_URI, link);
				mediaIntent.putExtra(VideoViewer.ENABLE_VIDEO_CONTROLS, true);
				startActivity(mediaIntent);
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
		if (LINK_PATTERN.matcher(link.getScheme() + "://" + link.getHost() + link.getPath()).matches()) {
			List<String> segments = link.getPathSegments();
			Intent intent = new Intent(this, StatusActivity.class);
			intent.putExtra(KEY_STATUS_ID, Long.parseLong(segments.get(2)));
			intent.putExtra(KEY_STATUS_NAME, segments.get(0));
			startActivity(intent);
		}
		// open link in browser or preview
		else {
			if (settings.linkPreviewEnabled()) {
				linkPreview.show(tag);
			} else {
				// open link in a browser
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(link);
				try {
					startActivity(intent);
				} catch (ActivityNotFoundException err) {
					Toast.makeText(this, R.string.error_connection_failed, LENGTH_SHORT).show();
				}
			}
		}
	}


	/**
	 * load status into UI
	 *
	 * @param status Tweet information
	 */
	public void setStatus(Status status) {
		this.status = status;
		if (status.getEmbeddedStatus() != null) {
			repostName.setVisibility(VISIBLE);
			repostName.setText(status.getAuthor().getScreenname());
			status = status.getEmbeddedStatus();
		} else {
			repostName.setVisibility(GONE);
		}
		User author = status.getAuthor();
		invalidateOptionsMenu();

		NumberFormat buttonNumber = NumberFormat.getIntegerInstance();
		if (status.isReposted()) {
			AppStyles.setDrawableColor(rtwButton, settings.getRepostIconColor());
		} else {
			AppStyles.setDrawableColor(rtwButton, settings.getIconColor());
		}
		if (status.isFavorited()) {
			AppStyles.setDrawableColor(favButton, settings.getFavoriteIconColor());
		} else {
			AppStyles.setDrawableColor(favButton, settings.getIconColor());
		}
		if (author.isVerified()) {
			userName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.verify, 0, 0, 0);
			AppStyles.setDrawableColor(userName, settings.getIconColor());
		} else {
			userName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		}
		if (author.isProtected()) {
			screenName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock, 0, 0, 0);
			AppStyles.setDrawableColor(screenName, settings.getIconColor());
		} else {
			screenName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		}
		userName.setText(author.getUsername());
		screenName.setText(author.getScreenname());
		createdAt.setText(SimpleDateFormat.getDateTimeInstance().format(status.getTimestamp()));
		ansButton.setText(buttonNumber.format(status.getReplyCount()));
		favButton.setText(buttonNumber.format(status.getFavoriteCount()));
		rtwButton.setText(buttonNumber.format(status.getRepostCount()));
		statusApi.setText(R.string.tweet_sent_from);
		statusApi.append(status.getSource());

		if (!status.getText().isEmpty()) {
			Spannable spannableText = Tagger.makeTextWithLinks(status.getText(), settings.getHighlightColor(), this);
			statusText.setVisibility(VISIBLE);
			statusText.setText(spannableText);
		} else {
			statusText.setVisibility(GONE);
		}
		if (status.getRepliedStatusId() > 0) {
			if (!status.getReplyName().isEmpty())
				replyName.setText(status.getReplyName());
			else
				replyName.setText(R.string.status_replyname_empty);
			replyName.setVisibility(VISIBLE);
		} else {
			replyName.setVisibility(GONE);
		}
		if (status.isSensitive()) {
			sensitive_media.setVisibility(VISIBLE);
		} else {
			sensitive_media.setVisibility(GONE);
		}
		switch (status.getMediaType()) {
			case Status.MEDIA_PHOTO:
				mediaButton.setVisibility(VISIBLE);
				mediaButton.setImageResource(R.drawable.image);
				break;

			case Status.MEDIA_VIDEO:
				mediaButton.setVisibility(VISIBLE);
				mediaButton.setImageResource(R.drawable.video);
				break;

			case Status.MEDIA_GIF:
				mediaButton.setVisibility(VISIBLE);
				mediaButton.setImageResource(R.drawable.gif);
				break;

			default:
				mediaButton.setVisibility(GONE);
				mediaButton.setImageResource(0);
				break;
		}
		AppStyles.setDrawableColor(mediaButton, settings.getIconColor());
		String profileImageUrl = author.getProfileImageThumbnailUrl();
		if (settings.imagesEnabled() && !profileImageUrl.isEmpty()) {
			Transformation roundCorner = new RoundedCornersTransformation(4, 0);
			picasso.load(profileImageUrl).transform(roundCorner).error(R.drawable.no_image).into(profileImage);
		} else {
			profileImage.setImageResource(0);
		}
		String placeName = status.getLocationName();
		if (placeName != null && !placeName.isEmpty()) {
			locationName.setVisibility(VISIBLE);
			locationName.setText(placeName);
		} else {
			locationName.setVisibility(GONE);
		}
		String location = status.getLocationCoordinates();
		if (!location.isEmpty()) {
			coordinates.setVisibility(VISIBLE);
			coordinates.setText(location);
		} else {
			coordinates.setVisibility(GONE);
		}
		if (rtwButton.getVisibility() != VISIBLE) {
			rtwButton.setVisibility(VISIBLE);
			favButton.setVisibility(VISIBLE);
			ansButton.setVisibility(VISIBLE);
		}
	}

	/**
	 * called after a status action
	 *
	 * @param action action type
	 */
	public void OnSuccess(int action) {
		switch (action) {
			case StatusAction.REPOST:
				Toast.makeText(this, R.string.info_tweet_retweeted, LENGTH_SHORT).show();
				break;

			case StatusAction.REMOVE_REPOST:
				Toast.makeText(this, R.string.info_tweet_unretweeted, LENGTH_SHORT).show();
				// todo remove old retweet from list fragment
				break;

			case StatusAction.FAVORITE:
				if (settings.likeEnabled())
					Toast.makeText(this, R.string.info_tweet_liked, LENGTH_SHORT).show();
				else
					Toast.makeText(this, R.string.info_tweet_favored, LENGTH_SHORT).show();
				break;

			case StatusAction.UNFAVORITE:
				if (settings.likeEnabled())
					Toast.makeText(this, R.string.info_tweet_unliked, LENGTH_SHORT).show();
				else
					Toast.makeText(this, R.string.info_tweet_unfavored, LENGTH_SHORT).show();
				break;

			case StatusAction.HIDE:
				hidden = true;
				invalidateOptionsMenu();
				Toast.makeText(this, R.string.info_reply_hidden, LENGTH_SHORT).show();
				break;

			case StatusAction.UNHIDE:
				hidden = false;
				invalidateOptionsMenu();
				Toast.makeText(this, R.string.info_reply_unhidden, LENGTH_SHORT).show();
				break;

			case StatusAction.DELETE:
				if (status != null) {
					Toast.makeText(this, R.string.info_tweet_removed, LENGTH_SHORT).show();
					Intent returnData = new Intent();
					if (status.getEmbeddedStatus() != null)
						returnData.putExtra(INTENT_STATUS_REMOVED_ID, status.getEmbeddedStatus().getId());
					else
						returnData.putExtra(INTENT_STATUS_REMOVED_ID, status.getId());
					setResult(RETURN_STATUS_REMOVED, returnData);
					finish();
				}
				break;
		}
	}

	/**
	 * called when an error occurs
	 *
	 * @param error Error information
	 */
	public void onError(@Nullable ConnectionException error) {
		ErrorHandler.handleFailure(this, error);
		if (status == null) {
			finish();
		} else {
			if (error != null && error.getErrorCode() == ConnectionException.RESOURCE_NOT_FOUND) {
				// Mark status as removed, so it can be removed from the list
				Intent returnData = new Intent();
				returnData.putExtra(INTENT_STATUS_REMOVED_ID, status.getId());
				setResult(RETURN_STATUS_REMOVED, returnData);
				finish();
			}
		}
	}
}