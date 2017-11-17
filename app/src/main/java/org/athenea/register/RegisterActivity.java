package org.athenea.register;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.athenea.R;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        this.setTitle("Register");


    }
}
