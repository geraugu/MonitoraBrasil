package com.gamfig.monitorabrasil.views;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.actions.ActionsCreator;
import com.gamfig.monitorabrasil.actions.DialogaActions;
import com.gamfig.monitorabrasil.dispatcher.Dispatcher;
import com.gamfig.monitorabrasil.model.Pergunta;
import com.gamfig.monitorabrasil.model.Tema;
import com.gamfig.monitorabrasil.stores.DialogaStore;
import com.parse.ParseObject;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.SearchTimeline;
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;
import com.twitter.sdk.android.tweetui.TweetView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Dispatcher dispatcher;
    private ActionsCreator actionsCreator;
    private DialogaStore dialogaStore;

    private ProgressBar pbDialoga;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        initDependencies();
        setupView();

        //monta card do dialoga
        pbDialoga.setVisibility(View.VISIBLE);
        actionsCreator.getPerguntaAleatoria();

        //tweet search #monitoraBrasil
        buscaTweet();



    }

    private void initDependencies() {
        dispatcher = Dispatcher.get(new Bus());
        actionsCreator = ActionsCreator.get(dispatcher);
        dialogaStore = DialogaStore.get(dispatcher);
    }

    private void setupView() {
        pbDialoga = (ProgressBar) findViewById(R.id.pbDialoga);

    }

    private void buscaTweet() {
        SearchTimeline searchTimeline = new SearchTimeline.Builder()
                .query("#monitoraBrasil")
                .maxItemsPerRequest(1)
                .build();

        final TweetTimelineListAdapter adapter = new TweetTimelineListAdapter.Builder(this)
                .setTimeline(searchTimeline)
                .build();
        Tweet tweet = adapter.getItem(0);

        CardView tweetCardView = (CardView)findViewById(R.id.card_twitter);

        tweetCardView.addView(new TweetView(MainActivity.this, tweet));
    }

    /**
     * Atualiza a UI depois de uma action
     * @param event
     */
    @Subscribe
    public void onTodoStoreChange(DialogaStore.DialogaStoreChangeEvent event) {
        switch (dialogaStore.getEvento()){
            case DialogaActions.DIALOGA_GET_PERGUNTA_ALETORIA:
                montaCardDialoga(dialogaStore.getPergunta());
                break;
        }
    }

    /**
     * Monta o card Dialoga
     * @param pergunta
     */
    private void montaCardDialoga(Pergunta pergunta) {
        pbDialoga.setVisibility(View.GONE);
        //monta o topo
        final ParseObject mPergunta = pergunta.getPergunta();
        ParseObject mTema = (ParseObject) mPergunta.get("tema");

        TextView txtTema = (TextView) findViewById(R.id.txtNomeTema);
        txtTema.setText(mTema.getString("Nome"));
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        linearLayout.setBackgroundResource(Tema.buscaCor(mTema.getString("imagem")));
        LinearLayout llCardDialoga = (LinearLayout) findViewById(R.id.llCardDialoga);
        llCardDialoga.setBackgroundResource(Tema.buscaCor(mTema.getString("imagem")));
        ImageView imgIcone = (ImageView)findViewById(R.id.icone);
        imgIcone.setBackgroundResource(Tema.buscaIcone(mTema.getString("imagem")));

        //preenche a pergunta
        TextView txtPergunta = (TextView) findViewById(R.id.txtPergunta);
        txtPergunta.setText(pergunta.getPergunta().getString("texto"));

        //evento do botao para ir para activity dialoga
        Button btnDialoga = (Button) findViewById(R.id.btnDialoga);
        btnDialoga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ir para activity do dialoga e abrir na pergunta selecionada
             /*   Intent intent = new Intent(this,DialogaActivity.class);
                try {
                    mPergunta.pin();
                    ((ParseObject) mPergunta.get("tema")).pin();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                intent.putExtra("perguntaId", mPergunta.getObjectId());
                intent.putExtra("temaId", ((ParseObject) mPergunta.get("tema")).getObjectId());
                startActivity(intent);*/
            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camara) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
