package com.gamfig.monitorabrasil.POJO;

import com.parse.ParseObject;

import java.util.List;

/**
 * Created by geral_000 on 14/02/2016.
 */
public class ComentarioEvent extends Event{
    public  List<ParseObject> comentarios;
    public ParseObject comentario;

    /**
     * Cria um evento de comentario
     * @param action nome da action
     * @param list lista de comentarios
     * @param erro erro se houver
     */
    public ComentarioEvent(String action, List<ParseObject> list, String erro) {
        this.action = action;
        this.comentarios = list;
        this.erro = erro;
    }


    public ComentarioEvent(String action, ParseObject comentario, String erro) {
        this.action = action;
        this.comentario = comentario;
        this.erro = erro;
    }



    public ComentarioEvent(ParseObject object, String erro) {
        this.comentario = object;
        this.erro = erro;
    }

    public ComentarioEvent(String action) {
        this.action = action;
    }



    public ParseObject getComentario() {
        return comentario;
    }

    public void setComentario(ParseObject comentario) {
        this.comentario = comentario;
    }


    public List<ParseObject> getComentarios() {
        return comentarios;
    }

    public void setComentarios(List<ParseObject> comentarios) {
        this.comentarios = comentarios;
    }



    public ComentarioEvent( List<ParseObject> comentarios, String erro){

        this.comentarios=comentarios;
        this.erro = erro;
    }

}
