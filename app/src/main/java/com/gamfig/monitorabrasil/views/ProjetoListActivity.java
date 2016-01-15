package com.gamfig.monitorabrasil.views;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.actions.ActionsCreator;
import com.gamfig.monitorabrasil.dispatcher.Dispatcher;
import com.gamfig.monitorabrasil.interfaces.RecyclerViewOnClickListenerHack;
import com.gamfig.monitorabrasil.stores.ProjetoStore;
import com.gamfig.monitorabrasil.views.adapters.ProjetoAdapter;
import com.gamfig.monitorabrasil.views.dialogs.DialogFiltro;
import com.parse.ParseObject;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

/**
 * An activity representing a list of Projetos. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ProjetoDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ProjetoListFragment} and the item details
 * (if present) is a {@link ProjetoDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link ProjetoListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class ProjetoListActivity extends AppCompatActivity
        implements RecyclerViewOnClickListenerHack {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private Dispatcher dispatcher;
    private ActionsCreator actionsCreator;
    private ProjetoStore projetoStore;
    private RecyclerView mRecyclerView;
    private ProjetoAdapter mAdapter;

    private String casa;


    private boolean realizouBusca;
    private int previousTotal = 0;
    private boolean loading = true;
    private int visibleThreshold = 2;
    int firstVisibleItem, visibleItemCount, totalItemCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parlamentar_app_bar);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());


        // Show the Up button in the action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initDependencies();
        if(getIntent().getExtras() != null){
            //casa (camara ou senado)
            if(getIntent().getExtras().getString("casa") != null) {
                casa = getIntent().getExtras().getString("casa");
                actionsCreator.salvaNoSharedPreferences("casa",casa);
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
        }
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("Lista Projetos")
                .putContentType("todos projetos")
                .putContentId(casa));




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

        actionsCreator.getAllProjetos(null,casa, 0);

    }


    private void initDependencies() {
        dispatcher = Dispatcher.get(new Bus());
        actionsCreator = ActionsCreator.get(dispatcher);
        projetoStore = ProjetoStore.get(dispatcher);
    }


    private void setupView() {

        //tableview
        mRecyclerView = (RecyclerView) findViewById(R.id.parlamentar_list);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        final LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);
        mAdapter = new ProjetoAdapter(actionsCreator);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setRecyclerViewOnClickListenerHack(this);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                visibleItemCount = mRecyclerView.getChildCount();
                totalItemCount = llm.getItemCount();
                firstVisibleItem = llm.findFirstVisibleItemPosition();

                if (loading) {
                    if (totalItemCount > previousTotal) {
                        loading = false;
                        previousTotal = totalItemCount;
                    }
                }
                if (!loading && (totalItemCount - visibleItemCount)
                        <= (firstVisibleItem + visibleThreshold) && totalItemCount > 14) {
                    //carregar mais projetos
                    actionsCreator.getAllProjetos(null,casa, previousTotal);

                    loading = true;
                }
            }
        });

        if(mTwoPane){
            //viewPager = (ViewPager) findViewById(R.id.viewpager);
            // tabLayout = (TabLayout) findViewById(R.id.tabLayout);
            //fab = (FloatingActionButton) findViewById(R.id.fab);

        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_filter){
            //abrir o dialog para filtrar
            DialogFiltro filtro = DialogFiltro.newInstance("Escolha um filtro","projeto");
            filtro.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    //realizar a busca por uf do autor, partido, ano ou casa
                    projetoStore.limpaProjetos();
                    actionsCreator.getAllProjetos(null,casa, 0);


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
        final ParseObject projeto = projetoStore.getProjetos().get(position);
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
//            Intent intent = new Intent(this, ParlamentarDetailActivity.class);
//            intent.putExtra(ParlamentarDetailActivity.ID_POLITICO,projeto.getObjectId());
//            startActivity(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        dispatcher.register(this);
        dispatcher.register(projetoStore);
    }

    @Override
    public void onPause() {
        super.onPause();
        dispatcher.unregister(this);
        dispatcher.unregister(projetoStore);
    }

    private void updateUI() {
        mAdapter.setItems(projetoStore.getProjetos());
    }

    /**
     * Atualiza a UI depois de uma action
     * @param event
     */
    @Subscribe
    public void onTodoStoreChange(ProjetoStore.ProjetoStoreChangeEvent event) {

        updateUI();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_lista_projetos, menu);

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
                        projetoStore.limpaProjetos();
                        actionsCreator.buscaProjetoPorPalavra(query,casa);
                        realizouBusca = true;
                    }

                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if(realizouBusca && newText.isEmpty()){
                        projetoStore.limpaProjetos();
                        actionsCreator.getAllProjetos(null,casa, 0);
                        realizouBusca = false;
                    }
                    return false;
                }
            });
        }
        return super.onCreateOptionsMenu(menu);

    }
}
