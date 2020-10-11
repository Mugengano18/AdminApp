package com.example.administrator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.email)
    EditText email;
    @BindView(R.id.Password)
    EditText pass;
    @BindView(R.id.loginB)
    Button login;

    private FirebaseAuth firebaseAuth;
    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        login.setOnClickListener(this);


        loadingDialog = new Dialog(MainActivity.this);
        loadingDialog.setContentView(R.layout.loading_progressbar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        firebaseAuth = FirebaseAuth.getInstance();


        if(firebaseAuth.getCurrentUser() != null){

            Intent intent = new Intent(MainActivity.this,SetsActivity.class);
            startActivity(intent);
            finish();
        };
    }



    @Override
    public void onClick(View view) {

        if (email.getText().toString().isEmpty()){
            email.setError("Enter Email ID");
            return;
        }else {
            email.setError(null);
        }
        if (pass.getText().toString().isEmpty()){
            pass.setError("Enter Password");
            return;
        }else {
            email.setError(null);
        }

        firebaseLogin();
    }

    private void firebaseLogin() {

        loadingDialog.show();
        firebaseAuth.signInWithEmailAndPassword(email.getText().toString(), pass.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Successfully Signed In", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(MainActivity.this,SetsActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(MainActivity.this, "Sign In Failed", Toast.LENGTH_SHORT).show();
                        }
                        loadingDialog.dismiss();
                    }
                });
    }

}