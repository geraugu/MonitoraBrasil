package com.gamfig.monitorabrasil.model;

import com.parse.ParseObject;

/**
 * Created by Geraldo on 07/01/2016.
 */

public class Comparacao {


    private String produto;
    private float valor;
    private ParseObject cota;

    public String getProduto() {
        return produto;
    }

    public void setProduto(String produto) {
        this.produto = produto;
    }

    public float getValor() {
        return valor;
    }

    public void setValor(float valor) {
        this.valor = valor;
    }

    public ParseObject getCota() {
        return cota;
    }

    public void setCota(ParseObject cota) {
        this.cota = cota;
    }

}
