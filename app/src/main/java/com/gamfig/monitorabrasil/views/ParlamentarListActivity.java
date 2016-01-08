package com.gamfig.monitorabrasil.views;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.actions.ActionsCreator;
import com.gamfig.monitorabrasil.actions.PoliticoActions;
import com.gamfig.monitorabrasil.dispatcher.Dispatcher;
import com.gamfig.monitorabrasil.interfaces.RecyclerViewOnClickListenerHack;
import com.gamfig.monitorabrasil.stores.PoliticoStore;
import com.gamfig.monitorabrasil.views.adapters.PoliticoAdapter;
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

    private ViewPager viewPager;
    private TabLayout tabLayout;

    private String tipo;//camara ou senado

    private boolean realizouBusca;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parlamentar_app_bar);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        // Show the Up button in the action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("Lista Politicos")
                .putContentType(tipo));

        initDependencies();

        if(getIntent().getExtras() != null){
            if(getIntent().getExtras().getString("tipo") != null) {
                tipo = getIntent().getExtras().getString("tipo");
                actionsCreator.salvaTipoPolitico(tipo);
            }
        }else {
            tipo = actionsCreator.getTipoPolitico();
        }

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

        actionsCreator.getAllPoliticos(tipo);

        // TODO: If exposing deep links into your app, handle intents here.
    }


    private void initDependencies() {
        dispatcher = Dispatcher.get(new Bus());
        actionsCreator = ActionsCreator.get(dispatcher);
        politicoStore = PoliticoStore.get(dispatcher);
    }


    private void setupView() {

        //tableview
        mRecyclerView = (RecyclerView) findViewById(R.id.parlamentar_list);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);
        mAdapter = new PoliticoAdapter(actionsCreator,tipo);
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
        final ParseObject politico = politicoStore.getPoliticos().get(position);
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
                        mAdapter.setItems(politicoStore.filtrar(query));
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

                        mAdapter.setItems(politicoStore.filtrar(newText));
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
