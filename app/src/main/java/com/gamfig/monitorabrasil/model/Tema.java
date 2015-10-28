package com.gamfig.monitorabrasil.model;

import com.gamfig.monitorabrasil.R;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * Created by Geraldo on 03/09/2015.
 */
public class Tema {

    public Tema(){}

    public static int buscaCor(String tema){


        switch (tema){
            case "ic_cultura":
                return R.color.cor3;
            case "ic_saude":
                return R.color.cor1;
            case "ic_seguranca":
                return R.color.cor9;
            case "ic_educacao":
                return R.color.cor4;
            case "ic_infra":
                return R.color.cor5;
            case "ic_politic":
                return R.color.cor8;
            default:
                return R.color.cor9;
        }
    }

    public static int buscaIcone(String imagem) {
        switch (imagem){
            case "ic_cultura":
                return R.mipmap.ic_cultura;
            case "ic_saude":
                return R.mipmap.ic_saude;
            case "ic_seguranca":
                return R.mipmap.ic_seguranca;
            case "ic_educacao":
                return R.mipmap.ic_educacao;
            case "ic_infra":
                return R.mipmap.ic_infra;
            case "ic_politic":
                return R.mipmap.ic_politic;
            default:
                return R.mipmap.ic_infra;
        }


    }
    public static ParseObject getTema(String idString) {
        ParseObject tema = null;
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Tema");
        try {
            query.fromLocalDatastore();
            tema = query.get(idString);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tema;
    }

}

