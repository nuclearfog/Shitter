package org.nuclearfog.twidda.backend.utils;

import android.graphics.Bitmap;
import android.util.SparseArray;

/**
 * Blur hash decoder class
 * this class provides methods to create a blur bitmap from a hash string
 * <p>
 * source <a href="https://github.com/mastodon/mastodon-android/blob/master/mastodon/src/main/java/org/joinmastodon/android/ui/utils/BlurHashDecoder.java"/>
 */
public class BlurHashDecoder {

	/**
	 * use in memory cache for the calculated math, reused by images with same size.
	 * if the cache does not exist yet it will be created and populated with new calculations.
	 * By default it is true.
	 */
	private static final boolean USE_CACHE = true;

	private static final int DEFAULT_SIZE = 16;

	// cache Math.cos() calculations to improve performance.
	// The number of calculations can be huge for many bitmaps: width * height * numCompX * numCompY * 2 * nBitmaps
	// the cache is enabled by default, it is recommended to disable it only when just a few images are displayed
	private static SparseArray<double[]> cacheCosinesX = new SparseArray<>();
	private static SparseArray<double[]> cacheCosinesY = new SparseArray<>();

	private static final String CHAR_MAP = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz#$%*+,-.:;=?@[]^_{|}~";

	/**
	 *
	 */
	private BlurHashDecoder() {
	}

	/**
	 * Clear calculations stored in memory cache.
	 * The cache is not big, but will increase when many image sizes are used,
	 * if the app needs memory it is recommended to clear it.
	 */
	public static void clearCache() {
		cacheCosinesX.clear();
		cacheCosinesY.clear();
	}

	/**
	 * create blurred bitmap using hash string
	 *
	 * @param blurHash hash string
	 * @return blurred bitmap
	 */
	public static Bitmap decode(String blurHash) {
		return decode(blurHash, DEFAULT_SIZE, DEFAULT_SIZE, 1f);
	}

	/**
	 * create scaled bitmap using hash string
	 *
	 * @param blurHash hash string
	 * @param ratio    ratio ob the bitmap to generate
	 * @return blurred bitmap
	 */
	public static Bitmap decode(String blurHash, float ratio) {
		if (ratio > 1.0f) {
			return decode(blurHash, DEFAULT_SIZE, (int) (DEFAULT_SIZE / ratio), 1f);
		} else if (ratio < 1.0f && ratio > 0.0f) {
			return decode(blurHash, (int) (DEFAULT_SIZE * ratio), DEFAULT_SIZE, 1f);
		}
		return decode(blurHash, DEFAULT_SIZE, DEFAULT_SIZE, 1f);
	}

	/**
	 * create blurred bitmap with custom size
	 *
	 * @param blurHash hash string
	 * @param width    bitmap width
	 * @param height   bitmap height
	 * @return blurred bitmap
	 */
	public static Bitmap decode(String blurHash, int width, int height, float punch) {
		if (blurHash == null || blurHash.length() < 6)
			return null;
		int numCompEnc = decode83(blurHash, 0, 1);
		int numCompX = (numCompEnc % 9) + 1;
		int numCompY = (numCompEnc / 9) + 1;
		if (blurHash.length() != 4 + 2 * numCompX * numCompY)
			return null;
		int maxAcEnc = decode83(blurHash, 1, 2);
		float maxAc = (maxAcEnc + 1) / 166f;
		float[][] colors = new float[numCompX * numCompY][];
		colors[0] = decodeDc(decode83(blurHash, 2, 6));
		for (int i = 1; i < colors.length; i++) {
			int from = 4 + i * 2;
			int colorEnc = decode83(blurHash, from, from + 2);
			colors[i] = decodeAc(colorEnc, maxAc * punch);
		}
		return composeBitmap(width, height, numCompX, numCompY, colors);
	}

