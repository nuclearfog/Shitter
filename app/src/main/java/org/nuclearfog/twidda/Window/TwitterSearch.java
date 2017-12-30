package org.nuclearfog.twidda.Window;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class TwitterSearch extends AppCompatActivity {

    private String keyWord;

    @Override
    protected void onCreate(Bundle bundle){
        super.onCreate(bundle);
        keyWord = getIntent().getExtras().getString("search");

    }



}
