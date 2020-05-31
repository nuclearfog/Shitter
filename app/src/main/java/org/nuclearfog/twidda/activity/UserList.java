package org.nuclearfog.twidda.activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.FragmentAdapter;
import org.nuclearfog.twidda.backend.helper.FontTool;
import org.nuclearfog.twidda.database.GlobalSettings;


public class UserList extends AppCompatActivity {

    public static final String KEY_USERLIST_ID = "userlist-owner";
    private FragmentAdapter adapter;
    private ViewPager pager;

    @Override
    protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_list);
        View root = findViewById(R.id.list_view);
        Toolbar toolbar = findViewById(R.id.list_toolbar);
        pager = findViewById(R.id.list_pager);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(R.string.list_appbar);

        GlobalSettings settings = GlobalSettings.getInstance(this);
        FontTool.setViewFontAndColor(settings, root);
        root.setBackgroundColor(settings.getBackgroundColor());
    }


    @Override
    protected void onStart() {
        super.onStart();
        Bundle param = getIntent().getExtras();
        if (adapter == null && param != null && param.containsKey(KEY_USERLIST_ID)) {
            long listId = param.getLong(KEY_USERLIST_ID);
            adapter = new FragmentAdapter(getSupportFragmentManager());
            adapter.setupListPage(listId);
            pager.setAdapter(adapter);
        }
    }
}