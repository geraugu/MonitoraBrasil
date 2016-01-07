package com.gamfig.monitorabrasil.views.adapters;

import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.interfaces.RecyclerViewOnClickListenerHack;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by geral_000 on 23/07/2015.
 */
public class PerguntaAdapter extends RecyclerView.Adapter<PerguntaAdapter.ViewHolder> {

    private List<ParseObject> mDataset;
    private RecyclerViewOnClickListenerHack mRecyclerViewOnClickListenerHack;
    private FragmentActivity mActivity;


    int[] cores = new int[]{R.color.cor1, R.color.cor2,R.color.cor3, R.color.cor4,R.color.cor5, R.color.cor6,
            R.color.cor7, R.color.cor8,R.color.cor9, R.color.cor10};



    public PerguntaAdapter() {
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
                .inflate(R.layout.item_pergunta, viewGroup, false);
        // set the view's size, margins, paddings and layout parameter
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {

        final ParseObject pergunta = mDataset.get(i);
        viewHolder.pergunta = pergunta;

        viewHolder.txtPergunta.setText(pergunta.getString("texto"));
        int posCor=i;
        if(i > 19)
            posCor = i-20;
        else
            if(i > 9)
                posCor = i-10;

            ((CardView) viewHolder.itemView).setCardBackgroundColor(cores[posCor]);

      //  viewHolder.llItemTema.setBackgroundResource(cores[posCor]);
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

        public TextView txtPergunta;
        public ParseObject pergunta;
        public LinearLayout llItemTema;

        public ViewHolder(View v) {
            super(v);
            txtPergunta = (TextView) v.findViewById(R.id.note_text);
           // llItemTema = (LinearLayout)v.findViewById(R.id.llItemTema);
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
