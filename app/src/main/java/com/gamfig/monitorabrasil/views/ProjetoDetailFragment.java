package com.gamfig.monitorabrasil.views;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.gamfig.monitorabrasil.POJO.ProjetoEvent;
import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.actions.ActionsCreator;
import com.gamfig.monitorabrasil.actions.ProjetoActions;
import com.gamfig.monitorabrasil.application.AppController;
import com.gamfig.monitorabrasil.model.Projeto;
import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


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
    private Switch switchAcompanhar;
    private Projeto projeto;
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
        switchAcompanhar = (Switch)view.findViewById(R.id.switchAcompanhar);

        switchAcompanhar.setChecked(projetoActions.estaAcompanhando(Integer.parseInt(getArguments().getString(ARG_ITEM_ID))));

        switchAcompanhar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if(ParseUser.getCurrentUser() != null){
                    projetoActions.salvaProjetoFavorito(projeto.getId(),b);
                }else{
                    Snackbar.make(getView(), "Para salvar é necessário estar logado", Snackbar.LENGTH_LONG)
                            .setAction("Logar", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(AppController.getInstance(),LoginActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    AppController.getInstance().startActivity(intent);
                                }
                            }).show();
                }
            }
        });

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
        projeto = event.getProjeto();
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
