package org.nuclearfog.twidda.activity;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.FragmentAdapter;
import org.nuclearfog.twidda.backend.helper.FontTool;
import org.nuclearfog.twidda.database.GlobalSettings;

import static org.nuclearfog.twidda.adapter.FragmentAdapter.AdapterType.LIST_PAGE;

public class UserList extends AppCompatActivity {

    public static final String KEY_USERLIST_ID = "userlist-owner";
    private long userId;


    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_list);
        View root = findViewById(R.id.list_view);
        ViewPager pager = findViewById(R.id.list_pager);
        Toolbar toolbar = findViewById(R.id.list_toolbar);

        Bundle param = getIntent().getExtras();
        if (param != null)
            userId = param.getLong(KEY_USERLIST_ID);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(R.string.list_appbar);

        GlobalSettings settings = GlobalSettings.getInstance(this);
        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager(), LIST_PAGE, userId, "");
        pager.setAdapter(adapter);

        FontTool.setViewFont(root, settings.getFontFace());
        root.setBackgroundColor(settings.getBackgroundColor());
    }
}