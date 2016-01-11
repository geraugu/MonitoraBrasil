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
import com.gamfig.monitorabrasil.actions.ActionsCreator;
import com.gamfig.monitorabrasil.application.AppController;
import com.gamfig.monitorabrasil.interfaces.RecyclerViewOnClickListenerHack;
import com.gamfig.monitorabrasil.views.ComentarioActivity;
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
public class ProjetoAdapter extends RecyclerView.Adapter<ProjetoAdapter.ViewHolder> {

    private static ActionsCreator actionsCreator;
    private List<ParseObject> projetos;
    private RecyclerViewOnClickListenerHack mRecyclerViewOnClickListenerHack;

    public ProjetoAdapter(ActionsCreator actionsCreator) {
        projetos = new ArrayList<>();
        ProjetoAdapter.actionsCreator = actionsCreator;
    }




    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        // create a new view
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_projeto, viewGroup, false);
        // set the view's size, margins, paddings and layout parameter
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        Drawable apoio = ContextCompat.getDrawable(AppController.getInstance().getApplicationContext(), R.mipmap.ic_action_like_gray);
        Drawable napoio = ContextCompat.getDrawable(AppController.getInstance().getApplicationContext(), R.mipmap.ic_action_unlike_gray);
        final Drawable napoioVermelho = ContextCompat.getDrawable(AppController.getInstance().getApplicationContext(), R.mipmap.ic_action_unlike_red);
        final Drawable apoioVerde = ContextCompat.getDrawable(AppController.getInstance().getApplicationContext(), R.mipmap.ic_action_like_green);

        viewHolder.btnConcordo.setBackground(apoio);
        viewHolder.btnDiscordo.setBackground(napoio);

        viewHolder.btnDiscordo.setEnabled(true);
        viewHolder.btnConcordo.setEnabled(true);
        viewHolder.voto = "sem_voto";
        final ParseObject projeto = projetos.get(i);
        viewHolder.projeto = projeto;
        //verifica se ja votou
        ParseQuery<ParseObject> query = ParseQuery.getQuery("VotoProposicao");
        query.fromLocalDatastore();
        //query.whereEqualTo("id_proposicao", projeto.getNumber("id_proposicao"));
        query.whereEqualTo("projeto", projeto);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (list.size() > 0) {
                    String voto = list.get(0).get("tp_voto").toString();
                    viewHolder.projetoVotado = list.get(0);
                    if (voto.equals("s")) {
                        viewHolder.btnConcordo.setBackground(apoioVerde);
                        viewHolder.btnConcordo.setEnabled(false);
                        viewHolder.voto = "apoioado";

                    } else {
                        viewHolder.btnDiscordo.setBackground(napoioVermelho);
                        viewHolder.btnDiscordo.setEnabled(false);
                        viewHolder.voto = "nao_apoioado";
                    }
                }
            }
        });

        viewHolder.numero.setText(projeto.getString("tx_nome"));
       // viewHolder.autor.setText(projeto.getParseObject(""));

        if(projeto.get("dt_apresentacao")!= null)
            viewHolder.data.setText(projeto.get("dt_apresentacao").toString());

        viewHolder.descricao.setText(projeto.get("txt_ementa").toString());
        int numComentario = 0;
        if(projeto.get("nr_comentarios") != null){
            numComentario = Integer.valueOf( projeto.get("nr_comentarios").toString());
        }
        viewHolder.btnComentar.setText(String.valueOf(numComentario));
        int num=0;
        if(projeto.get("nrVotosS") != null){
            num =  projeto.getNumber("nrVotosS").intValue();
        }

        viewHolder.numApoio.setText(String.valueOf(num));
        num=0;
        if(projeto.get("nrVotosN") != null){
            num = projeto.getNumber("nrVotosN").intValue();
        }
        viewHolder.numNaoApoio.setText(String.valueOf(num));
