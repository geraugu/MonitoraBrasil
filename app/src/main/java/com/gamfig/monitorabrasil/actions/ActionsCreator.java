package com.gamfig.monitorabrasil.actions;

import com.gamfig.monitorabrasil.dispatcher.Dispatcher;
import com.gamfig.monitorabrasil.model.Comparacao;
import com.gamfig.monitorabrasil.model.Pergunta;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.LogOutCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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


    /**
     * Actions do usuario - logout
     */
    public void logout(){
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(com.parse.ParseException e) {
                dispatcher.dispatch(
                        UserActions.USER_LOGOUT,
                        UserActions.KEY_TEXT, "sucesso"
                );
            }
        });
    }

    /**
     * Actions do usuario - cadastrar
     * @param nome nome
     * @param password senha
     * @param email email
     * @param mParseFile foto
     */
    public void cadastrar(final String nome, final String password, final String email, final ParseFile mParseFile) {

        mParseFile.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                // If successful add file to user and signUpInBackground
                if (null == e) {
                    ParseUser user = new ParseUser();
                    user.setUsername(email);
                    user.setPassword(password);
                    user.setEmail(email);
                    user.put("nome", nome);
                    user.put("foto", mParseFile);
                    user.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(com.parse.ParseException e) {
                            if (e == null) {
                                dispatcher.dispatch(
                                        UserActions.USER_CADASTRO,
                                        UserActions.KEY_TEXT, "sucesso"
                                );
                            } else {
                                dispatcher.dispatch(
                                        UserActions.USER_CADASTRO,
                                        UserActions.KEY_TEXT, "erro"
                                );
                            }
                        }


                    });

                }

            }
        });





    }

    public void logar(String inputUsuario, String inputSenha){
        ParseUser.logInInBackground(inputUsuario, inputSenha, new LogInCallback() {

            @Override
            public void done(ParseUser parseUser, com.parse.ParseException e) {
                if (parseUser != null) {
                    dispatcher.dispatch(
                            UserActions.USER_LOGAR,
                            UserActions.KEY_TEXT, "sucesso"
                    );
                } else {
                    dispatcher.dispatch(
                            UserActions.USER_LOGAR,
                            UserActions.KEY_TEXT, "erro"
                    );
                }
            }


        });

    }

    /*
    #   ACTIONS DE COMENTARIO
    **/


    public void getAllComentarios(String tipo, String idObject){
        ParseQuery<ParseObject> query = ParseQuery.getQuery(tipo);
        if(tipo.equals("ComentarioProjeto")){
            ParseObject projeto = ParseObject.createWithoutData("Proposicao", idObject);
            query.whereEqualTo("proposicao", projeto);
        }else{
            ParseObject politico = ParseObject.createWithoutData("Politico", idObject);
            query.whereEqualTo("politico", politico);
        }
        query.include("user");
        query.include("usuario");
        query.addDescendingOrder("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, com.parse.ParseException e) {
                if (e == null) {
                    dispatcher.dispatch(
                            ComentarioActions.COMENTARIO_GET_ALL,
                            ComentarioActions.KEY_TEXT, list);
                } else {
                    dispatcher.dispatch(
                            ComentarioActions.COMENTARIO_GET_ALL,
                            ComentarioActions.KEY_TEXT, "erro");
                }
            }
        });
    }

    public void enviarMensagem (String mensagem, String tipo, String idObject){
        ParseUser user = ParseUser.getCurrentUser();
        if(user!= null){
            ParseObject comentario =new ParseObject(tipo);
            ParseObject object;
            if(!tipo.equals("ComentarioProjeto")){
                //busca politico
                object = ParseObject.createWithoutData("Politico", idObject);

                comentario.put("politico",object);
            }else{
                object = ParseObject.createWithoutData("Proposicao", idObject);
                comentario.put("proposicao",object);
            }
            comentario.put("tx_comentario",mensagem);
            comentario.put("user", user);
            comentario.put("nome", user.getString("nome"));

            comentario.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    dispatcher.dispatch(
                            ComentarioActions.COMENTARIO_ENVIAR,
                            ComentarioActions.KEY_TEXT, "sucesso"
                    );
                }
            });

            //incrementa o numero de cometarios
            object.fetchInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    object.increment("nr_comentarios");
                    object.saveInBackground();
                }
            });


        }else{
            dispatcher.dispatch(
                    ComentarioActions.COMENTARIO_ENVIAR,
                    ComentarioActions.KEY_TEXT, "erro"
            );
            return;
        }
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

    /**
     * Busca os politicos de uma casa especifica
     * @param casa camara ou senado
     */
    public void getAllPoliticos(String casa, String ordem) {
        //se tipo
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Politico");
        query.whereEqualTo("tipo",casa);
        if(ordem.equals("nome"))
            query.addAscendingOrder("nome");
        else{
            query.addDescendingOrder(ordem);
        }
        query.setLimit(1000);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, com.parse.ParseException e) {
                try {
                    ParseObject.pinAll(list);
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
                if (e == null) {
                    dispatcher.dispatch(
                            PoliticoActions.POLITICO_GET_ALL,
                            PoliticoActions.KEY_TEXT, list
                    );
                } else {
                    dispatcher.dispatch(
                            PoliticoActions.POLITICO_GET_ALL,
                            PoliticoActions.KEY_TEXT, "erro"
                    );
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
                    dispatcher.dispatch(
                            PoliticoActions.POLITICO_GET_PRESENCA,
                            PoliticoActions.KEY_TEXT, list
                    );
                } else {
                    dispatcher.dispatch(
                            PoliticoActions.POLITICO_GET_PRESENCA,
                            PoliticoActions.KEY_TEXT, "erro"
                    );
                }
            }
        });
    }


    /**
     * Busca os gastos de um politico
     * @param idCadastro cadastro do politico
     */
    public void getGastos(String idCadastro) {
        ParseObject politico = getPolitico(idCadastro);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("CotaPorCategoriaPercentual");
        query.whereEqualTo("politico",politico);
        query.addDescendingOrder("total");
        query.setLimit(1000);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, com.parse.ParseException e) {
                if (e == null) {
                    dispatcher.dispatch(
                            PoliticoActions.POLITICO_GET_GAST0S,
                            PoliticoActions.KEY_TEXT, list
                    );
                } else {
                    dispatcher.dispatch(
                            PoliticoActions.POLITICO_GET_GAST0S,
                            PoliticoActions.KEY_TEXT, "erro"
                    );
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

    /**
     * busca a classe configuracao
     * @return configuracao
     */
    public ParseObject getConfiguracao() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Configuracao");
        query.fromLocalDatastore();
        try {
            return query.getFirst();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void avaliar(ParseObject mPolitico, float rating, ParseObject mAvaliacao) {
        double ultimaAvaliacao=0;
        boolean jaVotou = false;
        //s
        if(mAvaliacao != null) {
            if(mAvaliacao.getObjectId()!= null)
                jaVotou = true;
        }
        if(!jaVotou){
            mAvaliacao = new ParseObject("AvaliacaoPolitico");
            mAvaliacao.put("politico",mPolitico);
            mAvaliacao.put("user", ParseUser.getCurrentUser());
        }else{
            //guardar a ultima valor da avaliacao anterior
            ultimaAvaliacao = mAvaliacao.getDouble("avaliacao");
        }

        mAvaliacao.put("nr_avaliacao", rating);
        mAvaliacao.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {

            }
        });
        mAvaliacao.pinInBackground();
        try {
            mPolitico.fetchFromLocalDatastore();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int nrAvalicao = mPolitico.getInt("qtdAvaliacao");
        double media =  mPolitico.getDouble("mediaAvaliacao");
        double total = nrAvalicao*media;
        if(jaVotou){
            total = total - ultimaAvaliacao;
        }else{
            nrAvalicao++;
            mPolitico.increment("qtdAvaliacao");
        }

        mPolitico.put("mediaAvaliacao", (rating + total) / nrAvalicao);

        mPolitico.saveInBackground();
        mPolitico.pinInBackground();
        dispatcher.dispatch(
                UserActions.USER_AVALIA_POLITICO,
                UserActions.KEY_TEXT, "sucesso"
        );
    }

    public void getAvaliacaoPolitico(ParseObject mPolitico) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("AvaliacaoPolitico");
        query.fromLocalDatastore();
        try {
            query.whereEqualTo("politico", mPolitico);
            query.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject avaliacao, ParseException e) {
                    if(avaliacao == null){
                        avaliacao = ParseObject.create("Avaliacao");
                    }
                    dispatcher.dispatch(
                            UserActions.USER_GET_AVALIACAO_POLITICO,
                            UserActions.KEY_TEXT, avaliacao
                    );
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                        JSONObject json = new JSONObject(jsonString);
                        Comparacao comparacao = new Comparacao();
                        comparacao.setProduto(json.getString("produto"));
                        comparacao.setValor((float) json.getDouble("conta"));
                        comparacao.setCota(buscaCota(json.getString("id")));
                        dispatcher.dispatch(
                                PoliticoActions.POLITICO_GET_COMPARACAO_GASTO,
                                PoliticoActions.KEY_TEXT,comparacao
                        );
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }


                }else{
                    dispatcher.dispatch(
                            PoliticoActions.POLITICO_GET_COMPARACAO_GASTO,
                            PoliticoActions.KEY_TEXT, "erro"
                    );
                }
            }
        });
    }

    private ParseObject buscaCota(String id) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("CotaPorCategoriaPercentual");
        try {
            ParseObject cota = query.get(id);
            return cota;

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Busca os projetos de um politico ou todos se o idPolitico = null
     *
     * @param idPolitico id
     * @param previousTotal paginacao
     */
    public void getAllProjetos(String idPolitico, String casa, int previousTotal) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Proposicao");
        query.whereEqualTo("tp_casa",casa);
        query.addDescendingOrder("nr_ano");
        query.addDescendingOrder("tx_nome");
        query.setLimit(15);
        query.setSkip(previousTotal);

        if(idPolitico!= null){
//            ParseObject politico = ParseObject.createWithoutData("Politico",idPolitico);
//            query.whereEqualTo("politico", politico);
            query.whereEqualTo("id_autor",idPolitico);
        }

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, com.parse.ParseException e) {
                if (e == null) {
                    dispatcher.dispatch(
                            ProjetoActions.PROJETO_GET_TODOS,
                            ProjetoActions.KEY_TEXT, list
                    );
                } else {
                    dispatcher.dispatch(
                            ProjetoActions.PROJETO_GET_TODOS,
                            ProjetoActions.KEY_TEXT, "erro"
                    );
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
        chave =  "(?i).*"+chave+".*";
        ParseQuery<ParseObject> query1 = ParseQuery.getQuery("Proposicao");
        query1.whereMatches("txt_ementa",chave);
        query1.whereEqualTo("casa",casa);

        ParseQuery<ParseObject> query2 = ParseQuery.getQuery("Proposicao");
        query2.whereMatches("txt_nome", chave);


        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
        queries.add(query1);
        queries.add(query2);

        ParseQuery<ParseObject> query = ParseQuery.or(queries);
        query.addDescendingOrder("nr_ano");
        query.addDescendingOrder("tx_nome");

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, com.parse.ParseException e) {
                if (e == null) {
                    dispatcher.dispatch(
                            ProjetoActions.PROJETO_GET_PROCURA,
                            ProjetoActions.KEY_TEXT, list
                    );
                } else {
                    dispatcher.dispatch(
                            ProjetoActions.PROJETO_GET_PROCURA,
                            ProjetoActions.KEY_TEXT, "erro"
                    );
                }
            }


        });
    }

    /**
     * Busca os partidos
     * @return lista de partidos na nuvem
     */
    public List<String> getPartidos() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Partido");
        query.addAscendingOrder("nome");
        List<ParseObject> partidos = null;
        try {
            partidos = query.find();
            ParseObject.pinAll(partidos);
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
     * Salva o parametro em configuracao
     *
     * @param parametro nome do parametro
     * @param valor valor do parametro
     */
    public void salvaParametroConfiguracao(String parametro, String valor) {
        ParseObject conf = getConfiguracao();
        if(conf == null)
            conf = new ParseObject("Configuracao");
        conf.put(parametro,valor);
        try {
            conf.pin();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    /**
     * Busca o item configuracao
     * @param item nome do item
     * @return string do valor
     */
    public String getItemConfiguracao(String item) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Configuracao");
        query.fromLocalDatastore();
        try {
            return query.getFirst().getString(item);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Salva o monitoramento do usuario e politico
     * @param politico passa o objeto politico
     * @param monitora o valor se esta ou nao monitorando
     * @throws ParseException
     */
    public void salvaUsuarioPolitico(ParseObject politico, boolean monitora) throws ParseException {
        if(monitora){
            ParseObject usuarioPolitico = new ParseObject("UsuarioPolitico");
            usuarioPolitico.put("politico",politico);
            usuarioPolitico.put("user",ParseUser.getCurrentUser());
            usuarioPolitico.saveInBackground();
            try {
                usuarioPolitico.pin();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }else{
            //busca o monitoramento e exclui
            ParseQuery<ParseObject> query = ParseQuery.getQuery("UsuarioPolitico");
            query.whereEqualTo("politico",politico);
            query.whereEqualTo("user",ParseUser.getCurrentUser());
            query.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    if(e == null)
                        object.deleteInBackground();
                    try {
                        object.unpin();
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                }
            });

        }
    }

    /**
     * Verifica se o usuario esta monitorando o politico
     * @param politico objeto politico
     * @return se esta ou nao
     */
    public boolean estaMonitorando(ParseObject politico){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("UsuarioPolitico");
        query.fromLocalDatastore();
        query.whereEqualTo("politico",politico);
        query.whereEqualTo("user",ParseUser.getCurrentUser());
        try {
            if(query.getFirst() != null){
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }
}
