package org.nuclearfog.twidda.activity;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener;
import com.google.android.material.tabs.TabLayout.Tab;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.FragmentAdapter;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.database.GlobalSettings;

/**
 *
 */
public class UserExclude extends AppCompatActivity implements OnTabSelectedListener {


    @Override
    protected void onCreate(Bundle savedInst) {
        super.onCreate(savedInst);
        setContentView(R.layout.page_user_exclude);
        View root = findViewById(R.id.userexclude_root);
        Toolbar toolbar = findViewById(R.id.userexclude_toolbar);
        TabLayout tablayout = findViewById(R.id.userexclude_tab);
        ViewPager pager = findViewById(R.id.userexclude_pager);

        GlobalSettings settings = GlobalSettings.getInstance(this);

        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(2);
        adapter.setupMuteBlockPage();
        tablayout.setupWithViewPager(pager);
        tablayout.addOnTabSelectedListener(this);

        setSupportActionBar(toolbar);
        AppStyles.setTheme(settings, root);
        AppStyles.setTabIcons(tablayout, settings, R.array.user_exclude_icons);
    }


    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    public void onTabSelected(Tab tab) {

    }

    @Override
    public void onTabUnselected(Tab tab) {

    }

    @Override
    public void onTabReselected(Tab tab) {

    }
}