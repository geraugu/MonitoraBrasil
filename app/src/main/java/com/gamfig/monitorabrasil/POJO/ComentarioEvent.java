package com.gamfig.monitorabrasil.POJO;

import com.parse.ParseObject;

import java.util.List;

/**
 * Created by geral_000 on 14/02/2016.
 */
public class ComentarioEvent {
    public  List<ParseObject> comentarios;
    public String erro;
    public ParseObject comentario;

    public ComentarioEvent() {

    }


    public ParseObject getComentario() {
        return comentario;
    }

    public void setComentario(ParseObject comentario) {
        this.comentario = comentario;
    }

    public ComentarioEvent(ParseObject object, String erro) {
        this.comentario = object;
        this.erro = erro;
    }

    public String getErro() {
        return erro;
    }

    public void setErro(String erro) {
        this.erro = erro;
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
