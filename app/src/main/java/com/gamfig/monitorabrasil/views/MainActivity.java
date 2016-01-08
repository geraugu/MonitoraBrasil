package com.gamfig.monitorabrasil.views;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.actions.ActionsCreator;
import com.gamfig.monitorabrasil.actions.DialogaActions;
import com.gamfig.monitorabrasil.actions.PoliticoActions;
import com.gamfig.monitorabrasil.application.AppController;
import com.gamfig.monitorabrasil.dispatcher.Dispatcher;
import com.gamfig.monitorabrasil.model.Comparacao;
import com.gamfig.monitorabrasil.model.Pergunta;
import com.gamfig.monitorabrasil.model.Tema;
import com.gamfig.monitorabrasil.stores.DialogaStore;
import com.gamfig.monitorabrasil.stores.PoliticoStore;
import com.gamfig.monitorabrasil.views.util.Card;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.twitter.sdk.android.tweetui.SearchTimeline;
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Dispatcher dispatcher;
    private ActionsCreator actionsCreator;
    private DialogaStore dialogaStore;
    private PoliticoStore politicoStore;

    private ProgressBar pbDialoga;

    private View headerView;
    private NavigationView navigationView;

    private View viewCardComparaGasto;

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

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        headerView = getLayoutInflater().inflate(R.layout.nav_header_main, navigationView, false);
        navigationView.addHeaderView(headerView);
        initDependencies();
        setupView();


        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(it);
            }
        });

        //monta card do dialoga
        pbDialoga.setVisibility(View.VISIBLE);
        actionsCreator.getPerguntaAleatoria();

        //monta o card de gasto
        actionsCreator.getComparacaoGasto(null);

        //tweet search #monitoraBrasil
        buscaTweet();

        //setup headerview
        if(ParseUser.getCurrentUser()!=null)
            setupHeader();



    }

    private void setupHeader() {
        TextView mNome = (TextView)headerView.findViewById(R.id.txtNome);
        ParseUser user = ParseUser.getCurrentUser();
        mNome.setText(user.getString("nome"));
        TextView mEmail = (TextView)headerView.findViewById(R.id.txtEmail);
        mEmail.setText(user.getEmail());

        if(user.getString("image_url") != null){
            ImageView img = (ImageView)headerView.findViewById(R.id.imgPerfil);
            DisplayImageOptions mDisplayImageOptions = new DisplayImageOptions.Builder().displayer(new RoundedBitmapDisplayer(100)).cacheInMemory(true).build();
            String url = "https://twitter.com/"+user.getUsername()+"/profile_image?size=normal";
            AppController.getInstance().getmImagemLoader().displayImage(user.getString("image_url"), img,mDisplayImageOptions);
        }

        ParseFile foto = (ParseFile)user.get("foto");
        if(foto!=null){
            foto.getDataInBackground(new GetDataCallback() {
                public void done(byte[] data, ParseException e) {
                    if (e == null) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        bitmap = Bitmap.createScaledBitmap(bitmap, 120, 120, true);
                        ImageView img = (ImageView) headerView.findViewById(R.id.imgPerfil);
                        img.setImageBitmap(bitmap);

                    } else {
                        // something went wrong
                    }
                }
            });
        }

       // headerView.setBackground(ContextCompat.getDrawable(this, Usuario.buscaImagemTopo()));
    }

    private void initDependencies() {
        dispatcher = Dispatcher.get(new Bus());
        actionsCreator = ActionsCreator.get(dispatcher);
        dialogaStore = DialogaStore.get(dispatcher);
        politicoStore = PoliticoStore.get(dispatcher);
    }

    private void setupView() {
        pbDialoga = (ProgressBar) findViewById(R.id.pbDialoga);
        viewCardComparaGasto = (View) findViewById(R.id.llCardComparaGastos);
    }

    private void buscaTweet() {
        SearchTimeline searchTimeline = new SearchTimeline.Builder()
                .query("#monitoraBrasil")
                .maxItemsPerRequest(1)
                .build();

        final TweetTimelineListAdapter adapter = new TweetTimelineListAdapter.Builder(this)
                .setTimeline(searchTimeline)
                .build();


        ListView lvTwitter = (ListView) findViewById(R.id.card_twitter);

        lvTwitter.setAdapter(adapter);

    }

    private void updateCardComparacao() {
        Comparacao comparacao = politicoStore.getGasto();
        ParseObject politico = actionsCreator.getPolitico(comparacao.getCota().getParseObject("politico").getObjectId());
        new Card().montaCardComparacaoGasto(viewCardComparaGasto,politico,comparacao);
    }
    /**
     * Atualiza a UI depois de uma action
     * @param event
     */
    @Subscribe
    public void onTodoStoreChange(DialogaStore.DialogaStoreChangeEvent event) {
        switch (dialogaStore.getEvento()){
            case DialogaActions.DIALOGA_GET_PERGUNTA_ALETORIA:
                Pergunta pergunta = dialogaStore.getPergunta();
                if(null != pergunta)
                    montaCardDialoga(dialogaStore.getPergunta());
                break;
        }
    }

    @Subscribe
    public void onTodoStoreChange(PoliticoStore.PoliticoStoreChangeEvent event) {
        switch (event.getEvento()){
            case PoliticoActions.POLITICO_GET_COMPARACAO_GASTO:
                updateCardComparacao();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        dispatcher.register(this);
        dispatcher.register(dialogaStore);
        dispatcher.register(politicoStore);
    }

    @Override
    public void onPause() {
        super.onPause();
        dispatcher.unregister(this);
        dispatcher.unregister(dialogaStore);
        dispatcher.unregister(politicoStore);
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
        final Intent intent = new Intent(this,DialogaActivity.class);
        btnDialoga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ir para activity do dialoga e abrir na pergunta selecionada

                try {
                    mPergunta.pin();
                    ((ParseObject) mPergunta.get("tema")).pin();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                intent.putExtra("perguntaId", mPergunta.getObjectId());
                intent.putExtra("temaId", ((ParseObject) mPergunta.get("tema")).getObjectId());
                startActivity(intent);
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

        if (id == R.id.nav_dialoga) {
            Intent intent = new Intent(this,DialogaActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_deputados) {
            Intent intent = new Intent(this,ParlamentarListActivity.class);
            intent.putExtra("tipo","c");
            startActivity(intent);

        } else if (id == R.id.nav_projetos_camara) {
            Intent intent = new Intent(this,ParlamentarListActivity.class);
            intent.putExtra("parlamentar","deputado");
            startActivity(intent);

        } else if (id == R.id.nav_cotas) {
            Intent intent = new Intent(this,ParlamentarListActivity.class);
            intent.putExtra("parlamentar","deputado");
            intent.putExtra("tipo","rank_cota");//para mostrar o ranking de quem gasta mais
            startActivity(intent);

        } else if (id == R.id.nav_senadores) {
            Intent intent = new Intent(this,ParlamentarListActivity.class);
            intent.putExtra("tipo","s");
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
