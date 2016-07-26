package com.gamfig.monitorabrasil.views;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.actions.ActionsCreator;
import com.gamfig.monitorabrasil.actions.PoliticoActions;
import com.gamfig.monitorabrasil.application.AppController;
import com.gamfig.monitorabrasil.views.dialogs.DialogGostou;
import com.gamfig.monitorabrasil.views.fragments.ContentMain;
import com.gamfig.monitorabrasil.views.fragments.ProjetosFragment;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.parse.ConfigCallback;
import com.parse.GetDataCallback;
import com.parse.ParseConfig;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    ActionsCreator actionsCreator;
    PoliticoActions politicoActions;

    private View headerView;
    private NavigationView navigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        headerView = getLayoutInflater().inflate(R.layout.nav_header_main, navigationView, false);
        navigationView.addHeaderView(headerView);
        initDependencies();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabComentario);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Answers.getInstance().logCustom(new CustomEvent("Comentario")
                        .putCustomAttribute("tela", "MainActivity"));

                Intent mIntent =new Intent(AppController.getInstance().getApplicationContext(), ComentarioActivity.class);
                mIntent.putExtra("tipo","principal");
                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                AppController.getInstance().startActivity(mIntent);
            }
        });
        verificaPush();

        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(it);
            }
        });

        //se primeira vez, buscar os politico
        ParseConfig.getInBackground(new ConfigCallback() {
            @Override
            public void done(ParseConfig config, ParseException e) {
                if(config.getString("dtAtualizacaoPoliltico")!=null){
                    String data = config.getString("dtAtualizacaoPoliltico");
                    if(actionsCreator.getValorSharedPreferences("dataAtualizacao")==null) {
                        actionsCreator.salvaNoSharedPreferences("dataAtualizacao", data);
                        politicoActions.getAllPoliticos();
                    }else{
                        if(!actionsCreator.getValorSharedPreferences("dataAtualizacao").equals(data)){
                            actionsCreator.salvaNoSharedPreferences("dataAtualizacao",data);
                            politicoActions.getAllPoliticos();
                        }
                    }
                }

            }
        });

        //setup headerview
        if(ParseUser.getCurrentUser()!=null) {
            setupHeader();
        }
        ContentMain frag = ContentMain.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if(getSupportFragmentManager().findFragmentByTag("contentMain")==null)
            ft.add(R.id.fragment_container, frag, "contentMain");
        else
            ft.replace(R.id.fragment_container, frag, "contentMain");
        ft.commit();

    }

    private void initDependencies() {
        actionsCreator = ActionsCreator.get();
        politicoActions = PoliticoActions.get();
    }

    private void verificaPush() {
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            if(extras.getString("tipo") != null) {
                String pushTipo = extras.getString("tipo");
                Intent intent;
                switch (pushTipo) {
                    case "dialoga":
                        intent = new Intent(this, DialogaActivity.class);

                        intent.putExtra("perguntaId", extras.getString("idPergunta"));
                        intent.putExtra("temaId", extras.getString("idTema"));
                        startActivity(intent);
                        break;
                    case "projeto":
                        intent = new Intent(this, ProjetoDetailActivity.class);
                        intent.putExtra(ProjetoDetailFragment.ARG_ITEM_ID, String.valueOf(extras.getInt(ProjetoDetailFragment.ARG_ITEM_ID)));
                        intent.putExtra(ProjetoDetailFragment.ARG_CASA, extras.getString(ProjetoDetailFragment.ARG_CASA));

                        startActivity(intent);
                        break;
                }
            }

        }
    }

    private void setupHeader() {
        TextView mNome = (TextView)headerView.findViewById(R.id.txtNome);
        ParseUser user = ParseUser.getCurrentUser();
        mNome.setText(user.getString("nome"));
        TextView mEmail = (TextView)headerView.findViewById(R.id.txtEmail);
        mEmail.setText(user.getEmail());

        if(user.getString("image_url") != null){
            ImageView img = (ImageView)headerView.findViewById(R.id.imgPerfil);
            DisplayImageOptions mDisplayImageOptions = new DisplayImageOptions.Builder().displayer(new RoundedBitmapDisplayer(100)).cacheInMemory(true).build();
            String url = "https://twitter.com/"+user.getUsername()+"/profile_image?size=normal";
            AppController.getInstance().getmImagemLoader().displayImage(user.getString("image_url"), img,mDisplayImageOptions);
        }

        ParseFile foto = (ParseFile)user.get("foto");
        if(foto!=null){
            foto.getDataInBackground(new GetDataCallback() {
                public void done(byte[] data, ParseException e) {
                    if (e == null) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        bitmap = Bitmap.createScaledBitmap(bitmap, 120, 120, true);
                        ImageView img = (ImageView) headerView.findViewById(R.id.imgPerfil);
                        img.setImageBitmap(bitmap);

                    } else {
                        // something went wrong
                    }
                }
            });
        }

        // headerView.setBackground(ContextCompat.getDrawable(this, Usuario.buscaImagemTopo()));
    }



    @Override
    public void onResume() {
        super.onResume();
        if(ParseUser.getCurrentUser()!=null)
            setupHeader();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START) || getFragmentManager().getBackStackEntryCount() > 0) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            }
            if (getFragmentManager().getBackStackEntryCount() > 0 ){
                getFragmentManager().popBackStack();
            }
        } else {
            super.onBackPressed();
        }
    }

  /*  @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.msg_compartilhe));
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_dialoga) {
            Intent intent = new Intent(this,DialogaActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_deputados) {
            Intent intent = new Intent(this,ParlamentarListActivity.class);
            intent.putExtra("casa","c");
            intent.putExtra("ordem","nome");
            startActivity(intent);

        }else if (id == R.id.nav_home) {
            ContentMain frag = ContentMain.newInstance();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, frag, "projetosFragment");
            ft.commit();

        }else if (id == R.id.nav_projetos_monitorados) {
            ProjetosFragment frag = ProjetosFragment.newInstance(null);
            frag.setArguments(getIntent().getExtras());
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, frag, "projetosFragment");
            ft.addToBackStack(null);
            ft.commit();

        } else if (id == R.id.nav_cotas) {
            Intent intent = new Intent(this,ParlamentarListActivity.class);
            intent.putExtra("casa","c");
            intent.putExtra("ordem","gastos");
            startActivity(intent);

        } else if (id == R.id.nav_senadores) {
            Intent intent = new Intent(this,ParlamentarListActivity.class);
            intent.putExtra("casa","s");
            intent.putExtra("ordem","nome");
            startActivity(intent);
        }else if (id == R.id.nav_cotas_senado) {
            Intent intent = new Intent(this,ParlamentarListActivity.class);
            intent.putExtra("casa","s");
            intent.putExtra("ordem","gastos");
            startActivity(intent);
        }else if (id == R.id.nav_gostou) {
            DialogFragment dialog = new DialogGostou();
            dialog.show(getSupportFragmentManager(), "dialog");
        }else if (id == R.id.nav_sobre) {
            Intent intent = new Intent(this,SobreActivity.class);
            startActivity(intent);
        }else if (id == R.id.nav_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.msg_compartilhe));
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }
        /*else if (id == R.id.nav_super_cidadao) {
            Intent sendIntent = new Intent(this,TrunfoMain.class);
            startActivity(sendIntent);
        }*/


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
