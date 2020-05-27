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
import org.nuclearfog.twidda.backend.helper.FontTool;
import org.nuclearfog.twidda.database.GlobalSettings;

import static org.nuclearfog.twidda.adapter.FragmentAdapter.AdapterType.MESSAGE_PAGE;


public class DirectMessage extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_dm);
        Toolbar tool = findViewById(R.id.dm_toolbar);
        View root = findViewById(R.id.dm_layout);
        ViewPager pager = findViewById(R.id.dm_pager);

        setSupportActionBar(tool);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(R.string.directmessage);

        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager(), MESSAGE_PAGE, 0, "");
        pager.setOffscreenPageLimit(1);
        pager.setAdapter(adapter);

        GlobalSettings settings = GlobalSettings.getInstance(this);
        root.setBackgroundColor(settings.getBackgroundColor());
        FontTool.setViewFontAndColor(settings, root);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.message, m);
        return super.onCreateOptionsMenu(m);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.message) {
            Intent sendDm = new Intent(this, MessagePopup.class);
            startActivity(sendDm);
        }
        return super.onOptionsItemSelected(item);
    }
}