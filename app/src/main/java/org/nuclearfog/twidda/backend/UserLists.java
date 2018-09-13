package org.nuclearfog.twidda.backend;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.listitems.TwitterUser;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.viewadapter.UserAdapter;
import org.nuclearfog.twidda.window.UserDetail;

import java.lang.ref.WeakReference;
import java.util.List;

import twitter4j.TwitterException;

public class UserLists extends AsyncTask <Long, Void, Boolean> {

    public static final long FOLLOWING = 0L;
    public static final long FOLLOWERS = 1L;
    public static final long RETWEETER = 2L;
    public static final long FAVORISER = 3L;

    private WeakReference<UserDetail> ui;
    private TwitterEngine mTwitter;
    private LayoutInflater inflater;
    private UserAdapter usrAdp;
    private Dialog popup;
    private String errorMessage = "E: Userlist, ";
    private int returnCode = 0;

    /**
     *@see UserDetail
     */
    public UserLists(UserDetail context) {
        GlobalSettings settings = GlobalSettings.getInstance(context);
        boolean imageLoad = settings.loadImages();
        inflater = LayoutInflater.from(context);
        popup = new Dialog(context);

        ui = new WeakReference<>(context);
        mTwitter = TwitterEngine.getInstance(context);
        RecyclerView userList = ui.get().findViewById(R.id.userlist);

        usrAdp = new UserAdapter(ui.get());
        usrAdp.toggleImage(imageLoad);
        userList.setAdapter(usrAdp);
    }

    @Override
    @SuppressLint("InflateParams")
    protected void onPreExecute() {
        popup.requestWindowFeature(Window.FEATURE_NO_TITLE);
        popup.setCanceledOnTouchOutside(false);
        if (popup.getWindow() != null)
            popup.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        View load = inflater.inflate(R.layout.item_load, null, false);
        View cancelButton = load.findViewById(R.id.kill_button);
        popup.setContentView(load);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
                if (!isCancelled())
                    cancel(true);
            }
        });
        popup.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (!isCancelled())
                    cancel(true);
            }
        });
        popup.show();
    }


    @Override
    protected Boolean doInBackground(Long... data) {
        long id = data[0];
        long mode = data[1];
        long cursor = data[2];
        try {
            List<TwitterUser> user;
            if(mode == FOLLOWING) {
                user = mTwitter.getFollowing(id,cursor);
                usrAdp.setData(user);
            }
            else if(mode == FOLLOWERS) {
                user = mTwitter.getFollower(id,cursor);
                usrAdp.setData(user);
            }
            else if(mode == RETWEETER) {
                user = mTwitter.getRetweeter(id,cursor);
                usrAdp.setData(user);
            }
            return true;
        }
        catch(TwitterException err) {
            returnCode = err.getErrorCode();
            if (returnCode > 0 && returnCode != 420) {
                errorMessage += err.getMessage();
            }
            return false;
        } catch(Exception err) {
            Log.e("User List", err.getMessage());
            return false;
        }
    }


    @Override
    @SuppressLint("InflateParams")
    protected void onPostExecute(Boolean success) {
        if(ui.get() == null)
            return;
        if(success) {
            usrAdp.notifyDataSetChanged();
        } else {
            if (returnCode == 420)
                Toast.makeText(ui.get(), R.string.rate_limit_exceeded, Toast.LENGTH_SHORT).show();
            else if (returnCode > 0)
                Toast.makeText(ui.get(), errorMessage, Toast.LENGTH_SHORT).show();
        }
        popup.dismiss();
    }
}