package com.gamfig.monitorabrasil.views.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.application.AppController;
import com.gamfig.monitorabrasil.model.Comparacao;
import com.gamfig.monitorabrasil.model.Imagens;
import com.gamfig.monitorabrasil.model.Projeto;
import com.gamfig.monitorabrasil.util.MyValueFormatter;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Geraldo on 07/01/2016.
 */
public class Card {

    public Card(){}

    public void montaCardProjeto(final View v, final Projeto projeto) {
        TextView data = (TextView) v.findViewById(R.id.data);
        TextView autor = (TextView) v.findViewById(R.id.autor);
        TextView ementa = (TextView) v.findViewById(R.id.descricao);
        TextView  numero = (TextView) v.findViewById(R.id.txtTitulo);
        //autor
        autor.setText(projeto.getNomeAutor());
        numero.setText(projeto.getNome());
        if(projeto.getDtApresentacao()!= null) {
            data.setText(projeto.getDtApresentacao());
        }
        ementa.setText(projeto.getEmenta());
    }



    public void montaCardComparacaoGasto(View mView, ParseObject politico, Comparacao comparacao){
        TextView nomePolitico = (TextView)mView.findViewById(R.id.gasto_deputado);
        TextView categoria =(TextView)mView.findViewById(R.id.gasto_categoria);
        TextView total =(TextView)mView.findViewById(R.id.gasto_total);
        TextView txtComparacao =(TextView)mView.findViewById(R.id.gasto_comparacao);
        ImageView foto = (ImageView)mView.findViewById(R.id.gasto_img);
        ImageView imgComparacao = (ImageView)mView.findViewById(R.id.gasto_imgcomp);
        String tipo;
        if(politico.getString("tipo").equals("c"))
            tipo = "Dep.";
        else
            tipo = "Sen.";
        nomePolitico.setText(String.format("%s %s",tipo,politico.getString("nome")));
        categoria.setText(comparacao.getCota().getString("tpCota"));

        MyValueFormatter formatter = new MyValueFormatter();
        total.setText("R$ "+formatter.formata(comparacao.getCota().getNumber("total").floatValue()));
        formatter.setMaximoDigitos(2);
        txtComparacao.setText(formatter.formata(comparacao.getValor())+" "+comparacao.getProduto());
        Imagens.getFotoPolitico(politico,foto);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                imgComparacao.setBackground(AppController.getInstance().getDrawable(getImage(comparacao.getProduto())));
            }
        }
    }

    public void montaCardComentario(View v, ParseObject comentario, ParseObject projeto) {

        TextView txtUser = (TextView) v.findViewById(R.id.txtUser);
        TextView txtComentario = (TextView) v.findViewById(R.id.txtComentario);
        TextView txtHorario = (TextView) v.findViewById(R.id.txtHorario);
        TextView txtPolitico =(TextView)v.findViewById(R.id.txtPolitico);
        final ImageView imgUser = (ImageView) v.findViewById(R.id.imgUser);
        txtUser.setText(comentario.getString("nome"));
        txtComentario.setText(comentario.get("tx_comentario").toString());
        txtPolitico.setText(String.format("%s - %s",
                projeto.getString("tx_nome"),
                projeto.getString("nome_autor")));


        Calendar cal = Calendar.getInstance();

        //calcula o horario da mensagem
        Date data = comentario.getCreatedAt();
        cal.setTime(data);
        cal.add(Calendar.HOUR_OF_DAY, 1);
        SimpleDateFormat dt1 = new SimpleDateFormat("dd/MM/yy HH:mm");
        txtHorario.setText(dt1.format(cal.getTime()));

        //busca a imagem do usuario
        ParseUser user = comentario.getParseUser("user");
        try {
            user.fetchIfNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(user != null) {
            ParseFile foto = user.getParseFile("foto");
            if (foto != null)
                foto.getDataInBackground(new GetDataCallback() {
                    public void done(byte[] data, ParseException e) {
                        if (e == null) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                            bitmap = Bitmap.createScaledBitmap(bitmap, 120, 120, true);
                            imgUser.setImageBitmap(bitmap);
                        }
                    }
                });
        }
    }

    public void montaCardComentario(View v,ParseObject comentario){
        TextView txtUser = (TextView) v.findViewById(R.id.txtUser);
        TextView txtComentario = (TextView) v.findViewById(R.id.txtComentario);
        TextView txtHorario = (TextView) v.findViewById(R.id.txtHorario);
        TextView txtPolitico =(TextView)v.findViewById(R.id.txtPolitico);
        final ImageView imgUser = (ImageView) v.findViewById(R.id.imgUser);
        txtUser.setText(comentario.getString("nome"));
        txtComentario.setText(comentario.get("tx_comentario").toString());

        ParseObject politico = comentario.getParseObject("politico");
        try {
            politico.fetchFromLocalDatastore();
            txtPolitico.setText(String.format("%s %s",
                    (politico.getString("tipo").equals("c")?"Dep.":"Sen."),
                    politico.getString("nome")));
        } catch (ParseException e) {
            e.printStackTrace();
        }


        Calendar cal = Calendar.getInstance();

        //calcula o horario da mensagem
        Date data = comentario.getCreatedAt();
        cal.setTime(data);
        cal.add(Calendar.HOUR_OF_DAY, 1);
        SimpleDateFormat dt1 = new SimpleDateFormat("dd/MM/yy HH:mm");
        txtHorario.setText(dt1.format(cal.getTime()));

        //busca a imagem do usuario
        ParseUser user = comentario.getParseUser("user");
        try {
            user.fetchIfNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(user != null) {
            ParseFile foto = user.getParseFile("foto");
            if (foto != null)
                foto.getDataInBackground(new GetDataCallback() {
                    public void done(byte[] data, ParseException e) {
                        if (e == null) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                            bitmap = Bitmap.createScaledBitmap(bitmap, 120, 120, true);
                            imgUser.setImageBitmap(bitmap);
                        }
                    }
                });
        }
    }

    private int getImage(String ref){
        int ret=0;
        switch (ref){
            case "Passagens de ônibus municipal":
                ret=R.mipmap.ic_bus;
                break;
            case "Botijões de Gás":
                ret=R.drawable.ic_fire;
                break;
            case "Salários mínimos":
                ret=R.drawable.ic_money;
                break;
            case "Transplantes de Coração":
                ret=R.drawable.ic_heart;
                break;
            case "Mamografias":
                ret=R.drawable.ic_hospital;
                break;
            case "Cestas Básicas":
                ret=R.drawable.ic_food;
                break;
        }

        return ret;
    }



}
