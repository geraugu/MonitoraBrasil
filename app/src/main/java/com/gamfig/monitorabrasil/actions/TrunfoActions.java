package com.gamfig.monitorabrasil.actions;

import com.gamfig.monitorabrasil.POJO.DialogaEvent;
import com.gamfig.monitorabrasil.POJO.TrunfoEvent;
import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.application.AppController;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Created by geral_000 on 23/02/2016.
 */
public class TrunfoActions {

    public static final java.lang.String TRUNFO_GET_JOGOS_DISPONIVEIS = "trunfo_get_jogos_disponiveis";
    private static TrunfoActions instance;

    TrunfoActions() {
    }
    public static TrunfoActions get() {
        if (instance == null) {
            instance = new TrunfoActions();
        }
        return instance;
    }


    public void getJogosDisponiveis() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Partida");
        query.whereDoesNotExist("cartas1");
        query.addAscendingOrder("createdAt");
        query.include("j1");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if(objects.size() > 0) {
                        EventBus.getDefault().post(new TrunfoEvent(TRUNFO_GET_JOGOS_DISPONIVEIS, objects, null));
                    }else{
                        TrunfoEvent ce = new TrunfoEvent(TRUNFO_GET_JOGOS_DISPONIVEIS);
                        ce.setErro(AppController.getInstance().getString(R.string.erro_geral));
                        EventBus.getDefault().post(ce);
                    }
                } else {
                    TrunfoEvent ce = new TrunfoEvent(TRUNFO_GET_JOGOS_DISPONIVEIS);
                    ce.setErro(AppController.getInstance().getString(R.string.erro_geral));
                    EventBus.getDefault().post(ce);
                }
            }
        });
    }

}
