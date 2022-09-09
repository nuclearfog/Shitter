package org.nuclearfog.twidda.adapter.holder;

import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.database.GlobalSettings;

/**
 * Holder class for a message view
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.adapter.MessageAdapter
 */
public class MessageHolder extends ViewHolder {

	public final TextView username, screenname, receiver, time, message;
	public final ImageView profile, verifiedIcon, lockedIcon;
	public final ImageButton mediaButton;
	public final Button answer, delete;

	/**
	 * @param parent Parent view from adapter
	 */
	public MessageHolder(ViewGroup parent, GlobalSettings settings) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false));
		CardView background = (CardView) itemView;
		ViewGroup container = itemView.findViewById(R.id.item_message_container);
		profile = itemView.findViewById(R.id.item_message_profile);
		verifiedIcon = itemView.findViewById(R.id.item_message_verified);
		lockedIcon = itemView.findViewById(R.id.item_message_private);
		mediaButton = itemView.findViewById(R.id.item_message_media);
		username = itemView.findViewById(R.id.item_message_username);
		screenname = itemView.findViewById(R.id.item_message_screenname);
		receiver = itemView.findViewById(R.id.item_message_receiver);
		time = itemView.findViewById(R.id.item_message_time);
		message = itemView.findViewById(R.id.item_message_text);
		answer = itemView.findViewById(R.id.item_message_answer);
		delete = itemView.findViewById(R.id.item_message_delete);

		AppStyles.setTheme(container, 0);
		background.setCardBackgroundColor(settings.getCardColor());
		message.setMovementMethod(LinkMovementMethod.getInstance());
	}
}