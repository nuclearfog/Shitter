package org.nuclearfog.twidda.backend.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class represents an image cache for emoji icons
 * There are both, image cache and file cache. If image doesn't exist in the cache, the file cache is used to search for the file.
 * A new image will be cached and saved as file to the local cache storage.
 *
 * @author nuclearfog
 */
public class ImageCache {

	/**
	 * size of the lru cache (max entry count)
	 */
	private static final int SIZE = 64;

	/**
	 * folder name of the local cache
	 */
	private static final String FOLDER = "images";

	private static ImageCache instance;

	@Nullable
	private File imageFolder;

	private LruCache<String, Bitmap> cache;

	private Map<String, File> files;


	/**
	 * @param context context used to determine cache folder path
	 */
	private ImageCache(Context context) {
		files = new TreeMap<>();
		cache = new LruCache<>(SIZE);
		try {
			imageFolder = new File(context.getExternalCacheDir(), FOLDER);
			imageFolder.mkdirs();
			File[] imageFiles = imageFolder.listFiles();
			if (imageFiles != null) {
				for (File file : imageFiles) {
					files.put(file.getName(), file);
				}
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}

	/**
	 * put image to cache and save as file if not exists
	 *
	 * @param key   key of the image (tag)
	 * @param image image bitmap
	 */
	public void putImage(String key, Bitmap image) {
		cache.put(key, image);
		if (imageFolder != null && !files.containsKey(key)) {
			try {
				File file = new File(imageFolder, key);
				if (file.createNewFile()) {
					FileOutputStream output = new FileOutputStream(file);
					image.compress(Bitmap.CompressFormat.PNG, 1, output);
					files.put(key, file);
				}
			} catch (IOException|SecurityException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * get image from cache or file
	 *
	 * @param key key of the image (tag)
	 * @return image bitmap or null if not found
	 */
	@Nullable
	public Bitmap getImage(String key) {
		Bitmap result = cache.get(key);
		if (result == null) {
			File file = files.get(key);
			if (file != null) {
				try {
					FileInputStream inputStream = new FileInputStream(file);
					result = BitmapFactory.decodeStream(inputStream);
					cache.put(key, result);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	/**
	 * create singleton instance of this class
	 *
	 * @return singleton instance of this class
	 */
	public static ImageCache getInstance(Context context) {
		if (instance == null)
			instance = new ImageCache(context);
		return instance;
	}
}