package com.gamfig.monitorabrasil.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;
import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.actions.ActionsCreator;
import com.gamfig.monitorabrasil.application.AppController;
import com.gamfig.monitorabrasil.dispatcher.Dispatcher;
import com.gamfig.monitorabrasil.stores.PoliticoStore;
import com.gamfig.monitorabrasil.views.fragments.FichaFragment;
import com.gamfig.monitorabrasil.views.fragments.GastosFragment;
import com.gamfig.monitorabrasil.views.fragments.ProjetosFragment;
import com.gamfig.monitorabrasil.views.fragments.TwitterFragment;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

/**
 * An activity representing a single Parlamentar detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ParlamentarListActivity}.
 * <p/>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link ParlamentarDetailFragment}.
 */
public class ParlamentarDetailActivity extends AppCompatActivity {
    public static final String ID_POLITICO = "id_politico";
    private ParseObject politico;
    private Dispatcher dispatcher;
    private ActionsCreator actionsCreator;
    private PoliticoStore politicoStore;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parlamentar_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initDependencies();

        //busca o politico'
        politico = ParseObject.createWithoutData("Politico",getIntent().getExtras().getString(ID_POLITICO));
        try {
            politico.fetchFromLocalDatastore();
            setTitle(politico.getString( "nome"));
        } catch (ParseException e) {
            e.printStackTrace();
        }



        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("Ficha")
                .putContentType(politico.getString( "nome")));

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
//        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(mViewPager);
//        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.detail_tabs);
        tabLayout.setupWithViewPager(mViewPager);

        final FloatingActionButton fabComentario = (FloatingActionButton) findViewById(R.id.fabComentario);
        fabComentario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Answers.getInstance().logCustom(new CustomEvent("Comentario")
                        .putCustomAttribute("tela", "Politico Detalhe")
                        .putCustomAttribute("politico", politico.getString("nome")));

                Intent mIntent =new Intent(AppController.getInstance().getApplicationContext(), ComentarioActivity.class);
                mIntent.putExtra(ID_POLITICO,politico.getObjectId());
                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                AppController.getInstance().startActivity(mIntent);
            }
        });

        final boolean estaMonitorando = actionsCreator.estaMonitorando(politico);
        final FloatingActionButton fabMonitorar = (FloatingActionButton) findViewById(R.id.fabMonitorar);
        fabMonitorar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Answers.getInstance().logCustom(new CustomEvent("TouchMonitorar")
                        .putCustomAttribute("acao", String.valueOf(estaMonitorando))
                        .putCustomAttribute("politico", politico.getString("nome")));

                if(estaMonitorando){
                    fabMonitorar.setBackgroundResource(android.R.drawable.star_big_off);
                    Snackbar.make(view, "Agora vc está monitorando", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }else{
                    fabMonitorar.setBackgroundResource(android.R.drawable.star_big_on);
                    Snackbar.make(view, "Retirado de sua lista de monitoramento", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                try {
                    actionsCreator.salvaUsuarioPolitico(politico,true);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        });

        if(estaMonitorando){
            fabMonitorar.setBackgroundResource(android.R.drawable.star_big_on);
        }else{
            fabMonitorar.setBackgroundResource(android.R.drawable.star_big_off);
        }


        // Show the Up button in the action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(ParlamentarDetailFragment.ARG_ITEM_ID,
                    getIntent().getStringExtra(ParlamentarDetailFragment.ARG_ITEM_ID));
            ParlamentarDetailFragment fragment = new ParlamentarDetailFragment();
            fragment.setArguments(arguments);
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.parlamentar_detail_container, fragment)
//                    .commit();
        }
    }

    private void initDependencies() {
        dispatcher = Dispatcher.get(new Bus());
        actionsCreator = ActionsCreator.get(dispatcher);
        politicoStore = PoliticoStore.get(dispatcher);
    }


    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        //DADOS GERAIS
        FichaFragment fichaFragment = FichaFragment.newInstance(politico.getObjectId());
        adapter.addFrag(fichaFragment,"Geral");
        //GASTOS
        GastosFragment fragment = GastosFragment.newInstance(politico.getObjectId());
        adapter.addFrag(fragment, "Gastos");

        //TWITTER
        if(null != politico.getString("twitter")) {
            if(politico.getString("twitter").length() > 0) {
                TwitterFragment fragmentTwitter = TwitterFragment.newInstance(politico.getString("twitter"), null);
                adapter.addFrag(fragmentTwitter, "Twitter");
            }
        }

        //PROJETOS
        ProjetosFragment projetosFragment = ProjetosFragment.newInstance(politico.getObjectId());
        adapter.addFrag(projetosFragment, "Projetos");

        //HASHTAG

        //BENS



        viewPager.setAdapter(adapter);

//        ImageView imgHeader = (ImageView) findViewById(R.id.header);
//        String url = "https://twitter.com/"+twitter+"/profile_image?size=original";
//        AppController.getInstance().getmImagemLoader().displayImage(url, imgHeader);
    }

    static class ViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
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
            NavUtils.navigateUpTo(this, new Intent(this, ParlamentarListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



}
