package com.rackluxury.rollsroyce.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rackluxury.rollsroyce.R;
import com.rackluxury.rollsroyce.adapters.UserProfile;
import com.royrodriguez.transitionbutton.TransitionButton;

import java.util.Objects;
import java.util.regex.Pattern;

import es.dmoral.toasty.Toasty;


public class RegistrationActivity extends AppCompatActivity implements
        GestureDetector.OnGestureListener {

    public static final int SWIPE_THRESHOLD = 100;
    public static final int SWIPE_VELOCITY_THRESHOLD = 100;

    String[] required_permission = new String[]{
            Manifest.permission.READ_MEDIA_IMAGES,
    };

    boolean is_storage_image_permitted = false;
    String TAG = "Permission";

    ActivityResultLauncher<String> galleryLauncher;
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile(
                    "(?=.*[0-9])" +
                            "(?=.*[a-zA-Z])" +
                            "(?=.*[@#$%^&+=])" +
                            "(?=\\S+$)" +
                            ".{4,}"
            );
    private static final int PICK_IMAGE = 123;
    String Email, PhoneNo, Password, Name;
    Uri imagePath;
    private TextInputLayout textInputEmail;
    private TextInputLayout textInputPhone;
    private TextInputLayout textInputUsername;
    private TextInputLayout textInputPassword;
    private EditText userName;
    private EditText userPhoneNo;
    private EditText userEmail;
    private EditText userPassword;
    private ImageView userProfilePic;
    private TransitionButton regButton;
    private TextView userLogin;
    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;
    private GestureDetector gestureDetector;
    private SharedPreferences coins;


    private final TextWatcher registrationTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String nameInput = userName.getText().toString().trim();
            String emailInput = userEmail.getText().toString().trim();
            String passwordInput = userPassword.getText().toString().trim();
            regButton.setEnabled(!nameInput.isEmpty() && !emailInput.isEmpty() && !passwordInput.isEmpty());
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();


        if (user != null) {
            finish();
            Intent openHomeActivityFromLogin = new Intent(RegistrationActivity.this, HomeActivity.class);
            startActivity(openHomeActivityFromLogin);
            Animatoo.animateSlideUp(RegistrationActivity.this);
        } else {
            setContentView(R.layout.activity_registration);
            setupUIViews();


            loadRegisterFunctionality();
        }


    }


    private void setupUIViews() {
        textInputEmail = findViewById(R.id.registration_email_layout);
        textInputUsername = findViewById(R.id.registration_username_layout);
        textInputPassword = findViewById(R.id.registration_password_layout);
        textInputPhone = findViewById(R.id.registration_phone_no_layout);
        userName = findViewById(R.id.etUserName);
        userPhoneNo = findViewById((R.id.etUserPhoneNo));
        userEmail = findViewById(R.id.etUserEmail);
        userPassword = findViewById(R.id.etUserPassword);
        userProfilePic = findViewById(R.id.ivProfileRegistration);
        regButton = findViewById(R.id.btnRegister);
        userLogin = findViewById(R.id.tvUserLogin);
        gestureDetector = new GestureDetector(RegistrationActivity.this, this);
        coins = getSharedPreferences("Rewards", MODE_PRIVATE);

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri uri) {
                userProfilePic.setImageURI(uri);
            }
        });


    }

    private void loadRegisterFunctionality() {


        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

        storageReference = firebaseStorage.getReference();


        progressDialog = new ProgressDialog(RegistrationActivity.this);


        userProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (!allPermissionResultCheck()) {
                    requestPermissionStorageImages();
                } else {
                    getProfilePic();
                }


            }
        });


        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (validateReg()) {
                    regButton.startAnimation();
                    regButton.setEnabled(false);
                    Name = userName.getText().toString().trim();
                    Email = userEmail.getText().toString().trim();
                    Password = userPassword.getText().toString().trim();
                    PhoneNo = userPhoneNo.getText().toString().trim();
                    progressDialog.setMessage("Loading...");
                    progressDialog.show();

                    firebaseAuth.createUserWithEmailAndPassword(Email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override

                        public void onComplete(@NonNull Task<AuthResult> task) {


                            if (task.isSuccessful()) {

                                regButton.stopAnimation(TransitionButton.StopAnimationStyle.EXPAND, new TransitionButton.OnAnimationStopEndListener() {
                                    @Override
                                    public void onAnimationStopEnd() {

                                        progressDialog.dismiss();
                                        sendEmailVerification();

                                    }
                                });


                            } else {
                                progressDialog.dismiss();
                                regButton.stopAnimation(TransitionButton.StopAnimationStyle.SHAKE, null);
                                regButton.setEnabled(true);
                                Toasty.error(RegistrationActivity.this, "Registration Failed", Toast.LENGTH_LONG).show();

                            }


                        }
                    });


                }


            }


        });
        userLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openLoginActivityFromRegistration = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(openLoginActivityFromRegistration);
                finish();
                Animatoo.animateSwipeRight(RegistrationActivity.this);

            }
        });

        userName.addTextChangedListener(registrationTextWatcher);
        userEmail.addTextChangedListener(registrationTextWatcher);
        userPassword.addTextChangedListener(registrationTextWatcher);
    }

    public boolean allPermissionResultCheck() {
        return is_storage_image_permitted;
    }

    public void requestPermissionStorageImages() {
        if (ContextCompat.checkSelfPermission(RegistrationActivity.this, required_permission[0]) == PackageManager.PERMISSION_GRANTED) {
            is_storage_image_permitted = true;
            getProfilePic();

        } else {
            request_permission_launcher_storage_images.launch(required_permission[0]);
        }
    }

    private ActivityResultLauncher<String> request_permission_launcher_storage_images =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            is_storage_image_permitted = true;
                            getProfilePic();
                        } else {
                            is_storage_image_permitted = false;
                            Toasty.error(RegistrationActivity.this, "Permission denied...!", Toast.LENGTH_LONG).show();
                            sendToSettingDialog();


                        }
                    });

    public void sendToSettingDialog() {
        new AlertDialog.Builder(RegistrationActivity.this)
                .setTitle("Alert for Permission")
                .setMessage("Go to Settings for Permissions")
                .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                        dialogInterface.dismiss();

                    }
                })
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        finish();
                    }
                })
                .show();
    }


    private void getProfilePic() {

        ImagePicker.with(RegistrationActivity.this)
                .crop()                    //Crop image(Optional), Check Customization for more option
                .compress(1024)            //Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
                .start();
        Animatoo.animateDiagonal(RegistrationActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri = data.getData();
        userProfilePic.setImageURI(uri);

    }

    private boolean validateReg() {
        boolean result;

        String usernameInput = Objects.requireNonNull(textInputUsername.getEditText()).getText().toString().trim();
        String emailInput = Objects.requireNonNull(textInputEmail.getEditText()).getText().toString().trim();
        String phoneInput = Objects.requireNonNull(textInputPhone.getEditText()).getText().toString().trim();
        String passwordInput = Objects.requireNonNull(textInputPassword.getEditText()).getText().toString().trim();
        ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();


        if (usernameInput.length() > 15) {
            textInputUsername.setError("Username too long");
            result = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            textInputUsername.setError(null);
            textInputEmail.setError("Please enter a valid email address");
            result = false;
        } else if (!Patterns.PHONE.matcher(phoneInput).matches()) {
            textInputEmail.setError(null);
            textInputPhone.setError("Please enter a valid phone number");
            result = false;
        } else if (!PASSWORD_PATTERN.matcher(passwordInput).matches()) {
            textInputPhone.setError(null);
            textInputPassword.setError("Password should contain at least 4 characters with no white spaces and at least 1 digit, 1 letter and 1 special character.");
            result = false;
        } else if (null == activeNetwork) {
            textInputPassword.setError(null);
            setNoInternetDialogue();
            result = false;
        } else {
            textInputPassword.setError(null);
            result = true;
        }
        return result;


    }

    private void setNoInternetDialogue() {
        final NoInternetDialogue noInternetDialogue = new NoInternetDialogue(RegistrationActivity.this);
        noInternetDialogue.startNoInternetDialogue();
        Handler handler = new Handler();
        int TRANSITION_SCREEN_TIME = 4000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                noInternetDialogue.dismissDialogue();
            }
        }, TRANSITION_SCREEN_TIME);
    }

    private void sendEmailVerification() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {

                        sendUserData();


                        Toasty.success(RegistrationActivity.this, "Successfully Registered", Toast.LENGTH_LONG).show();
                        Toasty.info(RegistrationActivity.this, "Verify Your Email Address", Toast.LENGTH_LONG).show();

                        int coinCount = Integer.parseInt(coins.getString("Coins", "0"));
                        coinCount = coinCount + 10000;
                        SharedPreferences.Editor coinsEdit = coins.edit();
                        coinsEdit.putString("Coins", String.valueOf(coinCount));
                        coinsEdit.apply();

                        firebaseAuth.signOut();
                        finish();
                        Intent openLoginActivityFromRegistration = new Intent(RegistrationActivity.this, LoginActivity.class);
                        startActivity(openLoginActivityFromRegistration);
                        Animatoo.animateSwipeRight(RegistrationActivity.this);

                    } else {

                        Toasty.error(RegistrationActivity.this, "Failed to send registration E-Mail", Toast.LENGTH_LONG).show();

                    }
                }
            });
        }
    }


    private void sendUserData() {

        Name = userName.getText().toString();
        Email = userEmail.getText().toString();
        PhoneNo = userPhoneNo.getText().toString();


        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference mUIDReference = firebaseDatabase.getReference(firebaseAuth.getUid());
        StorageReference imageReference = storageReference.child(firebaseAuth.getUid()).child("Images").child("Profile Pic");
        UploadTask uploadTask = imageReference.putFile(imagePath);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toasty.error(RegistrationActivity.this, "Profile Pic Upload failed!", Toast.LENGTH_LONG).show();

            }
        }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                Toasty.success(RegistrationActivity.this, "Profile Pic Upload successful!", Toast.LENGTH_LONG).show();

            }
        });


        UserProfile userProfile = new UserProfile(Name, Email, PhoneNo);
        mUIDReference.setValue(userProfile);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
        Animatoo.animateSlideRight(RegistrationActivity.this);

    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent downEvent, MotionEvent moveEvent, float velocityX, float velocityY) {
        boolean result = false;
        float diffY = moveEvent.getY() - downEvent.getY();
        float diffX = moveEvent.getX() - downEvent.getX();

        if (Math.abs(diffX) > Math.abs(diffY)) {

            if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX > 0) {
                    onSwipeRight();
                }
                result = true;
            }
        }

        return result;
    }


    private void onSwipeRight() {
        finish();
        startActivity(new Intent(RegistrationActivity.this, LoginOrRegisterActivity.class));
        Animatoo.animateSlideRight(RegistrationActivity.this);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

}


