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
import androidx.fragment.app.FragmentTransaction;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.fragment.MessageFragment;

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
        setContentView(R.layout.page_fragment);
        View root = findViewById(R.id.fragment_root);
        Toolbar tool = findViewById(R.id.fragment_toolbar);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, new MessageFragment());
        fragmentTransaction.commit();

        tool.setTitle(R.string.directmessage);
        setSupportActionBar(tool);

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