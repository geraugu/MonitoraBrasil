package com.gamfig.monitorabrasil.POJO;

import com.gamfig.monitorabrasil.actions.PoliticoActions;
import com.gamfig.monitorabrasil.model.Comparacao;
import com.parse.ParseException;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by geral_000 on 14/02/2016.
 */
public class PoliticoEvent extends Event{
    private Comparacao comparacao;
    private List<ParseObject> list;
    private List<ParseObject> presenca;
    private List<ParseObject> politicos;
    private List<ParseObject> politicosFiltro;
    private List<ParseObject> gastos;

    public PoliticoEvent(String action, Comparacao comparacao, String erro) {
        this.action = action;
        this.erro = erro;
        this.comparacao = comparacao;
    }

    public PoliticoEvent(String erro) {
        this.erro = erro;
    }

    public PoliticoEvent(String action, List<ParseObject> list, String erro) {
        this.action = action;
        this.erro = erro;
        switch (action){
            case PoliticoActions.POLITICO_GET_PRESENCA:
                this.presenca = list;
                break;
            case PoliticoActions.POLITICO_GET_ALL:
                this.list = list;
                break;
            case PoliticoActions.POLITICO_GET_GAST0S:
                this.gastos = list;
                break;
        }

    }

    public  List<ParseObject> filtrar(String query, boolean ranking){
        if(politicosFiltro!= null)
            politicosFiltro.clear();
        else
            politicosFiltro = new ArrayList<>();
        for (int i = 0; i < politicos.size(); i++) {
            if(ranking){
                try {

                    ParseObject p = politicos.get(i);
                    p.fetchIfNeeded();
                    if(p.getString("nome") == null){
                        p = p.getParseObject("politico");
                        p.fetchIfNeeded();
                    }
                    if(p.getString("nome").toUpperCase().contains(query.toUpperCase()))
                        politicosFiltro.add(politicos.get(i));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }else{
                if(politicos.get(i).getString("nome").toUpperCase().contains(query.toUpperCase()))
                    politicosFiltro.add(politicos.get(i));
            }


        }
        return politicosFiltro;
    }


    public Comparacao getComparacao() {
        return comparacao;
    }

    public void setComparacao(Comparacao comparacao) {
        this.comparacao = comparacao;
    }

    public List<ParseObject> getList() {
        return list;
    }

    public List<ParseObject> getPoliticos() {
        for(int i=0; i < politicos.size();i++){
            politicos.get(i).put("pos",String.valueOf((i+1)));
        } return politicos;
    }

    public void setPoliticos(List<ParseObject> politicos) {
        this.politicos = politicos;
    }

    public List<ParseObject> getPoliticosFiltro() {
        return politicosFiltro;
    }

    public List<ParseObject> getPresenca() {
        return presenca;
    }

    public List<ParseObject> getGastos() {        return gastos;
    }

    public void setPoliticosFiltro(List<ParseObject> politicosFiltro) {
        this.politicosFiltro = politicosFiltro;
    }
}
