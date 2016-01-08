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

        spnUf = (Spinner) view.findViewById(R.id.spnUf);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(), R.array.todasufs, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnUf.setAdapter(adapter);

        spnPartido = (Spinner) view.findViewById(R.id.spnPartido);
        ArrayAdapter<String> adapterPartido = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, actionsCreator.getPartidos());
        adapterPartido.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnPartido.setAdapter(adapterPartido);

        Button btnCancelar = (Button) view.findViewById(R.id.cancel);
        btnCancelar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                getDialog().dismiss();

            }
        });

        Button btnOk = (Button) view.findViewById(R.id.ok);
        btnOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {

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
