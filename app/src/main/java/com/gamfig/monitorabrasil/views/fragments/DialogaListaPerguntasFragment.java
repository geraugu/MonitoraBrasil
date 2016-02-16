package com.gamfig.monitorabrasil.views.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.gamfig.monitorabrasil.POJO.DialogaEvent;
import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.actions.ActionsCreator;
import com.gamfig.monitorabrasil.actions.DialogaActions;
import com.gamfig.monitorabrasil.application.AppController;
import com.gamfig.monitorabrasil.interfaces.RecyclerViewOnClickListenerHack;
import com.gamfig.monitorabrasil.model.Tema;
import com.gamfig.monitorabrasil.views.LoginActivity;
import com.gamfig.monitorabrasil.views.adapters.PerguntaAdapter;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DialogaListaPerguntasFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DialogaListaPerguntasFragment extends Fragment implements RecyclerViewOnClickListenerHack {

    private static final String ARG_NOME = "nome";
    private static final String ARG_ICONE = "icone";
    private static final String ARG_COR = "cor";
    private static final String ARG_IDTEMA = "idTema";


    private String nome;
    private String icone;
    private int cor;
    private String idTema;


    private ActionsCreator actionsCreator;
    private DialogaActions dialogaActions;
    private DialogaEvent dialogaEvent;

    private OnFragmentInteractionListener mListener;

    private RecyclerView mRecyclerView;
    private PerguntaAdapter mAdapter;

    private ProgressBar pb;

    private ParseObject pergunta;
    private List<ParseObject> respostas;
    private ParseObject respostaAtual;

    final Drawable napoioVermelho = ContextCompat.getDrawable(AppController.getInstance().getApplicationContext(), R.mipmap.ic_action_unlike_red);
    final Drawable apoioVerde = ContextCompat.getDrawable(AppController.getInstance().getApplicationContext(), R.mipmap.ic_action_like_green);
    Drawable imgapoio = ContextCompat.getDrawable(AppController.getInstance().getApplicationContext(), R.mipmap.ic_action_like_gray);
    Drawable imgnapoio = ContextCompat.getDrawable(AppController.getInstance().getApplicationContext(), R.mipmap.ic_action_unlike_gray);


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *

     * @return A new instance of fragment DialogaVotoFragment.
     * @param nome
     * @param icone
     * @param background
     * @param idTema
     */
    public static DialogaListaPerguntasFragment newInstance(String nome, String icone, int background, String idTema) {
        DialogaListaPerguntasFragment fragment = new DialogaListaPerguntasFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NOME, nome);
        args.putString(ARG_ICONE, icone);
        args.putInt(ARG_COR, background);
        args.putString(ARG_IDTEMA,idTema);
        fragment.setArguments(args);
        return fragment;
    }

    public DialogaListaPerguntasFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            nome = getArguments().getString(ARG_NOME);
            icone = getArguments().getString(ARG_ICONE);
            cor = getArguments().getInt(ARG_COR);
            idTema = getArguments().getString(ARG_IDTEMA);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialoga_lista_pergunta, container, false);

        initDependencies();
        setupView(view);

        //busca a ultima pergunta ou a mais votada ou a que est� finalizando
        dialogaActions.getPerguntas(idTema);

        return view;
    }

    private void initDependencies() {
        actionsCreator = ActionsCreator.get();
        dialogaActions = DialogaActions.get();
    }


    private void setupView(View view) {
        pb = (ProgressBar) view.findViewById(R.id.progressBar5);
        TextView txtTema = (TextView) view.findViewById(R.id.txtNomeTema);
        txtTema.setText(nome);
        LinearLayout linearLayout = (LinearLayout)view.findViewById(R.id.linearLayout);
        linearLayout.setBackgroundResource(cor);
        ImageView imgIcone = (ImageView)view.findViewById(R.id.icone);
        imgIcone.setBackgroundResource(Tema.buscaIcone(this.icone));


        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_perguntas);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(false);


        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,
                StaggeredGridLayoutManager.VERTICAL));
        mAdapter = new PerguntaAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setRecyclerViewOnClickListenerHack(this);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle("Pergunta");
                alert.setMessage(AppController.getInstance().getString(R.string.qual_pergunta));
                LayoutInflater li = LayoutInflater.from(getActivity());
                View view2 = li.inflate(R.layout.dialog_insere_questao, null);
                final EditText input = (EditText) view2.findViewById(R.id.txtPergunta);

                alert.setView(view2);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String pergunta = input.getText().toString();
                        if (ParseUser.getCurrentUser() != null) {
                            if (!pergunta.isEmpty()) {
                                //inserir resposta para votacao
                                dialogaActions.enviarPergunta(pergunta, idTema);
                            } else {
                                Snackbar.make(getView(), "Insira uma pergunta", Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            }
                        } else {
                            Snackbar.make(view, "Para votar é necessário estar logado", Snackbar.LENGTH_LONG)
                                    .setAction("Logar", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(AppController.getInstance(),LoginActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            AppController.getInstance().startActivity(intent);
                                        }
                                    }).show();
                        }

                    }
                });

                alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });
                alert.show();

            }
        });

    }

    private void updateUI() {
        mAdapter.setItems(dialogaEvent.getPerguntas());
        pb.setVisibility(View.INVISIBLE);
    }



    /**
     * Atualiza a UI depois de uma action
     * @param event
     */
    @Subscribe
    public void onMessageEvent(DialogaEvent event){
        dialogaEvent=event;
        switch (dialogaEvent.getAction()){
            case DialogaActions.DIALOGA_GET_PERGUNTAS:
                updateUI();
                break;
            case DialogaActions.DIALOGA_ENVIAR_PERGUNTA:
                dialogaActions.getPerguntas(idTema);
                break;
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onClickListener(View view, int position) {
        ParseObject pergunta = dialogaEvent.getPerguntas().get(position);

        Answers.getInstance().logCustom(new CustomEvent("TouchPergunta")
                .putCustomAttribute("pergunta", pergunta.getObjectId()));

        DialogaVotoFragment frag = DialogaVotoFragment.newInstance(
                nome,icone,cor,idTema,pergunta.getObjectId());

        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment, frag, "dialogaVoto");
        ft.addToBackStack("tag");
        ft.commit();


    }

//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }

    //    @Override
    //    public void onDetach() {
    //        super.onDetach();
    //        mListener = null;
    //    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
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
