package com.gamfig.monitorabrasil.views.fragments;


import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.actions.ActionsCreator;
import com.gamfig.monitorabrasil.dispatcher.Dispatcher;
import com.gamfig.monitorabrasil.model.Grafico;
import com.gamfig.monitorabrasil.stores.PoliticoStore;
import com.gamfig.monitorabrasil.views.adapters.GastoAdapter;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.parse.ParseObject;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GastosFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GastosFragment extends Fragment implements OnChartValueSelectedListener {
    private static final String ARG_PARAM1 = "param1";

    private String idPolitico;

    private ParseObject politico;
    private Dispatcher dispatcher;
    private ActionsCreator actionsCreator;
    private PoliticoStore politicoStore;
    private RecyclerView mRecyclerView;
    private GastoAdapter mAdapter;

    private PieChart mChart;
    private HorizontalBarChart mHorizontalChart;
    private Typeface tf;
    private Grafico grafico;
    private NestedScrollView mNestedScroll;


    public GastosFragment() {
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
    public static GastosFragment newInstance(String param1) {
        GastosFragment fragment = new GastosFragment();
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
        final View rootView = inflater.inflate(R.layout.fragment_gastos, container, false);
        mNestedScroll = (NestedScrollView)rootView;
        initDependencies();
        //busca as informacoes do politico
        politico = actionsCreator.getPolitico(idPolitico);
        setupView(rootView);
        actionsCreator.getGastos(politico);

        return rootView;
    }

    private void initDependencies() {
        dispatcher = Dispatcher.get(new Bus());
        actionsCreator = ActionsCreator.get(dispatcher);
        politicoStore = PoliticoStore.get(dispatcher);
    }

    private void setupView(View rootView) {

        //tableview
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);
        mAdapter = new GastoAdapter(actionsCreator);
        mRecyclerView.setAdapter(mAdapter);
      //  mAdapter.setRecyclerViewOnClickListenerHack(this);

        tf = Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Regular.ttf");
        grafico = new Grafico(tf);
        //grafico
        mChart = (PieChart) rootView.findViewById(R.id.chart1);
        mChart = grafico.setupPieChartGastos(mChart);

        mChart.setCenterTextTypeface(Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Light.ttf"));
//        mChart.setCenterText(generateCenterSpannableText());
        mChart.setCenterText(politico.getString("nome"));

        // add a selection listener
        mChart.setOnChartValueSelectedListener(this);

        mHorizontalChart = (HorizontalBarChart) rootView.findViewById(R.id.chart2);
        mHorizontalChart = grafico.setupHorizontalChartGastos(mHorizontalChart);


    }

    private void setData(List<ParseObject> gastos) {

        mChart.setData(grafico.pieDataGastos(gastos));
        // undo all highlights
        mChart.highlightValues(null);
        mChart.setDrawSliceText(!mChart.isDrawSliceTextEnabled());
        mChart.invalidate();
        mNestedScroll.scrollTo(0, 0);
    }


    private SpannableString generateCenterSpannableText() {


        SpannableString s = new SpannableString(politico.getString("nome"));
        s.setSpan(new RelativeSizeSpan(1.7f), 0, 14, 0);
     //   s.setSpan(new StyleSpan(Typeface.NORMAL), 14, s.length(), 0);
      //  s.setSpan(new ForegroundColorSpan(Color.GRAY), 14, s.length() , 0);
       // s.setSpan(new RelativeSizeSpan(.8f), 14, s.length() - 15, 0);
        //s.setSpan(new StyleSpan(Typeface.ITALIC), s.length() - 14, s.length(), 0);
        //s.setSpan(new ForegroundColorSpan(ColorTemplate.getHoloBlue()), s.length() - 14, s.length(), 0);
        return s;
    }

    private void updateUI() {
        List<ParseObject> gastos = politicoStore.getGastos();
        if(gastos != null) {
            mAdapter.setItems(gastos);
            setData(gastos);
        }
//       Offset(0f);
    }

    private void updateHorizontalGraph(){
        List<ParseObject> gastos = politicoStore.getGastos();
        if(gastos != null) {
            mHorizontalChart.setData(grafico.horizontalBarDataGastos(gastos));
            // undo all highlights
            mHorizontalChart.animateY(2500);
            mHorizontalChart.invalidate();
        }
    }

    /**
     * Atualiza a UI depois de uma action
     * @param event
     */
    @Subscribe
    public void onTodoStoreChange(PoliticoStore.PoliticoStoreChangeEvent event) {
            updateUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        dispatcher.register(this);
      //  dispatcher.register(politicoStore);
    }

    @Override
    public void onPause() {
        super.onPause();
        dispatcher.unregister(this);
   //     dispatcher.unregister(politicoStore);
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }
}
