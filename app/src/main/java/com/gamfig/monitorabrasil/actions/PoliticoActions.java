package com.gamfig.monitorabrasil.actions;

import com.gamfig.monitorabrasil.POJO.PoliticoEvent;
import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.application.AppController;
import com.gamfig.monitorabrasil.model.Comparacao;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Geraldo on 13/08/2015.
 */
public class PoliticoActions {
    public static final String POLITICO_AVALIAR = "politico_avaliar";
    public static final String POLITICO_COMENTAR = "politico_comentar";
    public static final String POLITICO_GET_INFOS = "politico_get_infos";
    public static final String POLITICO_GET_GAST0S = "politico_get_gastos";
    public static final String POLITICO_GET_PROJETOS = "politico_get_projetos";
    public static final String POLITICO_GET_ALL = "politico_get_all";
    public static final String POLITICO_GET_COMPARACAO_GASTO = "politico_get_comparacao_gasto";
    public static final String POLITICO_GET_PRESENCA = "politico_get_presenca";
    public static final String POLITICO_GET_FILTRO = "politico_get_filtro";

    private static PoliticoActions instance;

    PoliticoActions() {
    }
    public static PoliticoActions get() {
        if (instance == null) {
            instance = new PoliticoActions();
        }
        return instance;
    }




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

