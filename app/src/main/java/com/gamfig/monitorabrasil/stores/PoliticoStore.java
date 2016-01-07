package com.gamfig.monitorabrasil.stores;

import com.gamfig.monitorabrasil.actions.Action;
import com.gamfig.monitorabrasil.actions.PoliticoActions;
import com.gamfig.monitorabrasil.dispatcher.Dispatcher;
import com.gamfig.monitorabrasil.model.Comparacao;
import com.parse.ParseObject;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Geraldo on 13/08/2015.
 */
public class PoliticoStore extends Store{

    private static PoliticoStore instance;
    private String status;
    private String evento;
    private List<ParseObject> politicos;
    private List<ParseObject> politicosFiltro;
    private List<ParseObject> gastos;
    private ParseObject politico;
    private Comparacao gasto;

    public Comparacao getGasto() {
        return gasto;
    }

    protected PoliticoStore(Dispatcher dispatcher) {
        super(dispatcher);
        politicos = new ArrayList<>();
        politicosFiltro = new ArrayList<>();
    }

    public static PoliticoStore get(Dispatcher dispatcher) {
        if (instance == null) {
            instance = new PoliticoStore(dispatcher);
        }
        return instance;
    }

    public List<ParseObject> getGastos(){
        return gastos;
    }

    public  List<ParseObject> filtrar(String query){
        politicosFiltro.clear();
        for (int i = 0; i < politicos.size(); i++) {
            if(politicos.get(i).getString("nome").toUpperCase().contains(query.toUpperCase()))
                politicosFiltro.add(politicos.get(i));

        }
        return politicosFiltro;
    }

    public void setPoliticosFiltro(List<ParseObject> politicosFiltro) {
        this.politicosFiltro = politicosFiltro;
    }
    public ParseObject getPolitico(){return politico;}

    public List<ParseObject> getPoliticos(){
        return politicos;
    }



    @Override
    @Subscribe
    public void onAction(Action action) {
        status = "erro";
        this.evento = action.getType();

        switch (action.getType()) {
            case PoliticoActions.POLITICO_GET_ALL:
                politicos = ((List<ParseObject>) action.getData().get(PoliticoActions.KEY_TEXT));
                status = "sucesso";
                emitStoreChange();
                break;
            case PoliticoActions.POLITICO_GET_INFOS:
                politico = ((ParseObject) action.getData().get(PoliticoActions.KEY_TEXT));
                emitStoreChange();
                break;
            case PoliticoActions.POLITICO_GET_GAST0S:
                gastos = ((List<ParseObject>) action.getData().get(PoliticoActions.KEY_TEXT));
                emitStoreChange();
                break;

            case PoliticoActions.POLITICO_GET_COMPARACAO_GASTO:
                gasto = ((Comparacao) action.getData().get(PoliticoActions.KEY_TEXT));
                emitStoreChange();
                break;
        }
    }

    @Override
    StoreChangeEvent changeEvent() {
        PoliticoStoreChangeEvent mPoliticoStoreChangeEvent = new PoliticoStoreChangeEvent();
        mPoliticoStoreChangeEvent.status = this.status;
        mPoliticoStoreChangeEvent.evento = this.evento;
        return mPoliticoStoreChangeEvent;
    }

    public ParseObject getPolitico(String string) {
        for (int i = 0; i < politicos.size(); i++) {
            if(politicos.get(i).getObjectId().equals(string))
                return politicos.get(i);

        }
        return null;
    }


    public class PoliticoStoreChangeEvent implements StoreChangeEvent {
        private String status;
        private String evento;

        public String getEvento() {
            return evento;
        }

        public String getStatus() {
            return status;
        }
    }
}
