package com.gamfig.monitorabrasil.POJO;

import com.parse.ParseObject;

import java.util.List;

/**
 * Created by geral_000 on 23/02/2016.
 */
public class TrunfoEvent extends Event{
    private List<ParseObject> partidas;

    public TrunfoEvent(String action){
        this.action=action;
    }

    public TrunfoEvent(String action, List<ParseObject> partidas, String erro) {
        this.action = action;
        this.partidas = partidas;
        this.erro = erro;
    }

    public List<ParseObject> getPartidas() {
        return partidas;
    }
}
