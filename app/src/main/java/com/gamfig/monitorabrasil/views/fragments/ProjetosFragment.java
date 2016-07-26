package com.gamfig.monitorabrasil.views.fragments;


import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;

import com.gamfig.monitorabrasil.POJO.ProjetoEvent;
import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.actions.ActionsCreator;
import com.gamfig.monitorabrasil.actions.ProjetoActions;
import com.gamfig.monitorabrasil.interfaces.RecyclerViewOnClickListenerHack;
import com.gamfig.monitorabrasil.views.ProjetoDetailActivity;
import com.gamfig.monitorabrasil.views.ProjetoDetailFragment;
import com.gamfig.monitorabrasil.views.adapters.ProjetoAdapter;
import com.parse.ParseException;
import com.parse.ParseObject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProjetosFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProjetosFragment extends Fragment implements RecyclerViewOnClickListenerHack{
    private static final String ARG_PARAM1 = "param1";

    private String idPolitico;

    private ParseObject politico;
    private ActionsCreator actionsCreator;
    private ProjetoActions projetoActions;
    private ProjetoEvent projetoEvent;

    private Switch swCamara;
    private Switch swSenado;
    private Switch swMonitorados;

    private RecyclerView mRecyclerView;
    private ProjetoAdapter mAdapter;
    private int previousTotal = 0;
    private boolean loading = true;
    private int visibleThreshold = 2;
    int firstVisibleItem, visibleItemCount, totalItemCount;
    boolean camara,senado,monitorado;

    private LinearLayout llFiltro;
    private ProgressBar pb;

    private boolean realizouBusca;


    public ProjetosFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment GastosFragment.
     */
    public static ProjetosFragment newInstance(String param1) {
        ProjetosFragment fragment = new ProjetosFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            idPolitico = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_projetos, container, false);
        initDependencies();
        setupView(rootView);
        if(idPolitico != null){
            llFiltro.setVisibility(View.GONE);
            politico = ParseObject.createWithoutData("Politico",idPolitico);
            try {
                politico.fetchFromLocalDatastore();

                // projetoStore.limpaProjetos();
                projetoActions.getAllProjetos(politico,politico.getString("tipo"),0, null);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }else{
            projetoActions.getAllProjetos(camara,senado,monitorado, previousTotal, null);
        }

        pb.setVisibility(View.VISIBLE);

        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_lista_projetos, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

                @Override
                public boolean onQueryTextSubmit(String query) {
                    if (!query.isEmpty()) {
                        //realizar a busca

                        pb.setVisibility(View.VISIBLE);
                        projetoEvent.limpaProjetos();
                        projetoActions.buscaProjetoPorPalavra(query.trim(),camara,senado,monitorado);
                        realizouBusca = true;
                    }

                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if(realizouBusca && newText.isEmpty()){
                        projetoEvent.limpaProjetos();
                        projetoActions.getAllProjetos(camara,senado,monitorado, 0, null);
                        realizouBusca = false;
                    }
                    return false;
                }
            });
        }


    }

    private void initDependencies() {
        actionsCreator = ActionsCreator.get();
        projetoActions = ProjetoActions.get();
        camara = true;
        senado = true;
    }

    private void setupView(View rootView) {
        pb = (ProgressBar)rootView.findViewById(R.id.progressBar7);
        llFiltro = (LinearLayout)rootView.findViewById(R.id.llFiltro);

        //tableview
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rec_projetos);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        final LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);
        mAdapter = new ProjetoAdapter(actionsCreator);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setRecyclerViewOnClickListenerHack(this);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                visibleItemCount = mRecyclerView.getChildCount();
                totalItemCount = llm.getItemCount();
                firstVisibleItem = llm.findFirstVisibleItemPosition();

                if (loading) {
                    if (totalItemCount > previousTotal) {
                        loading = false;
                        previousTotal = totalItemCount;
                    }
                }
                if (!loading && (totalItemCount - visibleItemCount)
                        <= (firstVisibleItem + visibleThreshold) && totalItemCount > 14) {
                    //carregar mais projetos
                    if(idPolitico != null){
                        projetoActions.getAllProjetos(politico,politico.getString("tipo"), previousTotal, projetoEvent.getProjetos());
                    }else{
                        projetoActions.getAllProjetos(camara,senado,monitorado, previousTotal, projetoEvent.getProjetos());
                    }

                    loading = true;
                }
            }
        });
        swCamara = (Switch) rootView.findViewById(R.id.swCamara);
        swSenado = (Switch) rootView.findViewById(R.id.swSenado);
        swMonitorados = (Switch) rootView.findViewById(R.id.swMonitorados);

        swCamara.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    camara = true;
                }else{
                    camara = false;
                }
                pb.setVisibility(View.VISIBLE);
                projetoActions.getAllProjetos(camara,senado,monitorado, 0, null);
            }
        });

        swSenado.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    senado = true;
                }else{
                    senado = false;
                }
                pb.setVisibility(View.VISIBLE);
                projetoActions.getAllProjetos(camara,senado,monitorado, 0, null);
            }
        });

        swMonitorados.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    monitorado = true;
                }else{
                    monitorado = false;
                }
                pb.setVisibility(View.VISIBLE);
                projetoActions.getAllProjetos(camara,senado,monitorado, 0, null);
            }
        });


    }


    private void updateUI() {
        pb.setVisibility(View.INVISIBLE);
        List<ParseObject> projetos = projetoEvent.getProjetos();
        mAdapter.setItems(projetos);

    }

    /**
     * Atualiza a UI depois de uma action
     * @param event
     */
    @Subscribe
    public void onMessageEvent(ProjetoEvent event){
        if(event.getAction().equals(ProjetoActions.PROJETO_GET_TODOS)
                || event.getAction().equals(ProjetoActions.PROJETO_GET_PROCURA)) {
            projetoEvent = event;
            updateUI();
        }
    }

    // This method will be called when a SomeOtherEvent is posted
//    @Subscribe
//    public void onEvent(SomeOtherEvent event){
//        doSomethingWith(event);
//    }

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
    public void onClickListener(View view, int position) {
        ParseObject projeto = projetoEvent.getProjetos().get(position);
        Intent intent = new Intent(getContext(), ProjetoDetailActivity.class);
        intent.putExtra(ProjetoDetailFragment.ARG_ITEM_ID,String.valueOf(projeto.getNumber("id_proposicao").intValue()));
        intent.putExtra(ProjetoDetailFragment.ARG_CASA,projeto.getString("tp_casa"));
        intent.putExtra("objectId",projeto.getObjectId());
        startActivity(intent);
    }

}