	/**
	 *
	 */
	private static int decode83(String str, int from, int to) {
		int result = 0;
		for (int i = from; i < to; i++) {
			int index = CHAR_MAP.indexOf(str.charAt(i));
			if (index != -1)
				result = result * 83 + index;
		}
		return result;
	}

	/**
	 *
	 */
	private static float[] decodeDc(int colorEnc) {
		int r = colorEnc >> 16;
		int g = (colorEnc >> 8) & 255;
		int b = colorEnc & 255;
		return new float[]{srgbToLinear(r), srgbToLinear(g), srgbToLinear(b)};
	}

	/**
	 *
	 */
	private static float srgbToLinear(int colorEnc) {
		float v = colorEnc / 255f;
		return v <= 0.4045f ? (v / 12.92f) : (float) Math.pow((v + 0.055f) / 1.055f, 2.4f);
	}

	/**
	 *
	 */
	private static float[] decodeAc(int value, float maxAc) {
		int r = value / (19 * 19);
		int g = (value / 19) % 19;
		int b = value % 19;
		return new float[]{signedPow2((r - 9) / 9f) * maxAc, signedPow2((g - 9) / 9f) * maxAc, signedPow2((b - 9) / 9f) * maxAc};
	}

	/**
	 *
	 */
	private static float signedPow2(float value) {
		return value * value * Math.signum(value);
	}

	/**
	 *
	 */
	private static Bitmap composeBitmap(int width, int height, int numCompX, int numCompY, float[][] colors) {
		// use an array for better performance when writing pixel colors
		int[] imageArray = new int[width * height];
		boolean calculateCosX = !USE_CACHE || cacheCosinesX.get(width * numCompX) == null;
		double[] cosinesX = getArrayForCosinesX(calculateCosX, width, numCompX);
		boolean calculateCosY = !USE_CACHE || cacheCosinesY.get(height * numCompY) == null;
		double[] cosinesY = getArrayForCosinesY(calculateCosY, height, numCompY);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				float r = 0f, g = 0f, b = 0f;
				for (int j = 0; j < numCompY; j++) {
					for (int i = 0; i < numCompX; i++) {
						double cosX = calculateCosX ? (cosinesX[i + numCompX * x] = Math.cos(Math.PI * x * i / width)) : cosinesX[i + numCompX * x];
						double cosY = calculateCosY ? (cosinesY[j + numCompY * y] = Math.cos(Math.PI * y * j / height)) : cosinesY[j + numCompY * y];
						float basis = (float) (cosX * cosY);
						float[] color = colors[j * numCompX + i];
						r += color[0] * basis;
						g += color[1] * basis;
						b += color[2] * basis;
					}
				}
				imageArray[x + width * y] = 0xFF000000 | linearToSrgb(b) | (linearToSrgb(g) << 8) | (linearToSrgb(r) << 16);
			}
		}
		return Bitmap.createBitmap(imageArray, width, height, Bitmap.Config.ARGB_8888);
	}

	/**
	 *
	 */
	private static double[] getArrayForCosinesY(boolean calculate, int height, int numCompY) {
		if (calculate) {
			double[] res = new double[height * numCompY];
			cacheCosinesY.put(height * numCompY, res);
			return res;
		} else {
			return cacheCosinesY.get(height * numCompY);
		}
	}

	/**
	 *
	 */
	private static double[] getArrayForCosinesX(boolean calculate, int width, int numCompX) {
		if (calculate) {
			double[] res = new double[width * numCompX];
			cacheCosinesX.put(width * numCompX, res);
			return res;
		} else {
			return cacheCosinesX.get(width * numCompX);
		}
	}

	/**
	 *
	 */
	private static int linearToSrgb(float value) {
		float v = Math.max(0f, Math.min(1f, value));
		return v <= 0.0031308f ? (int) (v * 12.92f * 255f + 0.5f) : (int) ((1.055f * (float) Math.pow(v, 1 / 2.4f) - 0.055f) * 255 + 0.5f);
	}
}