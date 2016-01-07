package com.gamfig.monitorabrasil.model;

import android.graphics.Color;
import android.graphics.Typeface;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Geraldo on 07/01/2016.
 */
public class Grafico {

    public Typeface getTf() {
        return tf;
    }

    public void setTf(Typeface tf) {
        this.tf = tf;
    }

    private Typeface tf;

    public Grafico(){}
    public Grafico(Typeface tf){this.tf=tf;}

    /**
     * Busca o dataset para colocar no grafico PIE
     * @param gastos gastos do pollitico selecionado
     * @return dataset
     */
    public PieData pieDataGastos(List<ParseObject> gastos){
        ArrayList<Entry> yVals1 = new ArrayList<Entry>();


        // IMPORTANT: In a PieChart, no values (Entry) should have the same
        // xIndex (even if from different DataSets), since no values can be
        // drawn above each other.
        for (int i = 0; i < gastos.size() ; i++) {
            yVals1.add(new Entry(gastos.get(i).getNumber("total").floatValue(), i));
        }


        ArrayList<String> xVals = new ArrayList<String>();


        for (int i = 0; i < gastos.size(); i++)
            xVals.add(gastos.get(i).getString("categoria"));


        PieDataSet dataSet = new PieDataSet(yVals1, "Gastos");
        dataSet.setSliceSpace(2f);
        dataSet.setSelectionShift(5f);


        // add a lot of colors


        ArrayList<Integer> colors = new ArrayList<Integer>();


        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);


        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);


        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);


        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);


        //  for (int c : ColorTemplate.PASTEL_COLORS)
        //      colors.add(c);


        colors.add(ColorTemplate.getHoloBlue());


        dataSet.setColors(colors);
        //dataSet.setSelectionShift(0f);


        PieData data = new PieData(xVals, dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
        data.setValueTypeface(tf);
        return data;
    }

    /**
     * Faz o setup do grafico para ser reusado em outras telas
     * @param mChart a view do grafico
     * @return o mesmo grafico agora configurado
     */
    public PieChart setupPieChartGastos(PieChart mChart) {
        mChart.setUsePercentValues(true);
        mChart.setDescription("");
        mChart.setExtraOffsets(5, 10, 5, 5);
        mChart.setDragDecelerationFrictionCoef(0.95f);

        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColorTransparent(true);


        mChart.setTransparentCircleColor(Color.WHITE);
        mChart.setTransparentCircleAlpha(110);


        mChart.setHoleRadius(58f);
        mChart.setTransparentCircleRadius(61f);


        mChart.setDrawCenterText(true);


        mChart.setRotationAngle(0);
        // enable rotation of the chart by touch
        mChart.setRotationEnabled(true);
        mChart.setHighlightPerTapEnabled(true);


        // mChart.setUnit(" ?");
        // mChart.setDrawUnitsInChart(true);


        mChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        // mChart.spin(2000, 0, 360);


        Legend l = mChart.getLegend();
        l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);

        return mChart;
    }
}
