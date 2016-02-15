package com.gamfig.monitorabrasil.actions;

import com.gamfig.monitorabrasil.POJO.ComentarioEvent;
import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.application.AppController;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Created by Geraldo on 13/08/2015.
 */
public class ComentarioActions {

    private static ComentarioActions instance;
    public static final String COMENTARIO_GET_ALL = "comentario_get_all";
    public static final String COMENTARIO_ENVIAR = "comentario_enviar";

    public static final String COMENTARIO_POLITICO_GET_ULTIMO = "comentario_politico_get_ultimo";
    public static final String COMENTARIO_PROJETO_GET_ULTIMO = "comentario_projeto_get_ultimo";

    ComentarioActions() {
    }
    public static ComentarioActions get() {
        if (instance == null) {
            instance = new ComentarioActions();
        }
        return instance;
    }




    public void getAllComentarios(String tipo, String idObject){
        ParseQuery<ParseObject> query = ParseQuery.getQuery(tipo);
        if(!tipo.equals("Comentario")) {
            if (tipo.equals("ComentarioProjeto")) {
                ParseObject projeto = ParseObject.createWithoutData("Proposicao", idObject);
                query.whereEqualTo("proposicao", projeto);
            } else {
                ParseObject politico = ParseObject.createWithoutData("Politico", idObject);
                query.whereEqualTo("politico", politico);
            }
        }
        query.include("user");
        query.addDescendingOrder("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, com.parse.ParseException e) {
                if (e == null) {
                    EventBus.getDefault().post(new ComentarioEvent(COMENTARIO_GET_ALL, list,null));

                } else {
                    ComentarioEvent ce = new ComentarioEvent(COMENTARIO_GET_ALL);
                    ce.setErro(AppController.getInstance().getString(R.string.erro_geral));
                    EventBus.getDefault().post(ce);
                }
            }
        });
    }

    public void getUltimoComentarioPolitico(String id){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ComentarioPolitico");
        if(id != null){
            ParseObject politico = ParseObject.createWithoutData("Politico",id);
            query.whereEqualTo("politico",politico);
        }
        query.addDescendingOrder("createdAt");
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    EventBus.getDefault().post(new ComentarioEvent(COMENTARIO_POLITICO_GET_ULTIMO, object, null));
                } else {
                    ComentarioEvent ce = new ComentarioEvent(COMENTARIO_POLITICO_GET_ULTIMO);
                    ce.setErro(AppController.getInstance().getString(R.string.erro_geral));
                    EventBus.getDefault().post(ce);
                }
            }
        });
    }

    public void getUltimoComentarioProjeto(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ComentarioProjeto");
        query.addDescendingOrder("createdAt");
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    EventBus.getDefault().post(new ComentarioEvent(COMENTARIO_PROJETO_GET_ULTIMO, object, null));
                } else {
                    ComentarioEvent ce = new ComentarioEvent(COMENTARIO_PROJETO_GET_ULTIMO);
                    ce.setErro(AppController.getInstance().getString(R.string.erro_geral));
                    EventBus.getDefault().post(ce);
                }
            }
        });
    }

    public void enviarMensagem (String mensagem, String tipo, String idObject){
        ParseUser user = ParseUser.getCurrentUser();
        if(user!= null){
            ParseObject comentario =new ParseObject(tipo);
            ParseObject object;
            if(!tipo.equals("Comentario")) {
                if (!tipo.equals("ComentarioProjeto")) {
                    //busca politico
                    object = ParseObject.createWithoutData("Politico", idObject);

                    comentario.put("politico", object);
                } else {
                    object = ParseObject.createWithoutData("Proposicao", idObject);
                    comentario.put("proposicao", object);
                }
                //incrementa o numero de cometarios
                object.fetchInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject object, ParseException e) {
                        object.increment("nr_comentarios");
                        object.saveInBackground();
                    }
                });
            }
            comentario.put("tx_comentario",mensagem);
            comentario.put("user", user);
            comentario.put("nome", user.getString("nome"));

            comentario.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {

                }
            });
        }else{
            ComentarioEvent ce = new ComentarioEvent(COMENTARIO_ENVIAR);
            ce.setErro(AppController.getInstance().getString(R.string.erro_enviar_comentario));
            EventBus.getDefault().post(ce);
            return;
        }
    }

}
