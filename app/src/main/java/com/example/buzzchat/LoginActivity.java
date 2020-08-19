package com.example.buzzchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private CountryCodePicker ccp;
    private EditText phoneText, codeText;
    private Button nextConfirmBtn;
    private String checker = "", phoneNumber = "";
    private LinearLayout linearLayoutPhoneNo, linearLayoutEnterCode;
    private RelativeLayout relativeLayout;
    private TextView enter6DigitCode;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callBack;
    private FirebaseAuth fAuth;
    private String verificationID;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        try{

            getActionBar().setTitle("Hello world App");
            getSupportActionBar().setTitle("Hello world App");
        }catch (Exception e){
            Log.v("ssyss",e.toString());
        }
        ccp = (CountryCodePicker) findViewById(R.id.countryCodeHolder);
        phoneText = (EditText) findViewById(R.id.phoneNumber);
        ccp.registerCarrierNumberEditText(phoneText);
        codeText = (EditText) findViewById(R.id.codeText);
        nextConfirmBtn = (Button) findViewById(R.id.nextSubmitButton);
        linearLayoutPhoneNo = (LinearLayout) findViewById(R.id.enterPhoneNo);
        linearLayoutEnterCode = (LinearLayout) findViewById(R.id.enterCodeText);
        enter6DigitCode = (TextView) findViewById(R.id.enter6DigitCode);
        fAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);

        codeText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start,int before, int count) {
                if(codeText.getText().toString().length()==6){
                    nextConfirmBtn.performClick();
                }
            }
        });

        nextConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(nextConfirmBtn.getText().equals("Confirm") || checker.equals("Code Sent")){
                    String verificationCode = codeText.getText().toString();

                    if(verificationCode.equals("")){
                        Toast.makeText(LoginActivity.this,"Enter the code",Toast.LENGTH_SHORT).show();
                    }else{
                        loadingBar.setTitle("Code Verifying..");
                        loadingBar.setMessage("Please wait, while we verify the code");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();

                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationID,verificationCode);
                        signInWithPhoneAuthCredential(credential);
                    }
                }else{
                    phoneNumber = ccp.getFullNumberWithPlus();
                    if(phoneNumber.equals("")){
                        Toast.makeText(LoginActivity.this,"Please enter a valid phone number",Toast.LENGTH_SHORT).show();
                    }else{
                        loadingBar.setTitle("Phone Number Verifying..");
                        loadingBar.setMessage("Please wait, while we verify your Phone Number");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();

                        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber,60,TimeUnit.SECONDS, LoginActivity.this,callBack);        // OnVerificationStateChangedCallbacks
                    }
                }
            }
        });


        callBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(LoginActivity.this,"Invalid Phone Number...",Toast.LENGTH_LONG).show();
                loadingBar.dismiss();
                linearLayoutPhoneNo.setVisibility(View.VISIBLE);


                nextConfirmBtn.setText("Next");
                linearLayoutEnterCode.setVisibility(View.GONE);
                enter6DigitCode.setVisibility(View.GONE);
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                linearLayoutPhoneNo.setVisibility(View.GONE);
                checker = "Code Sent";

                verificationID = s;
                resendToken = forceResendingToken;

                nextConfirmBtn.setText("Confirm");
                linearLayoutEnterCode.setVisibility(View.VISIBLE);
                enter6DigitCode.setVisibility(View.VISIBLE);

                loadingBar.dismiss();
                Toast.makeText(LoginActivity.this,"Code has been sent.",Toast.LENGTH_SHORT).show();
            }
        };


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if(firebaseUser!=null){
            Intent i = new Intent(LoginActivity.this, TabbedActivity.class);
            startActivity(i);
            finish();
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        fAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loadingBar.dismiss();
                            Toast.makeText(LoginActivity.this,"Phone number verified",Toast.LENGTH_SHORT).show();
                            Toast.makeText(LoginActivity.this,"Successfully Logged In",Toast.LENGTH_LONG).show();
                            Intent i = new Intent(LoginActivity.this, SettingUpProfileFirstTime.class);
                            startActivity(i);
                            finish();
                        } else {
                            loadingBar.dismiss();
                            Toast.makeText(LoginActivity.this,"Error: Enter a valid code",Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

}