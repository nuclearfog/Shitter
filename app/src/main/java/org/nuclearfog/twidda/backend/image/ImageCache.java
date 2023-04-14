package org.nuclearfog.twidda.backend.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
	private static final int SIZE = 128;

	/**
	 * cache size limit of the image folder
	 */
	private static final long CACHE_SIZE = 1024 * 1024 * 32;

	/**
	 * folder name of the local cache
	 */
	private static final String FOLDER = "images";

	private static ImageCache instance;

	private final LruCache<String, Bitmap> cache;
	private Map<String, File> files;
	private File imageFolder;

	/**
	 * @param context context used to determine cache folder path
	 */
	private ImageCache(Context context) {
		files = new ConcurrentHashMap<>();
		cache = new LruCache<>(SIZE);
		try {
			imageFolder = new File(context.getExternalCacheDir(), FOLDER);
			if (!imageFolder.exists())
				imageFolder.mkdirs();
			else if (imageFolder.isFile()) {
				imageFolder.delete();
				imageFolder.mkdirs();
			}
			File[] imageFiles = imageFolder.listFiles();
			if (imageFiles != null) {
				for (File file : imageFiles) {
					if (file.isFile() && file.length() > 0) {
						files.put(file.getName(), file);
					} else {
						file.delete();
					}
				}
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		}
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

	/**
	 * free image cache
	 */
	public static void clear() {
		synchronized (instance.cache) {
			if (instance != null) {
				instance.cache.evictAll();
			}
		}
	}

	/**
	 * put image to cache and save as file if not exists
	 *
	 * @param key   key of the image (tag)
	 * @param image image bitmap
	 */
	public void putImage(String key, Bitmap image) {
		synchronized (instance.cache) {
			cache.put(key, image);
			if (!files.containsKey(key)) {
				try {
					File file = new File(imageFolder, key);
					if (file.createNewFile()) {
						FileOutputStream output = new FileOutputStream(file);
						image.compress(Bitmap.CompressFormat.PNG, 1, output);
						output.close();
						files.put(key, file);
					}
				} catch (
						IOException |
						SecurityException e) {
					e.printStackTrace();
				}
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
		synchronized (instance.cache) {
			Bitmap result = cache.get(key);
			if (result == null) {
				try {
					File file = files.get(key);
					if (file != null && file.canRead()) {
						result = BitmapFactory.decodeFile(file.getAbsolutePath());
						if (result != null) {
							cache.put(key, result);
						}
					}
				} catch (
						SecurityException e) {
					e.printStackTrace();
				}
			}
			return result;
		}
	}

	/**
	 * check if cache size exceeded the limit
	 * and reduce cache size by deleting old image files
	 */
	public void trimCache() {
		File[] files = imageFolder.listFiles();
		if (files != null) {
			long size = 0L;
			for (File file : files) {
				size += file.length();
			}
			if (size > CACHE_SIZE) {
				Arrays.sort(files, new Comparator<File>() {
					@Override
					public int compare(File file1, File file2) {
						if (file1.lastModified() > file2.lastModified())
							return 1;
						else if (file1.lastModified() < file2.lastModified())
							return -1;
						return 0;
					}
				});
				for (File file : files) {
					size -= file.length();
					file.delete();
					if (size < CACHE_SIZE) {
						break;
					}
				}
			}
		}
	}
}