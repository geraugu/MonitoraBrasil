package com.gamfig.monitorabrasil.views.fragments;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.actions.ActionsCreator;
import com.gamfig.monitorabrasil.actions.DialogaActions;
import com.gamfig.monitorabrasil.dispatcher.Dispatcher;
import com.gamfig.monitorabrasil.interfaces.RecyclerViewOnClickListenerHack;
import com.gamfig.monitorabrasil.model.Tema;
import com.gamfig.monitorabrasil.stores.DialogaStore;
import com.gamfig.monitorabrasil.views.adapters.TemaAdapter;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

/**
 * A placeholder fragment containing a simple view.
 */
public class DialogaActivityFragment extends Fragment implements RecyclerViewOnClickListenerHack {

    private RecyclerView mRecyclerView;
    private TemaAdapter mAdapter;

    private Dispatcher dispatcher;
    private ActionsCreator actionsCreator;
    private DialogaStore dialogaStore;

    public DialogaActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialoga, container, false);
        initDependencies();
        setupView(view);

        //verificar se tem um id de pergunta
        if(getArguments() != null){
            String perguntaId = getArguments().getString("perguntaId");

            if(null != perguntaId){
                String temaId = getArguments().getString("temaId");
                ParseObject tema = Tema.getTema(temaId);
                DialogaVotoFragment frag = DialogaVotoFragment.newInstance(
                        tema,perguntaId);
                getArguments().clear();

                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment, frag, "dialogaVoto");
                ft.addToBackStack("tag");
                ft.commit();
            }


        }

        actionsCreator.getAllTemas();


        return view;
    }

    private void initDependencies() {
        dispatcher = Dispatcher.get(new Bus());
        actionsCreator = ActionsCreator.get(dispatcher);
        dialogaStore = DialogaStore.get(dispatcher);
    }


    private void setupView(View view) {

//tableview
        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_temas);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager llm = new GridLayoutManager(getActivity(),2,GridLayoutManager.VERTICAL, false);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);
        mAdapter = new TemaAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setRecyclerViewOnClickListenerHack(this);
    }

    private void updateUI() {
        mAdapter.setItems(dialogaStore.getTemas());
//        pb.setVisibility(View.INVISIBLE);
    }


    /**
     * Atualiza a UI depois de uma action
     * @param event
     */
    @Subscribe
    public void onTodoStoreChange(DialogaStore.DialogaStoreChangeEvent event) {
        String evento = dialogaStore.getEvento();
        switch (evento){
            case DialogaActions.DIALOGA_GET_TEMAS:
                updateUI();
                break;
            case DialogaActions.DIALOGA_ENVIAR_PERGUNTA:
                Snackbar.make(getView(), "Pergunta inserida!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                break;
        }
    }

    @Override
    public void onClickListener(View view, int position) {


        ParseObject tema =  dialogaStore.getTemas().get(position);
        try {
            tema.pin();
            DialogaListaPerguntasFragment frag = DialogaListaPerguntasFragment.newInstance(
                    tema.getString("Nome"),tema.getString("imagem"),
                    Tema.buscaCor(tema.getString("imagem")),tema.getObjectId());

            Answers.getInstance().logCustom(new CustomEvent("TouchTema")
                    .putCustomAttribute("tema", tema.getString("Nome")));

            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment, frag, "dialogaListaPerguntas");
            ft.addToBackStack("tag");
            ft.commit();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        dispatcher.register(this);
        dispatcher.register(dialogaStore);
    }

    @Override
    public void onPause() {
        super.onPause();
        dispatcher.unregister(this);
        dispatcher.unregister(dialogaStore);
    }

}
