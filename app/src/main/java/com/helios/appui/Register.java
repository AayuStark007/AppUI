package com.helios.appui;

import android.app.ProgressDialog;
import android.content.Intent;
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

public class Register extends AppCompatActivity {

    private static final String TAG = Register.class.getSimpleName();

    private EditText regNameField, emailField, regPassField;
    private TextInputLayout regLayoutName, regLayoutEmail, regLayoutPass;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make Full-Screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Set Layout
        setContentView(R.layout.activity_register);

        //Setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Setup Floating Action Button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Setup data members
        setupDataMembers();

        //setup floating labels
        setupFloatingLabels();
    }

    public void setupDataMembers() {
        // Edit Text
        regNameField = (EditText) findViewById(R.id.regNameField);
        emailField = (EditText) findViewById(R.id.emailField);
        regPassField = (EditText) findViewById(R.id.regPassField);

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

    public void prepRegister(View view) {
        String name = regNameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String password = regPassField.getText().toString().trim();

        if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
            if(validateAll())
                registerUser(name, email, password);
        } else {
            Toast.makeText(getApplicationContext(),
                    "Please enter your details!", Toast.LENGTH_LONG)
                    .show();
        }
    }


    /**
     * Function to store user in MySQL database will post params(tag, name,
     * email, password) to register url
     * */
    private void registerUser(final String name, final String email,
                              final String password) {
        // Tag used to cancel the request
        String tag_string_req = "req_register";

        pDialog.setMessage("Registering ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        // User successfully stored in MySQL
                        // Now store the user in sqlite
                        String uid = jObj.getString("uid");

                        JSONObject user = jObj.getJSONObject("user");
                        String name = user.getString("name");
                        String email = user.getString("email");
                        String created_at = user
                                .getString("created_at");

                        // Inserting row in users table
                        db.addUser(name, email, uid, created_at);

                        Toast.makeText(getApplicationContext(), "User successfully registered. Try login now!", Toast.LENGTH_LONG).show();

                        // Launch login activity
                        Intent intent = new Intent(
                                Register.this,
                                Login.class);
                        startActivity(intent);
                        finish();
                    } else {

                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", name);
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

        regLayoutName = (TextInputLayout) findViewById(R.id.reg_layout_name);
        regLayoutEmail = (TextInputLayout) findViewById(R.id.reg_layout_email);
        regLayoutPass = (TextInputLayout) findViewById(R.id.reg_layout_password);

        regNameField.addTextChangedListener(new MyTextWatcher(regNameField));
        emailField.addTextChangedListener(new MyTextWatcher(emailField));
        regPassField.addTextChangedListener(new MyTextWatcher(regPassField));
    }

    public boolean validateAll() {
        if(!validateName() && !validateEmail() && !validatePassword())
            return false;
        return true;
    }

    public boolean validateName() {
        if (regNameField.getText().toString().trim().isEmpty()) {
            regLayoutName.setError(getString(R.string.err_msg_name));
            requestFocus(regNameField);
            return false;
        } else {
            regLayoutName.setErrorEnabled(false);
        }

        return true;
    }


    public boolean validateEmail() {
        String email = emailField.getText().toString().trim();

        if (email.isEmpty() || !isValidEmail(email)) {
            regLayoutEmail.setError(getString(R.string.err_msg_email));
            requestFocus(emailField);
            return false;
        } else {
            regLayoutEmail.setErrorEnabled(false);
        }
        return true;
    }

    public boolean validatePassword() {
        if (regPassField.getText().toString().trim().isEmpty()) {
            regLayoutPass.setError(getString(R.string.err_msg_password));
            requestFocus(regPassField);
            return false;
        } else {
            regLayoutPass.setErrorEnabled(false);
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
            switch (view.getId()) {
                case R.id.regNameField:
                    validateName();
                    break;
                case R.id.regPassField:
                    validatePassword();
                    break;
                case R.id.emailField:
                    validateEmail();
                    break;
            }
        }
    }

}
