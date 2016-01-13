package com.gamfig.monitorabrasil.views.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.actions.ActionsCreator;
import com.gamfig.monitorabrasil.interfaces.RecyclerViewOnClickListenerHack;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by geral_000 on 05/07/2015.
 */
public class ComentarioAdapter extends RecyclerView.Adapter<ComentarioAdapter.ViewHolder> {

    private static ActionsCreator actionsCreator;
    private List<ParseObject> mDataset;
    private RecyclerViewOnClickListenerHack mRecyclerViewOnClickListenerHack;

    private SimpleDateFormat dt1 = new SimpleDateFormat("dd/MM/yy HH:mm");
    private Calendar cal = Calendar.getInstance();

    public ComentarioAdapter(ActionsCreator actionsCreator) {
        mDataset = new ArrayList<>();
        ComentarioAdapter.actionsCreator = actionsCreator;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        // create a new view
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_comentario, viewGroup, false);
        // set the view's size, margins, paddings and layout parameter
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }
    public void setRecyclerViewOnClickListenerHack(RecyclerViewOnClickListenerHack r){
        mRecyclerViewOnClickListenerHack = r;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        ParseObject comentario = mDataset.get(i);

        viewHolder.txtUser.setText(comentario.get("nome").toString());
        viewHolder.txtComentario.setText(comentario.get("tx_comentario").toString());

        //calcula o horario da mensagem
        Date data = comentario.getCreatedAt();
        cal.setTime(data);
        cal.add(Calendar.HOUR_OF_DAY, 1);
        viewHolder.txtHorario.setText(dt1.format(cal.getTime()));

        //busca a imagem do usuario
        ParseUser user = comentario.getParseUser("user");
        ParseFile foto = user.getParseFile("foto");
        if(foto != null)
            foto.getDataInBackground(new GetDataCallback() {
                public void done(byte[] data, ParseException e) {
                    if (e == null) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        bitmap = Bitmap.createScaledBitmap(bitmap, 120, 120, true);
                        viewHolder.imgUser.setImageBitmap(bitmap);
                    }
                }
            });
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
        public TextView txtUser;
        public TextView txtComentario;
        public TextView txtHorario;
        public ImageView imgUser;
        public ViewHolder(View v) {
            super(v);
            txtUser = (TextView) v.findViewById(R.id.txtUser);
            txtComentario = (TextView) v.findViewById(R.id.txtComentario);
            txtHorario = (TextView) v.findViewById(R.id.txtHorario);
            imgUser = (ImageView) v.findViewById(R.id.imgUser);
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
