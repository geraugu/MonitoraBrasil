package com.gamfig.monitorabrasil.views;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.actions.ActionsCreator;
import com.gamfig.monitorabrasil.dispatcher.Dispatcher;
import com.gamfig.monitorabrasil.model.Projeto;
import com.gamfig.monitorabrasil.stores.ProjetoStore;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

/**
 * A fragment representing a single Projeto detail screen.
 * This fragment is either contained in a {@link ProjetoListActivity}
 * in two-pane mode (on tablets) or a {@link ProjetoDetailActivity}
 * on handsets.
 */
public class ProjetoDetailFragment extends Fragment {

    private Dispatcher dispatcher;
    private ActionsCreator actionsCreator;
    private ProjetoStore projetoStore;
    private TextView txtDtApresentacao;
    private TextView txtAutor;
    private TextView txtSituacao;
    private TextView txtdtUltimoDespacho;
    private TextView txtUltimoDespacho;
    private TextView txtEmenta;
    private TextView txtExplicacao;
    private TextView titulo;
    private TextView link;
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    public static final String ARG_CASA = "item_casa";


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ProjetoDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {



            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
//            if (appBarLayout != null) {
//               appBarLayout.setTitle("teste2");
//            }

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_projeto_detail, container, false);

        initDependencies();
        setupView(rootView);
        actionsCreator.getInfoProjeto(getArguments().getString(ARG_ITEM_ID),getArguments().getString(ARG_CASA));
        return rootView;
    }

    private void initDependencies() {
        dispatcher = Dispatcher.get(new Bus());
        actionsCreator = ActionsCreator.get(dispatcher);
        projetoStore = ProjetoStore.get(dispatcher);
    }

    private void setupView(View view) {
        txtDtApresentacao= (TextView) view.findViewById(R.id.txtDtApresentacao);
        txtAutor= (TextView) view.findViewById(R.id.txtAutor);
        txtSituacao= (TextView) view.findViewById(R.id.txtSituacao);
        txtdtUltimoDespacho= (TextView) view.findViewById(R.id.txtdtUltimoDespacho);
        txtUltimoDespacho= (TextView) view.findViewById(R.id.txtUltimoDespacho);
        txtEmenta= (TextView) view.findViewById(R.id.txtEmenta);
        txtExplicacao= (TextView) view.findViewById(R.id.txtExplicacao);
        titulo= (TextView) view.findViewById(R.id.title);
        link= (TextView) view.findViewById(R.id.link);
    }

    /**
     * Atualiza a UI depois de uma action
     * @param event
     */
    @Subscribe
    public void onTodoStoreChange(ProjetoStore.ProjetoStoreChangeEvent event) {

        updateUI();
    }

    private void updateUI() {
        Projeto projeto = projetoStore.getmProjeto();
        titulo.setText(projeto.getNome());
        txtDtApresentacao.setText(projeto.getDtApresentacao());
        txtAutor.setText(projeto.getNomeAutor());
        txtSituacao.setText(projeto.getSituacao());
        txtdtUltimoDespacho.setText(projeto.getDtUltimoDespacho());
        txtUltimoDespacho.setText(projeto.getUltimoDespacho());
        txtEmenta.setText(projeto.getEmenta());
        txtExplicacao.setText(projeto.getExplicacao());
        link.setText(projeto.getLink());
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


}
