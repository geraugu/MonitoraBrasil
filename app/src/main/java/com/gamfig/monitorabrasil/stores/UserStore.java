package com.gamfig.monitorabrasil.stores;


import com.gamfig.monitorabrasil.actions.Action;
import com.gamfig.monitorabrasil.actions.UserActions;
import com.gamfig.monitorabrasil.dispatcher.Dispatcher;
import com.parse.ParseObject;
import com.squareup.otto.Subscribe;

import java.util.List;

/**
 * Created by 89741803168 on 13/08/2015.
 */
public class UserStore extends Store{

    private static UserStore instance;
    private String status;
    private String evento;
    private ParseObject mAvaliacao;
    private boolean jaVotou;
    private List<ParseObject> cidades;

    public ParseObject getmAvaliacao(){return mAvaliacao;}

    public boolean isJaVotou(){return  jaVotou;}

    public String getStatus(){
        return status;
    }

    public String getEvento(){
        return evento;
    }


    protected UserStore(Dispatcher dispatcher) {
        super(dispatcher);
    }

    public static UserStore get(Dispatcher dispatcher) {
        if (instance == null) {
            instance = new UserStore(dispatcher);
        }
        return instance;
    }


    @Override
    @Subscribe
    public void onAction(Action action) {
        status = "erro";
        this.evento = action.getType();
        switch (action.getType()) {
            case UserActions.USER_LOGAR:
                this.status = ((String) action.getData().get(UserActions.KEY_TEXT));
                emitStoreChange();
                break;
            case UserActions.USER_LOGOUT:
                this.status = ((String) action.getData().get(UserActions.KEY_TEXT));
                emitStoreChange();
                break;
            case UserActions.USER_CADASTRO:
                this.status = ((String) action.getData().get(UserActions.KEY_TEXT));
                emitStoreChange();
                break;
            case UserActions.USER_GET_AVALIACAO_POLITICO:
                mAvaliacao = ((ParseObject) action.getData().get(UserActions.KEY_TEXT));
                if(mAvaliacao != null){
                    jaVotou = true;
                }

                emitStoreChange();
                break;

            case UserActions.USER_GET_CIDADES:
                cidades = ((List<ParseObject>) action.getData().get(UserActions.KEY_TEXT));
                emitStoreChange();
                break;

        }
    }

    @Override
    StoreChangeEvent changeEvent() {
        UserStoreChangeEvent mUserStoreChangeEvent = new UserStoreChangeEvent();
        return mUserStoreChangeEvent;
    }

    public List<ParseObject> getCidades() {
        return cidades;
    }

    public class UserStoreChangeEvent implements StoreChangeEvent {

    }
}
