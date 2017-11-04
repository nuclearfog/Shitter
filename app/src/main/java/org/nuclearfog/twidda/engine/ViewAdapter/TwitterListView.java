package org.nuclearfog.twidda.engine.ViewAdapter;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class TwitterListView extends ListView {
    public TwitterListView(Context c) {
        super(c);
    }
    public TwitterListView(Context c, AttributeSet attributeSet) {
        super(c, attributeSet);
    }
    public TwitterListView(Context c, AttributeSet attributeSet, int defStyle) {
        super(c, attributeSet, defStyle);
    }

    @Override
    public void onMeasure(int width, int heigh) {
        int expand = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(width, expand);
    }
}