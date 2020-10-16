package org.nuclearfog.twidda.activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener;
import com.google.android.material.tabs.TabLayout.Tab;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.FragmentAdapter;
import org.nuclearfog.twidda.backend.utils.FontTool;
import org.nuclearfog.twidda.database.GlobalSettings;

/**
 * Activity to show an user list, members and tweets
 */
public class ListDetail extends AppCompatActivity implements OnTabSelectedListener {

    public static final String KEY_LISTDETAIL_ID = "list-id";
    public static final String KEY_LISTDETAIL_NAME = "list-name";

    private FragmentAdapter adapter;
    private TabLayout tablayout;
    private ViewPager pager;

    @Override
    protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_listdetail);
        View root = findViewById(R.id.listdetail_root);
        Toolbar toolbar = findViewById(R.id.listdetail_toolbar);
        tablayout = findViewById(R.id.listdetail_tab);
        pager = findViewById(R.id.listdetail_pager);

        GlobalSettings settings = GlobalSettings.getInstance(this);
        FontTool.setViewFontAndColor(settings, root);
        root.setBackgroundColor(settings.getBackgroundColor());

        adapter = new FragmentAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(2);
        tablayout.setupWithViewPager(pager);
        tablayout.setSelectedTabIndicatorColor(settings.getHighlightColor());
        tablayout.addOnTabSelectedListener(this);

        Bundle param = getIntent().getExtras();
        if (param != null && param.containsKey(KEY_LISTDETAIL_ID)) {
            long id = param.getLong(KEY_LISTDETAIL_ID);
            String name = param.getString(KEY_LISTDETAIL_NAME, "");
            adapter.setupListContentPage(id);
            Tab tlTab = tablayout.getTabAt(0);
            Tab trTab = tablayout.getTabAt(1);
            if (tlTab != null && trTab != null) {
                tlTab.setIcon(R.drawable.list);
                trTab.setIcon(R.drawable.user);
            }
            toolbar.setTitle(name);
            setSupportActionBar(toolbar);
        }
    }


    @Override
    public void onBackPressed() {
        if (tablayout.getSelectedTabPosition() > 0) {
            pager.setCurrentItem(0);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public void onTabSelected(Tab tab) {
    }


    @Override
    public void onTabUnselected(Tab tab) {
        adapter.scrollToTop(tab.getPosition());
    }


    @Override
    public void onTabReselected(Tab tab) {
    }
}