//        try {
//            if(projeto.getParseObject("politico")!= null) {
//                ParseObject autor = projeto.getParseObject("politico").getString("idCadastro");
//
//                autor.fetchFromLocalDatastore();
//                viewHolder.autor.setText(autor.get("nome").toString());
//            }
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }

        viewHolder.btnConcordo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                registraVoto(v, projeto, "s", viewHolder);

            }
        });

        viewHolder.btnDiscordo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registraVoto(v, projeto, "n", viewHolder);
            }
        });

        viewHolder.btnComentar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent =new Intent(AppController.getInstance().getApplicationContext(), ComentarioActivity.class);
                mIntent.putExtra("projeto",viewHolder.projeto.getString("id_proposicao"));

                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                AppController.getInstance().startActivity(mIntent);



            }
        });
    }

    private void registraVoto(final View v, ParseObject projeto, final String tipoVoto, final ViewHolder viewHolder2) {

        //verificar se esta logado
        if(ParseUser.getCurrentUser() == null){
            Snackbar.make(v, "Para votar é necessário estar logado", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }

        Drawable apoio = ContextCompat.getDrawable(AppController.getInstance().getApplicationContext(), R.mipmap.ic_action_like_gray);
        Drawable napoio = ContextCompat.getDrawable(AppController.getInstance().getApplicationContext(), R.mipmap.ic_action_unlike_gray);
        final Drawable napoioVermelho = ContextCompat.getDrawable(AppController.getInstance().getApplicationContext(), R.mipmap.ic_action_unlike_red);
        final Drawable apoioVerde = ContextCompat.getDrawable(AppController.getInstance().getApplicationContext(), R.mipmap.ic_action_like_green);

        //verificar se ja votou nesse projeto
        boolean votado;
        if(viewHolder2.voto.equals("sem_voto")){

            votado = false;
        }else{
            votado=true;

        }
        if(votado){

            if(null!= viewHolder2.projetoVotado){
                viewHolder2.projetoVotado.unpinInBackground();
                viewHolder2.projetoVotado.deleteInBackground();
            }



        }

        ParseObject voto = new ParseObject("VotoProposicao");
        voto.put("projeto", projeto);
        voto.put("user", ParseUser.getCurrentUser());
        voto.put("tp_voto", tipoVoto);
        Button btnVoto = (Button) v;
        if(tipoVoto.equals("s")){
            if(votado){
                viewHolder2.numNaoApoio.setText(String.valueOf(Integer.valueOf(viewHolder2.numNaoApoio.getText().toString()) - 1));
                projeto.increment("nrVotosN", -1);
            }
            viewHolder2.numApoio.setText(String.valueOf(Integer.valueOf(viewHolder2.numApoio.getText().toString()) + 1));
            projeto.increment("nrVotosS");
            btnVoto.setBackground(apoioVerde);
            viewHolder2.btnDiscordo.setBackground(napoio);
            viewHolder2.voto = "apoiado";
            viewHolder2.btnConcordo.setEnabled(false);
            viewHolder2.btnDiscordo.setEnabled(true);

        }else{
            if(votado){
                viewHolder2.numApoio.setText(String.valueOf(Integer.valueOf(viewHolder2.numApoio.getText().toString()) - 1));
                projeto.increment("nrVotosS", -1);
            }
            projeto.increment("nrVotosN");
            viewHolder2.numNaoApoio.setText(String.valueOf(Integer.valueOf(viewHolder2.numNaoApoio.getText().toString()) + 1));
            btnVoto.setBackground(napoioVermelho);
            viewHolder2.btnConcordo.setBackground(apoio);
            viewHolder2.voto = "nao_apoiado";
            viewHolder2.btnDiscordo.setEnabled(false);
            viewHolder2.btnConcordo.setEnabled(true);

        }
        projeto.saveInBackground();

        try {
            voto.pin();
            voto.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    Snackbar.make(v, "Voto registrado", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }
            });
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void setRecyclerViewOnClickListenerHack(RecyclerViewOnClickListenerHack r){
        mRecyclerViewOnClickListenerHack = r;
    }

    @Override
    public int getItemCount() {
        return projetos.size();
    }

    public void setItems(List<ParseObject> projetos) {
        this.projetos = projetos;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        // each data item is just a string in this case
        public ParseObject projetoVotado;
        public TextView data;
        public TextView autor;
        public TextView descricao;
        public TextView numApoio;
        public TextView numNaoApoio;
        public TextView numero;
        public Button btnConcordo;
        public Button btnDiscordo;
        public Button btnComentar;
        public String voto;
        public ParseObject projeto;

        public ViewHolder(View v) {
            super(v);
            data = (TextView) v.findViewById(R.id.data);
            autor = (TextView) v.findViewById(R.id.autor);
            numApoio = (TextView) v.findViewById(R.id.txtNumApoio);
            numNaoApoio = (TextView) v.findViewById(R.id.txtNumNaoApoio);
            descricao = (TextView) v.findViewById(R.id.descricao);
            btnConcordo = (Button) v.findViewById(R.id.btnConcordo);
            btnDiscordo = (Button) v.findViewById(R.id.btnDiscordo);
            btnComentar = (Button) v.findViewById(R.id.btnComentar);
            numero = (TextView) v.findViewById(R.id.txtTitulo);

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
