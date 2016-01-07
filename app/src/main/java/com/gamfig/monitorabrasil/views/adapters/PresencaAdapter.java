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
public class PresencaAdapter extends RecyclerView.Adapter<PresencaAdapter.ViewHolder> {

    private static ActionsCreator actionsCreator;
    private List<ParseObject> presencas;
    private RecyclerViewOnClickListenerHack mRecyclerViewOnClickListenerHack;


    public PresencaAdapter(ActionsCreator actionsCreator) {
        presencas = new ArrayList<>();
        PresencaAdapter.actionsCreator = actionsCreator;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        // create a new view
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_presenca, viewGroup, false);
        // set the view's size, margins, paddings and layout parameter
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    public void setRecyclerViewOnClickListenerHack(RecyclerViewOnClickListenerHack r){
        mRecyclerViewOnClickListenerHack = r;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        ParseObject presenca = presencas.get(i);
        viewHolder.txtAno.setText(presenca.getNumber("nr_ano").toString());
        viewHolder.txtPresenca.setText(presenca.getNumber("nr_presenca").toString());
        viewHolder.txtFaltaJust.setText(presenca.getNumber("nr_ausencia_justificada").toString());
        viewHolder.txtFaltaNaoJusti.setText(presenca.getNumber("nr_ausencia_nao_justificada").toString());

    }

    @Override
    public int getItemCount() {
        return presencas.size();
    }

    public void setItems(List<ParseObject> presencas) {
        this.presencas = presencas;
        notifyDataSetChanged();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        // each data item is just a string in this case
        public TextView txtAno;
        public TextView txtPresenca;
        public TextView txtFaltaJust;
        public TextView txtFaltaNaoJusti;
        public TextView txtTotal;
        public String idObjectPolitico;
        public ViewHolder(View v) {
            super(v);
            txtAno = (TextView) v.findViewById(R.id.txtAno);
            txtPresenca = (TextView) v.findViewById(R.id.txtPresenca);
            txtFaltaJust = (TextView) v.findViewById(R.id.txtFaltaJust);
            txtFaltaNaoJusti = (TextView) v.findViewById(R.id.txtFaltaNaoJusti);
            txtTotal = (TextView) v.findViewById(R.id.txtTotal);

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
