package com.gamfig.monitorabrasil.views.fragments;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.gamfig.monitorabrasil.interfaces.RecyclerViewOnClickListenerHack;
import com.gamfig.monitorabrasil.model.Comparacao;
import com.gamfig.monitorabrasil.model.Projeto;
import com.gamfig.monitorabrasil.model.Tema;
import com.gamfig.monitorabrasil.views.DialogaActivity;
import com.gamfig.monitorabrasil.views.ParlamentarDetailActivity;
import com.gamfig.monitorabrasil.views.ProjetoDetailActivity;
import com.gamfig.monitorabrasil.views.ProjetoDetailFragment;
import com.gamfig.monitorabrasil.views.util.Card;
import com.parse.ConfigCallback;
import com.parse.GetCallback;
import com.parse.ParseConfig;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.twitter.sdk.android.tweetui.SearchTimeline;
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Created by geral_000 on 20/02/2016.
 */
public class ContentMain extends Fragment {

    ActionsCreator actionsCreator;
    DialogaActions dialogaActions;
    PoliticoActions politicoActions;
    ComentarioActions comentarioActions;
    ProjetoActions projetoActions;

    private NestedScrollView mNestedScroll;

    private ProgressBar pbDialoga;

    private View viewCardComparaGasto;
    private View viewCardComentarioPolitico;
    private View viewCardComentarioProjeto;
    private View viewCardUltimoProjeto;

    private View mainView;


    public static ContentMain newInstance() {
        ContentMain fragment = new ContentMain();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.content_main, container, false);
        initDependencies();
        setupView(rootView);
        mainView = rootView;

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
            //busca ultimo projeto de um político que está monitorando
            projetoActions.getUltimoProjeto();
        }

        return rootView;
    }



    private void initDependencies() {

        actionsCreator = ActionsCreator.get();
        comentarioActions = ComentarioActions.get();
        dialogaActions = DialogaActions.get();
        politicoActions = PoliticoActions.get();
        projetoActions = ProjetoActions.get();
    }

    private void setupView(View view) {

        mNestedScroll = (NestedScrollView)view.findViewById(R.id.nested);
        pbDialoga = (ProgressBar) view.findViewById(R.id.pbDialoga);
        viewCardComparaGasto =  view.findViewById(R.id.llCardComparaGastos);
        viewCardComentarioPolitico = view.findViewById(R.id.llCardComentario);
        viewCardComentarioProjeto = view.findViewById(R.id.llCardComentarioProjeto);
        viewCardUltimoProjeto = view.findViewById(R.id.llCardUltimoProjeto);
        viewCardUltimoProjeto.setVisibility(View.GONE);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(menu.findItem(R.id.action_share)== null) {
            inflater.inflate(R.menu.main, menu);
            MenuItem actionShare = menu.findItem(R.id.action_share);
            if (menu.findItem(R.id.action_search) != null) {
                menu.removeItem(R.id.action_search);
            }

            actionShare.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.msg_compartilhe));
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                    return true;
                }
            });
        }

    }

    private void buscaTweet() {
        SearchTimeline searchTimeline = new SearchTimeline.Builder()
                .query("#monitoraBrasil")
                .maxItemsPerRequest(1)
                .build();

        final TweetTimelineListAdapter adapter = new TweetTimelineListAdapter.Builder(getActivity())
                .setTimeline(searchTimeline)
                .build();


        ListView lvTwitter = (ListView) mainView.findViewById(R.id.card_twitter);

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
                Intent intent = new Intent(getContext().getApplicationContext(), ProjetoDetailActivity.class);
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
                        Intent intent = new Intent(getContext().getApplicationContext(), ProjetoDetailActivity.class);
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
                    Intent intent = new Intent(getContext().getApplicationContext(), ParlamentarDetailActivity.class);
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

                    Intent intent = new Intent(getContext().getApplicationContext(), ParlamentarDetailActivity.class);
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

        TextView txtTema = (TextView) mainView.findViewById(R.id.txtNomeTema);
        txtTema.setText(mTema.getString("Nome"));
        LinearLayout linearLayout = (LinearLayout) mainView.findViewById(R.id.linearLayout);
        linearLayout.setBackgroundResource(Tema.buscaCor(mTema.getString("imagem")));
        LinearLayout llCardDialoga = (LinearLayout) mainView.findViewById(R.id.llCardDialoga);
        llCardDialoga.setBackgroundResource(Tema.buscaCor(mTema.getString("imagem")));
        ImageView imgIcone = (ImageView)mainView.findViewById(R.id.icone);
        imgIcone.setBackgroundResource(Tema.buscaIcone(mTema.getString("imagem")));

        //preenche a pergunta
        TextView txtPergunta = (TextView) mainView.findViewById(R.id.txtPergunta);
        txtPergunta.setText(mPergunta.getString("texto"));

        //evento do botao para ir para activity dialoga
        Button btnDialoga = (Button) mainView.findViewById(R.id.btnDialoga);
        final Intent intent = new Intent(getActivity(),DialogaActivity.class);
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





}
