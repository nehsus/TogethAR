package io.agora.agoravideodemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rilixtech.CountryCodePicker;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.agora.agoravideodemo.model.UserInfo;
import io.agora.agoravideodemo.ui.MainActivity;

import static io.agora.agoravideodemo.utils.CommonUtilsKt.hideKeyboard;

public class FirebasePhoneNumAuthActivity extends AppCompatActivity {

    private static final String TAG = "FirebasePhoneNumAuth";
    private String verificationId;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
    private EditText phoneEt, verifyCodeEt;
    private ProgressBar progressBar;
    private CountryCodePicker countryCodePicker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firebase_phone_auth_layout);

        progressBar = findViewById(R.id.progress_bar);
        countryCodePicker = findViewById(R.id.country_code_picker);
        phoneEt = findViewById(R.id.number_et);
        verifyCodeEt = findViewById(R.id.auth_et);

        if (firebaseAuth.getCurrentUser() != null)
            launchNextActivity();
    }


    public void sendCodeOnClick(View view) {
        hideKeyboard(view);
        semdSmsCode();
    }

    public void verifyCodeOnClick(View view) {
        hideKeyboard(view);
        verifySmsCode();
    }

    public void signOuOnClick(View view) {
        hideKeyboard(view);
        signOut();
    }

    public void changePhoneNumber(View view) {
        hideKeyboard(view);
        signOut();
    }

    private void updateChangePhoneNumberText(String phoneNumber) {
        TextView tv = findViewById(R.id.verification_tv);
        if (tv != null) {
            String text = "Verification code sent to " + phoneNumber;
            tv.setText(text);
        }
    }

    private void semdSmsCode() {
        if (!validatePhoneNumberAndName(getPhoneNumberWithCountryCode())) {
            return;
        }
        verifyPhoneNumber(getPhoneNumberWithCountryCode());
    }

    private boolean validatePhoneNumberAndName(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            phoneEt.setError("Can't be empty");
            return false;
        } else if (TextUtils.isEmpty(getPreferredName())) {
            ((EditText) findViewById(R.id.name_et)).setError("Can't be empty");
            return false;
        }
        return true;
    }

    private void verifyPhoneNumber(String phno) {
        showProgressBar(true);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phno, 70,
                TimeUnit.SECONDS, this, callbacks);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential credential) {
            Log.d(TAG, "verification completed" + credential);
            showProgressBar(false);
            signInWithPhoneAuthCredential(credential);
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Log.w(TAG, "verification failed", e);
            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                phoneEt.setError("Invalid phone number.");
                Toast.makeText(FirebasePhoneNumAuthActivity.this,
                        "The format of the phone number provided is incorrect. " +
                                "The phone number must be in the format [+][country code][subscriber number].",
                        Toast.LENGTH_LONG).show();

            } else if (e instanceof FirebaseTooManyRequestsException) {
                Toast.makeText(FirebasePhoneNumAuthActivity.this,
                        "Too many attempts. Please try after some time",
                        Toast.LENGTH_SHORT).show();
            }
            showProgressBar(false);
        }

        @Override
        public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
            Log.d(TAG, "code sent " + verificationId);
            FirebasePhoneNumAuthActivity.this.verificationId = verificationId;
            showVerificationGroup();
            updateChangePhoneNumberText(getPhoneNumberWithCountryCode());
            showProgressBar(false);
        }
    };


    private void verifySmsCode() {
        if (verificationId != null) {
            final String verification_code = verifyCodeEt.getText().toString().trim();
            PhoneAuthCredential credential = PhoneAuthProvider.
                    getCredential(verificationId, verification_code);
            signInWithPhoneAuthCredential(credential);
        } else {
            Log.e(TAG, "verificationId is null");
            Toast.makeText(FirebasePhoneNumAuthActivity.this,
                    "Something went wrong please try again.",
                    Toast.LENGTH_SHORT).show();
        }

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        showProgressBar(true);
        verifyCodeEt.setText(credential.getSmsCode());
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        showProgressBar(false);
                        if (task.isSuccessful()) {
                            Log.d(TAG, "code verified signIn successful");
                            addUserToFireStore(task.getResult().getUser());
                        } else {
                            Log.w(TAG, "code verification failed", task.getException());
                            if (task.getException() instanceof
                                    FirebaseAuthInvalidCredentialsException) {
                                verifyCodeEt.setError("Invalid code.");
                            }
                        }
                    }

                });
    }

    private void signOut() {
        firebaseAuth.signOut();
        UserInfo.INSTANCE.clear();
        showLoginGroup();
    }


    private void addUserToFireStore(final FirebaseUser user) {
        showProgressBar(true);
        Map<String, Object> map = new HashMap<>();
        map.put("userId", user.getUid());
        map.put("name", getPreferredName());
        map.put("phone", getPhoneNumber());
        map.put("countryCode", countryCodePicker.getDefaultCountryCodeAsInt());
        map.put("lastUpdated", System.currentTimeMillis());
        map.put("verified", true);

        firestoreDB.collection("users").document(user.getUid())
                .set(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        showProgressBar(false);
                        saveUserDetailsToPrefs(user.getUid(), getPreferredName(), getPhoneNumber(), countryCodePicker.getDefaultCountryCodeAsInt());
                        launchNextActivity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error adding phone auth info", e);
                        showProgressBar(false);
                    }
                });
    }

    private void launchNextActivity() {
        // showSignoutGroup();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void saveUserDetailsToPrefs(String uid, String preferredName, String phoneNumber, int defaultCountryCodeAsInt) {
        UserInfo.INSTANCE.setUserId(uid);
        UserInfo.INSTANCE.setName(preferredName);
        UserInfo.INSTANCE.setPhone(phoneNumber);
        UserInfo.INSTANCE.setCountryCode(defaultCountryCodeAsInt);

    }

    private void showLoginGroup() {
        findViewById(R.id.verification_code_group).setVisibility(View.GONE);
        findViewById(R.id.send_code_group).setVisibility(View.VISIBLE);
        findViewById(R.id.logout_group).setVisibility(View.GONE);
    }

    private void showVerificationGroup() {
        findViewById(R.id.verification_code_group).setVisibility(View.VISIBLE);
        findViewById(R.id.send_code_group).setVisibility(View.GONE);
        findViewById(R.id.logout_group).setVisibility(View.GONE);
    }

    private void showSignoutGroup() {
        findViewById(R.id.send_code_group).setVisibility(View.GONE);
        findViewById(R.id.verification_code_group).setVisibility(View.GONE);
        findViewById(R.id.logout_group).setVisibility(View.VISIBLE);
    }

    private void showProgressBar(boolean visibility) {
        progressBar.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }

    private String getPhoneNumber() {
        return phoneEt.getText().toString().trim();
    }

    private String getPhoneNumberWithCountryCode() {
        return countryCodePicker.getSelectedCountryCodeWithPlus() + phoneEt.getText().toString().trim();
    }

    private String getPreferredName() {
        return ((EditText) findViewById(R.id.name_et)).getText().toString().trim();
    }
}