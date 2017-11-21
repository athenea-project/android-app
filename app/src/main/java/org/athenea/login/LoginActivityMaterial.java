package org.athenea.login;

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
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.athenea.MainActivity;
import org.athenea.R;
import org.athenea.register.SignupActivityMaterial;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivityMaterial extends AppCompatActivity {

  private static final String TAG = "LoginActivity";
  private static final int REQUEST_SIGNUP = 0;
  private static ProgressDialog progressDialog;

  @BindView(R.id.input_email)
  EditText _emailText;
  @BindView(R.id.input_password)
  EditText _passwordText;
  @BindView(R.id.btn_login)
  Button _loginButton;
  @BindView(R.id.link_signup)
  TextView _signupLink;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login_material);
    ButterKnife.bind(this);

    _loginButton.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        login();
      }
    });

    _signupLink.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        // Start the Signup activity
        Intent intent = new Intent(getApplicationContext(), SignupActivityMaterial.class);
        startActivityForResult(intent, REQUEST_SIGNUP);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
      }
    });
  }

  public void login() {
    Log.d(TAG, "Login");

    if (!validate()) {
      onLoginFailed();
      return;
    }

    _loginButton.setEnabled(false);

    progressDialog = new ProgressDialog(LoginActivityMaterial.this,
        R.style.AppTheme);
    progressDialog.setIndeterminate(true);
    progressDialog.setMessage("Authenticating...");
    progressDialog.show();

    String email = _emailText.getText().toString();
    String password = _passwordText.getText().toString();

    // TODO: Implement your own authentication logic here.

    UserLoginTask userLoginTask = new UserLoginTask(email,
        password);   // Create Async Task to query users microservice
    userLoginTask.execute();

  }


  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    Log.d("ON ACTIVITY RESULT:", "IM WORKING");
    if (requestCode == REQUEST_SIGNUP) {

      if (resultCode == RESULT_OK) {

        Log.d("ACTIVITY FINISHED:", "AUTO LOGIN");

        // TODO: Implement successful signup login here
        // By default we just finish the Activity and log them in automatically

      }
    }
  }

  @Override
  public void onBackPressed() {
    // Disable going back to the MainActivity
    moveTaskToBack(true);
  }

  public void onLoginSuccess(String username, String email) {

    _loginButton.setEnabled(true);

    // Login success remove dialog and start main activity
    progressDialog.dismiss();

    Intent intent = new Intent(this, MainActivity.class);

    intent.putExtra("username", username);
    intent.putExtra("useremail", email);

    startActivity(intent);
    finish();
    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);

  }

  public void onLoginFailed() {

    progressDialog.hide();
    finish();
    Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
    Intent intent = new Intent(getApplicationContext(), LoginActivityMaterial.class);
    startActivity(intent);
    _loginButton.setEnabled(true);

  }

  public boolean validate() {
    boolean valid = true;

    String email = _emailText.getText().toString();
    String password = _passwordText.getText().toString();

    MessageDigest messageDigest = null;
    try {
      messageDigest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    assert messageDigest != null;
    messageDigest.update(password.getBytes());
    String hashedPassword = new String(messageDigest.digest());

    Log.d("HASHED PASSWORD: ", hashedPassword);

    if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
      _emailText.setError("enter a valid email address");
      valid = false;
    } else {
      _emailText.setError(null);
    }

    if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
      _passwordText.setError("between 4 and 10 alphanumeric characters");
      valid = false;
    } else {
      _passwordText.setError(null);
    }

    return valid;
  }

  /**
   * Represents an asynchronous login/registration task used to authenticate
   * the user.
   */
  @SuppressLint("StaticFieldLeak")
  public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

    private final String mEmail;
    private String username;
    private final String mPassword;
    final ProgressDialog progressDialog = new ProgressDialog(LoginActivityMaterial.this,
        R.style.AppTheme);

    UserLoginTask(String email, String password) {
      mEmail = email;
      mPassword = password;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {

      Log.d("IN BACKGROUND", "QUERYING USERS MICROSERVICE");

      // Url to query user
      URL url;
      String response;

      try {

        // Query users
        url = new URL("http://www.athenea-project.org/users-microservice/api/user/email");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        urlConnection.setRequestProperty("email", this.mEmail); // Add email as header param

        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
        response = readStream(in);

        try {
         JSONObject user = new JSONObject(response);  // create user from response
          this.username = user.getString("name");
        } catch (JSONException e) {
          e.printStackTrace();
        }

        urlConnection.disconnect();

        Log.d("RESPONSE:", response);

        // TODO: handle response
        return !response.isEmpty();

      } catch (IOException e) {
        e.printStackTrace();
        Log.d("ERROR:", "WRONG QUERY");
        return false;

      }
    }

    /**
     * When query executed
     *
     * @param userExists if user exists in DB
     */
    @Override
    protected void onPostExecute(Boolean userExists) {

      Log.d("QUERY EXECUTED", "DEAL WITH RESPONSE");

      if (userExists) {
        new android.os.Handler().postDelayed(
            new Runnable() {
              public void run() {
                // On complete call either onLoginSuccess or onLoginFailed
                onLoginSuccess(username, mEmail);
                progressDialog.dismiss();
              }
            }, 3000);
      } else {
        new android.os.Handler().postDelayed(
            new Runnable() {
              public void run() {
                // On complete call either onLoginSuccess or onLoginFailed
                onLoginFailed();
                progressDialog.dismiss();
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
