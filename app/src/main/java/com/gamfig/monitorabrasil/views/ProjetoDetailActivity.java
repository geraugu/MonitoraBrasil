package com.gamfig.monitorabrasil.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.application.AppController;

/**
 * An activity representing a single Projeto detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * <p/>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link ProjetoDetailFragment}.
 */
public class ProjetoDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projeto_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntent =new Intent(AppController.getInstance().getApplicationContext(), ComentarioActivity.class);
                mIntent.putExtra("projeto",getIntent().getStringExtra("objectId"));
                startActivity(mIntent);
            }
        });

        // Show the Up button in the action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(ProjetoDetailFragment.ARG_ITEM_ID,
                    getIntent().getStringExtra(ProjetoDetailFragment.ARG_ITEM_ID));
            arguments.putString(ProjetoDetailFragment.ARG_CASA,
                    getIntent().getStringExtra(ProjetoDetailFragment.ARG_CASA));
            ProjetoDetailFragment fragment = new ProjetoDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.projeto_detail_container, fragment)
                    .commit();
        }
    }


}
