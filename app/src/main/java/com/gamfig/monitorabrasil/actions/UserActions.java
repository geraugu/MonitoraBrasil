package com.gamfig.monitorabrasil.actions;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.answers.LoginEvent;
import com.crashlytics.android.answers.SignUpEvent;
import com.gamfig.monitorabrasil.POJO.UserEvent;
import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.application.AppController;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Geraldo on 13/08/2015.
 */
public class UserActions {
    public static final String USER_LOGAR = "user-logar";
    public static final String USER_LOGOUT = "user-logout";
    public static final String  USER_CADASTRO = "user_cadastro";
    public static final String  USER_GET_AVALIACAO_POLITICO = "user_get_avalicao_politico";
    public static final String  USER_AVALIA_POLITICO = "user_avalia_politico";
    public static final String USER_GET_CIDADES = "user_get_cidades";

    private static UserActions instance;

    UserActions() {
    }
    public static UserActions get() {
        if (instance == null) {
            instance = new UserActions();
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
                Answers.getInstance().logCustom(new CustomEvent("Logout"));

                EventBus.getDefault().post(new UserEvent(USER_LOGOUT,  null));

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
                                Answers.getInstance().logSignUp(new SignUpEvent().putSuccess(true));
                                EventBus.getDefault().post(new UserEvent(USER_CADASTRO,  null));
                            } else {
                                Answers.getInstance().logSignUp(new SignUpEvent().putSuccess(false));
                                Crashlytics.log(0,"cadastro",e.toString());
                                UserEvent ce = new UserEvent(USER_CADASTRO);
                                ce.setErro(AppController.getInstance().getString(R.string.erro_geral));
                                EventBus.getDefault().post(ce);
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
                    Answers.getInstance().logLogin(new LoginEvent().putSuccess(true));
                    EventBus.getDefault().post(new UserEvent(USER_LOGAR,  null));
                } else {
                    Answers.getInstance().logLogin(new LoginEvent().putSuccess(false));
                    Crashlytics.log(0,"login",e.toString());
                    UserEvent ce = new UserEvent(USER_LOGAR);
                    ce.setErro(AppController.getInstance().getString(R.string.erro_geral));
                    EventBus.getDefault().post(ce);
                }
            }


        });

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
        EventBus.getDefault().post(new UserEvent(USER_AVALIA_POLITICO, null));
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

                    EventBus.getDefault().post(new UserEvent(USER_GET_AVALIACAO_POLITICO, avaliacao, null));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                        if(object != null)
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
            if(query.find().size() > 0){
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }
}
