package org.athenea.register;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import org.athenea.MainActivity;
import org.athenea.R;
import org.athenea.login.LoginActivityMaterial;
import org.json.JSONException;
import org.json.JSONObject;

public class SignupActivityMaterial extends AppCompatActivity {

  private static final String TAG = "SignupActivity";
  private ProgressDialog progressDialog;

  @BindView(R.id.input_name)
  EditText _nameText;
  @BindView(R.id.input_username)
  EditText _addressText;
  @BindView(R.id.input_email)
  EditText _emailText;
  @BindView(R.id.input_mobile)
  EditText _mobileText;
  @BindView(R.id.input_password)
  EditText _passwordText;
  @BindView(R.id.input_reEnterPassword)
  EditText _reEnterPasswordText;
  @BindView(R.id.btn_signup)
  Button _signupButton;
  @BindView(R.id.link_login)
  TextView _loginLink;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_signup_material);
    ButterKnife.bind(this);

    _signupButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        signup();
      }
    });
    progressDialog = new ProgressDialog(SignupActivityMaterial.this,
        R.style.AppTheme);

    _loginLink.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // Finish the registration screen and return to the Login activity
        Intent intent = new Intent(getApplicationContext(), LoginActivityMaterial.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
      }
    });
  }

  public void signup() {
    Log.d(TAG, "Signup");

    if (!validate()) {
      onSignupFailed();
      return;
    }

    _signupButton.setEnabled(false);


    progressDialog.setIndeterminate(true);
    progressDialog.setMessage("Creating Account...");
    progressDialog.show();

    String name = _nameText.getText().toString();
    String username = _addressText.getText().toString();
    String email = _emailText.getText().toString();
    String mobile = _mobileText.getText().toString();
    String password = _passwordText.getText().toString();
    String reEnterPassword = _reEnterPasswordText.getText().toString();

    // TODO: Implement your own signup logic here.

    UserRegisterTask userRegisterTask = new UserRegisterTask(name, username, email, mobile, password);   // Create Async Task to post users microservice
    userRegisterTask.execute();
  }


  public void onSignupSuccess(String username, String email) {

    Toast.makeText(getBaseContext(), "Register Success!", Toast.LENGTH_LONG).show();
    progressDialog.dismiss();
    _signupButton.setEnabled(true);
    setResult(RESULT_OK, null);
    finish();

    Intent intent = new Intent(this, MainActivity.class);

    intent.putExtra("username", username);
    intent.putExtra("useremail", email);

    startActivity(intent);
    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);

  }

  /**
   * If signup failed go back to signing view
   */
  public void onSignupFailed() {
    Toast.makeText(getBaseContext(), "Register failed", Toast.LENGTH_LONG).show();
    progressDialog.dismiss();
    _signupButton.setEnabled(true);
  }

  /**
   * Validate every field to avoid errors in server
   * @return if all fields are valid or not
   */
  public boolean validate() {
    boolean valid = true;

    String name = _nameText.getText().toString();
    String username = _addressText.getText().toString();
    String email = _emailText.getText().toString();
    String mobile = _mobileText.getText().toString();
    String password = _passwordText.getText().toString();
    String reEnterPassword = _reEnterPasswordText.getText().toString();

    if (name.isEmpty() || name.length() < 3) {
      _nameText.setError("at least 3 characters");
      valid = false;
    } else {
      _nameText.setError(null);
    }

    if (username.isEmpty() || username.length() < 3) {
      _addressText.setError("At least 3 characters");
      valid = false;
    } else {
      _addressText.setError(null);
    }

    if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
      _emailText.setError("Enter a valid email address");
      valid = false;
    } else {
      _emailText.setError(null);
    }

    if (mobile.isEmpty() || !android.util.Patterns.PHONE.matcher(mobile).matches()) {
      _mobileText.setError("Enter Valid Mobile Number");
      valid = false;
    } else {
      _mobileText.setError(null);
    }

    if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
      _passwordText.setError("Between 4 and 10 alphanumeric characters");
      valid = false;
    } else {
      _passwordText.setError(null);
    }

    if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || reEnterPassword.length() > 10
        || !(reEnterPassword.equals(password))) {
      _reEnterPasswordText.setError("Password Do not match");
      valid = false;
    } else {
      _reEnterPasswordText.setError(null);
    }

    return valid;
  }

  /**
   * Represents an asynchronous registration task used to register
   * the user.
   */
  @SuppressLint("StaticFieldLeak")
  public class UserRegisterTask extends AsyncTask<Void, Void, Boolean> {

    private String name;
    private String username;
    private String email;
    private String mobile;
    private String password;


    final ProgressDialog progressDialog = new ProgressDialog(SignupActivityMaterial.this,
        R.style.AppTheme);

    UserRegisterTask(String name, String username, String email, String mobile,
        String password) {
      this.name = name;
      this.username = username;
      this.email = email;
      this.mobile = mobile;
      this.password = password;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {

      Log.d("IN BACKGROUND", "POSTING TO USERS MICROSERVICE ");

      URL url;
      String response;

      JSONObject userJSON = new JSONObject();   // It will be our body parameter
      long phone_number = Long.parseLong(mobile);   // may be 8+ digits -> long
      // Create user JSON OBJECT
      try {
        userJSON.put("email", email).put("username", username).put("name", name).put("phone_number", phone_number).put("password", password);
      } catch (JSONException e) {
        e.printStackTrace();
      }

      Log.d("USER JSON: ", userJSON.toString());

      try {

        // Send User to microservice to register
        url = new URL("http://www.athenea-project.org/users-microservice/api/user/insert");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("accept", "application/json ");
        urlConnection.setRequestProperty("content-type", "application/json ");
        urlConnection.setDoOutput(true);    // write into connection
        OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
        wr.write(userJSON.toString());    // send our userJSON
        wr.flush();

        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
        response = readStream(in);

        wr.close();
        urlConnection.disconnect();

        Log.d("RESPONSE:", response);   // Print response
        Log.d("RESPONSE CODE", urlConnection.getResponseCode() + "");

        if(urlConnection.getResponseCode() == 200)
          return true;
        // TODO: handle error response, user already exists || bad data supplied
        else
          return false;

      } catch (IOException e) {
        e.printStackTrace();
        Log.d("ERROR:", "ERROR WHEN CONNECTING TO SERVER");
        return false;

      }
    }

    /**
     * When query executed
     * @param registerSuccess if user exists in DB
     */
    @Override
    protected void onPostExecute(Boolean registerSuccess) {

      if (registerSuccess) {
        new android.os.Handler().postDelayed(
            new Runnable() {
              public void run() {
                // On complete call either onLoginSuccess or onLoginFailed
                onSignupSuccess(username, email);
                progressDialog.dismiss();
              }
            }, 3000);
      } else {
        new android.os.Handler().postDelayed(
            new Runnable() {
              public void run() {
                // On complete call eithe∫ onLoginSuccess or onLoginFailed
                onSignupFailed();
                progressDialog.hide();
              }
            }, 3000);
      }


    }
  }
  /*
     * Input stream Reader
     * @param is
     * @return
     * @throws IOException
     */
  private String readStream(InputStream is) throws IOException {
    StringBuilder sb = new StringBuilder();
    BufferedReader r = new BufferedReader(new InputStreamReader(is), 1000);
    for (String line = r.readLine(); line != null; line = r.readLine()) {
      sb.append(line);
    }
    is.close();
    return sb.toString();
  }
}
