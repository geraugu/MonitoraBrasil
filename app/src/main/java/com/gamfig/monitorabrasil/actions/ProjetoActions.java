package com.gamfig.monitorabrasil.actions;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.SearchEvent;
import com.gamfig.monitorabrasil.POJO.ProjetoEvent;
import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.application.AppController;
import com.gamfig.monitorabrasil.model.Projeto;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Geraldo on 13/08/2015.
 */
public class ProjetoActions {
    public static final String PROJETO_GET_TODOS = "projeto_get_todos";
    public static final String PROJETO_GET_PROCURA = "projeto_get_procura";
    public static final String PROJETO_GET_DETALHE = "projeto_get_detalhe";
    public static final String PROJETO_GET_ULTIMO_POLITICO_USER = "projeto_get_ultimo_politico_user";

    private static ProjetoActions instance;

    ProjetoActions() {
    }
    public static ProjetoActions get() {
        if (instance == null) {
            instance = new ProjetoActions();
        }
        return instance;
    }

    public void getInfoProjeto(String id,String casa) {
        HashMap<String, String> params = new HashMap();
        params.put("id", id);
        params.put("casa", casa);
        ParseCloud.callFunctionInBackground("getProjeto", params, new FunctionCallback<String>() {


            @Override
            public void done(String jsonString, ParseException e) {
                if (e == null) {
                    try {
                        if(jsonString != null) {
                            JSONObject json = new JSONObject(jsonString);
                            if(json.getString("ementa").length()>0){
                                Projeto projeto = new Projeto(Integer.parseInt(json.getString("id")));
                                projeto.setNome(json.getString("nome"));
                                projeto.setSituacao(json.getString("situacao"));
                                projeto.setLink(json.getString("link"));
                                projeto.setFormaApreciacao(json.getString("formaApreciacao"));
                                projeto.setRegime(json.getString("regime"));
                                projeto.setUltimoDespacho(json.getString("ultimoDespacho"));
                                projeto.setDtUltimoDespacho(json.getString("dtUltimoDespacho"));
                                projeto.setEmenta(json.getString("ementa"));
                                projeto.setNomeAutor(json.getString("nomeAutor"));
                                projeto.setExplicacao(json.getString("explicacao"));
                                EventBus.getDefault().post(new ProjetoEvent(PROJETO_GET_DETALHE, projeto, null));
                            }
                        }
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }


                }else{
                    ProjetoEvent ce = new ProjetoEvent(PROJETO_GET_DETALHE);
                    ce.setErro(AppController.getInstance().getString(R.string.erro_geral));
                    EventBus.getDefault().post(ce);
                }
            }
        });
    }

    /**
     * Busca os projetos de um politico ou todos se o idPolitico = null
     *  @param politico
     * @param casa
     * @param previousTotal
     * @param projetos
     */
    public void getAllProjetos(ParseObject politico, String casa, int previousTotal, final List<ParseObject> projetos) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Proposicao");
        query.whereEqualTo("tp_casa",casa);
        if(casa.equals("s"))
            query.whereEqualTo("tramitando","Sim");
        query.addDescendingOrder("nr_ano");
        query.addDescendingOrder("tx_nome");
        query.setLimit(15);
        query.setSkip(previousTotal);

        if(politico!= null){
//            ParseObject politico = ParseObject.createWithoutData("Politico",idPolitico);
//            query.whereEqualTo("politico", politico);
            query.whereEqualTo("autor",politico);
        }

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, com.parse.ParseException e) {
                if (e == null) {
                    if(projetos != null) {
                        projetos.addAll(list);
                        EventBus.getDefault().post(new ProjetoEvent(PROJETO_GET_TODOS, projetos, null));
                    } else{
                        EventBus.getDefault().post(new ProjetoEvent(PROJETO_GET_TODOS, list, null));
                    }

                } else {
                    ProjetoEvent ce = new ProjetoEvent(PROJETO_GET_TODOS);
                    ce.setErro(AppController.getInstance().getString(R.string.erro_geral));
                    EventBus.getDefault().post(ce);
                }
            }


        });
    }

    /**
     * Pesquisa de projetos
     *
     * @param chave pesquisa
     * @param casa
     */
    public void buscaProjetoPorPalavra(String chave, String casa) {
        ParseQuery<ParseObject> query1 = ParseQuery.getQuery("Proposicao");
        String[] chaves = chave.toLowerCase().split(" ");
        query1.whereContainsAll("words", Arrays.asList(chaves));
        //       query1.whereEqualTo("casa",casa);

        ParseQuery<ParseObject> query2 = ParseQuery.getQuery("Proposicao");
        query2.whereStartsWith("tx_nome", chave.toUpperCase());
//        query2.whereEqualTo("casa",casa);


        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
        queries.add(query1);
        queries.add(query2);

        ParseQuery<ParseObject> query = ParseQuery.or(queries);
        query.addDescendingOrder("nr_ano");
        //query.addDescendingOrder("tx_nome");

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, com.parse.ParseException e) {
                if (e == null) {
                    EventBus.getDefault().post(new ProjetoEvent(PROJETO_GET_PROCURA, list, null));

                } else {
                    ProjetoEvent ce = new ProjetoEvent(PROJETO_GET_PROCURA);
                    ce.setErro(AppController.getInstance().getString(R.string.erro_geral));
                    EventBus.getDefault().post(ce);
                }
            }


        });
        Answers.getInstance().logSearch(new SearchEvent()
                .putQuery(chave));
    }

    public void getUltimoProjeto() {
        HashMap<String, String> params = new HashMap();
        params.put("user", ParseUser.getCurrentUser().getObjectId());
        ParseCloud.callFunctionInBackground("buscaUltimoProjeto", params, new FunctionCallback<String>() {


            @Override
            public void done(String jsonString, ParseException e) {
                if (e == null) {
                    try {
                        if(jsonString != null) {
                            JSONObject json = new JSONObject(jsonString);
                            if(json.getString("ementa").length()>0){
                                Projeto projeto = new Projeto(Integer.parseInt(json.getString("id")));
                                projeto.setNome(json.getString("nome"));
                                projeto.setEmenta(json.getString("ementa"));
                                projeto.setNomeAutor(json.getString("autor"));
                                projeto.setCasa(json.getString("casa"));
                                projeto.setObjectId(json.getString("object_id"));
                                projeto.setDtApresentacao(json.getString("data"));
                                projeto.setS(json.getInt("s"));
                                projeto.setN(json.getInt("n"));
                                EventBus.getDefault().post(new ProjetoEvent(PROJETO_GET_ULTIMO_POLITICO_USER, projeto, null));
                            }
                        }
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }


                }else{
                    ProjetoEvent ce = new ProjetoEvent(PROJETO_GET_ULTIMO_POLITICO_USER);
                    ce.setErro(AppController.getInstance().getString(R.string.erro_geral));
                    EventBus.getDefault().post(ce);
                }
            }
        });
    }
}
