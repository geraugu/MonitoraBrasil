package com.gamfig.monitorabrasil.views.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.actions.ActionsCreator;
import com.gamfig.monitorabrasil.dispatcher.Dispatcher;
import com.gamfig.monitorabrasil.interfaces.RecyclerViewOnClickListenerHack;
import com.gamfig.monitorabrasil.stores.ProjetoStore;
import com.gamfig.monitorabrasil.views.ProjetoDetailActivity;
import com.gamfig.monitorabrasil.views.ProjetoDetailFragment;
import com.gamfig.monitorabrasil.views.adapters.ProjetoAdapter;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

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
    private Dispatcher dispatcher;
    private ActionsCreator actionsCreator;
    private ProjetoStore projetoStore;
    private RecyclerView mRecyclerView;
    private ProjetoAdapter mAdapter;
    private int previousTotal = 0;
    private boolean loading = true;
    private int visibleThreshold = 2;
    int firstVisibleItem, visibleItemCount, totalItemCount;



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
        //busca as informacoes do politico
        politico = ParseObject.createWithoutData("Politico",idPolitico);
        try {
            politico.fetchFromLocalDatastore();
            setupView(rootView);
            projetoStore.limpaProjetos();
            actionsCreator.getAllProjetos(politico,politico.getString("tipo"),0);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        return rootView;
    }

    private void initDependencies() {
        dispatcher = Dispatcher.get(new Bus());
        actionsCreator = ActionsCreator.get(dispatcher);
        projetoStore = ProjetoStore.get(dispatcher);
    }

    private void setupView(View rootView) {

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
                    actionsCreator.getAllProjetos(politico,politico.getString("tipo"), previousTotal);

                    loading = true;
                }
            }
        });


    }


    private void updateUI() {
        List<ParseObject> projetos = projetoStore.getProjetos();
        mAdapter.setItems(projetos);

    }

    /**
     * Atualiza a UI depois de uma action
     * @param event
     */
    @Subscribe
    public void onTodoStoreChange(ProjetoStore.ProjetoStoreChangeEvent event) {
            updateUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        dispatcher.register(this);
        dispatcher.register(projetoStore);
    }

    @Override
    public void onPause() {
        super.onPause();
        dispatcher.unregister(this);
        dispatcher.unregister(projetoStore);
    }


    @Override
    public void onClickListener(View view, int position) {
        ParseObject projeto = projetoStore.getProjetos().get(position);
        Intent intent = new Intent(getContext(), ProjetoDetailActivity.class);
        intent.putExtra(ProjetoDetailFragment.ARG_ITEM_ID,String.valueOf(projeto.getNumber("id_proposicao").intValue()));
        intent.putExtra(ProjetoDetailFragment.ARG_CASA,projeto.getString("tp_casa"));
        intent.putExtra("objectId",projeto.getObjectId());
        startActivity(intent);
    }
}
