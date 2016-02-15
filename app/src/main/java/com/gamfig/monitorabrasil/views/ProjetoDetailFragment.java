package com.gamfig.monitorabrasil.views;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gamfig.monitorabrasil.POJO.ProjetoEvent;
import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.actions.ActionsCreator;
import com.gamfig.monitorabrasil.actions.ProjetoActions;
import com.gamfig.monitorabrasil.model.Projeto;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * A fragment representing a single Projeto detail screen.
 * This fragment is either contained in a {@link ProjetoListActivity}
 * in two-pane mode (on tablets) or a {@link ProjetoDetailActivity}
 * on handsets.
 */
public class ProjetoDetailFragment extends Fragment {


    private ActionsCreator actionsCreator;
    private ProjetoActions projetoActions;
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
        projetoActions.getInfoProjeto(getArguments().getString(ARG_ITEM_ID),getArguments().getString(ARG_CASA));
        return rootView;
    }

    private void initDependencies() {
        actionsCreator = ActionsCreator.get();
        projetoActions = ProjetoActions.get();
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
    public void onMessageEvent(ProjetoEvent event){
        updateUI(event);
    }


    private void updateUI(ProjetoEvent event) {
        Projeto projeto = event.getProjeto();
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
