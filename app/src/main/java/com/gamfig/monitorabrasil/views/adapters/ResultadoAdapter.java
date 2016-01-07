package com.gamfig.monitorabrasil.views.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.actions.ActionsCreator;
import com.gamfig.monitorabrasil.interfaces.RecyclerViewOnClickListenerHack;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by geraugu on 6/7/15.
 */
public class ResultadoAdapter extends RecyclerView.Adapter<ResultadoAdapter.ViewHolder> {

    private static ActionsCreator actionsCreator;
    private List<ParseObject> resultado;
    private RecyclerViewOnClickListenerHack mRecyclerViewOnClickListenerHack;

    public ResultadoAdapter(ActionsCreator actionsCreator) {
        resultado = new ArrayList<>();
        ResultadoAdapter.actionsCreator = actionsCreator;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        // create a new view
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_resultado, viewGroup, false);
        // set the view's size, margins, paddings and layout parameter
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    public void setRecyclerViewOnClickListenerHack(RecyclerViewOnClickListenerHack r){
        mRecyclerViewOnClickListenerHack = r;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        ParseObject resposta = resultado.get(i);
        viewHolder.txtResposta.setText(resposta.get("texto").toString());
        viewHolder.txtQtdSim.setText(String.valueOf(resposta.getInt("qtd_sim")));
    }

    @Override
    public int getItemCount() {
        return resultado.size();
    }

    public void setItems(List<ParseObject> politicos) {
        this.resultado = politicos;
        notifyDataSetChanged();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        // each data item is just a string in this case
        public TextView txtResposta;
        public TextView txtQtdSim;
        public ViewHolder(View v) {
            super(v);
            txtResposta = (TextView) v.findViewById(R.id.txtResposta);
            txtQtdSim = (TextView) v.findViewById(R.id.txtQtdSim);


            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(mRecyclerViewOnClickListenerHack != null){
                mRecyclerViewOnClickListenerHack.onClickListener(v, getPosition());
            }
        }
    }
}
