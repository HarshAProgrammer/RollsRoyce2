package com.rackluxury.rollsroyce.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrListener;
import com.rackluxury.rollsroyce.BuildConfig;
import com.rackluxury.rollsroyce.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import es.dmoral.toasty.Toasty;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class ExpensiveDetailActivity extends AppCompatActivity {

    String[] required_permission = new String[]{
            Manifest.permission.READ_MEDIA_IMAGES,
    };
    boolean is_storage_image_permitted = false;
    String TAG = "Permission";
    private static final String SHOWCASE_ID = "single expensive detail";
    TextView expensiveName;
    TextView expensiveDescription;
    ImageView expensiveImage;
    String shareExpensiveImageDescription;
    private Toolbar toolbar;
    private FileOutputStream outputStream;
    private Bitmap bitmap;
    private BitmapDrawable drawable;
    private SharedPreferences prefs;
    private SoundPool soundPool;
    private int soundSaveImage;
    private int soundLike;
    private int soundWallpaper;
    private AnimatedVectorDrawable avd2;
    private AnimatedVectorDrawableCompat avd;
    private ImageView mainGreyHeart;
    private CardView cardViewLike;
    private ImageView mainRedHeart;
    private ImageView heart;
    private ImageView love;
    private ImageView shocked;
    private ImageView sad;
    private ImageView happy;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expensive_detail);
        setUpUIViewsDetailActivity();
        setTransitionDialogue();
        getInformationFromMain();
        setBitmap();
        initToolbar();
        prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean firstStart = prefs.getBoolean("expensiveDetailFirst", true);
        if (firstStart) {
            onFirstIntro();
        }

        new MaterialShowcaseView.Builder(this)
                .setTarget(expensiveName)
                .setDismissText("GOT IT")
                .setContentText("Swipe Up to get more Information.")
                .setContentTextColor(getResources().getColor(R.color.colorWhite))
                .setMaskColour(getResources().getColor(R.color.colorGreen))
                .setDelay(1000)
                .withRectangleShape(true)
                .singleUse(SHOWCASE_ID)
                .show();


        SlidrConfig config = new SlidrConfig.Builder()
                .listener(new SlidrListener() {
                    @Override
                    public void onSlideStateChanged(int state) {

                    }

                    @Override
                    public void onSlideChange(float percent) {

                    }

                    @Override
                    public void onSlideOpened() {

                    }

                    @Override
                    public boolean onSlideClosed() {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("expensiveDetailFirst", false);
                        editor.apply();
                        return false;
                    }
                }).build();

        Slidr.attach(this, config);
    }

    private void setUpUIViewsDetailActivity() {
        toolbar = findViewById(R.id.toolbarExpensiveDetailActivity);
        MotionLayout expensiveDetailLay = findViewById(R.id.motionLayExpensiveDetail);
        expensiveName = findViewById(R.id.tvExpensiveDetailName);
        expensiveDescription = findViewById(R.id.tvExpensiveDetailDescription);
        expensiveImage = findViewById(R.id.ivExpensiveDetailImage);

        ImageView liker = findViewById(R.id.ivExpensiveDetailLiker);
        cardViewLike = findViewById(R.id.cvExpensiveLikerOptions);
        mainGreyHeart = findViewById(R.id.ivExpensiveDetailGreyHeart);
        mainRedHeart = findViewById(R.id.ivExpensiveDetailRedHeart);
        heart = findViewById(R.id.ivExpDetailReactHeart);
        happy = findViewById(R.id.ivExpDetailReactHappy);
        love = findViewById(R.id.ivExpDetailReactLove);
        sad = findViewById(R.id.ivExpDetailReactSad);
        shocked = findViewById(R.id.ivExpDetailReactShocked);


        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build();
        soundSaveImage = soundPool.load(this, R.raw.sound_save_image, 1);
        soundWallpaper = soundPool.load(this, R.raw.sound_set_wallpaper, 1);
        soundLike = soundPool.load(this, R.raw.sound_like, 1);

        Animation reactBounceAnim = AnimationUtils.loadAnimation(this, R.anim.react_bounce_anim);

        final Drawable mrhDrawable = mainRedHeart.getDrawable();
        heart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                soundPool.play(soundLike, 1, 1, 0, 0, 1);
                mainRedHeart.setAlpha(0.70f);

                if (mrhDrawable instanceof AnimatedVectorDrawableCompat) {
                    avd = (AnimatedVectorDrawableCompat) mrhDrawable;
                    avd.start();
                } else if (mrhDrawable instanceof AnimatedVectorDrawable) {
                    avd2 = (AnimatedVectorDrawable) mrhDrawable;
                    avd2.start();

                }
                heart.startAnimation(reactBounceAnim);
            }
        });


        happy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(soundLike, 1, 1, 0, 0, 1);
                happy.startAnimation(reactBounceAnim);
            }
        });
        love.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(soundLike, 1, 1, 0, 0, 1);

                love.startAnimation(reactBounceAnim);
            }
        });
        sad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(soundLike, 1, 1, 0, 0, 1);
                sad.startAnimation(reactBounceAnim);
            }
        });
        shocked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(soundLike, 1, 1, 0, 0, 1);
                shocked.startAnimation(reactBounceAnim);
            }
        });




        final Drawable drawable = mainGreyHeart.getDrawable();

        liker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainGreyHeart.setAlpha(0.70f);
                soundPool.play(soundLike, 1, 1, 0, 0, 1);

                prefs = getSharedPreferences("prefs", MODE_PRIVATE);
                boolean likeStart = prefs.getBoolean("expensiveDetailFirstLike", true);
                if (likeStart) {
                    onFirstLike();
                }



                if (drawable instanceof AnimatedVectorDrawableCompat) {
                    avd = (AnimatedVectorDrawableCompat) drawable;
                    avd.start();
                } else if (drawable instanceof AnimatedVectorDrawable) {
                    avd2 = (AnimatedVectorDrawable) drawable;
                    avd2.start();

                }
            }
        });

        Animation reactionsOpeningAnimation = AnimationUtils.loadAnimation(this, R.anim.like_reactions_animations);
        liker.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                prefs = getSharedPreferences("prefs", MODE_PRIVATE);
                boolean likeStart = prefs.getBoolean("expensiveDetailFirstLike", true);
                if (likeStart) {
                    onFirstLike();
                }
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("expensiveDetailFirstLike", false);
                editor.apply();

                //Showing card view with reactionsOpeningAnimation
                cardViewLike.setVisibility(View.VISIBLE);
                cardViewLike.startAnimation(reactionsOpeningAnimation);

                return false;
            }
        });

        Typeface detailExpensiveDescriptionFont = Typeface.createFromAsset(ExpensiveDetailActivity.this.getAssets(), "fonts/OpenSansCondensed-Light.ttf");
        expensiveDescription.setTypeface(detailExpensiveDescriptionFont);

    }

    public void onFirstIntro() {
        Toasty.info(ExpensiveDetailActivity.this, "Swipe Right to Dismiss", Toast.LENGTH_LONG).show();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("expensiveDetailFirst", false);
        editor.apply();
    }
    public void onFirstLike() {
        Toasty.info(ExpensiveDetailActivity.this, "Long Hold for other Reactions.", Toast.LENGTH_LONG).show();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("expensiveDetailFirstLike", false);
        editor.apply();
    }


    private void setTransitionDialogue() {
        final TransitionDialogue transitionDialogue = new TransitionDialogue(ExpensiveDetailActivity.this);
        transitionDialogue.startTransitionDialogue();
        Handler handler = new Handler();
        int TRANSITION_SCREEN_TIME = 700;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                transitionDialogue.dismissDialogue();
            }
        }, TRANSITION_SCREEN_TIME);
    }

    private void getInformationFromMain() {
        Bundle mBundle = getIntent().getExtras();
        if (mBundle != null) {
            expensiveImage.setImageResource(mBundle.getInt("Image"));
            expensiveName.setText(mBundle.getString("Name"));
            expensiveDescription.setText(mBundle.getString("Description"));
            shareExpensiveImageDescription = expensiveDescription.getText().toString();

        }
    }

    private void setBitmap() {
        drawable = (BitmapDrawable) expensiveImage.getDrawable();
        bitmap = drawable.getBitmap();
    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(expensiveName.getText().toString());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.expensive_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.save_image_expensive) {

            if (!allPermissionResultCheck()) {
                requestPermissionStorageImages();
            }else {
                downloadImage();
            }

            return true;
        } else if (item.getItemId() == R.id.share_image_expensive) {

            drawable = (BitmapDrawable) expensiveImage.getDrawable();
            bitmap = drawable.getBitmap();

            try {
                File file = new File(getApplicationContext().getExternalCacheDir(), File.separator + "Watches from Rolex.png");
                FileOutputStream fOut = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                fOut.flush();
                fOut.close();
                file.setReadable(true, false);
                final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                String shareImageSub = expensiveName.getText().toString();
                intent.putExtra(Intent.EXTRA_SUBJECT, shareImageSub);
                intent.putExtra(Intent.EXTRA_TEXT, shareExpensiveImageDescription);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Uri photoURI = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", file);

                intent.putExtra(Intent.EXTRA_STREAM, photoURI);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setType("image/png");

                startActivity(Intent.createChooser(intent, "Share image via"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        } else if (item.getItemId() == R.id.wallpaper_image_expensive) {
            setWallpaper();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundPool.release();
        soundPool = null;
    }

    public boolean allPermissionResultCheck() {
        return is_storage_image_permitted;
    }

    public void requestPermissionStorageImages() {
        if (ContextCompat.checkSelfPermission(ExpensiveDetailActivity.this, required_permission[0]) == PackageManager.PERMISSION_GRANTED) {
            is_storage_image_permitted = true;
            downloadImage();
        } else {
            request_permission_launcher_storage_images.launch(required_permission[0]);
        }
    }
    private ActivityResultLauncher<String> request_permission_launcher_storage_images =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            is_storage_image_permitted = true;
                            downloadImage();
                        } else {
                            is_storage_image_permitted = false;
                            Toasty.error(ExpensiveDetailActivity.this, "Permission denied...!", Toast.LENGTH_LONG).show();
                            sendToSettingDialog();


                        }
                    });

    public void sendToSettingDialog() {
        new AlertDialog.Builder(ExpensiveDetailActivity.this)
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

    private void downloadImage() {
        soundPool.play(soundSaveImage, 1, 1, 0, 0, 1);
        drawable=(BitmapDrawable) expensiveImage.getDrawable();
        bitmap=drawable.getBitmap();

        FileOutputStream fileOutputStream=null;

        File sdCard = Environment.getExternalStorageDirectory();
        File Directory=new File(sdCard.getAbsolutePath()+ "/Download/Watches from Rolex");
        Directory.mkdir();

        String filename=String.format("%d.jpg",System.currentTimeMillis());
        File outfile=new File(Directory,filename);
        Toasty.success(ExpensiveDetailActivity.this, "Image Saved Successfully", Toast.LENGTH_LONG).show();
        Toasty.info(ExpensiveDetailActivity.this, "Image saved in Download/Watches from Rolex", Toast.LENGTH_LONG).show();
        try {
            fileOutputStream=new FileOutputStream(outfile);
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();

            Intent intent=new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(outfile));
            sendBroadcast(intent);

        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    private void setWallpaper() {
        new AlertDialog.Builder(ExpensiveDetailActivity.this)
                .setTitle("Alert for Permission")
                .setMessage("Allow app to set wallpaper")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
                        try {
                            wallpaperManager.setBitmap(bitmap);
                            soundPool.play(soundWallpaper, 1, 1, 0, 0, 1);
                            Toasty.success(ExpensiveDetailActivity.this, "Wallpaper Set Successfully", Toast.LENGTH_LONG).show();


                        } catch (IOException e) {
                            Toasty.error(ExpensiveDetailActivity.this, "Wallpaper Not Set", Toast.LENGTH_LONG).show();


                        }

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                    }
                })
                .show();

    }

    @Override
    public void onBackPressed() {
        finish();
        Animatoo.animateSwipeRight(ExpensiveDetailActivity.this);
    }

}