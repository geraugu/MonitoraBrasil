package com.gamfig.monitorabrasil.views.adapters;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.application.AppController;
import com.gamfig.monitorabrasil.interfaces.RecyclerViewOnClickListenerHack;
import com.gamfig.monitorabrasil.views.ComentarioActivity;
import com.gamfig.monitorabrasil.views.LoginActivity;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by geral_000 on 25/06/2015.
 */
public class PartidaAdapter extends RecyclerView.Adapter<PartidaAdapter.ViewHolder> {


    private List<ParseObject> partidas;
    private RecyclerViewOnClickListenerHack mRecyclerViewOnClickListenerHack;

    public PartidaAdapter() {
        partidas = new ArrayList<>();
    }




    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        // create a new view
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_partida, viewGroup, false);
        // set the view's size, margins, paddings and layout parameter
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {

        final ParseObject partida = partidas.get(i);
        viewHolder.partida = partida;

        viewHolder.userName.setText(partida.getParseUser("j1").getString("nome"));

        viewHolder.tipo.setText(partida.getString("tipo"));

    }

    public void setRecyclerViewOnClickListenerHack(RecyclerViewOnClickListenerHack r){
        mRecyclerViewOnClickListenerHack = r;
    }

    @Override
    public int getItemCount() {
        return partidas.size();
    }

    public void setItems(List<ParseObject> projetos) {
        this.partidas = projetos;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        // each data item is just a string in this case
        public ParseObject partida;
        public TextView userName;
        public TextView tipo;

        public ViewHolder(View v) {
            super(v);
            userName = (TextView) v.findViewById(R.id.user);
            tipo = (TextView) v.findViewById(R.id.tipo);


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
