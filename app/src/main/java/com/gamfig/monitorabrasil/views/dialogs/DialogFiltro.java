package com.gamfig.monitorabrasil.views.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.actions.ActionsCreator;
import com.gamfig.monitorabrasil.dispatcher.Dispatcher;
import com.gamfig.monitorabrasil.stores.UserStore;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Geraldo on 07/01/2016.
 */
public class DialogFiltro extends DialogFragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";

    private String titulo;
    private String tipo;
    private String casa;

    private DialogInterface.OnDismissListener onDismissListener;


    private Dispatcher dispatcher;
    private ActionsCreator actionsCreator;
    private UserStore userStore;

    private Spinner spnUf;
    private Spinner spnPartido;
    private Spinner spnCategoria;
    private Spinner spnAno;

    public static DialogFiltro newInstance(String titulo, String tipo, String casa)
    {
        DialogFiltro myFragment = new DialogFiltro();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, titulo);
        args.putString(ARG_PARAM2, tipo);
        args.putString(ARG_PARAM3, casa);
        myFragment.setArguments(args);
        return myFragment;
    }
    public DialogFiltro() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            titulo = getArguments().getString(ARG_PARAM1);
            tipo = getArguments().getString(ARG_PARAM2);
            casa = getArguments().getString(ARG_PARAM3);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.dialog_filtro, container, false);
        initDependencies();
        setupView(view);

        return view;
    }

    private void initDependencies() {
        dispatcher = Dispatcher.get(new Bus());
        actionsCreator = ActionsCreator.get(dispatcher);
        userStore = UserStore.get(dispatcher);
    }


    private void setupView(View view) {

        LinearLayout llCategoria = (LinearLayout)view.findViewById(R.id.llCategoria);
        LinearLayout llAno = (LinearLayout)view.findViewById(R.id.llAno);
        switch (tipo){
            case "politico":
                llCategoria.setVisibility(View.GONE);
                break;
            case "gasto":
                llCategoria.setVisibility(View.VISIBLE);
                //llAno.setVisibility(View.VISIBLE);
                break;
            case "projeto":
                llCategoria.setVisibility(View.GONE);
               // llAno.setVisibility(View.VISIBLE);
                break;
        }

        spnAno = (Spinner) view.findViewById(R.id.spnAno);
        spnCategoria = (Spinner) view.findViewById(R.id.spnCategoria);
        spnUf = (Spinner) view.findViewById(R.id.spnUf);
        spnPartido = (Spinner) view.findViewById(R.id.spnPartido);

        //spinner UF
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(), R.array.todasufs, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnUf.setAdapter(adapter);
        if(actionsCreator.getValorSharedPreferences("ufPosSelecionada")!= null){
            int ufPos = Integer.valueOf(actionsCreator.getValorSharedPreferences("ufPosSelecionada"));
            if(ufPos > 0){
                spnUf.setSelection(ufPos,true);
            }

        }

        //spinner Partido
        ArrayList<String> arrayStrings = new ArrayList<String>();
        arrayStrings.add(0,"Todos Partidos");
        arrayStrings.addAll(actionsCreator.getPartidos(false));
        ArrayAdapter<String> adapterPartido = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, arrayStrings);
        adapterPartido.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnPartido.setAdapter(adapterPartido);
        if(actionsCreator.getValorSharedPreferences("partidoPosSelecionada")!= null){
            int pos = Integer.valueOf(actionsCreator.getValorSharedPreferences("partidoPosSelecionada"));
            if(pos > 0){
                spnPartido.setSelection(pos,true);
            }

        }

        //spinner Ano
        ArrayList<String> years = new ArrayList<String>();
        years.add(0,"");
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = 2015; i <= thisYear; i++) {
            years.add(Integer.toString(i));
        }
        ArrayAdapter<String> adapterAno = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, years);
        adapterAno.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnAno.setAdapter(adapterAno);
        if(actionsCreator.getValorSharedPreferences("anoPosSelecionada")!= null){
            int pos = Integer.valueOf(actionsCreator.getValorSharedPreferences("anoPosSelecionada"));
            if(pos > 0){
                spnAno.setSelection(pos,true);
            }

        }

        //spinner Categoria
        ArrayList<String> arrayCategoria = new ArrayList<String>();
        arrayCategoria.add(0,"Todas Categorias");
        arrayCategoria.addAll(actionsCreator.getCategoriasCotas(casa,false));
        ArrayAdapter<String> adapterCategoria = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, arrayCategoria);
        adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCategoria.setAdapter(adapterCategoria);
        if(actionsCreator.getValorSharedPreferences("categoriaPosSelecionada")!= null){
            int pos = Integer.valueOf(actionsCreator.getValorSharedPreferences("categoriaPosSelecionada"));
            if(pos > 0){
                spnCategoria.setSelection(pos,true);
            }

        }

        Button btnCancelar = (Button) view.findViewById(R.id.cancel);
        btnCancelar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                getDialog().dismiss();

            }
        });

        Button btnLlimpar = (Button) view.findViewById(R.id.btnLimpar);
        btnLlimpar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                actionsCreator.salvaNoSharedPreferences("ufPosSelecionada","0");
                actionsCreator.salvaNoSharedPreferences("partidoPosSelecionada","0");
                actionsCreator.salvaNoSharedPreferences("anoPosSelecionada","0");
                actionsCreator.salvaNoSharedPreferences("categoriaPosSelecionada","0");
                actionsCreator.salvaNoSharedPreferences("ufSelecionada",null);
                actionsCreator.salvaNoSharedPreferences("partidoSelecionada",null);
                actionsCreator.salvaNoSharedPreferences("anoSelecionada",null);
                actionsCreator.salvaNoSharedPreferences("categoriaSelecionada",null);

                spnUf.setSelection(0,true);
                spnPartido.setSelection(0,true);
                spnAno.setSelection(0,true);
                spnCategoria.setSelection(0,true);

            }
        });

        Button btnOk = (Button) view.findViewById(R.id.ok);
        btnOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                //salva os filtros selecionados
                int ufSelecionadaPosition = spnUf.getSelectedItemPosition();
                actionsCreator.salvaNoSharedPreferences("ufPosSelecionada", String.valueOf(ufSelecionadaPosition));

                if(ufSelecionadaPosition == 0) {
                    actionsCreator.salvaNoSharedPreferences("ufSelecionada", null);
                }else{
                    actionsCreator.salvaNoSharedPreferences("ufSelecionada", spnUf.getAdapter().getItem(ufSelecionadaPosition).toString());
                }

                int partidoSelecionadaPosition = spnPartido.getSelectedItemPosition();
                actionsCreator.salvaNoSharedPreferences("partidoPosSelecionada", String.valueOf(partidoSelecionadaPosition));
                if(partidoSelecionadaPosition == 0) {
                    actionsCreator.salvaNoSharedPreferences("partidoSelecionada", null);
                }else{
                    actionsCreator.salvaNoSharedPreferences("partidoSelecionada", spnPartido.getAdapter().getItem(partidoSelecionadaPosition).toString());
                }

                int anoSelecionadaPosition = spnAno.getSelectedItemPosition();
                actionsCreator.salvaNoSharedPreferences("anoPosSelecionada", String.valueOf(anoSelecionadaPosition));

                if(anoSelecionadaPosition == 0) {
                    actionsCreator.salvaNoSharedPreferences("anoSelecionada", null);
                }else{
                    actionsCreator.salvaNoSharedPreferences("anoSelecionada", spnAno.getAdapter().getItem(anoSelecionadaPosition).toString());
                }

                int categoriaSelecionadaPosition = spnCategoria.getSelectedItemPosition();
                actionsCreator.salvaNoSharedPreferences("categoriaPosSelecionada", String.valueOf(categoriaSelecionadaPosition));

                if(categoriaSelecionadaPosition == 0) {
                    actionsCreator.salvaNoSharedPreferences("categoriaSelecionada",
                            null);
                }else{
                    actionsCreator.salvaNoSharedPreferences("categoriaSelecionada",
                            spnCategoria.getAdapter().getItem(categoriaSelecionadaPosition).toString());
                }
                getDialog().dismiss();
            }
        });


    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setTitle(titulo);

        // WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        // lp.copyFrom(dialog.getWindow().getAttributes());
        // lp.width = WindowManager.LayoutParams.MATCH_PARENT-20;
        // dialog.show();
        // dialog.getWindow().setAttributes(lp);

        return dialog;
    }



    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss(dialog);
        }
        //dispatcher.register(this);
        //dispatcher.register(userStore);
    }
    @Override
    public void onResume() {
        super.onResume();
        dispatcher.register(this);
        try {
            dispatcher.register(userStore);
        }catch (Exception e){

        }

    }

    @Override
    public void onPause() {
        super.onPause();
        dispatcher.unregister(this);
        dispatcher.unregister(userStore);
    }
}
