package org.nuclearfog.twidda.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.holder.LoginHolder;
import org.nuclearfog.twidda.backend.model.Account;
import org.nuclearfog.twidda.backend.model.User;
import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.fragment.AccountFragment;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;
import static org.nuclearfog.twidda.adapter.holder.LoginHolder.IDX_CREATED;
import static org.nuclearfog.twidda.adapter.holder.LoginHolder.IDX_SCR_NAME;
import static org.nuclearfog.twidda.adapter.holder.LoginHolder.IDX_USERNAME;

/**
 * adapter for {@link AccountFragment}
 *
 * @author nuclearfog
 */
public class AccountAdapter extends Adapter<LoginHolder> {

    private List<Account> data = new ArrayList<>();
    private GlobalSettings settings;
    private OnAccountClickListener listener;

    /**
     * @param settings app settings for theme
     * @param listener item click listener
     */
    public AccountAdapter(GlobalSettings settings, OnAccountClickListener listener) {
        this.settings = settings;
        this.listener = listener;
    }


    @NonNull
    @Override
    public LoginHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LoginHolder holder = new LoginHolder(parent, settings);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getLayoutPosition();
                if (position != NO_POSITION) {
                    Account account = data.get(position);
                    listener.onAccountClick(account);
                }
            }
        });
        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getLayoutPosition();
                if (position != NO_POSITION) {
                    Account account = data.get(position);
                    listener.onAccountRemove(account);
                }
            }
        });
        return holder;
    }


    @Override
    public void onBindViewHolder(@NonNull LoginHolder holder, int position) {
        Account account = data.get(position);
        User user = account.getUser();
        String date = StringTools.formatCreationTime(account.getLoginDate());
        holder.text[IDX_CREATED].setText(date);
        if (user != null) {
            // set profile information
            holder.text[IDX_USERNAME].setText(user.getUsername());
            holder.text[IDX_SCR_NAME].setText(user.getScreenname());
            // set profile image
            if (settings.imagesEnabled()) {
                String pbLink = user.getImageLink();
                if (!user.hasDefaultProfileImage()) {
                    pbLink += settings.getImageSuffix();
                }
                Picasso.get().load(pbLink).transform(new RoundedCornersTransformation(2, 0))
                        .error(R.drawable.no_image).into(holder.profile);
            }
        } else {
            holder.profile.setImageResource(0);
            holder.text[IDX_USERNAME].setText(R.string.account_user_unnamed);
            holder.text[IDX_SCR_NAME].setText(R.string.account_user_id_prefix);
            holder.text[IDX_SCR_NAME].append(Long.toString(account.getId()));
        }
    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    /**
     * sets login data
     *
     * @param data list with login items
     */
    @MainThread
    public void setData(List<Account> data) {
        this.data.clear();
        this.data.addAll(data);
        notifyDataSetChanged();
    }

    /**
     * clear adapter data
     */
    @MainThread
    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }

    /**
     * click listener for an account item
     */
    public interface OnAccountClickListener {

        /**
         * called on item select
         *
         * @param account selected account information
         */
        void onAccountClick(Account account);

        /**
         * called to remove item
         *
         * @param account selected account information
         */
        void onAccountRemove(Account account);
    }
}