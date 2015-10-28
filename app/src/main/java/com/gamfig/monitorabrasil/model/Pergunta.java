package com.gamfig.monitorabrasil.model;

import com.parse.ParseObject;

import java.util.List;

/**
 * Created by 89741803168 on 03/09/2015.
 */
public class Pergunta {

    private ParseObject pergunta;
    private List<ParseObject> respostas;

    public ParseObject getPergunta() {
        return pergunta;
    }

    public void setPergunta(ParseObject pergunta) {
        this.pergunta = pergunta;
    }

    public List<ParseObject> getRespostas() {
        return respostas;
    }

    public void setRespostas(List<ParseObject> respostas) {
        this.respostas = respostas;
    }


    public Pergunta(ParseObject pergunta,List<ParseObject> respostas){
        this.pergunta=pergunta;
        this.respostas=respostas;
    }

}
