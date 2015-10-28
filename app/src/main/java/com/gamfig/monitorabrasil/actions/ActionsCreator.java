package com.gamfig.monitorabrasil.actions;

import com.gamfig.monitorabrasil.dispatcher.Dispatcher;
import com.gamfig.monitorabrasil.model.Pergunta;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

/**
 * Created by 89741803168 on 13/08/2015.
 */
public class ActionsCreator {
    private static ActionsCreator instance;
    final Dispatcher dispatcher;

    ActionsCreator(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public static ActionsCreator get(Dispatcher dispatcher) {
        if (instance == null) {
            instance = new ActionsCreator(dispatcher);
        }
        return instance;
    }

    /*
    #   ACTIONS DE DIALOGA
    **/

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
            public void done(List<ParseObject> list, com.parse.ParseException e) {
                if (e == null) {
                    dispatcher.dispatch(
                            DialogaActions.DIALOGA_GET_PERGUNTA_ALETORIA,
                            DialogaActions.KEY_TEXT, list
                    );
                } else {
                    dispatcher.dispatch(
                            DialogaActions.DIALOGA_GET_PERGUNTA_ALETORIA,
                            DialogaActions.KEY_TEXT, "erro"
                    );
                }
            }


        });
    }

    /**
     * Busca a cidade selecionada
     * @return
     */
    public ParseObject buscaCidade(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Config");
        query.fromLocalDatastore();
        try {
            ParseObject config = query.getFirst();
            ParseObject cidade = (ParseObject) config.get("cidade");
            cidade.fetchFromLocalDatastore();
            return cidade;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Salva a cidade selecionada
     * @param cidade
     */
    public void salvaCidade(ParseObject cidade){

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Config");
        query.fromLocalDatastore();
        try {
            ParseObject config = query.getFirst();
            config.put("cidade", cidade);
            //config.save();
            config.pin();
        } catch (ParseException e) {
            try {
                ParseObject config = new ParseObject("Config");
                config.put("cidade", cidade);
                cidade.pin();
                config.pin();
            } catch (ParseException e1) {
                e1.printStackTrace();
            }

        }
    }

    /**
     * Envia uma nova resposta
     * @param resposta
     * @param pergunta
     */
    public void enviarResposta(String resposta, final ParseObject pergunta) {
        ParseObject respostaObject = new ParseObject("Resposta");
        respostaObject.put("user",ParseUser.getCurrentUser());
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
                                dispatcher.dispatch(
                                        DialogaActions.DIALOGA_ENVIAR_RESPOSTA,
                                        DialogaActions.KEY_TEXT, "sucesso"
                                );
                            } else {
                                dispatcher.dispatch(
                                        DialogaActions.DIALOGA_ENVIAR_RESPOSTA,
                                        DialogaActions.KEY_TEXT, "erro"
                                );
                            }

                        }
                    });

                } else {
                    dispatcher.dispatch(
                            DialogaActions.DIALOGA_ENVIAR_RESPOSTA,
                            DialogaActions.KEY_TEXT, "erro"
                    );
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
                    dispatcher.dispatch(
                            DialogaActions.DIALOGA_GET_RESULTADO,
                            DialogaActions.KEY_TEXT, list
                    );
                } else {
                    dispatcher.dispatch(
                            DialogaActions.DIALOGA_GET_RESULTADO,
                            DialogaActions.KEY_TEXT, "erro"
                    );
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
                    dispatcher.dispatch(
                            DialogaActions.DIALOGA_GET_TEMAS,
                            DialogaActions.KEY_TEXT, list
                    );
                } else {
                    dispatcher.dispatch(
                            DialogaActions.DIALOGA_GET_TEMAS,
                            DialogaActions.KEY_TEXT, "erro"
                    );
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
                    query.whereEqualTo("cidade", buscaCidade());
                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> perguntas, ParseException e) {
                            dispatcher.dispatch(
                                    DialogaActions.DIALOGA_GET_PERGUNTAS,
                                    DialogaActions.KEY_TEXT, perguntas
                            );
                        }
                    });

                } else {
                    dispatcher.dispatch(
                            DialogaActions.DIALOGA_GET_PERGUNTAS,
                            DialogaActions.KEY_TEXT, "erro"
                    );
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
                    dispatcher.dispatch(
                            DialogaActions.DIALOGA_GET_PERGUNTA_RESPOSTAS,
                            DialogaActions.KEY_TEXT, "erro"
                    );
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
                    dispatcher.dispatch(
                            DialogaActions.DIALOGA_GET_PERGUNTA_RESPOSTAS,
                            DialogaActions.KEY_TEXT, perguntaResposta
                    );
                } else {
                    dispatcher.dispatch(
                            DialogaActions.DIALOGA_GET_PERGUNTA_RESPOSTAS,
                            DialogaActions.KEY_TEXT, "erro"
                    );
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
                                dispatcher.dispatch(
                                        DialogaActions.DIALOGA_CONCORDO,
                                        DialogaActions.KEY_TEXT, "sucesso"
                                );
                            } else {
                                dispatcher.dispatch(
                                        DialogaActions.DIALOGA_CONCORDO,
                                        DialogaActions.KEY_TEXT, "erro"
                                );
                            }
                        }
                    });
                }
            });
        }else{
            voto = new ParseObject("VotoDialoga");
            voto.put("user",ParseUser.getCurrentUser());
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
                                dispatcher.dispatch(
                                        DialogaActions.DIALOGA_CONCORDO,
                                        DialogaActions.KEY_TEXT, "sucesso"
                                );
                            } else {
                                dispatcher.dispatch(
                                        DialogaActions.DIALOGA_CONCORDO,
                                        DialogaActions.KEY_TEXT, "erro"
                                );
                            }
                        }
                    });
                }
            });

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
                                dispatcher.dispatch(
                                        DialogaActions.DIALOGA_CONCORDO,
                                        DialogaActions.KEY_TEXT, "sucesso"
                                );
                            } else {
                                dispatcher.dispatch(
                                        DialogaActions.DIALOGA_CONCORDO,
                                        DialogaActions.KEY_TEXT, "erro"
                                );
                            }
                        }
                    });
                }
            });
        }else {
            voto = new ParseObject("VotoDialoga");
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
                                dispatcher.dispatch(
                                        DialogaActions.DIALOGA_CONCORDO,
                                        DialogaActions.KEY_TEXT, "sucesso"
                                );

                            } else {
                                dispatcher.dispatch(
                                        DialogaActions.DIALOGA_CONCORDO,
                                        DialogaActions.KEY_TEXT, "erro"
                                );
                            }
                        }
                    });
                }
            });
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
        respostaObject.put("cidade", buscaCidade());
        respostaObject.put("tema", buscaTema(tema));
        respostaObject.put("qtd_resposta", 0);
        respostaObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    //salvar no grupo para receber push quando inserir uma resposta
                    ParsePush.subscribeInBackground(respostaObject.getObjectId());


                    dispatcher.dispatch(
                            DialogaActions.DIALOGA_ENVIAR_PERGUNTA,
                            DialogaActions.KEY_TEXT, "sucesso"
                    );
                } else {
                    dispatcher.dispatch(
                            DialogaActions.DIALOGA_ENVIAR_PERGUNTA,
                            DialogaActions.KEY_TEXT, "erro"
                    );
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
