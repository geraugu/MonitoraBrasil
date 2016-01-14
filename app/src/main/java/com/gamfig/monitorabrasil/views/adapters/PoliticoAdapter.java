package com.gamfig.monitorabrasil.views.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.actions.ActionsCreator;
import com.gamfig.monitorabrasil.interfaces.RecyclerViewOnClickListenerHack;
import com.gamfig.monitorabrasil.model.Imagens;
import com.parse.ParseObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by geraugu on 6/7/15.
 */
public class PoliticoAdapter extends RecyclerView.Adapter<PoliticoAdapter.ViewHolder> {

    private static ActionsCreator actionsCreator;
    private List<ParseObject> politicos;
    private RecyclerViewOnClickListenerHack mRecyclerViewOnClickListenerHack;

    private NumberFormat mFormat;
    private String tipo;//camara ou senado

    public PoliticoAdapter(ActionsCreator actionsCreator, String tipo) {
        politicos = new ArrayList<>();
        PoliticoAdapter.actionsCreator = actionsCreator;
        this.tipo = tipo;
        mFormat = NumberFormat.getInstance();
        mFormat.setMaximumFractionDigits(2);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        // create a new view
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_politico, viewGroup, false);
        // set the view's size, margins, paddings and layout parameter
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    public void setRecyclerViewOnClickListenerHack(RecyclerViewOnClickListenerHack r){
        mRecyclerViewOnClickListenerHack = r;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {

        ParseObject politico = politicos.get(i);
        Number gasto;
        if(politico.getParseObject("politico")!= null){
            gasto = politico.getNumber("total");
            politico = politico.getParseObject("politico");
        }else{
            gasto = politico.getNumber("gastos");
        }
        politico.pinInBackground();
        viewHolder.mTextView.setText(politico.get("nome").toString());
        viewHolder.txtPartido.setText(String.format("%s-%s",politico.get("siglaPartido").toString(),politico.getString("uf")));
        if(politico.getNumber("faltas")!=null)
            viewHolder.txtFaltas.setText(String.format("Faltas: %f",politico.getNumber("faltas").floatValue()));
        else
            viewHolder.txtFaltas.setVisibility(View.GONE);
        if(politico.getString("twitter")!= null)
            if(politico.getString("twitter").length() > 0)
                viewHolder.txtTwitter.setText(politico.getString("twitter"));
        else
            viewHolder.txtTwitter.setVisibility(View.GONE);

        if(politico.getNumber("gastos")==null)
            viewHolder.txtGastos.setText("Gastos: não disponível");
        else
            viewHolder.txtGastos.setText("Gastos: R$ "+mFormat.format(gasto) );
        viewHolder.rb.setRating((float)politico.getDouble("mediaAvaliacao"));

        Imagens.getFotoPolitico(politico,viewHolder.foto);
        Imagens.getImagemPartido(politico.get("siglaPartido").toString(),viewHolder.imgPartido);

    }

    @Override
    public int getItemCount() {
        return politicos.size();
    }

    public void setItems(List<ParseObject> politicos) {
        this.politicos = politicos;
        notifyDataSetChanged();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        // each data item is just a string in this case
        public TextView mTextView;
        public TextView txtPartido;
        public TextView txtGastos;
        public TextView txtTwitter;
        public TextView txtFaltas;
        public ImageView foto;
        public ImageView imgPartido;
        public RatingBar rb;
        public ViewHolder(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.txtNome);
            txtTwitter = (TextView) v.findViewById(R.id.txtTwitter);
            txtFaltas = (TextView) v.findViewById(R.id.txtFaltas);
            txtPartido = (TextView) v.findViewById(R.id.txtPartido);
            txtGastos = (TextView) v.findViewById(R.id.txtGastos);
            foto  = (ImageView)v.findViewById(R.id.foto);
            imgPartido  = (ImageView)v.findViewById(R.id.imgPartido);
            rb = (RatingBar)v.findViewById(R.id.ratingBar);

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
