package com.gamfig.monitorabrasil.views;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.gamfig.monitorabrasil.R;
import com.gamfig.monitorabrasil.actions.ActionsCreator;
import com.gamfig.monitorabrasil.actions.UserActions;
import com.gamfig.monitorabrasil.dispatcher.Dispatcher;
import com.gamfig.monitorabrasil.stores.UserStore;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class LoginActivity extends AppCompatActivity {

    private LinearLayout llTwitter;

    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText mNome;
    private View mProgressView;
    private Button btnFazerCadastro;
    private Button btnLogar;
    private Button btnLogout;
    private Button btnSalvar;
    private Switch mSwitchTwitter;
    private ImageView foto;
    private ParseUser currentUser;
    private ImageButton btnFoto;

    private Dispatcher dispatcher;
    private ActionsCreator actionsCreator;
    private UserStore userStore;

    private LinearLayout form;


    private final int PICK_IMAGE=2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initDependencies();
        setupView();
    }

    private void initDependencies() {
        dispatcher = Dispatcher.get(new Bus());
        actionsCreator = ActionsCreator.get(dispatcher);
        userStore = UserStore.get(dispatcher);
    }

    private void setupView() {
//        setupActionBar();

        llTwitter = (LinearLayout) findViewById(R.id.llTwitter);
        form = (LinearLayout) findViewById(R.id.email_login_form);

        mSwitchTwitter = (Switch) findViewById(R.id.swTwitter);

        //progressView
        mProgressView = findViewById(R.id.login_progress);

        //botoes
        btnSalvar = (Button) findViewById(R.id.btnSalvar);
        btnFoto = (ImageButton) findViewById(R.id.btnFoto);
        btnLogar = (Button) findViewById(R.id.email_sign_in_button);
        btnLogout = (Button) findViewById(R.id.btnLogout);

        //usuario atual
        currentUser = ParseUser.getCurrentUser();

        //atualizar usuario
        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress(true);
                atualizaCadastro();
            }
        });


        //evento: logar
        btnLogar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgress(true);
                logar();
            }
        });

        //evento: logout
        btnLogout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showProgress(true);
                logout();


            }
        });

        //Botao para fazer o cadastro
        btnFazerCadastro = (Button) findViewById(R.id.fazer_cadastro);
        btnFazerCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnFazerCadastro.getText().toString().equals("Fazer Cadastro")) {
                    mNome.setVisibility(View.VISIBLE);
                    btnLogar.setVisibility(View.GONE);
                    llTwitter.setVisibility(View.GONE);
                    btnFazerCadastro.setText("Cadastrar");
                    btnFoto.setVisibility(View.VISIBLE);
                    btnFoto.setImageResource(R.mipmap.ic_person);
                    View view = mNome;
                    view.requestFocus();
                } else {
                    if (validaCampos()) {
                        realizarCadastro();
                    }

                }

            }
        });

        //evento: Busca foto para colocar no perfil
        btnFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Selecione uma foto");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

                startActivityForResult(chooserIntent, PICK_IMAGE);
            }
        });

        mSwitchTwitter.setVisibility(View.INVISIBLE);

//        mSwitchTwitter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                final ParseUser user = ParseUser.getCurrentUser();
//                if (b) {
//                    if (!ParseTwitterUtils.isLinked(user)) {
//                        ParseTwitterUtils.link(user, getApplicationContext(), new SaveCallback() {
//                            @Override
//                            public void done(ParseException ex) {
//                                if (ParseTwitterUtils.isLinked(user)) {
//                                    Snackbar.make(form, "Woohoo!! Conta adicionada!", Snackbar.LENGTH_LONG)
//                                            .setAction("Action", null).show();
//                                }
//                            }
//                        });
//                    }
//                } else {
//                    ParseTwitterUtils.unlinkInBackground(user, new SaveCallback() {
//                        @Override
//                        public void done(ParseException ex) {
//                            if (ex == null) {
//                                if (ParseTwitterUtils.isLinked(user)) {
//                                    Snackbar.make(form, "Conta do Twitter removida", Snackbar.LENGTH_LONG)
//                                            .setAction("Action", null).show();
//                                }
//                            }
//                        }
//                    });
//                }
//            }
//        });


        //EditTextViews
        mNome = (EditText) findViewById(R.id.txtNome);
        mNome.setVisibility(View.GONE);

        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
