package com.gamfig.monitorabrasil.views.adapters;

import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.interfaces.RecyclerViewOnClickListenerHack;
import com.gamfig.monitorabrasil.model.Tema;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by geral_000 on 23/07/2015.
 */
public class TemaAdapter extends RecyclerView.Adapter<TemaAdapter.ViewHolder> {

    private List<ParseObject> mDataset;
    private RecyclerViewOnClickListenerHack mRecyclerViewOnClickListenerHack;
    private FragmentActivity mActivity;


    int[] cores = new int[]{R.color.cor3, R.color.cor4,R.color.cor5,
            R.color.cor8,R.color.cor6 ,R.color.cor9};



    public TemaAdapter() {
        mDataset = new ArrayList<>();
    }

    public void setItems(List<ParseObject> temas) {
        this.mDataset = temas;
        notifyDataSetChanged();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        // create a new view
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_tema, viewGroup, false);
        // set the view's size, margins, paddings and layout parameter
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {

        final ParseObject tema = mDataset.get(i);
        viewHolder.tema = tema;

        viewHolder.txtTema.setText(tema.getString("Nome"));
        int posCor=i;
        if(i > 19)
            posCor = i-20;
        else
            if(i > 9)
                posCor = i-10;

        viewHolder.llItemTema.setBackgroundResource(cores[posCor]);
        viewHolder.icone.setBackgroundResource(Tema.buscaIcone(tema.getString("imagem")));
    }

    public void setRecyclerViewOnClickListenerHack(RecyclerViewOnClickListenerHack r){
        mRecyclerViewOnClickListenerHack = r;
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        // each data item is just a string in this case

        public TextView txtTema;
        public ImageView icone;
        public ParseObject tema;
        public LinearLayout llItemTema;

        public ViewHolder(View v) {
            super(v);
            txtTema = (TextView) v.findViewById(R.id.txtTema);
            icone = (ImageView) v.findViewById(R.id.imgIconeTema);
            llItemTema = (LinearLayout)v.findViewById(R.id.llItemTema);
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
