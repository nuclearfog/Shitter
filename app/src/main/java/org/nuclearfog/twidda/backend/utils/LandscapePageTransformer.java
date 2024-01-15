package org.nuclearfog.twidda.backend.utils;

import android.content.res.Configuration;
import android.view.View;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;
import androidx.viewpager2.widget.ViewPager2.PageTransformer;

/**
 * Custom PageTransformer implementation to create a landscape view with containing two visible pages minimum
 *
 * @author nuclearfog
 */
public class LandscapePageTransformer implements PageTransformer {


	@Override
	public void transformPage(@NonNull View page, float position) {
		ViewParent parent = page.getParent().getParent();
		if (parent instanceof ViewPager2) {
			ViewPager2 viewPager = (ViewPager2) parent;
			if (viewPager.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				page.getLayoutParams().width = viewPager.getMeasuredWidth() / 2 + 1;
			}
		}
	}
}