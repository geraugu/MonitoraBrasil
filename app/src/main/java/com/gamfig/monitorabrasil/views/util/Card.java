package com.gamfig.monitorabrasil.views.util;

import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.application.AppController;
import com.gamfig.monitorabrasil.model.Comparacao;
import com.gamfig.monitorabrasil.model.Imagens;
import com.gamfig.monitorabrasil.util.MyValueFormatter;
import com.parse.ParseObject;

/**
 * Created by Geraldo on 07/01/2016.
 */
public class Card {

    public Card(){}

    public void montaCardComparacaoGasto(View mView, ParseObject politico, Comparacao comparacao){
        TextView nomePolitico = (TextView)mView.findViewById(R.id.gasto_deputado);
        TextView categoria =(TextView)mView.findViewById(R.id.gasto_categoria);
        TextView total =(TextView)mView.findViewById(R.id.gasto_total);
        TextView txtComparacao =(TextView)mView.findViewById(R.id.gasto_comparacao);
        ImageView foto = (ImageView)mView.findViewById(R.id.gasto_img);
        ImageView imgComparacao = (ImageView)mView.findViewById(R.id.gasto_imgcomp);

        nomePolitico.setText(politico.getString("nome"));
        categoria.setText(comparacao.getCota().getString("categoria"));

        MyValueFormatter formatter = new MyValueFormatter();
        total.setText("R$ "+formatter.formata(comparacao.getCota().getNumber("total").floatValue()));
        formatter.setMaximoDigitos(0);
        txtComparacao.setText(formatter.formata(comparacao.getValor())+" "+comparacao.getProduto());
        Imagens.getFotoPolitico(politico,foto);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                imgComparacao.setBackground(AppController.getInstance().getDrawable(R.mipmap.ic_bus));
            }
        }
    }
}
