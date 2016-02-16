package com.gamfig.monitorabrasil.views.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.gamfig.monitorabrasil.POJO.DialogaEvent;
import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.actions.ActionsCreator;
import com.gamfig.monitorabrasil.actions.DialogaActions;
import com.gamfig.monitorabrasil.application.AppController;
import com.gamfig.monitorabrasil.model.Tema;
import com.gamfig.monitorabrasil.views.LoginActivity;
import com.gamfig.monitorabrasil.views.adapters.ResultadoAdapter;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DialogaVotoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DialogaVotoFragment extends Fragment {

    private static final String ARG_NOME = "nome";
    private static final String ARG_ICONE = "icone";
    private static final String ARG_COR = "cor";
    private static final String ARG_IDTEMA = "idTema";
    private static final String ARG_IDPERGUNTA = "idPergunta";


    private String nome;
    private String icone;
    private int cor;
    private String idTema;
    private String idPergunta;


    private ActionsCreator actionsCreator;
    private DialogaActions dialogaActions;
    private DialogaEvent dialogaEvent;

    private OnFragmentInteractionListener mListener;

    private  TextView txtPergunta;
    private CardView cardViewResposta;
    private TextView txtResposta;
    //botoes
    private ImageButton btnConcordo;
    private ImageButton btnDiscordo;
    private Button btnResultado;
    private Button btnProxima;
    private Button btnInsereResposta;

    //RelativeLayout
    private RelativeLayout rlResultado;
    private RelativeLayout rlVoto;

    private RecyclerView resultadosRC;

    private ResultadoAdapter mAdapter;

    private ProgressBar pb;

    private FloatingActionButton fab;

    private Switch aSwitch;//receber push qndo inserir uma resposta

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
    public static DialogaVotoFragment newInstance(String nome, String icone, int background, String idTema, String idPergunta) {
        DialogaVotoFragment fragment = new DialogaVotoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NOME, nome);
        args.putString(ARG_ICONE, icone);
        args.putInt(ARG_COR, background);
        args.putString(ARG_IDTEMA,idTema);
        args.putString(ARG_IDPERGUNTA,idPergunta);
        fragment.setArguments(args);
        return fragment;
    }

    public static DialogaVotoFragment newInstance(ParseObject tema, String perguntaId) {
        DialogaVotoFragment fragment = new DialogaVotoFragment();
        Bundle args = new Bundle();
        if(tema != null) {
            args.putString(ARG_NOME, tema.getString("Nome"));
            args.putString(ARG_ICONE, tema.getString("imagem"));
            args.putInt(ARG_COR, Tema.buscaCor(tema.getString("imagem")));
            args.putString(ARG_IDTEMA, tema.getObjectId());
            args.putString(ARG_IDPERGUNTA, perguntaId);
            fragment.setArguments(args);
        }
        return fragment;
    }

    public DialogaVotoFragment() {
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
            idPergunta = getArguments().getString(ARG_IDPERGUNTA);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialoga_vota_proposta, container, false);

        initDependencies();
        setupView(view);

        //busca a ultima pergunta ou a mais votada ou a que est� finalizando
        dialogaActions.getPerguntaRespostas(idPergunta);

        return view;
    }

    private void initDependencies() {
        actionsCreator = ActionsCreator.get();
        dialogaActions = DialogaActions.get();
    }


    private void setupView(View view) {

        TextView txtTema = (TextView) view.findViewById(R.id.txtNomeTema);
        txtTema.setText(nome);
        LinearLayout linearLayout = (LinearLayout)view.findViewById(R.id.linearLayout);
        linearLayout.setBackgroundResource(cor);
        ImageView imgIcone = (ImageView)view.findViewById(R.id.icone);
        imgIcone.setBackgroundResource(Tema.buscaIcone(this.icone));

        pb = (ProgressBar) view.findViewById(R.id.progressBar3);
        pb.setVisibility(View.VISIBLE);

        //RecyclerView
        resultadosRC = (RecyclerView) view.findViewById(R.id.my_recycler_view);
        resultadosRC.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        resultadosRC.setLayoutManager(llm);
        mAdapter = new ResultadoAdapter(actionsCreator);
        resultadosRC.setAdapter(mAdapter);

        txtPergunta = (TextView)view.findViewById(R.id.txtPergunta);
        cardViewResposta = (CardView) view.findViewById(R.id.cardViewOpniao);
        txtResposta = (TextView) view.findViewById(R.id.txtResposta);

        //botoes
        btnConcordo = (ImageButton) view.findViewById(R.id.btnConcordo);
        btnDiscordo = (ImageButton) view.findViewById(R.id.btnDiscordo);
        btnInsereResposta = (Button) view.findViewById(R.id.btnInserirOpniao);
        btnProxima = (Button) view.findViewById(R.id.btnProxima);
        btnResultado = (Button) view.findViewById(R.id.btnResultado);

        //relativeLayout
        rlResultado = (RelativeLayout)view.findViewById(R.id.rlResultado);
        rlVoto = (RelativeLayout)view.findViewById(R.id.rlVoto);

        //acompanhar switch
        aSwitch = (Switch) view.findViewById(R.id.switch1);




        //actions dos botoes

        btnInsereResposta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                //TODO mostrar a caixa para inserir a resposta
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle("Opine");
                alert.setMessage(AppController.getInstance().getString(R.string.diga_oq_pensa));
                // Create TextView
                final EditText input = new EditText (getActivity());
                alert.setView(input);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String resposta = input.getText().toString();
                        if(ParseUser.getCurrentUser() != null){
                            if(!resposta.isEmpty()){
                                //inserir resposta para votacao
                                dialogaActions.enviarResposta(resposta,pergunta);
                            }else{
                                Snackbar.make(getView(), "Insira um resposta", Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            }
                        }else{
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

        btnProxima.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO mostrar a proxima resposta se houver
                //verificar se eh a ultima resposta
                if(respostas != null) {
                    int qtdRespostas = respostas.size();
                    int posicaoRespAtual = respostas.lastIndexOf(respostaAtual);
                    if (posicaoRespAtual == (qtdRespostas - 1)) {
                        btnProxima.setVisibility(View.GONE);
                        btnInsereResposta.setVisibility(View.VISIBLE);
                        btnDiscordo.setVisibility(View.GONE);
                        btnConcordo.setVisibility(View.GONE);
                        txtResposta.setText(AppController.getInstance().getString(R.string.nao_ha_opnioes));
                    } else {
                        respostaAtual = respostas.get(posicaoRespAtual + 1);
                        txtResposta.setText(respostaAtual.getString("texto"));
                        btnConcordo.setBackground(imgapoio);
                        btnDiscordo.setBackground(imgnapoio);
                        //verifica se ja tem voto
                        ParseObject voto = dialogaActions.getVoto(respostaAtual);

                        if (null != voto) {
                            if (voto.getString("sim_nao").equals("s")) {
                                btnConcordo.setBackground(apoioVerde);
                            } else {
                                btnDiscordo.setBackground(napoioVermelho);
                            }
                        }
                    }
                }
            }
        });

        btnResultado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO mostrar o resultado dos votos das respostas por ordem de mais concordancia
                rlVoto.setVisibility(View.GONE);
                rlResultado.setVisibility(View.VISIBLE);
                dialogaActions.getResultado(pergunta);
                pb.setVisibility(View.VISIBLE);
                fab.setVisibility(View.VISIBLE);
            }
        });

        btnConcordo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(null != ParseUser.getCurrentUser()){
                    //verifica se ja vatou
                    ParseObject voto = dialogaActions.getVoto(respostaAtual);
                    if(voto == null){
                        dialogaActions.concordo(respostaAtual, voto);
                    }else{
                        if(voto.getString("sim_nao").equals("n")){

                            btnDiscordo.setBackground(imgnapoio);
                            dialogaActions.concordo(respostaAtual,voto);
                        }
                    }
                    btnConcordo.setBackground(apoioVerde);


                }else{
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

        btnDiscordo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(null != ParseUser.getCurrentUser()){//verifica se ja vatou
                    //verifica se ja vatou
                    ParseObject voto = dialogaActions.getVoto(respostaAtual);
                    if(voto == null){
                        dialogaActions.discordo(respostaAtual, voto);
                    }else{
                        if(voto.getString("sim_nao").equals("s")){

                            btnConcordo.setBackground(imgapoio);
                            dialogaActions.discordo(respostaAtual, voto);
                        }
                    }
                    btnDiscordo.setBackground(napoioVermelho);

                }else{
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
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setVisibility(View.INVISIBLE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody;
                if(pergunta != null){
                    shareBody = pergunta.getString("texto")
                            +" https://play.google.com/store/apps/details?id=com.monitorabrasil.participacidadao #monitorabrasil";
                }else {
                    shareBody = "Recomendo o app " + AppController.getInstance().getString(R.string.app_name)
                            + " https://play.google.com/store/apps/details?id=com.monitorabrasil.participacidadao";
                }
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, AppController.getInstance().getString(R.string.app_name));
                sharingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Compartilhar via"));

            }
        });
    }

    private void updateUI() {
        pergunta = dialogaEvent.getPerguntaResposta().getPergunta();
        respostas = dialogaEvent.getPerguntaResposta().getRespostas();
        if(pergunta != null){
            txtPergunta.setText(pergunta.getString("texto"));

            if(respostas.size() > 0){
                respostaAtual = respostas.get(0);
                txtResposta.setText(respostaAtual.getString("texto"));

                btnInsereResposta.setVisibility(View.GONE);
                btnProxima.setVisibility(View.VISIBLE);
                btnResultado.setVisibility(View.VISIBLE);
                btnConcordo.setVisibility(View.VISIBLE);
                btnDiscordo.setVisibility(View.VISIBLE);
                //verifica se ja tem voto
                ParseObject voto = dialogaActions.getVoto(respostaAtual);

                if(null != voto){
                    if(voto.getString("sim_nao").equals("s")){
                        btnConcordo.setBackground(apoioVerde);
                    }else{
                        btnDiscordo.setBackground(napoioVermelho);
                    }
                }



            }else{
                //mostrar opcao para inserir uma resposta
                btnInsereResposta.setVisibility(View.VISIBLE);
                btnProxima.setVisibility(View.GONE);
                btnResultado.setVisibility(View.GONE);
                btnConcordo.setVisibility(View.GONE);
                btnDiscordo.setVisibility(View.GONE);

                txtResposta.setText("Seja o primeiro a opinar!");
            }
        }else{


        }

        ParseInstallation.getCurrentInstallation().fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if(e == null){
                    List<String> canais = ParseInstallation.getCurrentInstallation().getList("channels");
                    if(canais != null)
                        if(canais.contains("p_"+pergunta.getObjectId())){
                            aSwitch.setChecked(true);
                        }else{
                            aSwitch.setChecked(false);
                        }
                }

            }
        });


        //switch para acompanhar o codigo
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                String channel = "p_"+pergunta.getObjectId();
                if (b) {
                    ParsePush.subscribeInBackground(channel, new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e != null){
                                Log.i("participa",e.toString());
                            }
                        }
                    });
                } else {
                    ParsePush.unsubscribeInBackground(channel);

                }
            }
        });




        pb.setVisibility(View.GONE);
    }

    private void carregaLista() {
        mAdapter.setItems(dialogaEvent.getResultado());
        pb.setVisibility(View.GONE);
    }

    /**
     * Atualiza a UI depois de uma action
     * @param event
     */
    @Subscribe
    public void onMessageEvent(DialogaEvent event){
        dialogaEvent = event;
        switch (event.getAction()){
            case DialogaActions.DIALOGA_GET_PERGUNTA_RESPOSTAS:
                updateUI();
                break;
            case DialogaActions.DIALOGA_ENVIAR_RESPOSTA:
                dialogaActions.getPerguntaRespostas(idPergunta);
                Snackbar.make(getView(), "Resposta inserida ;)", Snackbar.LENGTH_LONG)
                        .show();
                break;
            case DialogaActions.DIALOGA_GET_RESULTADO:
                carregaLista();
                break;
        }
    }



    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
