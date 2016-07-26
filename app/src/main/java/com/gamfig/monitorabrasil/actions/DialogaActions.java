package com.gamfig.monitorabrasil.actions;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.gamfig.monitorabrasil.POJO.DialogaEvent;
import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.application.AppController;
import com.gamfig.monitorabrasil.model.Pergunta;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SendCallback;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Random;

/**
 * Created by Geraldo on 13/08/2015.
 */
public class DialogaActions {
    public static final  String DIALOGA_GET_TEMAS = "dialoga_get_temas";
    public static final String DIALOGA_GET_PERGUNTA_RESPOSTAS = "dialoga_get_pergunta_resposta";
    public static final String DIALOGA_GET_PERGUNTAS = "dialoga_get_perguntas";
    public static final String DIALOGA_CONCORDO = "dialoga_concordo";
    public static final String DIALOGA_DISCORDO = "dialoga_discordo";
    public static final String DIALOGA_ENVIAR_RESPOSTA = "dialoga_enviar_resposta";
    public static final String DIALOGA_ENVIAR_PERGUNTA = "dialoga_enviar_pergunta";
    public static final String DIALOGA_GET_RESULTADO = "dialoga_get_resultado";
    public static final String DIALOGA_GET_PERGUNTA_ALETORIA = "dialoga_get_pergunta_aleatoria";



    private static DialogaActions instance;

    DialogaActions() {
    }
    public static DialogaActions get() {
        if (instance == null) {
            instance = new DialogaActions();
        }
        return instance;
    }

