package org.nuclearfog.twidda.ui.adapter.recyclerview.holder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.image.PicassoBuilder;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Reaction;

/**
 * @author nuclearfog
 */
public class ReactionHolder extends ViewHolder implements OnClickListener {

	private ImageView icon;
	private TextView description;

	private OnHolderClickListener listener;
	private GlobalSettings settings;
	private Picasso picasso;

	public ReactionHolder(ViewGroup parent, OnHolderClickListener listener) {
		super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reaction, parent, false));
		icon = itemView.findViewById(R.id.item_reaction_icon);
		description = itemView.findViewById(R.id.item_reaction_text);
		picasso = PicassoBuilder.get(parent.getContext());
		settings = GlobalSettings.get(parent.getContext());
		this.listener = listener;

		description.setTextColor(settings.getTextColor());

		itemView.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.item_reaction_root) {
			int position = getLayoutPosition();
			if (position != RecyclerView.NO_POSITION) {
				listener.onItemClick(position, OnHolderClickListener.ANNOUNCEMENT_REACTION);
			}
		}
	}

	/**
	 *
	 */
	public void setContent(Reaction reaction) {
		if (!reaction.getImageUrl().isEmpty() && settings.imagesEnabled()) {
			icon.setVisibility(View.VISIBLE);
			picasso.load(reaction.getImageUrl()).into(icon);
			description.setText("");
		} else {
			icon.setVisibility(View.GONE);
			icon.setImageResource(0);
			description.setText(reaction.getName() + " ");
		}
		description.append(Integer.toString(reaction.getCount()));
	}
}