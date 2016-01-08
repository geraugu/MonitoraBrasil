package com.gamfig.monitorabrasil.stores;


import com.gamfig.monitorabrasil.actions.Action;
import com.gamfig.monitorabrasil.dispatcher.Dispatcher;
import com.parse.ParseObject;

import java.util.List;

/**
 * Created by lgvalle on 02/08/15.
 */
public abstract class Store {

    final Dispatcher dispatcher;

    protected Store(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    void emitStoreChange() {
        dispatcher.emitChange(changeEvent());
    }

    abstract StoreChangeEvent changeEvent();
    public abstract void onAction(Action action);



    public interface StoreChangeEvent {}
}
