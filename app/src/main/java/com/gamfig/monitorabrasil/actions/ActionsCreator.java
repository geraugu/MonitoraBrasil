package com.gamfig.monitorabrasil.actions;

import android.content.Context;
import android.content.SharedPreferences;

import com.gamfig.monitorabrasil.application.AppController;

/**
 * Created by 89741803168 on 13/08/2015.
 */
public class ActionsCreator {
    private static ActionsCreator instance;

    ActionsCreator() {
    }
    public static ActionsCreator get() {
        if (instance == null) {
            instance = new ActionsCreator();
        }
        return instance;
    }


    public void salvaNoSharedPreferences(String nome, String valor){
        SharedPreferences sharedPref = AppController.getInstance().getSharedPreferences("com.monitorabrasil.CHAVE_PREFERENCIA",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(nome, valor);
        editor.commit();
    }

    public String getValorSharedPreferences(String nome){
        SharedPreferences sharedPref = AppController.getInstance().getSharedPreferences("com.monitorabrasil.CHAVE_PREFERENCIA",Context.MODE_PRIVATE);
        return sharedPref.getString(nome,null);
    }
}
