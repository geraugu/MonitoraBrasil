package com.gamfig.monitorabrasil.views;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.gamfig.monitorabrasil.POJO.TrunfoEvent;
import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.actions.TrunfoActions;
import com.gamfig.monitorabrasil.interfaces.RecyclerViewOnClickListenerHack;
import com.gamfig.monitorabrasil.views.adapters.PartidaAdapter;
import com.parse.ParseObject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class TrunfoMainFragment extends Fragment  implements RecyclerViewOnClickListenerHack {

    private RecyclerView mRecyclerView;
    private TrunfoActions trunfoActions;
    private ProgressBar pb;
    private PartidaAdapter mAdapter;
    private List<ParseObject> partidas;

    public TrunfoMainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_trunfo_main, container, false);

        initDependencies();
        setupView(rootView);
        trunfoActions.getJogosDisponiveis();

        return rootView;
    }

    private void initDependencies() {
        trunfoActions = TrunfoActions.get();
    }

    private void setupView(View rootView) {
        pb = (ProgressBar)rootView.findViewById(R.id.progressBar8);

        //tableview
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        final LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);
        mAdapter = new PartidaAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setRecyclerViewOnClickListenerHack(this);
    }


    /**
     * Atualiza a UI depois de uma action
     * @param event
     */
    @Subscribe
    public void onMessageEvent(TrunfoEvent event){
        if(event.getAction().equals(TrunfoActions.TRUNFO_GET_JOGOS_DISPONIVEIS)){
            partidas = event.getPartidas();
            updateUI();
        }

    }

    private void updateUI() {
        pb.setVisibility(View.INVISIBLE);
        mAdapter.setItems(partidas);
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

    @Override
    public void onClickListener(View view, int position) {

    }
}
