package com.dglproject.brand.ux;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dglproject.brand.utilities.Config;
import com.dglproject.brand.json.JSONParser;
import com.dglproject.brand.R;
import com.dglproject.brand.utilities.PrefManager;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
/**
 * Author: Tortuvshin Byambaa.
 * Project: DglBrand
 * URL: https://www.github.com/tortuvshin
 */
public class ActivitySignup extends AppCompatActivity {

    private static final String TAG = "SignUp";
    EditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    Button signUpButton;
    TextView loginLink;
    PrefManager prefManager;

    JSONParser jsonParser = new JSONParser();

    String URL = Config.UserService;

    int i=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        prefManager = new PrefManager(this);

        nameEditText = (EditText)findViewById(R.id.sName);
        emailEditText = (EditText)findViewById(R.id.sEmail);
        passwordEditText = (EditText)findViewById(R.id.sPassword);
        confirmPasswordEditText = (EditText)findViewById(R.id.sComfirmPassword);
        signUpButton = (Button)findViewById(R.id.btnSignUp);
        loginLink = (TextView)findViewById(R.id.linkLogin);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();

            }
        });
        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });
    }
    public void signup() {
        if (!validate()) {
            onSignupFailed();
            return;
        }
        signUpButton.setEnabled(false);
        final ProgressDialog progressDialog = new ProgressDialog(ActivitySignup.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.register_loaging));
        progressDialog.show();

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        onSignupSuccess();
                        progressDialog.dismiss();
                    }
                }, 3000);
    }

    public void onSignupSuccess() {
        signUpButton.setEnabled(true);
        setResult(RESULT_OK, null);
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        finish();
                    }
                }, 3000);

        Toast.makeText(getBaseContext(), getString(R.string.register_success), Toast.LENGTH_LONG).show();
        String name = nameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        CreateUser createUser = new CreateUser();
        createUser.execute(String.valueOf(Config.generateAccessKey()),"signup",name,password,email);
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), getString(R.string.register_error), Toast.LENGTH_LONG).show();
        signUpButton.setEnabled(true);
    }

    public boolean validate() {

        boolean valid = true;

        String name = nameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            nameEditText.setError(getString(R.string.err_username_short));
            valid = false;
        } else {
            nameEditText.setError(null);
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError(getString(R.string.err_mail));
            valid = false;
        } else {
            emailEditText.setError(null);
        }
        if (password.isEmpty() || password.length() < 4 || password.length() > 17) {
            passwordEditText.setError(getString(R.string.err_pass_short));
            passwordEditText.setText("");
            valid = false;
        } else {
            passwordEditText.setError(null);
        }
        if (!password.equals(confirmPassword)){
            confirmPasswordEditText.setError(getString(R.string.err_pass_confirm));
            valid = false;
        }
        else{
            confirmPasswordEditText.setError(null);
        }

        return valid;
    }

    /**
     * Hashes the password with MD5.
     * @param s
     * @return
     */
    private String PasswordHashes(String s) {
        try {

            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            return s;
        }
    }
    private class CreateUser extends AsyncTask<String, String, JSONObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... args) {


            String email = args[4];
            String password = args[3];
            String name= args[2];
            String state = args[1];
            String accesskey = args[0];

            ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("accesskey", accesskey));
            params.add(new BasicNameValuePair("state", state));
            params.add(new BasicNameValuePair("username", name));
            params.add(new BasicNameValuePair("password", password));
            params.add(new BasicNameValuePair("email",email));

            JSONObject json = jsonParser.makeHttpRequest(URL, "POST", params);

            Log.d("","Json: "+json);

            return json;
        }

        protected void onPostExecute(JSONObject result) {

            try {
                if (result != null) {
                    Toast.makeText(getApplicationContext(),result.getString("message"),Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.server_error), Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}