    /**
     * Busca os partidos
     * @return lista de partidos na nuvem
     */
    public List<String> getPartidos(boolean nuvem) {

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Partido");
        if(!nuvem)
            query.fromLocalDatastore();
        query.addAscendingOrder("nome");
        List<ParseObject> partidos = null;
        try {
            partidos = query.find();
            if(nuvem)
                ParseObject.pinAllInBackground(partidos);
            List<String> retorno = new ArrayList<>();
            Iterator<ParseObject> it = partidos.iterator();
            while (it.hasNext()){
                ParseObject partido = it.next();
                retorno.add(partido.getString("nome"));
            }
            return retorno;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Busca os categorias de cotas
     * @return lista de categorias na nuvem
     */
    public List<String> getCategoriasCotas(String casa, boolean nuvem) {

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Subcota");
        if(casa != null)
            query.whereEqualTo("casa",casa);
        if(!nuvem)
            query.fromLocalDatastore();
        query.addAscendingOrder("txt_descricao");
        List<ParseObject> categorias = null;
        try {
            categorias = query.find();
            if(nuvem)
                ParseObject.pinAllInBackground(categorias);
            List<String> retorno = new ArrayList<>();
            Iterator<ParseObject> it = categorias.iterator();
            while (it.hasNext()){
                ParseObject cat = it.next();
                retorno.add(cat.getString("txt_descricao"));
            }
            return retorno;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Busca os politicos de uma casa especifica
     * @param casa camara ou senado
     */
    public void getAllPoliticos(String casa, String ordem) {

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Politico");
        query.fromLocalDatastore();
        query.whereEqualTo("tipo",casa);
        if(ordem.equals("nome"))
            query.addAscendingOrder("nome");
        else{
            query.addDescendingOrder(ordem);
        }
        ActionsCreator actionsCreator = ActionsCreator.get();
        //verifica filtros
        if(null != actionsCreator.getValorSharedPreferences("ufSelecionada")){
            query.whereEqualTo("uf",actionsCreator.getValorSharedPreferences("ufSelecionada"));
        }
        if(null != actionsCreator.getValorSharedPreferences("partidoSelecionada")){
            query.whereEqualTo("siglaPartido",actionsCreator.getValorSharedPreferences("partidoSelecionada"));
        }
        if(null != actionsCreator.getValorSharedPreferences("anoSelecionada")){
            //somente para cotas
            ParseQuery<ParseObject> innerQuery = query;
            query = ParseQuery.getQuery("CotaPorAno");
            query.whereEqualTo("ano",Integer.valueOf(actionsCreator.getValorSharedPreferences("anoSelecionada")));
            query.whereMatchesQuery("politico", innerQuery);
            query.include("politico");
            query.addDescendingOrder("total");
        }
        if(null != actionsCreator.getValorSharedPreferences("categoriaSelecionada")){
            //somente para cotas
            ParseQuery<ParseObject> innerQuery = query;
            query = ParseQuery.getQuery("CotaXCategoria");
            if(null != actionsCreator.getValorSharedPreferences("anoSelecionada")){
                query.whereEqualTo("ano",Integer.valueOf(actionsCreator.getValorSharedPreferences("anoSelecionada")));
            }
            query.whereEqualTo("ano",2015);
            query.whereEqualTo("tpCota",actionsCreator.getValorSharedPreferences("categoriaSelecionada"));
            query.whereMatchesQuery("politico", innerQuery);
            query.include("politico");
            query.addDescendingOrder("total");
        }


        query.setLimit(1000);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, com.parse.ParseException e) {
                ParseObject.pinAllInBackground(list);
                if (e == null) {
                    EventBus.getDefault().post(new PoliticoEvent(POLITICO_GET_ALL, list, null));

                } else {
                    PoliticoEvent ce = new PoliticoEvent(POLITICO_GET_ALL);
                    ce.setErro(AppController.getInstance().getString(R.string.erro_geral));
                    EventBus.getDefault().post(ce);
                }
            }
        });
    }

    public void getPresenca(ParseObject politico) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Presenca");
        query.whereEqualTo("politico",politico);
        query.addDescendingOrder("nr_ano");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, com.parse.ParseException e) {
                if (e == null) {
                    EventBus.getDefault().post(new PoliticoEvent(POLITICO_GET_PRESENCA, list, null));

                } else {
                    PoliticoEvent ce = new PoliticoEvent(POLITICO_GET_PRESENCA);
                    ce.setErro(AppController.getInstance().getString(R.string.erro_geral));
                    EventBus.getDefault().post(ce);
                }
            }
        });
    }


    /**
     * Busca os gastos de um politico
     * @param politico cadastro do politico
     */
    public void getGastos(ParseObject politico) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("CotaXCategoria");
        query.whereEqualTo("politico",politico);
        query.addDescendingOrder("total");
        query.setLimit(1000);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, com.parse.ParseException e) {
                if (e == null) {
                    EventBus.getDefault().post(new PoliticoEvent(POLITICO_GET_GAST0S, list, null));
                } else {
                    PoliticoEvent ce = new PoliticoEvent(POLITICO_GET_GAST0S);
                    ce.setErro(AppController.getInstance().getString(R.string.erro_geral));
                    EventBus.getDefault().post(ce);
                }
            }
        });
    }


    /**
     * metodo para buscar politico do local DS
     * @param objectId objectId do politico
     * @return o objeto de politico
     */
    public ParseObject getPolitico(String objectId){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Politico");
        query.fromLocalDatastore();
        try {
            ParseObject politico = query.get(objectId);
            if(null == politico){
                getPoliticoCloud(objectId);
            }else{
                return politico;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * metodo para buscar politico na nuvem
     * @param id objectId do politico
     * @return o objeto de politico
     */
    public ParseObject getPoliticoCloud(String id){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Politico");
        try {
            ParseObject politico = query.get(id);
            return politico;

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }






    public void getComparacaoGasto(String objectId) {
        HashMap<String, String> params = new HashMap();
        if(objectId != null)
            params.put("politico", objectId);
        ParseCloud.callFunctionInBackground("getComparacaoGasto", params, new FunctionCallback<String>() {


            @Override
            public void done(String jsonString, ParseException e) {
                if (e == null) {
                    try {
                        if(jsonString != null) {
                            JSONObject json = new JSONObject(jsonString);
                            Comparacao comparacao = new Comparacao();
                            comparacao.setProduto(json.getString("produto"));
                            comparacao.setValor((float) json.getDouble("conta"));
                            comparacao.setCota(buscaCota(json.getString("id")));

                            EventBus.getDefault().post(new PoliticoEvent(POLITICO_GET_COMPARACAO_GASTO, comparacao,null));
                        }
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }


                }else{
                    PoliticoEvent ce = new PoliticoEvent(POLITICO_GET_COMPARACAO_GASTO);
                    ce.setErro(AppController.getInstance().getString(R.string.erro_geral));
                    EventBus.getDefault().post(ce);
                }
            }
        });
    }

    private ParseObject buscaCota(String id) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("CotaXCategoria");
        try {
            ParseObject cota = query.get(id);
            return cota;

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
