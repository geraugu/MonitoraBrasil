package com.gamfig.monitorabrasil.stores;


import com.gamfig.monitorabrasil.actions.Action;
import com.gamfig.monitorabrasil.actions.ComentarioActions;
import com.gamfig.monitorabrasil.dispatcher.Dispatcher;
import com.parse.ParseObject;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Geraldo on 13/08/2015.
 */
public class ComentarioStore extends Store{

    private static ComentarioStore instance;
    private String status;

    public String getEvento() {
        return evento;
    }

    private String evento;
    private List<ParseObject> comentarios;
    private ParseObject comentario;

    protected ComentarioStore(Dispatcher dispatcher) {
        super(dispatcher);
        comentarios = new ArrayList<>();
    }

    public static ComentarioStore get(Dispatcher dispatcher) {
        if (instance == null) {
            instance = new ComentarioStore(dispatcher);
        }
        return instance;
    }

    public ParseObject getComentario(){return comentario;}

    public List<ParseObject> getComentarios(){
        return comentarios;
    }


    @Override
    @Subscribe
    public void onAction(Action action) {
        status = "erro";
        this.evento = action.getType();

        switch (action.getType()) {
            case ComentarioActions.COMENTARIO_GET_ALL:
                comentarios = ((List<ParseObject>) action.getData().get(ComentarioActions.KEY_TEXT));
                emitStoreChange();
                break;
            case ComentarioActions.COMENTARIO_POLITICO_GET_ULTIMO:
                comentario = ((ParseObject) action.getData().get(ComentarioActions.KEY_TEXT));
                emitStoreChange();
                break;
            case ComentarioActions.COMENTARIO_ENVIAR:
                emitStoreChange();
                break;

        }
    }

    @Override
    StoreChangeEvent changeEvent() {
        ComentarioStoreChangeEvent mComentarioStoreChangeEvent = new ComentarioStoreChangeEvent();
        return mComentarioStoreChangeEvent;
    }




    public class ComentarioStoreChangeEvent implements StoreChangeEvent {

    }
}
