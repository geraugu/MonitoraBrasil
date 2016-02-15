package com.gamfig.monitorabrasil.views;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.gamfig.monitorabrasil.POJO.ComentarioEvent;
import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.actions.ComentarioActions;
import com.gamfig.monitorabrasil.application.AppController;
import com.gamfig.monitorabrasil.views.adapters.ComentarioAdapter;
import com.parse.ParseUser;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class ComentarioActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private ComentarioAdapter mAdapter;
    private ProgressBar pb;
    private String tipo;
    private String idObjeto;

    private ComentarioActions actionsCreator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comentario);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initDependencies();
        setupView();

        pb.setVisibility(View.VISIBLE);
        if (getIntent().getStringExtra("tipo")!= null){
            //comentarios da primeira pg
            idObjeto = null;
            tipo = "Comentario";
        }else{
            if (getIntent().getStringExtra("projeto")!= null){
                idObjeto = getIntent().getStringExtra("projeto");
                tipo="ComentarioProjeto";

            }
            else{
                idObjeto = getIntent().getStringExtra(ParlamentarDetailActivity.ID_POLITICO);
                tipo="ComentarioPolitico";
            }
        }

        actionsCreator.getAllComentarios(tipo,idObjeto);

    }

    private void initDependencies() {
        actionsCreator = new ComentarioActions();
    }


    private void setupView() {



        pb = (ProgressBar) findViewById(R.id.progressBar);


        //tableview
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            //fazer a animacao
            TransitionInflater inflater = TransitionInflater.from(this);
            Transition transition = inflater.inflateTransition(R.transition.transitions);

            getWindow().setSharedElementEnterTransition(transition);
        }

        //botao para enviar mensagem
        Button btnEnviar = (Button)findViewById(R.id.btnEnviar);
        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarMensagem(v);
            }
        });



        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        llm.setReverseLayout(true);
        mRecyclerView.setLayoutManager(llm);
        mAdapter = new ComentarioAdapter();
        mRecyclerView.setAdapter(mAdapter);
    }

    private void enviarMensagem(final View v) {
        final EditText txtMensagem = (EditText)findViewById(R.id.mensagem);
        String mensagem = txtMensagem.getText().toString();
        if(ParseUser.getCurrentUser()!=null) {
            if (mensagem.isEmpty()) {
                Snackbar.make(v, getString(R.string.qual_e_mensagem), Snackbar.LENGTH_LONG).show();
            } else {
                actionsCreator.enviarMensagem(mensagem, tipo, idObjeto);
                txtMensagem.setText("");
            }
        }else{
            Snackbar.make(v, getString(R.string.precisa_logar), Snackbar.LENGTH_LONG)
                    .setAction("Logar", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(AppController.getInstance(),LoginActivity.class);
                            startActivity(intent);
                        }
                    }).show();
        }

    }

    private void updateUI(ComentarioEvent event) {
        mAdapter.setItems(event.comentarios);
        pb.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_comentario, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    // This method will be called when a MessageEvent is posted
    @Subscribe
    public void onMessageEvent(ComentarioEvent event){
        updateUI(event);
    }

    // This method will be called when a SomeOtherEvent is posted
//    @Subscribe
//    public void onEvent(SomeOtherEvent event){
//        doSomethingWith(event);
//    }

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

 /*   @Override
    public void onResume() {
        super.onResume();
        dispatcher.register(this);
        dispatcher.register(comentarioStore);
    }

    @Override
    public void onPause() {
        super.onPause();
        dispatcher.unregister(this);
        dispatcher.unregister(comentarioStore);
    }*/
}
