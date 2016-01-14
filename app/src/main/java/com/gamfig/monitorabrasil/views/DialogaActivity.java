package com.gamfig.monitorabrasil.views;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.views.fragments.DialogaActivityFragment;


public class DialogaActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialoga);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("Dialoga Activity"));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DialogaActivityFragment frag = new DialogaActivityFragment();
        frag.setArguments(getIntent().getExtras());
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment, frag, "dialogaVoto");
        ft.commit();
    }

}