    /**
     * Busca uma lista de perguntas para serem sorteadas
     */
    public void getPerguntaAleatoria() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Questao");
        query.addDescendingOrder("createdAt");
        //buscar do Brasil
        //query.whereEqualTo("cidade", buscaCidade());
        query.include("tema");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if(objects.size() > 0)
                        EventBus.getDefault().post(new DialogaEvent(DIALOGA_GET_PERGUNTA_ALETORIA, getRandomList(objects),null));
                } else {
                    DialogaEvent ce = new DialogaEvent(DIALOGA_GET_PERGUNTA_ALETORIA);
                    ce.setErro(AppController.getInstance().getString(R.string.erro_geral));
                    EventBus.getDefault().post(ce);
                }
            }
        });
    }

    private Random random = new Random();
    public ParseObject getRandomList(List<ParseObject> list) {

        //0-4
        int index = random.nextInt(list.size());

        return list.get(index);

    }


    /**
     * Envia uma nova resposta
     * @param resposta
     * @param pergunta
     */
    public void enviarResposta(String resposta, final ParseObject pergunta) {
        ParseObject respostaObject = new ParseObject("Resposta");
        respostaObject.put("user", ParseUser.getCurrentUser());
        respostaObject.put("texto", resposta);
        respostaObject.put("questao", pergunta);
        respostaObject.put("qtd_sim", 0);
        respostaObject.put("qtd_nao", 0);
        respostaObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    pergunta.increment("qtd_resposta");
                    pergunta.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                //enviar push
                                ParsePush push = new ParsePush();
                                push.setChannel( "p_"+pergunta.getObjectId());
                                JSONObject data = new JSONObject();
                                JSONObject json = new JSONObject();
                                try {
                                    ParseObject tema = (ParseObject) pergunta.get("tema");
                                    data.put("is_background", false);
                                    json.put("idTema", tema.getObjectId());
                                    json.put("tipo", "dialoga");
                                    json.put("pergunta", pergunta.getObjectId());
                                    json.put("alerta", "Nova resposta para a pergunta: " + pergunta.getString("texto"));
                                    json.put("titulo", AppController.getInstance().getString(R.string.title_activity_dialoga ));
                                    data.put("data", json);

                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }
                                push.setData(data);
                                push.sendInBackground(new SendCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e != null) {
                                            Answers.getInstance().logCustom(new CustomEvent("envio_push")
                                                    .putCustomAttribute("pergunta", pergunta.getObjectId()));
                                        }

                                    }
                                });
                                EventBus.getDefault().post(new DialogaEvent(DIALOGA_ENVIAR_RESPOSTA,null));

                            } else {
                                DialogaEvent ce = new DialogaEvent(DIALOGA_ENVIAR_RESPOSTA);
                                ce.setErro(AppController.getInstance().getString(R.string.erro_geral));
                                EventBus.getDefault().post(ce);
                            }

                        }
                    });

                } else {
                    DialogaEvent ce = new DialogaEvent(DIALOGA_ENVIAR_RESPOSTA);
                    ce.setErro(AppController.getInstance().getString(R.string.erro_geral));
                    EventBus.getDefault().post(ce);
                }
            }
        });
    }

    /**
     * Busca o resultado das opnioes votadas
     * @param pergunta
     */
    public void getResultado(ParseObject pergunta){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Resposta");
        query.addDescendingOrder("qtd_sim");
        query.whereEqualTo("questao", pergunta);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, com.parse.ParseException e) {
                if (e == null) {
                    EventBus.getDefault().post(new DialogaEvent(DIALOGA_GET_RESULTADO,list,null));

                } else {
                    DialogaEvent ce = new DialogaEvent(DIALOGA_GET_RESULTADO);
                    ce.setErro(AppController.getInstance().getString(R.string.erro_geral));
                    EventBus.getDefault().post(ce);
                }
            }


        });
    }

    /**
     * Busca todos os temas
     */
    public void getAllTemas(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Tema");
        query.addAscendingOrder("Nome");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, com.parse.ParseException e) {
                if (e == null) {
                    EventBus.getDefault().post(new DialogaEvent(DIALOGA_GET_TEMAS,list,null));

                } else {
                    DialogaEvent ce = new DialogaEvent(DIALOGA_GET_TEMAS);
                    ce.setErro(AppController.getInstance().getString(R.string.erro_geral));
                    EventBus.getDefault().post(ce);
                }
            }


        });
    }

    /**
     * Buasca as perguntas do tema selecionado
     * @param idTema
     */
    public void getPerguntas(String idTema) {
        ParseObject tema = ParseObject.createWithoutData("Tema", idTema);
        tema.fetchFromLocalDatastoreInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    //busca primeiro a pergunta
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Questao");
                    query.addDescendingOrder("createdAt");
                    query.whereEqualTo("tema", object);
                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> perguntas, ParseException e) {
                            EventBus.getDefault().post(new DialogaEvent(DIALOGA_GET_PERGUNTAS,perguntas,null));

                        }
                    });

                } else {
                    DialogaEvent ce = new DialogaEvent(DIALOGA_GET_PERGUNTAS);
                    ce.setErro(AppController.getInstance().getString(R.string.erro_geral));
                    EventBus.getDefault().post(ce);
                }
            }
        });
    }

    /**
     * Busca pergunta e suas respostas
     * @param idPergunta
     */
    public void getPerguntaRespostas(String idPergunta) {
        ParseObject tema = ParseObject.createWithoutData("Questao", idPergunta);
        tema.fetchInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    getRespostas(object);
                } else {
                    DialogaEvent ce = new DialogaEvent(DIALOGA_GET_PERGUNTA_RESPOSTAS);
                    ce.setErro(AppController.getInstance().getString(R.string.erro_geral));
                    EventBus.getDefault().post(ce);
                }
            }
        });

    }

    /**
     * Busca as respostas de uma pergunta. Chamada do metodo getPerguntaRespostas
     * @param pergunta
     */
    private void getRespostas(final ParseObject pergunta) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Resposta");
        query.addAscendingOrder("createdAt");
        query.whereEqualTo("questao", pergunta);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, com.parse.ParseException e) {
                if (e == null) {
                    Pergunta perguntaResposta = new Pergunta(pergunta, list);
                    EventBus.getDefault().post(new DialogaEvent(DIALOGA_GET_PERGUNTA_RESPOSTAS,perguntaResposta,null));

                } else {
                    DialogaEvent ce = new DialogaEvent(DIALOGA_GET_PERGUNTA_RESPOSTAS);
                    ce.setErro(AppController.getInstance().getString(R.string.erro_geral));
                    EventBus.getDefault().post(ce);
                }
            }


        });
    }

    /**
     * Insere o voto sim para a resposta
     * @param resposta
     * @param voto
     */
    public void concordo(final ParseObject resposta, ParseObject voto) {
        if(null != voto){
            if(voto.getString("sim_nao").equals("n")){
                resposta.increment("qtd_nao",-1);
            }
            voto.put("sim_nao", "s");
            voto.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    resposta.increment("qtd_sim");
                    resposta.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                EventBus.getDefault().post(new DialogaEvent(DIALOGA_CONCORDO));

                            } else {
                                DialogaEvent ce = new DialogaEvent(DIALOGA_CONCORDO);
                                ce.setErro(AppController.getInstance().getString(R.string.erro_geral));
                                EventBus.getDefault().post(ce);
                            }
                        }
                    });
                }
            });
        }else{
            voto = new ParseObject("VotoDialoga");
            if (ParseUser.getCurrentUser() != null && resposta != null) {
                voto.put("user", ParseUser.getCurrentUser());
                voto.put("resposta", resposta);
                voto.put("sim_nao", "s");
                voto.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        //atualizar o contador da resposta
                        resposta.increment("qtd_sim");
                        resposta.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    EventBus.getDefault().post(new DialogaEvent(DIALOGA_CONCORDO));

                                } else {
                                    DialogaEvent ce = new DialogaEvent(DIALOGA_CONCORDO);
                                    ce.setErro(AppController.getInstance().getString(R.string.erro_geral));
                                    EventBus.getDefault().post(ce);
                                }
                            }
                        });
                    }
                });
            }
        }

        voto.pinInBackground();


    }

    /**
     * Insere o voto nao para a resposta
     * @param resposta
     * @param voto
     */
    public void discordo(final ParseObject resposta, ParseObject voto) {
        if(null != voto){
            if(voto.getString("sim_nao").equals("s")){
                resposta.increment("qtd_sim",-1);
            }
            voto.put("sim_nao", "n");
            voto.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    resposta.increment("qtd_nao");
                    resposta.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                EventBus.getDefault().post(new DialogaEvent(DIALOGA_CONCORDO));

                            } else {
                                DialogaEvent ce = new DialogaEvent(DIALOGA_CONCORDO);
                                ce.setErro(AppController.getInstance().getString(R.string.erro_geral));
                                EventBus.getDefault().post(ce);
                            }
                        }
                    });
                }
            });
        }else {
            voto = new ParseObject("VotoDialoga");
            if (ParseUser.getCurrentUser() != null && resposta != null) {
                voto.put("user", ParseUser.getCurrentUser());
                voto.put("resposta", resposta);
                voto.put("sim_nao", "n");
                voto.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        //atualizar o contador da resposta
                        resposta.increment("qtd_nao");
                        resposta.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    EventBus.getDefault().post(new DialogaEvent(DIALOGA_CONCORDO));

                                } else {
                                    DialogaEvent ce = new DialogaEvent(DIALOGA_CONCORDO);
                                    ce.setErro(AppController.getInstance().getString(R.string.erro_geral));
                                    EventBus.getDefault().post(ce);
                                }
                            }
                        });
                    }
                });
            }
        }
        voto.pinInBackground();

    }

    /**
     * Busca o voto para a resposta
     * @param respostaAtual
     * @return
     */
    public ParseObject getVoto(ParseObject respostaAtual) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("VotoDialoga");
        query.fromLocalDatastore();
        query.whereEqualTo("resposta", respostaAtual);
        ParseObject voto;
        try {
            voto = query.getFirst();

        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        return voto;
    }

    /**
     * Envia a pergunta
     * @param pergunta
     * @param tema
     */
    public void enviarPergunta(String pergunta, String tema) {
        final ParseObject respostaObject = new ParseObject("Questao");
        respostaObject.put("user", ParseUser.getCurrentUser());
        respostaObject.put("texto", pergunta);
        respostaObject.put("tema", buscaTema(tema));
        respostaObject.put("qtd_resposta", 0);
        respostaObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    //salvar no grupo para receber push quando inserir uma resposta
                    ParsePush.subscribeInBackground(respostaObject.getObjectId());

                    EventBus.getDefault().post(new DialogaEvent(DIALOGA_ENVIAR_PERGUNTA));

                } else {
                    DialogaEvent ce = new DialogaEvent(DIALOGA_ENVIAR_PERGUNTA);
                    ce.setErro(AppController.getInstance().getString(R.string.erro_geral));
                    EventBus.getDefault().post(ce);
                }
            }
        });

    }

    /**
     * Busca o objeto tema a partir do nome
     * @param tema
     * @return
     */
    private ParseObject buscaTema(String tema) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Tema");
        try {
            return query.get(tema);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
