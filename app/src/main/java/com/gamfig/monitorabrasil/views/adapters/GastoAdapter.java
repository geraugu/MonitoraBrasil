package com.gamfig.monitorabrasil.views.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.actions.ActionsCreator;
import com.gamfig.monitorabrasil.interfaces.RecyclerViewOnClickListenerHack;
import com.gamfig.monitorabrasil.util.MyValueFormatter;
import com.parse.ParseObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by geral_000 on 05/07/2015.
 */
public class GastoAdapter extends RecyclerView.Adapter<GastoAdapter.ViewHolder> {

    private static ActionsCreator actionsCreator;
    private List<ParseObject> mDataset;
    private RecyclerViewOnClickListenerHack mRecyclerViewOnClickListenerHack;

    private SimpleDateFormat dt1 = new SimpleDateFormat("dd/MM/yy HH:mm");
    private Calendar cal = Calendar.getInstance();
    private MyValueFormatter formatter;

    public GastoAdapter(ActionsCreator actionsCreator) {
        mDataset = new ArrayList<>();
        GastoAdapter.actionsCreator = actionsCreator;
        formatter = new MyValueFormatter();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        // create a new view
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_gasto, viewGroup, false);
        // set the view's size, margins, paddings and layout parameter
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }
    public void setRecyclerViewOnClickListenerHack(RecyclerViewOnClickListenerHack r){
        mRecyclerViewOnClickListenerHack = r;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        ParseObject gasto = mDataset.get(i);

        viewHolder.categoria.setText(gasto.getString("categoria"));
        viewHolder.valor.setText("R$ "+
                            formatter.formata(gasto.getNumber("total").floatValue()));

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void setItems(List<ParseObject> comentarios) {
        this.mDataset = comentarios;
        notifyDataSetChanged();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        // each data item is just a string in this case
        public TextView categoria;
        public TextView valor;
        public ViewHolder(View v) {
            super(v);
            categoria = (TextView) v.findViewById(R.id.categoria);
            valor = (TextView) v.findViewById(R.id.valor);
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
