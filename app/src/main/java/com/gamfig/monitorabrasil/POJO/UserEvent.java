package com.gamfig.monitorabrasil.POJO;

import com.parse.ParseObject;

/**
 * Created by geral_000 on 14/02/2016.
 */
public class UserEvent extends Event{

    private ParseObject avaliacao;

    public UserEvent(String action, String erro) {
        this.action = action;
        this.erro = erro;
    }

    public UserEvent(String action) {
        this.action=action;
    }

    public UserEvent(String action, ParseObject avaliacao, String error) {
        this.action = action;
        this.erro = erro;
        this.avaliacao = avaliacao;
    }

    public ParseObject getAvaliacao() {
        return avaliacao;
    }
}
