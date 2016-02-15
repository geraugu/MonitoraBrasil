package com.gamfig.monitorabrasil.views.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;

import com.gamfig.monitorabrasil.POJO.UserEvent;
import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.actions.UserActions;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Created by Geraldo on 07/01/2016.
 */
public class DialogAvaliacao extends DialogFragment {

    int idUser;
    int idPolitico;
    private String titulo;
    private ParseObject mPolitico;
    private ParseObject mAvaliacao;
    private RatingBar rb;

    private DialogInterface.OnDismissListener onDismissListener;

    private boolean jaVotou;



    private UserActions userActions;
    private UserEvent userEvent;

    public DialogAvaliacao(ParseObject politico, String titulo) {
        this.titulo=titulo;
        this.mPolitico = politico;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.dialog_avaliacao, container, false);
        initDependencies();
        setupView(view);

        return view;
    }

    private void initDependencies() {
        userActions = UserActions.get();
    }


    private void setupView(View view) {
        rb = (RatingBar) view.findViewById(R.id.ratingBar1);

        jaVotou = false;

        //rb.setRating(new UserDAO(getActivity()).buscaAvaliacaoSalva(idPolitico));

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
                if (ParseUser.getCurrentUser() != null) {
                    userActions.avaliar(mPolitico, rb.getRating(), userEvent.getAvaliacao());
                }
                getDialog().dismiss();
            }
        });

        //verificar se ja foi feita a avaliacao
        userActions.getAvaliacaoPolitico(mPolitico);

    }
    /**
     * Atualiza a UI depois de uma action
     * @param event
     */
    @Subscribe
    public void onMessageEvent(UserEvent event){
        userEvent = event;
        rb.setRating((float)event.getAvaliacao().getDouble("avaliacao"));
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
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
}
