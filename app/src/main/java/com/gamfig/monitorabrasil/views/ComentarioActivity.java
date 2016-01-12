package com.gamfig.monitorabrasil.views;


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

import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.actions.ActionsCreator;
import com.gamfig.monitorabrasil.actions.ComentarioActions;
import com.gamfig.monitorabrasil.dispatcher.Dispatcher;
import com.gamfig.monitorabrasil.stores.ComentarioStore;
import com.gamfig.monitorabrasil.views.adapters.ComentarioAdapter;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

public class ComentarioActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private ComentarioAdapter mAdapter;
    private ProgressBar pb;
    private String tipo;
    private String idObjeto;

    private Dispatcher dispatcher;
    private ActionsCreator actionsCreator;
    private ComentarioStore comentarioStore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comentario);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initDependencies();
        setupView();

        pb.setVisibility(View.VISIBLE);

        if (getIntent().getStringExtra("projeto")== null){
            idObjeto = getIntent().getStringExtra("projeto");
            tipo="ComentarioProjeto";

        }
        else{
            idObjeto = getIntent().getStringExtra(ParlamentarDetailActivity.ID_POLITICO);
            tipo="ComentarioPolitico";
        }
        actionsCreator.getAllComentarios(tipo,idObjeto);

    }

    private void initDependencies() {
        dispatcher = Dispatcher.get(new Bus());
        actionsCreator = ActionsCreator.get(dispatcher);
        comentarioStore = ComentarioStore.get(dispatcher);
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
        mAdapter = new ComentarioAdapter(actionsCreator);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void enviarMensagem(final View v) {
        final EditText txtMensagem = (EditText)findViewById(R.id.mensagem);
        String mensagem = txtMensagem.getText().toString();
        if(mensagem.isEmpty()){
            Snackbar.make(v,getString(R.string.qual_e_mensagem), Snackbar.LENGTH_LONG).show();
        }else{
            actionsCreator.enviarMensagem(mensagem,tipo,idObjeto);
            txtMensagem.setText("");
        }

    }

    private void updateUI() {
        mAdapter.setItems(comentarioStore.getComentarios());
        pb.setVisibility(View.INVISIBLE);
    }

    /**
     * Atualiza a UI depois de uma action
     * @param event
     */
    @Subscribe
    public void onTodoStoreChange(ComentarioStore.ComentarioStoreChangeEvent event) {
        String evento = comentarioStore.getEvento();
        switch (evento){
            case ComentarioActions.COMENTARIO_ENVIAR:
                actionsCreator.getAllComentarios(tipo,idObjeto);
                break;
            case ComentarioActions.COMENTARIO_GET_ALL:
                updateUI();
                break;
        }
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

    @Override
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
    }
}
