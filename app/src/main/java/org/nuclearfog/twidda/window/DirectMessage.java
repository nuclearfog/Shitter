package org.nuclearfog.twidda.window;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.MessagePagerAdapter;
import org.nuclearfog.twidda.database.GlobalSettings;


public class DirectMessage extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.page_dm);

        Toolbar tool = findViewById(R.id.dm_toolbar);
        View root = findViewById(R.id.dm_layout);
        ViewPager pager = findViewById(R.id.dm_pager);

        setSupportActionBar(tool);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(R.string.directmessage);

        MessagePagerAdapter adapter = new MessagePagerAdapter(getSupportFragmentManager());
        pager.setOffscreenPageLimit(1);
        pager.setAdapter(adapter);

        GlobalSettings settings = GlobalSettings.getInstance(this);
        root.setBackgroundColor(settings.getBackgroundColor());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.message, m);
        return super.onCreateOptionsMenu(m);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.message) {
            Intent sendDm = new Intent(this, MessagePopup.class);
            startActivity(sendDm);
        }
        return super.onOptionsItemSelected(item);
    }
}