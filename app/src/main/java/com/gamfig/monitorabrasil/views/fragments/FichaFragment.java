package com.gamfig.monitorabrasil.views.fragments;


import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.actions.ActionsCreator;
import com.gamfig.monitorabrasil.actions.PoliticoActions;
import com.gamfig.monitorabrasil.dispatcher.Dispatcher;
import com.gamfig.monitorabrasil.model.Comparacao;
import com.gamfig.monitorabrasil.model.Grafico;
import com.gamfig.monitorabrasil.stores.PoliticoStore;
import com.gamfig.monitorabrasil.views.adapters.PresencaAdapter;
import com.gamfig.monitorabrasil.views.dialogs.DialogAvaliacao;
import com.gamfig.monitorabrasil.views.util.Card;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FichaFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FichaFragment extends Fragment implements OnChartValueSelectedListener {
    private static final String ARG_PARAM1 = "param1";

    private String idPolitico;

    private ParseObject politico;
    private Dispatcher dispatcher;
    private ActionsCreator actionsCreator;
    private PoliticoStore politicoStore;
    private RecyclerView mRecyclerView;
    private PresencaAdapter mAdapter;
    private RatingBar mRatingBar;
    private NestedScrollView mNestedScroll;

    private PieChart mChart;
    private Typeface tf;
    private Grafico grafico;
    private View mView;


    public FichaFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment GastosFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FichaFragment newInstance(String param1) {
        FichaFragment fragment = new FichaFragment();
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
        final View rootView = inflater.inflate(R.layout.fragment_ficha, container, false);
        mNestedScroll = (NestedScrollView)rootView;
        initDependencies();
        //busca as informacoes do politico
        politico = actionsCreator.getPolitico(idPolitico);
        setupView(rootView);
        actionsCreator.getComparacaoGasto(politico.getObjectId());
        
        //so tem presenca para os deputados
        if(politico.getString("tipo").equals("c"))
            actionsCreator.getPresenca(politico);

        return rootView;
    }

    private void initDependencies() {
        dispatcher = Dispatcher.get(new Bus());
        actionsCreator = ActionsCreator.get(dispatcher);
        politicoStore = PoliticoStore.get(dispatcher);
    }

    private void setupView(View rootView) {
        mView= rootView;
        mRatingBar = (RatingBar)rootView.findViewById(R.id.ratingBar2);
        //tableview
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view_presenca);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);
        mAdapter = new PresencaAdapter(actionsCreator);
        mRecyclerView.setAdapter(mAdapter);
      //  mAdapter.setRecyclerViewOnClickListenerHack(this);


        //monta ficha
        montaFicha(rootView);

        Button btnAvalie = (Button) rootView.findViewById(R.id.btnAvalie);
        //btnAvaliar
        btnAvalie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (ParseUser.getCurrentUser() != null) {
                    DialogAvaliacao avaliacao = new DialogAvaliacao(politico, "Avalie");
                    avaliacao.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            //atualizar a avaliacao
                            try {
                                politico.fetchFromLocalDatastore();
                                mRatingBar.setRating((float) politico.getDouble("avaliacao"));
                                Snackbar.make(v, "Avaliação salva.", Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                        }
                    });
                    avaliacao.show(getActivity().getSupportFragmentManager(), "dialogAvaliar");
                } else {
                    Snackbar.make(v, "É necessário logar para avaliar.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }

            }
        });

    }

    private void montaFicha(View rootView) {
        TextView txtPartido = (TextView) rootView.findViewById(R.id.txtPartido);
        TextView txtTwitter = (TextView) rootView.findViewById(R.id.txtTwitter);
        TextView txtEmail = (TextView) rootView.findViewById(R.id.txtEmail);
        TextView txtTelefone = (TextView) rootView.findViewById(R.id.txtTelefone);
        TextView txtGabinete = (TextView) rootView.findViewById(R.id.txtGabinete);

        txtPartido.setText(politico.getString("siglaPartido")+"-"+politico.getString("uf"));
        if(politico.getString("twitter")!= null)
            txtTwitter.setText(politico.getString("twitter"));
        txtEmail.setText("Email: "+politico.getString("email"));
        txtTelefone.setText("Tel: "+politico.getString("telefone"));
        if(politico.getString("gabinete") != null)
            txtGabinete.setText("Gabinete: "+politico.getString("gabinete")
            +" Anexo "+politico.getString("anexo"));
        if(politico.getString("endereco")!=null)
            txtGabinete.setText(politico.getString("endereco"));

        mRatingBar.setRating((float) politico.getDouble("mediaAvaliacao"));
    }



    private void updateCardComparacao() {
        Comparacao comparacao = politicoStore.getGasto();

        new Card().montaCardComparacaoGasto(mView,politico,comparacao);

        mNestedScroll.scrollTo(0, 0);
    }

    /**
     * Atualiza a UI depois de uma action
     * @param event
     */
    @Subscribe
    public void onTodoStoreChange(PoliticoStore.PoliticoStoreChangeEvent event) {
        String evento =event.getEvento();
        
        switch (evento){
            case PoliticoActions.POLITICO_GET_COMPARACAO_GASTO:
                updateCardComparacao();
                break;
            case PoliticoActions.POLITICO_GET_PRESENCA:
                carregaPresenca();
                break;
        }
            
    }

    private void carregaPresenca() {
        List<ParseObject> presencas = politicoStore.getPresenca();
        mAdapter.setItems(presencas);
        mNestedScroll.scrollTo(0, 0);
    }


    @Override
    public void onResume() {
        super.onResume();
        dispatcher.register(this);
        dispatcher.register(politicoStore);
    }

    @Override
    public void onPause() {
        super.onPause();
        dispatcher.unregister(this);
        dispatcher.unregister(politicoStore);
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }
}
