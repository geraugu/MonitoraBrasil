package com.gamfig.monitorabrasil.POJO;

import com.gamfig.monitorabrasil.actions.DialogaActions;
import com.gamfig.monitorabrasil.model.Pergunta;
import com.parse.ParseObject;

import java.util.List;

/**
 * Created by geral_000 on 14/02/2016.
 */
public class DialogaEvent extends Event{
    private ParseObject pergunta;
    private List<ParseObject> perguntas;
    private List<ParseObject> resultado;
    private Pergunta perguntaResposta;

    public DialogaEvent(String erro) {
        this.erro = erro;
    }

    public DialogaEvent(String action, ParseObject pergunta, String erro) {
        this.action = action;
        this.pergunta = pergunta;
        this.erro = erro;
    }

    public DialogaEvent(String action, List<ParseObject> list, String erro) {
        this.action = action;
        this.erro = erro;
        switch (action){
            case DialogaActions.DIALOGA_GET_PERGUNTAS:
                perguntas = list;
                break;
            case DialogaActions.DIALOGA_GET_RESULTADO:
                resultado = list;
                break;
        }
    }

    public DialogaEvent(String action, Pergunta perguntaResposta, String erro) {
        this.action = action;
        this.perguntaResposta = perguntaResposta;
        this.erro = erro;
    }

    public ParseObject getPergunta() {
        return pergunta;
    }

    public void setPergunta(ParseObject pergunta) {
        this.pergunta = pergunta;
    }

    public List<ParseObject> getPerguntas() {
        return perguntas;
    }

    public List<ParseObject> getResultado() {
        return resultado;
    }

    public Pergunta getPerguntaResposta() {
        return perguntaResposta;
    }
}
