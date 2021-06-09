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

/**
 * adapter for {@link AccountFragment}
 *
 * @author nuclearfog
 */
public class AccountAdapter extends Adapter<LoginHolder> {

    private List<Account> data = new ArrayList<>();
    private GlobalSettings settings;
    private OnLoginClickListener listener;

    /**
     *
     */
    public AccountAdapter(GlobalSettings settings, OnLoginClickListener l) {
        this.settings = settings;
        this.listener = l;
    }

    /**
     * sets login data
     *
     * @param data list with login items
     */
    public void setData(List<Account> data) {
        this.data.clear();
        this.data.addAll(data);
        notifyDataSetChanged();
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
                    listener.onLoginClick(account);
                }
            }
        });
        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getLayoutPosition();
                if (position != NO_POSITION) {
                    Account account = data.get(position);
                    listener.onDeleteClick(account);
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
        holder.date.setText(date);
        if (user != null) {
            holder.username.setText(user.getUsername());
            holder.screenname.setText(user.getScreenname());
            String pbLink = user.getImageLink();
            if (!user.hasDefaultProfileImage()) {
                pbLink += settings.getImageSuffix();
            }
            Picasso.get().load(pbLink).transform(new RoundedCornersTransformation(2, 0))
                    .error(R.drawable.no_image).into(holder.profile);
        }
    }


    @Override
    public int getItemCount() {
        return data.size();
    }


    @MainThread
    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }

    /**
     * click listener for an item
     */
    public interface OnLoginClickListener {

        /**
         * called on item select
         *
         * @param account selected account information
         */
        void onLoginClick(Account account);

        /**
         * called to remove item
         *
         * @param account selected account information
         */
        void onDeleteClick(Account account);
    }
}