package com.example.harishmanikantan.verifyd;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.orm.SugarContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Register extends AppCompatActivity {

    private EditText nameView;
    private EditText usernameView;
    private EditText emailView;
    private EditText passwordView;

    private Button registerView;
    private Context context;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        context= this;
        SugarContext.init(context);

        nameView=(EditText) findViewById(R.id.name);
        usernameView=(EditText) findViewById(R.id.username);
        emailView=(EditText) findViewById(R.id.email);
        passwordView=(EditText) findViewById(R.id.password);

        registerView=(Button) findViewById(R.id.register_button);

        registerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptRegister();
            }
        });
    }

    private void attemptRegister(){

        nameView.setError(null);
        usernameView.setError(null);
        emailView.setError(null);
        passwordView.setError(null);

        String name = nameView.getText().toString();
        String username = usernameView.getText().toString();
        String email = emailView.getText().toString();
        String password = passwordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!isValidName(name)){
            nameView.setError("Please enter a valid name");
            cancel = true;
            focusView = nameView;
        }
        if (!isValidUsername(username)){
            usernameView.setError("Username can only contain alphabets and numbers");
            cancel = true;
            focusView = usernameView;
        }
        if (!isValidEmail(email)) {
            emailView.setError("Please enter a valid email address");
            cancel = true;
            focusView = emailView;
        }
        if (!isValidPassword(password)) {
            passwordView.setError("Password should be between 8 and 24 characters");
            cancel = true;
            focusView = passwordView;
        }

        if (cancel){
            focusView.requestFocus();
        }
        else{
            Toast.makeText(this,"Successfully Registered",Toast.LENGTH_SHORT).show();
            User registeredUser = new User(name,username,email,password);
            registeredUser.save();
            startActivity(new Intent(this,LoginActivity.class));
        }

    }

    public boolean isValidName(String name){
        name=name.trim();
        int n=0;
        for (int i=0;i<name.length();i++){
            if (name.charAt(i)==' '){
                n++;
                if (n==2)
                    return false;
            }
            if (!Character.isLetter(name.charAt(i)))
                return false;
        }
        return true;
    }

    public boolean isValidUsername(String username){
        username=username.trim();
        for (int i=0;i<username.length();i++){
            if (!Character.isLetter(username.charAt(i)) && !Character.isDigit(username.charAt(i)))
                return false;
        }
        return true;

    }

    public boolean isValidEmail(String email){
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    public boolean isValidPassword(String password){
        int len=password.length();
        if (len<8||len>24)
            return false;
        return true;
    }

    @Override
    protected void onDestroy() {
        SugarContext.terminate();
        super.onDestroy();
    }
}
