package com.gamfig.monitorabrasil.actions;

import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

/**
 * Created by Geraldo on 13/08/2015.
 */
public class PoliticoActions {
    String POLITICO_AVALIAR = "politico_avaliar";
    String POLITICO_COMENTAR = "politico_comentar";
    String POLITICO_GET_INFOS = "politico_get_infos";
    String POLITICO_GET_GAST0S = "politico_get_gastos";
    String POLITICO_GET_PROJETOS = "politico_get_projetos";
    String POLITICO_GET_ALL = "politico_get_all";
    String POLITICO_GET_COMPARACAO_GASTO = "politico_get_comparacao_gasto";
    String POLITICO_GET_PRESENCA = "politico_get_presenca";
    String KEY_TEXT = "key-text";
    String KEY_ID = "key-id";
    String POLITICO_GET_FILTRO = "politico_get_filtro";

    public void getAllPoliticos(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Politico");

        query.setLimit(1000);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, com.parse.ParseException e) {
                ParseObject.pinAllInBackground(list);
            }
        });
        getPartidos(true);
        getCategoriasCotas(null, true);
    }
}
