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
import android.widget.ProgressBar;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.gamfig.monitorabrasil.POJO.DialogaEvent;
import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.actions.ActionsCreator;
import com.gamfig.monitorabrasil.actions.DialogaActions;
import com.gamfig.monitorabrasil.interfaces.RecyclerViewOnClickListenerHack;
import com.gamfig.monitorabrasil.model.Tema;
import com.gamfig.monitorabrasil.views.adapters.TemaAdapter;
import com.parse.ParseException;
import com.parse.ParseObject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * A placeholder fragment containing a simple view.
 */
public class DialogaActivityFragment extends Fragment implements RecyclerViewOnClickListenerHack {

    private RecyclerView mRecyclerView;
    private TemaAdapter mAdapter;


    private ActionsCreator actionsCreator;
    private DialogaActions dialogaActions;
    private DialogaEvent dialogaEvent;

    private ProgressBar pb;

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

        dialogaActions.getAllTemas();


        return view;
    }

    private void initDependencies() {
        actionsCreator = ActionsCreator.get();
        dialogaActions = DialogaActions.get();
    }


    private void setupView(View view) {
        pb = (ProgressBar) view.findViewById(R.id.progressBar4);
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

        mAdapter.setItems(dialogaEvent.getTemas());
        pb.setVisibility(View.INVISIBLE);
    }


    /**
     * Atualiza a UI depois de uma action
     * @param event
     */
    @Subscribe
    public void onMessageEvent(DialogaEvent event){
        dialogaEvent = event;
        switch (dialogaEvent.getAction()){
            case DialogaActions.DIALOGA_GET_TEMAS:
                updateUI();
                break;
            case DialogaActions.DIALOGA_ENVIAR_PERGUNTA:
                if(dialogaEvent.getErro()==null) {
                    Snackbar.make(getView(), "Pergunta inserida!", Snackbar.LENGTH_LONG)
                            .show();
                }else{
                    Snackbar.make(getView(), "Erro ao inserir a pergunta", Snackbar.LENGTH_LONG)
                            .show();
                }

                break;
        }
    }


    @Override
    public void onClickListener(View view, int position) {
        ParseObject tema =  dialogaEvent.getTemas().get(position);
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
