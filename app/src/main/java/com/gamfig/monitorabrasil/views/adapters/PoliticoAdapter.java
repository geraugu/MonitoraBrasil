package com.gamfig.monitorabrasil.views.adapters;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.actions.ActionsCreator;
import com.gamfig.monitorabrasil.application.AppController;
import com.gamfig.monitorabrasil.interfaces.RecyclerViewOnClickListenerHack;
import com.gamfig.monitorabrasil.model.Imagens;
import com.gamfig.monitorabrasil.util.MyValueFormatter;
import com.gamfig.monitorabrasil.views.ParlamentarListActivity;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by geraugu on 6/7/15.
 */
public class PoliticoAdapter extends RecyclerView.Adapter<PoliticoAdapter.ViewHolder> {

    private static ActionsCreator actionsCreator;
    private List<ParseObject> politicos;
    private RecyclerViewOnClickListenerHack mRecyclerViewOnClickListenerHack;

    private MyValueFormatter mFormat;
    private String tipo;//camara ou senado
    private boolean ranking;
    ParseObject mPolitico;
    ParlamentarListActivity activity;

    public PoliticoAdapter(ActionsCreator actionsCreator, String tipo, boolean ranking, ParlamentarListActivity parlamentarListActivity) {
        politicos = new ArrayList<>();
        PoliticoAdapter.actionsCreator = actionsCreator;
        this.tipo = tipo;
        mFormat = new MyValueFormatter();
        this.ranking=ranking;
        this.activity=parlamentarListActivity;
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
    public void onBindViewHolder(final ViewHolder viewHolder, final int i) {

        ParseObject politico = politicos.get(i);
        Number gasto;
        if(ranking){
            viewHolder.txtRank.setText(String.format("%sº",politico.getString("pos")));
            viewHolder.txtRank.setVisibility(View.VISIBLE);
        }else{
            viewHolder.txtRank.setVisibility(View.GONE);
        }
        if(politico.getParseObject("politico")!= null){
            gasto = politico.getNumber("total");
            politico = politico.getParseObject("politico");

        }else{
            gasto = politico.getNumber("gastos");

        }
        //  politico.pinInBackground();

        viewHolder.mTextView.setText(politico.get("nome").toString());
        viewHolder.txtPartido.setText(String.format("%s-%s",politico.get("siglaPartido").toString(),politico.getString("uf")));
        if(politico.getNumber("faltas")!=null)
            viewHolder.txtFaltas.setText(String.format("Faltas: %d",politico.getNumber("faltas").intValue()));
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
            viewHolder.txtGastos.setText("Gastos: R$ "+mFormat.formata(gasto.floatValue()) );
        viewHolder.rb.setRating((float)politico.getDouble("mediaAvaliacao"));

        Imagens.getFotoPolitico(politico,viewHolder.foto);
        Imagens.getImagemPartido(politico.get("siglaPartido").toString(),viewHolder.imgPartido);

        viewHolder.btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPolitico = politicos.get(i);
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String pos="";
                if(ranking)
                    pos = String.format("%sº lugar: ",mPolitico.getString("pos"));

                String gasto;
                if(mPolitico.getNumber("gastos")==null)
                    gasto = "não disponível";
                else
                    gasto = mFormat.formata(mPolitico.getNumber("gastos").floatValue()) ;

                String nome=mPolitico.getString("nome");
                if(mPolitico.getString("twitter")!= null)
                    if(mPolitico.getString("twitter").length() > 0)
                        nome = mPolitico.getString("twitter");

                String avaliacao="";
                if(mPolitico.getNumber("mediaAvaliacao")!= null){
                    avaliacao =String.format("Avaliação: %s",mFormat.formata(mPolitico.getNumber("mediaAvaliacao").floatValue()));
                }

                String faltas="";
                if(mPolitico.getNumber("faltas")!= null){
                    faltas =String.format("Faltas: %d",mPolitico.getNumber("faltas").intValue());
                }
                String shareBody= String.format("%s%s(%s-%s) Gastos: R$ %s %s %s #monitoraBrasil",
                        pos,nome,mPolitico.getString("siglaPartido"),mPolitico.getString("uf"),
                        gasto,faltas,avaliacao );

                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, AppController.getInstance().getString(R.string.app_name));
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                sharingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(Intent.createChooser(sharingIntent, "Compartilhar via"));
            }
        });


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
        public TextView txtRank;
        public ImageView foto;
        public ImageView imgPartido;
        public ImageButton btnShare;
        public RatingBar rb;
        public ViewHolder(View v) {
            super(v);
            txtRank = (TextView) v.findViewById(R.id.txtRank);
            mTextView = (TextView) v.findViewById(R.id.txtNome);
            txtTwitter = (TextView) v.findViewById(R.id.txtTwitter);
            txtFaltas = (TextView) v.findViewById(R.id.txtFaltas);
            txtPartido = (TextView) v.findViewById(R.id.txtPartido);
            txtGastos = (TextView) v.findViewById(R.id.txtGastos);
            foto  = (ImageView)v.findViewById(R.id.foto);
            imgPartido  = (ImageView)v.findViewById(R.id.imgPartido);
            rb = (RatingBar)v.findViewById(R.id.ratingBar);
            btnShare = (ImageButton)v.findViewById(R.id.btnShare);

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
