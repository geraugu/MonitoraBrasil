package com.gamfig.monitorabrasil.util;


import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.NumberFormat;

/**
 * Created by geral_000 on 03/01/2016.
 */
public class MyValueFormatter implements ValueFormatter {

    private NumberFormat  mFormat;


    public MyValueFormatter() {
        mFormat = NumberFormat.getInstance();
        mFormat.setMaximumFractionDigits(2);


    }


    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        return  mFormat.format(value) ;
    }

    public String formata(float value){
        return  mFormat.format(value) ;
    }
}
