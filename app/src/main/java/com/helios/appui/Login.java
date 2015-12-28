package com.helios.appui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.helios.appui.app.AppConfig;
import com.helios.appui.app.AppController;
import com.helios.appui.helper.SQLiteHandler;
import com.helios.appui.helper.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {

    private static final String TAG = Login.class.getSimpleName();

    private EditText usrName, pass;
    private TextInputLayout inputLayoutEmail, inputLayoutPass;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;

    private boolean isGuest = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make Full-Screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Set Layout
        setContentView(R.layout.activity_login);

        //Setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Setup Floating Action Button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Not a member?", Snackbar.LENGTH_SHORT)
                        .setAction("REGISTER", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startActivity(new Intent(Login.this, Register.class));
                            }
                        })
                        .setActionTextColor(Color.RED)
                        .show();
            }
        });
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Setup data members

        setupDataMembers();

        //Setup floating labels
        setupFloatingLabels();

    }

    public void openHome(View view)
    {
        isGuest = true;
        Intent intent = new Intent(this, Home.class);
        intent.putExtra("guest?", isGuest);
        startActivity(intent);
    }

    public void setupDataMembers() {
        // Edit Text
        usrName = (EditText) findViewById(R.id.nameField);
        pass = (EditText) findViewById(R.id.passField);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SQLite db handler
        db = new SQLiteHandler(getApplicationContext());

        //Session manager
        session = new SessionManager(getApplicationContext());

        // Check if user is already logged in or not.
        if (session.isLoggedIn()) {
            // Let's GO!!!!!
            startActivity(new Intent(this, Home.class));
            finish();
        }
    }

    public void prepareLogin(View view) {
        String email = usrName.getText().toString().trim();
        String password = pass.getText().toString().trim();

        // Check for empty data in the form
        if (!email.isEmpty() && !password.isEmpty()) {
            if(validateAll())
                // login user
                checkLogin(email, password);
        } else {
            // Prompt user to enter credentials
            Toast.makeText(getApplicationContext(),
                    "Please enter the credentials!", Toast.LENGTH_LONG)
                    .show();
        }
    }

    public void checkLogin(final String email, final String password) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        pDialog.setMessage("Logging in ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_LOGIN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        // user successfully logged in
                        // Create login session
                        session.setLogin(true);

                        // Now store the user in SQLite
                        String uid = jObj.getString("uid");

                        JSONObject user = jObj.getJSONObject("user");
                        String name = user.getString("name");
                        String email = user.getString("email");
                        String created_at = user
                                .getString("created_at");

                        // Inserting row in users table
                        db.addUser(name, email, uid, created_at);

                        // Launch home activity
                        isGuest = false;
                        Intent intent = new Intent(Login.this, Home.class);
                        intent.putExtra("guest?", isGuest);
                        startActivity(intent);
                        finish();
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("password", password);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();


    }

    public void setupFloatingLabels() {

        inputLayoutEmail = (TextInputLayout) findViewById(R.id.input_layout_email);
        inputLayoutPass = (TextInputLayout) findViewById(R.id.input_layout_password);

        usrName.addTextChangedListener(new MyTextWatcher(usrName));
        pass.addTextChangedListener(new MyTextWatcher(pass));
    }

    public boolean validateAll() {
        if(!validateEmail() && !validatePassword())
            return false;
        return true;
    }

    public boolean validateEmail() {
        String email = usrName.getText().toString().trim();

        if (email.isEmpty() || !isValidEmail(email)) {
            inputLayoutEmail.setError(getString(R.string.err_msg_email));
            requestFocus(usrName);
            return false;
        } else {
            inputLayoutEmail.setErrorEnabled(false);
        }
        return true;
    }

    public boolean validatePassword() {
        if (pass.getText().toString().trim().isEmpty()) {
            inputLayoutPass.setError(getString(R.string.err_msg_password));
            requestFocus(pass);
            return false;
        } else {
            inputLayoutPass.setErrorEnabled(false);
        }

        return true;
    }

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }


    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch(view.getId()) {
                case R.id.nameField:
                    validateEmail();
                    break;
                case R.id.passField:
                    validatePassword();
                    break;
            }
        }
    }



}
