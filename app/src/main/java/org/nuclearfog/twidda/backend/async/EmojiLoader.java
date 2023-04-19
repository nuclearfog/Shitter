package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.model.Emoji;

import java.util.List;


public class EmojiLoader extends AsyncExecutor<Void, List<Emoji>> {

	private AppDatabase db;
	private Connection connection;


	public EmojiLoader(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
		db = new AppDatabase(context);
	}


	@Override
	protected List<Emoji> doInBackground(@NonNull Void param) {
		List<Emoji> result;
		try {
			// get online emojis
			result = connection.getEmojis();
			db.saveEmojis(result);
		} catch (Exception e) {
			e.printStackTrace();
			// get offline emojis
			result = db.getEmojis();
		}
		return result;
	}
}