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

    private String titulo;

    private DialogInterface.OnDismissListener onDismissListener;


    private Dispatcher dispatcher;
    private ActionsCreator actionsCreator;
    private UserStore userStore;

    private Spinner spnUf;
    private Spinner spnPartido;
    private Spinner spnCategoria;
    private Spinner spnAno;


    public DialogFiltro( String titulo) {
        this.titulo=titulo;
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

        spnAno = (Spinner) view.findViewById(R.id.spnAno);
        spnCategoria = (Spinner) view.findViewById(R.id.spnCategoria);
        spnUf = (Spinner) view.findViewById(R.id.spnUf);
        spnPartido = (Spinner) view.findViewById(R.id.spnPartido);

        //spinner UF
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(), R.array.todasufs, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnUf.setAdapter(adapter);
        if(actionsCreator.getItemConfiguracao("ufPosSelecionada")!= null){
            int ufPos = Integer.valueOf(actionsCreator.getItemConfiguracao("ufPosSelecionada"));
            if(ufPos > 0){
                spnUf.setSelection(ufPos,true);
            }

        }

        //spinner Partido
        ArrayAdapter<String> adapterPartido = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, actionsCreator.getPartidos());
        adapterPartido.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnPartido.setAdapter(adapterPartido);
        if(actionsCreator.getItemConfiguracao("partidoPosSelecionada")!= null){
            int pos = Integer.valueOf(actionsCreator.getItemConfiguracao("partidoPosSelecionada"));
            if(pos > 0){
                spnPartido.setSelection(pos,true);
            }

        }

        //spinner Ano
        ArrayList<String> years = new ArrayList<String>();
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = 2015; i <= thisYear; i++) {
            years.add(Integer.toString(i));
        }
        ArrayAdapter<String> adapterAno = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, years);
        adapterAno.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnAno.setAdapter(adapterAno);
        if(actionsCreator.getItemConfiguracao("anoPosSelecionada")!= null){
            int pos = Integer.valueOf(actionsCreator.getItemConfiguracao("anoPosSelecionada"));
            if(pos > 0){
                spnAno.setSelection(pos,true);
            }

        }

        //spinner Categoria
        ArrayAdapter<String> adapterCategoria = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, actionsCreator.getCategoriasCotas());
        adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCategoria.setAdapter(adapterCategoria);
        if(actionsCreator.getItemConfiguracao("categoriaPosSelecionada")!= null){
            int pos = Integer.valueOf(actionsCreator.getItemConfiguracao("categoriaPosSelecionada"));
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

                actionsCreator.salvaParametroConfiguracao("ufPosSelecionada","0");
                actionsCreator.salvaParametroConfiguracao("partidoPosSelecionada","0");
                actionsCreator.salvaParametroConfiguracao("anoPosSelecionada","0");
                actionsCreator.salvaParametroConfiguracao("categoriaPosSelecionada","0");
                actionsCreator.salvaParametroConfiguracao("ufSelecionada",null);
                actionsCreator.salvaParametroConfiguracao("partidoSelecionada",null);
                actionsCreator.salvaParametroConfiguracao("anoSelecionada",null);
                actionsCreator.salvaParametroConfiguracao("categoriaSelecionada",null);

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
                actionsCreator.salvaParametroConfiguracao("ufPosSelecionada",String.valueOf(ufSelecionadaPosition));
                actionsCreator.salvaParametroConfiguracao("ufSelecionada",spnUf.getAdapter().getItem(ufSelecionadaPosition).toString());

                int partidoSelecionadaPosition = spnPartido.getSelectedItemPosition();
                actionsCreator.salvaParametroConfiguracao("partidoPosSelecionada",String.valueOf(partidoSelecionadaPosition));
                actionsCreator.salvaParametroConfiguracao("partidoSelecionada",spnPartido.getAdapter().getItem(partidoSelecionadaPosition).toString());

                int anoSelecionadaPosition = spnAno.getSelectedItemPosition();
                actionsCreator.salvaParametroConfiguracao("anoPosSelecionada",String.valueOf(anoSelecionadaPosition));
                actionsCreator.salvaParametroConfiguracao("anoSelecionada",spnAno.getAdapter().getItem(anoSelecionadaPosition).toString());

                int categoriaSelecionadaPosition = spnCategoria.getSelectedItemPosition();
                actionsCreator.salvaParametroConfiguracao("categoriaPosSelecionada",String.valueOf(categoriaSelecionadaPosition));
                actionsCreator.salvaParametroConfiguracao("categoriSelecionada",spnCategoria.getAdapter().getItem(categoriaSelecionadaPosition).toString());
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
        dispatcher.register(userStore);
    }

    @Override
    public void onPause() {
        super.onPause();
        dispatcher.unregister(this);
        dispatcher.unregister(userStore);
    }
}
