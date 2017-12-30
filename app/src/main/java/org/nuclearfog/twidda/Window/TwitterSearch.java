package org.nuclearfog.twidda.Window;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.nuclearfog.twidda.R;


public class TwitterSearch extends AppCompatActivity {

    private String keyWord;

    @Override
    protected void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.search);
        keyWord = getIntent().getExtras().getString("search");
    }



}