//                if (id == R.id.login || id == EditorInfo.IME_NULL) {
//                    attemptLogin();
//                    return true;
//                }
                return false;
            }
        });

        //verifica se esta logado
        if (currentUser != null) {
            montaFormLogout();
        } else {
            montaFormLogin();
        }
    }

    /**
     * Verifica se esta logado
     */
    private void verificaLogin() {
        if(ParseUser.getCurrentUser()!=null){
            montaFormLogout();
        }else{
            montaFormLogin();
        }
    }


    /**
     * Realiza o logout
     */
    private void logout() {

        actionsCreator.logout();
    }

    /**
     * Monta o form de login
     */
    private void montaFormLogin() {

        mEmailView.setVisibility(View.VISIBLE);
        mPasswordView.setVisibility(View.VISIBLE);
        btnLogar.setVisibility(View.VISIBLE);
        btnFazerCadastro.setVisibility(View.VISIBLE);

        mNome.setVisibility(View.GONE);
        llTwitter.setVisibility(View.GONE);
        btnFoto.setVisibility(View.INVISIBLE);
        btnSalvar.setVisibility(View.GONE);
        btnLogout.setVisibility(View.GONE);
    }

    /**
     * Monta o form para logout
     */
    private void montaFormLogout() {

        ParseUser user = ParseUser.getCurrentUser();

        mPasswordView.setVisibility(View.GONE);
        btnLogar.setVisibility(View.GONE);
        btnFazerCadastro.setVisibility(View.GONE);

        mEmailView.setVisibility(View.VISIBLE);
        mEmailView.setText(user.getEmail());
        mNome.setVisibility(View.VISIBLE);
        mNome.setText(user.get("nome").toString());
        llTwitter.setVisibility(View.VISIBLE);
        btnFoto.setVisibility(View.VISIBLE);
        btnSalvar.setVisibility(View.VISIBLE);
        btnLogout.setVisibility(View.VISIBLE);
        showProgress(false);

        ParseFile foto = (ParseFile)user.get("foto");
        if(foto!=null){
            foto.getDataInBackground(new GetDataCallback() {
                public void done(byte[] data, ParseException e) {
                    if (e == null) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data , 0, data.length);
                        bitmap = Bitmap.createScaledBitmap(bitmap, 120, 120, true);
                        btnFoto.setImageBitmap(bitmap);

                    } else {
                        // something went wrong
                    }
                }
            });
        }


    }

    /**
     * Realiza o login
     */
    private void logar() {
        if(validaCampos()){
            actionsCreator.logar(getInputUsuario(), getInputSenha());
        }
    }

    private String getInputUsuario() {
        return mEmailView.getText().toString();
    }

    private String getInputSenha() {
        return mPasswordView.getText().toString();
    }

    /**
     * Valida os campos do formulario (login e cadastro)
     * @return
     */
    private boolean validaCampos() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mNome.setError(null);

        boolean valido = true;
        View focusView = null;
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String nome =mNome.getText().toString();

        if (mNome.getVisibility() == View.VISIBLE){
            if (TextUtils.isEmpty(nome)) {
                mNome.setError(getString(R.string.error_field_required));
                focusView = mNome;
                valido = false;
            }
        }


        // Checar senha foi preenchida
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            valido = false;
        }

        // checar se email eh valido
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            valido = false;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            valido = false;
        }
        if(!valido){
            focusView.requestFocus();
        }

        return valido;
    }

    private void atualizaCadastro() {
        //ParseFile
        final ParseFile mParseFile = new ParseFile("foto.png",buscaFoto());
        mParseFile.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                // If successful add file to user and signUpInBackground
                if (e == null) {
                    if(mParseFile!= null)
                        currentUser.put("foto", mParseFile);
                    currentUser.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            currentUser.setEmail(mEmailView.getText().toString());
                            currentUser.put("nome", mNome.getText().toString());
                            currentUser.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    showProgress(false);
                                    if (null == e) {
                                        Toast.makeText(LoginActivity.this, getString(R.string.cadastro_atualizado), Toast.LENGTH_LONG).show();
                                    } else {
                                        try {
                                            currentUser.fetch();
                                            mEmailView.setText(currentUser.getEmail());
                                        } catch (ParseException e1) {
                                            e1.printStackTrace();
                                        }
                                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    });
                } else {
                    Toast.makeText(LoginActivity.this,e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private byte[] buscaFoto(){
        Bitmap bitmap = ((BitmapDrawable)btnFoto.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return  byteArray;
    }


    /**
     * Atualiza a UI depois de uma action
     * @param event
     */
    @Subscribe
    public void onTodoStoreChange(UserStore.UserStoreChangeEvent event) {
        switch (userStore.getEvento()) {
            case UserActions.USER_CADASTRO:
                if(userStore.getStatus().equals("erro")){
                    showProgress(false);
                    Snackbar.make(form, "Houve um erro ao fazer seu cadastro", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                break;
            case UserActions.USER_LOGAR:
                if(userStore.getStatus().equals("erro")){
                    showProgress(false);
                    Snackbar.make(form, "Houve um erro ao fazer o login", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                break;
        }
        updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        dispatcher.register(this);
        dispatcher.register(userStore);
    }

    @Override
    protected void onPause() {
        super.onPause();
        dispatcher.unregister(this);
        dispatcher.unregister(userStore);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }
            try {
                Uri selectedImage = data.getData();
                InputStream imageStream = getContentResolver().openInputStream(selectedImage);
                Bitmap mImage = BitmapFactory.decodeStream(imageStream);
                btnFoto.setImageBitmap(Bitmap.createScaledBitmap(mImage, 120, 120, false));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }



    private void showProgress(boolean b) {
        if(b){
            mProgressView.setVisibility(View.VISIBLE);

        }else{
            mProgressView.setVisibility(View.GONE);
        }
    }




    private void realizarCadastro() {
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String nome =mNome.getText().toString();
        //foto
        Bitmap bitmap = ((BitmapDrawable)btnFoto.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        //ParseFile
        ParseFile mParseFile = new ParseFile("foto.png",byteArray);

        showProgress(true);
        actionsCreator.cadastrar(nome, password, email,mParseFile);
    }



    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
//    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        // toolbar.setNavigationIcon(R.drawable.ic_good);
        toolbar.setTitle("Login");

//        toolbar.setTitleTextColor(getResources().getColor(R.color.md_white_1000));
//        toolbar.setSubtitleTextColor(getResources().getColor(R.color.md_white_1000));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Realiza o login
     */
    public void attemptLogin() {
        showProgress(true);
        logar();
    }


    /**
     * Verifica se o email eh valido
     * @param email
     * @return
     */
    private boolean isEmailValid(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

    /**
     * verifica se a senha eh valida
     * @param password
     * @return
     */
    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }


    /**
     * Atualiza a UI
     */
    private void updateUI() {
        showProgress(false);
        verificaLogin();
    }



}
