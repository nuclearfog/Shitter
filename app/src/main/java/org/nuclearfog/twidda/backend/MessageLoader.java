package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.adapter.MessageAdapter;
import org.nuclearfog.twidda.backend.items.Message;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.fragment.MessageFragment;

import java.lang.ref.WeakReference;
import java.util.List;

import static android.widget.Toast.LENGTH_SHORT;


public class MessageLoader extends AsyncTask<Long, Void, List<Message>> {

    public enum Mode {
        DB,
        LOAD,
        DEL
    }

    @Nullable
    private TwitterEngine.EngineException twException;
    private Mode mode;
    private WeakReference<MessageFragment> ui;
    private TwitterEngine mTwitter;
    private AppDatabase db;
    private MessageAdapter adapter;
    private long id;


    public MessageLoader(MessageFragment fragment, Mode mode) {
        ui = new WeakReference<>(fragment);
        db = new AppDatabase(fragment.getContext());
        mTwitter = TwitterEngine.getInstance(fragment.getContext());
        adapter = fragment.getAdapter();
        this.mode = mode;
    }


    @Override
    protected void onPreExecute() {
        if (ui.get() != null)
            ui.get().setRefresh(true);
    }


    @Override
    protected List<Message> doInBackground(Long[] param) {
        List<Message> messages = null;
        long messageId = 0;
        try {
            switch (mode) {
                case DB:
                    messages = db.getMessages();
                    if (!messages.isEmpty())
                        break;

                case LOAD:
                    messages = mTwitter.getMessages();
                    db.storeMessage(messages);
                    messages = db.getMessages();
                    break;

                case DEL:
                    messageId = param[0];
                    id = messageId;
                    mTwitter.deleteMessage(messageId);
                    db.deleteDm(messageId);
                    break;
            }
        } catch (TwitterEngine.EngineException twException) {
            this.twException = twException;
            if (twException.statusNotFound())
                db.deleteDm(messageId);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return messages;
    }


    @Override
    protected void onPostExecute(@Nullable List<Message> messages) {
        if (ui.get() != null) {
            if (messages != null)
                adapter.replaceAll(messages);
            if (twException != null) {
                Toast.makeText(ui.get().getContext(), twException.getMessageResource(), LENGTH_SHORT).show();
                if (twException.statusNotFound())
                    adapter.remove(id);
            } else if (mode == Mode.DEL)
                adapter.remove(id);
            ui.get().setRefresh(false);
        }
    }


    @Override
    protected void onCancelled() {
        if (ui.get() != null)
            ui.get().setRefresh(false);
    }


    @Override
    protected void onCancelled(@Nullable List<Message> messages) {
        if (ui.get() != null) {
            if (messages != null)
                adapter.replaceAll(messages);
            ui.get().setRefresh(false);
        }
    }
}