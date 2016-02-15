package com.gamfig.monitorabrasil.POJO;

import com.gamfig.monitorabrasil.model.Projeto;
import com.parse.ParseObject;

import java.util.List;

/**
 * Created by geral_000 on 14/02/2016.
 */
public class ProjetoEvent extends Event{
    private Projeto projeto;
    private List<ParseObject> projetos;

    public ProjetoEvent(String action, Projeto projeto, String erro) {
        this.action = action;
        this.erro = erro;
        this.projeto = projeto;
    }

    public ProjetoEvent(String action) {
        this.action = action;
    }

    public ProjetoEvent(String action, List<ParseObject> projetos, String erro) {
        this.action = action;
        this.erro = erro;
        this.projetos = projetos;
    }

    public void limpaProjetos() {

        projetos.clear();

    }

    public Projeto getProjeto() {
        return projeto;
    }

    public void setProjeto(Projeto projeto) {
        this.projeto = projeto;
    }

    public List<ParseObject> getProjetos() {
        return projetos;
    }

    public void setProjetos(List<ParseObject> projetos) {
        this.projetos = projetos;
    }
}
