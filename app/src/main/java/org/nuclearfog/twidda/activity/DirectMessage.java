package org.nuclearfog.twidda.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.FragmentAdapter;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.database.GlobalSettings;

/**
 * Activity for the direct message page of the current user
 *
 * @author nuclearfog
 */
public class DirectMessage extends AppCompatActivity {

    private GlobalSettings settings;

    @Override
    protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_dm);
        Toolbar tool = findViewById(R.id.dm_toolbar);
        View root = findViewById(R.id.dm_layout);
        ViewPager pager = findViewById(R.id.dm_pager);

        tool.setTitle(R.string.directmessage);
        setSupportActionBar(tool);

        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager());
        adapter.setupMessagePage();
        pager.setOffscreenPageLimit(1);
        pager.setAdapter(adapter);

        settings = GlobalSettings.getInstance(this);
        AppStyles.setTheme(settings, root);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.message, m);
        AppStyles.setMenuIconColor(m, settings.getIconColor());
        return super.onCreateOptionsMenu(m);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.message) {
            Intent sendDm = new Intent(this, MessageEditor.class);
            startActivity(sendDm);
        }
        return super.onOptionsItemSelected(item);
    }
}