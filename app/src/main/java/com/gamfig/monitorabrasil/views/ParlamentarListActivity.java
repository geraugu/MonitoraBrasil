package com.gamfig.monitorabrasil.views;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.actions.ActionsCreator;
import com.gamfig.monitorabrasil.actions.PoliticoActions;
import com.gamfig.monitorabrasil.dispatcher.Dispatcher;
import com.gamfig.monitorabrasil.interfaces.RecyclerViewOnClickListenerHack;
import com.gamfig.monitorabrasil.stores.PoliticoStore;
import com.gamfig.monitorabrasil.views.adapters.PoliticoAdapter;
import com.gamfig.monitorabrasil.views.dialogs.DialogFiltro;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

/**
 * An activity representing a list of Parlamentares. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ParlamentarDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ParlamentarListFragment} and the item details
 * (if present) is a {@link ParlamentarDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link ParlamentarListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class ParlamentarListActivity extends AppCompatActivity
        implements  RecyclerViewOnClickListenerHack {

    private static final String TIPO_LISTA = "tipo_lista";
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private Dispatcher dispatcher;
    private ActionsCreator actionsCreator;
    private PoliticoStore politicoStore;
    private RecyclerView mRecyclerView;
    private PoliticoAdapter mAdapter;
    private ProgressBar pb;

    private ViewPager viewPager;
    private TabLayout tabLayout;

    private String casa;//camara ou senado
    private String ordem;

    private boolean realizouBusca;

    private boolean ranking;//se eh ranking ou nao

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parlamentar_app_bar);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Show the Up button in the action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        initDependencies();

        if(getIntent().getExtras() != null){
            //casa e ordem sao passados
            if(getIntent().getExtras().getString("casa") != null) {
                casa = getIntent().getExtras().getString("casa");
                actionsCreator.salvaNoSharedPreferences("casa",casa);
                ordem = getIntent().getExtras().getString("ordem");
                actionsCreator.salvaNoSharedPreferences("ordem",getIntent().getExtras().getString("ordem"));

                //limpa o filtro
                actionsCreator.salvaNoSharedPreferences("ufPosSelecionada","0");
                actionsCreator.salvaNoSharedPreferences("partidoPosSelecionada","0");
                actionsCreator.salvaNoSharedPreferences("anoPosSelecionada","0");
                actionsCreator.salvaNoSharedPreferences("categoriaPosSelecionada","0");
                actionsCreator.salvaNoSharedPreferences("ufSelecionada",null);
                actionsCreator.salvaNoSharedPreferences("partidoSelecionada",null);
                actionsCreator.salvaNoSharedPreferences("anoSelecionada",null);
                actionsCreator.salvaNoSharedPreferences("categoriaSelecionada",null);
            }
        }else {
            casa = actionsCreator.getValorSharedPreferences("casa");
            ordem = actionsCreator.getValorSharedPreferences("ordem");
        }
        Answers.getInstance().logCustom(new CustomEvent("Lista Politicos")
                .putCustomAttribute("casa", casa).putCustomAttribute("ordem", ordem));

        String titulo;
        if(casa.equals("c"))
            titulo="Deputados Federais";
        else
            titulo="Senadores";

        if(!ordem.equals("nome")) {
            titulo = String.format("Gastos-%s", titulo);
            ranking=true;
        }
        setTitle(titulo);

        setupView();
        if (findViewById(R.id.parlamentar_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((ParlamentarListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.parlamentar_list))
                    .setActivateOnItemClick(true);
        }

        actionsCreator.getAllPoliticos(casa,ordem);

    }


    private void initDependencies() {
        dispatcher = Dispatcher.get(new Bus());
        actionsCreator = ActionsCreator.get(dispatcher);
        politicoStore = PoliticoStore.get(dispatcher);
    }


    private void setupView() {

        pb = (ProgressBar)findViewById(R.id.progressBar2);
        pb.setVisibility(View.VISIBLE);

        //tableview
        mRecyclerView = (RecyclerView) findViewById(R.id.parlamentar_list);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);
        mAdapter = new PoliticoAdapter(actionsCreator,casa,ranking,this);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setRecyclerViewOnClickListenerHack(this);

        if(mTwoPane){
            viewPager = (ViewPager) findViewById(R.id.viewpager);
            // tabLayout = (TabLayout) findViewById(R.id.tabLayout);
            //fab = (FloatingActionButton) findViewById(R.id.fab);

        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_filter){
            //abrir o dialog para filtrar
            String tipo;
            if(ordem.equals("nome"))
                tipo = "politico";
            else
                tipo = "gasto";
            DialogFiltro filtro = DialogFiltro.newInstance("Escolha um filtro",tipo,casa);
            filtro.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    pb.setVisibility(View.VISIBLE);
                    actionsCreator.getAllPoliticos(casa,ordem);


                }
            });
            filtro.show(getSupportFragmentManager(), "dialogFiltro");
        }
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClickListener(View view, int position) {
        ParseObject politico = null;
        if(realizouBusca) {
            if (ranking) {
                ParseObject ranking = politicoStore.getPoliticosFiltro().get(position);
                try {
                    ranking.fetch();
                    politico = ranking.getParseObject("politico");
                    politico.fetchFromLocalDatastore();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {
                politico = politicoStore.getPoliticosFiltro().get(position);
            }
        }else {
            if (ranking){
                ParseObject ranking = politicoStore.getPoliticos().get(position);
                try {
                    ranking.fetch();
                    if(ranking.getString("nome")==null) {
                        politico = ranking.getParseObject("politico");
                    }else{
                        politico = ranking;
                    }
                    politico.fetchFromLocalDatastore();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }else{
                politico = politicoStore.getPoliticos().get(position);
            }
        }
        if (mTwoPane) {
//            Bundle arguments = new Bundle();
//            arguments.putString(VereadorDetailFragment.ID_POLITICO,politico.getObjectId());
//            arguments.putString(VereadorDetailFragment.ID_IMAGEM,politico.getString("cpf"));
//            arguments.putString(VereadorDetailFragment.NM_POLITICO,politico.getString("nome"));

//            setupViewPager(viewPager, politico.getObjectId());
//
//            fab.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Intent mIntent = new Intent(getApplicationContext(), ComentarioActivity.class);
//                    mIntent.putExtra(VereadorDetailFragment.ID_POLITICO, politico.getObjectId());
//                    mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(mIntent);
//                }
//            });


//            VereadorDetailFragment fragment = new VereadorDetailFragment();
//            fragment.setArguments(arguments);
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.proposições_detail_container, fragment)
//                    .commit();
        } else {
            Intent intent = new Intent(this, ParlamentarDetailActivity.class);
            intent.putExtra(ParlamentarDetailActivity.ID_POLITICO,politico.getObjectId());
            startActivity(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        dispatcher.register(this);
        dispatcher.register(politicoStore);
    }

    @Override
    public void onPause() {
        super.onPause();
        dispatcher.unregister(this);
        dispatcher.unregister(politicoStore);
    }

    private void updateUI() {
        mAdapter.setItems(politicoStore.getPoliticos());

        pb.setVisibility(View.INVISIBLE);
    }

    /**
     * Atualiza a UI depois de uma action
     * @param event
     */
    @Subscribe
    public void onTodoStoreChange(PoliticoStore.PoliticoStoreChangeEvent event) {
        switch (event.getEvento()) {
            case PoliticoActions.POLITICO_GET_ALL:
                if(event.getStatus().equals("erro")){

                }
                break;
        }
        updateUI();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_lista_parlamentares, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

                @Override
                public boolean onQueryTextSubmit(String query) {
                    if (!query.isEmpty()) {
                        //realizar a busca
                        mAdapter.setItems(politicoStore.filtrar(query, ranking));
                        mAdapter.notifyDataSetChanged();
                        realizouBusca = true;
                    }

                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if(realizouBusca && newText.isEmpty()){
                        mAdapter.setItems(politicoStore.getPoliticos());
                        mAdapter.notifyDataSetChanged();
                        realizouBusca = false;
                    }else{
                        //realizar a busca

                        mAdapter.setItems(politicoStore.filtrar(newText,ranking));
                        mAdapter.notifyDataSetChanged();
                        realizouBusca = true;
                    }
                    return false;
                }
            });
        }
        return super.onCreateOptionsMenu(menu);

    }

}
