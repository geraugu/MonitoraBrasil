package com.gamfig.monitorabrasil.POJO;

/**
 * Created by Geraldo on 15/02/2016.
 */
public abstract class Event {
    protected String erro;
    protected String action;

    public String getErro() {
        return erro;
    }

    public void setErro(String erro) {
        this.erro = erro;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
