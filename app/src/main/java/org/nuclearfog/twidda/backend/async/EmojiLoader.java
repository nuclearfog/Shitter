package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.model.Emoji;

import java.util.List;

/**
 * Background loader used to load emojis from network
 *
 * @author nuclearfog
 */
public class EmojiLoader extends AsyncExecutor<Void, List<Emoji>> {

	private AppDatabase db;
	private Connection connection;

	/**
	 *
	 */
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
		} catch (ConnectionException exception) {
			// get offline emojis
			result = db.getEmojis();
		}
		return result;
	}
}