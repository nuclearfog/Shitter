package org.nuclearfog.twidda.newwindow;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import org.nuclearfog.twidda.R;

public class Profile extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.profile);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.buttons, m);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(item.getItemId())
        {
            case R.id.action_profile:
                Intent i = new Intent(this, Profile.class);
                startActivity(i);
                break;
            case R.id.action_tweet:
                break;
        }
        return true;
    }




}
