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


public class TwitterList extends AppCompatActivity {

    public static final String KEY_USERLIST_OWNER_ID = "userlist-owner-id";
    public static final String KEY_USERLIST_OWNER_NAME = "userlist-owner-name";


    @Override
    protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_list);
        View root = findViewById(R.id.list_view);
        Toolbar toolbar = findViewById(R.id.list_toolbar);
        ViewPager pager = findViewById(R.id.list_pager);

        toolbar.setTitle(R.string.list_appbar);
        setSupportActionBar(toolbar);
        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);

        GlobalSettings settings = GlobalSettings.getInstance(this);
        FontTool.setViewFontAndColor(settings, root);
        root.setBackgroundColor(settings.getBackgroundColor());

        Bundle param = getIntent().getExtras();
        if (param != null) {
            if (param.containsKey(KEY_USERLIST_OWNER_ID)) {
                long ownerId = param.getLong(KEY_USERLIST_OWNER_ID);
                adapter.setupListPage(ownerId);
            } else if (param.containsKey(KEY_USERLIST_OWNER_NAME)) {
                String ownerName = param.getString(KEY_USERLIST_OWNER_NAME);
                adapter.setupListPage(ownerName);
            }
        }
    }
}