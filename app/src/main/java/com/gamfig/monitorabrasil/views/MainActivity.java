package com.gamfig.monitorabrasil.views;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.NestedScrollView;
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

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.gamfig.monitorabrasil.POJO.ComentarioEvent;
import com.gamfig.monitorabrasil.POJO.DialogaEvent;
import com.gamfig.monitorabrasil.POJO.PoliticoEvent;
import com.gamfig.monitorabrasil.POJO.ProjetoEvent;
import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.actions.ActionsCreator;
import com.gamfig.monitorabrasil.actions.ComentarioActions;
import com.gamfig.monitorabrasil.actions.DialogaActions;
import com.gamfig.monitorabrasil.actions.PoliticoActions;
import com.gamfig.monitorabrasil.actions.ProjetoActions;
import com.gamfig.monitorabrasil.application.AppController;
import com.gamfig.monitorabrasil.model.Comparacao;
import com.gamfig.monitorabrasil.model.Projeto;
import com.gamfig.monitorabrasil.model.Tema;
import com.gamfig.monitorabrasil.views.dialogs.DialogGostou;
import com.gamfig.monitorabrasil.views.util.Card;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.parse.ConfigCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseConfig;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.twitter.sdk.android.tweetui.SearchTimeline;
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    ActionsCreator actionsCreator;
    DialogaActions dialogaActions;
    PoliticoActions politicoActions;
    ComentarioActions comentarioActions;
    ProjetoActions projetoActions;

    private NestedScrollView mNestedScroll;

    private ProgressBar pbDialoga;

    private View headerView;
    private NavigationView navigationView;

    private View viewCardComparaGasto;
    private View viewCardComentarioPolitico;
    private View viewCardComentarioProjeto;
    private View viewCardUltimoProjeto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabComentario);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Answers.getInstance().logCustom(new CustomEvent("Comentario")
                        .putCustomAttribute("tela", "MainActivity"));

                Intent mIntent =new Intent(AppController.getInstance().getApplicationContext(), ComentarioActivity.class);
                mIntent.putExtra("tipo","principal");
                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                AppController.getInstance().startActivity(mIntent);
            }
        });
        verificaPush();

        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(it);
            }
        });

        //se primeira vez, buscar os politico
        ParseConfig.getInBackground(new ConfigCallback() {
            @Override
            public void done(ParseConfig config, ParseException e) {
                if(config.getString("dtAtualizacaoPoliltico")!=null){
                    String data = config.getString("dtAtualizacaoPoliltico");
                    if(actionsCreator.getValorSharedPreferences("dataAtualizacao")==null) {
                        actionsCreator.salvaNoSharedPreferences("dataAtualizacao", data);
                        politicoActions.getAllPoliticos();
                    }else{
                        if(!actionsCreator.getValorSharedPreferences("dataAtualizacao").equals(data)){
                            actionsCreator.salvaNoSharedPreferences("dataAtualizacao",data);
                            politicoActions.getAllPoliticos();
                        }
                    }
                }

            }
        });


        //monta card do dialoga
        pbDialoga.setVisibility(View.VISIBLE);
        dialogaActions.getPerguntaAleatoria();

        //monta o card de gasto
        politicoActions.getComparacaoGasto(null);

        //tweet search #monitoraBrasil
        buscaTweet();

        //busca ultimo comentario politico
        comentarioActions.getUltimoComentarioPolitico(null);

        //busca ultimo comentario politico
        comentarioActions.getUltimoComentarioProjeto();

        //setup headerview
        if(ParseUser.getCurrentUser()!=null) {
            setupHeader();
            //busca ultimo projeto de um político que está monitorando
            projetoActions.getUltimoProjeto();
        }



    }
    private void verificaPush() {
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            if(extras.getString("idPergunta") != null){
                //abrir a activity
                Intent intent = new Intent(this,DialogaActivity.class);

                intent.putExtra("perguntaId", extras.getString("idPergunta"));
                intent.putExtra("temaId", extras.getString("idTema"));
                startActivity(intent);
            }else{
                if(extras.getString("casa") != null){
                    Intent intent = new Intent(this, ProjetoDetailActivity.class);
                    intent.putExtra(ProjetoDetailFragment.ARG_ITEM_ID,String.valueOf(extras.getString(ProjetoDetailFragment.ARG_ITEM_ID)));
                    intent.putExtra(ProjetoDetailFragment.ARG_CASA,extras.getString(ProjetoDetailFragment.ARG_CASA));

                    startActivity(intent);
                }
            }
        }
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

        actionsCreator = ActionsCreator.get();
        comentarioActions = ComentarioActions.get();
        dialogaActions = DialogaActions.get();
        politicoActions = PoliticoActions.get();
    }

    private void setupView() {

        mNestedScroll = (NestedScrollView)findViewById(R.id.nested);
        pbDialoga = (ProgressBar) findViewById(R.id.pbDialoga);
        viewCardComparaGasto =  findViewById(R.id.llCardComparaGastos);
        viewCardComentarioPolitico = findViewById(R.id.llCardComentario);
        viewCardComentarioProjeto = findViewById(R.id.llCardComentarioProjeto);
        viewCardUltimoProjeto = findViewById(R.id.llCardUltimoProjeto);
        viewCardUltimoProjeto.setVisibility(View.GONE);
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


    /**
     * Atualiza a UI depois de uma action
     * @param event
     */


    @Subscribe
    public void onMessageEvent(DialogaEvent event){
        switch (event.getAction()){
            case DialogaActions.DIALOGA_GET_PERGUNTA_ALETORIA:
                montaCardDialoga(event);
                break;
        }
    }


    @Subscribe
    public void onMessageEvent(PoliticoEvent event){
        switch (event.getAction()){
            case PoliticoActions.POLITICO_GET_COMPARACAO_GASTO:
                updateCardComparacao(event);
                break;
        }
    }

    @Subscribe
    public void onMessageEvent(ComentarioEvent event){
        switch (event.getAction()){
            case ComentarioActions.COMENTARIO_POLITICO_GET_ULTIMO:
                updateCardComentarioPolitico(event);
                break;
            case  ComentarioActions.COMENTARIO_PROJETO_GET_ULTIMO:
                updateCardComentarioProjeto(event);
                break;
        }
    }

    @Subscribe
    public void onMessageEvent(ProjetoEvent event){
        switch (event.getAction()){
            case ProjetoActions.PROJETO_GET_ULTIMO_POLITICO_USER:
                updateCardprojetoPolitico(event);
                break;
        }
    }


    private void updateCardprojetoPolitico(ProjetoEvent event) {
        final Projeto projeto = event.getProjeto();
        new Card().montaCardProjeto(viewCardUltimoProjeto,projeto);
        viewCardUltimoProjeto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ProjetoDetailActivity.class);
                intent.putExtra(ProjetoDetailFragment.ARG_ITEM_ID,String.valueOf(projeto.getId()));
                intent.putExtra(ProjetoDetailFragment.ARG_CASA,projeto.getCasa());
                intent.putExtra("objectId",projeto.getObjectId());
                startActivity(intent);
            }
        });
        viewCardUltimoProjeto.setVisibility(View.VISIBLE);

    }


    private void updateCardComentarioProjeto(ComentarioEvent event) {
        final ParseObject comentario = event.getComentario();
        final ParseObject projeto =  comentario.getParseObject("proposicao");

        projeto.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                new Card().montaCardComentario(viewCardComentarioProjeto,comentario,projeto);
                viewCardComentarioProjeto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), ProjetoDetailActivity.class);
                        intent.putExtra(ProjetoDetailFragment.ARG_ITEM_ID,String.valueOf(projeto.getNumber("id_proposicao").intValue()));
                        intent.putExtra(ProjetoDetailFragment.ARG_CASA,projeto.getString("tp_casa"));
                        intent.putExtra("objectId",projeto.getObjectId());
                        startActivity(intent);
                    }
                });
            }
        });


    }

    private void updateCardComentarioPolitico(ComentarioEvent event) {
        ParseObject comentario = event.getComentario();
        final ParseObject politico =  comentario.getParseObject("politico");
        try {
            politico.fetchFromLocalDatastore();
            new Card().montaCardComentario(viewCardComentarioPolitico,comentario);
            viewCardComentarioPolitico.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), ParlamentarDetailActivity.class);
                    intent.putExtra(ParlamentarDetailActivity.ID_POLITICO,politico.getObjectId());
                    startActivity(intent);
                }
            });
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private void updateCardComparacao(PoliticoEvent event) {
        Comparacao comparacao = event.getComparacao();
        final ParseObject politico =  comparacao.getCota().getParseObject("politico");

        try {
            politico.fetchFromLocalDatastore();
            new Card().montaCardComparacaoGasto(viewCardComparaGasto,politico,comparacao);
            viewCardComparaGasto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Answers.getInstance().logCustom(new CustomEvent("TouchCardGasto")
                            .putCustomAttribute("Politico", politico.getString("nome")));

                    Intent intent = new Intent(getApplicationContext(), ParlamentarDetailActivity.class);
                    intent.putExtra(ParlamentarDetailActivity.ID_POLITICO,politico.getObjectId());
                    startActivity(intent);
                }
            });
        } catch (ParseException e) {
            e.printStackTrace();
        }

        mNestedScroll.scrollTo(0, 0);

    }

    /**
     * Monta o card Dialoga
     * @param event
     */
    private void montaCardDialoga(DialogaEvent event) {
        pbDialoga.setVisibility(View.GONE);
        //monta o topo
        final ParseObject mPergunta = event.getPergunta();
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
        txtPergunta.setText(mPergunta.getString("texto"));

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

                Answers.getInstance().logCustom(new CustomEvent("TouchCardDialoga")
                        .putCustomAttribute("Digaloga", mPergunta.getObjectId()));

                startActivity(intent);
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();

        if(ParseUser.getCurrentUser()!=null)
            setupHeader();
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
        if (id == R.id.action_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.msg_compartilhe));
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
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
            intent.putExtra("casa","c");
            intent.putExtra("ordem","nome");
            startActivity(intent);

        } else if (id == R.id.nav_projetos_camara) {
            Intent intent = new Intent(this,ProjetoListActivity.class);
            intent.putExtra("casa","c");
            startActivity(intent);

        } else if (id == R.id.nav_cotas) {
            Intent intent = new Intent(this,ParlamentarListActivity.class);
            intent.putExtra("casa","c");
            intent.putExtra("ordem","gastos");
            startActivity(intent);

        } else if (id == R.id.nav_senadores) {
            Intent intent = new Intent(this,ParlamentarListActivity.class);
            intent.putExtra("casa","s");
            intent.putExtra("ordem","nome");
            startActivity(intent);
        }else if (id == R.id.nav_projetos_senado) {
            Intent intent = new Intent(this,ProjetoListActivity.class);
            intent.putExtra("casa","s");
            startActivity(intent);
        }else if (id == R.id.nav_cotas_senado) {
            Intent intent = new Intent(this,ParlamentarListActivity.class);
            intent.putExtra("casa","s");
            intent.putExtra("ordem","gastos");
            startActivity(intent);
        }else if (id == R.id.nav_gostou) {
            DialogFragment dialog = new DialogGostou();
            dialog.show(getSupportFragmentManager(), "dialog");
        }else if (id == R.id.nav_sobre) {
            Intent intent = new Intent(this,SobreActivity.class);
            startActivity(intent);
        }else if (id == R.id.nav_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.msg_compartilhe));
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
