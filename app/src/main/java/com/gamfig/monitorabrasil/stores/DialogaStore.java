package com.gamfig.monitorabrasil.stores;


import com.gamfig.monitorabrasil.actions.Action;
import com.gamfig.monitorabrasil.actions.DialogaActions;
import com.gamfig.monitorabrasil.dispatcher.Dispatcher;
import com.gamfig.monitorabrasil.model.Pergunta;
import com.parse.ParseObject;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Geraldo on 13/08/2015.
 */
public class DialogaStore extends Store{

    private static DialogaStore instance;
    private String status;
    private String evento;
    private List<ParseObject> temas;
    private List<ParseObject> resultado; // lista de perguntas ordenadas
    private Pergunta pergunta;
    private List<ParseObject> perguntas;

    public List<ParseObject> getPerguntas() {
        return perguntas;
    }

    public void setPerguntas(List<ParseObject> perguntas) {
        this.perguntas = perguntas;
    }


    public String getEvento() {
        return evento;
    }

    public List<ParseObject> getResultado(){ return this.resultado;}

    public void setEvento(String evento) {
        this.evento = evento;
    }


    public Pergunta getPergunta() {
        return pergunta;
    }

    public void setPergunta(Pergunta pergunta) {
        this.pergunta = pergunta;
    }



    protected DialogaStore(Dispatcher dispatcher) {
        super(dispatcher);
        temas = new ArrayList<>();
        resultado = new ArrayList<>();
        perguntas  = new ArrayList<>();
    }

    public static DialogaStore get(Dispatcher dispatcher) {
        if (instance == null) {
            instance = new DialogaStore(dispatcher);
        }
        return instance;
    }

    public List<ParseObject> getTemas(){
        return temas;
    }


    @Override
    @Subscribe
    public void onAction(Action action) {
        status = "erro";
        this.evento = action.getType();

        switch (action.getType()) {
            case DialogaActions.DIALOGA_GET_TEMAS:
                temas = ((List<ParseObject>) action.getData().get(DialogaActions.KEY_TEXT));
                emitStoreChange();
                break;
            case DialogaActions.DIALOGA_GET_PERGUNTA_RESPOSTAS:
                pergunta = ((Pergunta) action.getData().get(DialogaActions.KEY_TEXT));
                emitStoreChange();
                break;
            case DialogaActions.DIALOGA_ENVIAR_RESPOSTA:
                emitStoreChange();
                break;
            case DialogaActions.DIALOGA_ENVIAR_PERGUNTA:
                emitStoreChange();
                break;
            case DialogaActions.DIALOGA_GET_RESULTADO:
                resultado = ((List<ParseObject>) action.getData().get(DialogaActions.KEY_TEXT));
                emitStoreChange();
                break;
            case DialogaActions.DIALOGA_GET_PERGUNTAS:
                perguntas = ((List<ParseObject>) action.getData().get(DialogaActions.KEY_TEXT));
                emitStoreChange();
                break;

            case DialogaActions.DIALOGA_GET_PERGUNTA_ALETORIA:
                perguntas = ((List<ParseObject>) action.getData().get(DialogaActions.KEY_TEXT));
                if(perguntas.size() > 0)
                    pergunta = new Pergunta(getRandomList(perguntas), null);
                emitStoreChange();
                break;


        }
    }
    private Random random = new Random();
    public ParseObject getRandomList(List<ParseObject> list) {

        //0-4
        int index = random.nextInt(list.size());

        return list.get(index);

    }

    @Override
    StoreChangeEvent changeEvent() {
        DialogaStoreChangeEvent mDialogaStoreChangeEvent = new DialogaStoreChangeEvent();
        return mDialogaStoreChangeEvent;
    }


    public class DialogaStoreChangeEvent implements StoreChangeEvent {

    }
}
