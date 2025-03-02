package com.example.myapplication;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

public class MainActivity extends AppCompatActivity {

    private int eggCount = 0;
    private CardView eggCard;
    private ImageView eggImageView;
    private TextView counterTextView;
    private TextView titleTextView;
    private TextView instructionText;
    private ConstraintLayout mainLayout;
    private SharedPreferences preferences;
    private boolean isAnimating = false;
    private Vibrator vibrator;

    private static final String PREF_NAME = "EggCounterPrefs";
    private static final String COUNT_KEY = "egg_count";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        eggCard = findViewById(R.id.eggCard);
        eggImageView = findViewById(R.id.eggImageView);
        counterTextView = findViewById(R.id.counterTextView);
        titleTextView = findViewById(R.id.titleTextView);
        instructionText = findViewById(R.id.instructionText);
        mainLayout = findViewById(R.id.mainConstraintLayout);
        // Initialize vibrator service
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Initialize SharedPreferences
        preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        eggCount = preferences.getInt(COUNT_KEY, 0);

        setupViews();
        updateUI();

        // Initial animation
        animateInstructionText();
    }

    private void setupViews() {
        // Set initial egg image (uncracked)
        eggImageView.setImageResource(R.drawable.egg_uncracked);

        // Card click listener with ripple effect
        eggCard.setOnClickListener(v -> {
            if (!isAnimating) {
                crackEgg();
            }
        });
    }

    private void animateInstructionText() {
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(instructionText, "alpha", 0f, 1f);
        fadeIn.setDuration(1000);
        fadeIn.start();

        // Pulse animation
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing() && eggCount == 0) {
                    ObjectAnimator pulse = ObjectAnimator.ofFloat(instructionText, "alpha", 1f, 0.5f, 1f);
                    pulse.setDuration(1500);
                    pulse.start();
                    handler.postDelayed(this, 3000);
                }
            }
        }, 3000);
    }

    private void crackEgg() {
        isAnimating = true;

        // Hide instruction text after first crack
        if (eggCount == 0) {
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(instructionText, "alpha", 1f, 0f);
            fadeOut.setDuration(500);
            fadeOut.start();
        }

        // Animate card scale
        AnimatorSet scaleSet = new AnimatorSet();
        ValueAnimator scaleDown = ValueAnimator.ofFloat(1f, 0.85f);
        ValueAnimator scaleUp = ValueAnimator.ofFloat(0.85f, 1.05f);
        ValueAnimator scaleNormal = ValueAnimator.ofFloat(1.05f, 1f);

        scaleDown.addUpdateListener(a -> {
            float value = (float) a.getAnimatedValue();
            eggCard.setScaleX(value);
            eggCard.setScaleY(value);
        });

        scaleUp.addUpdateListener(a -> {
            float value = (float) a.getAnimatedValue();
            eggCard.setScaleX(value);
            eggCard.setScaleY(value);
        });

        scaleNormal.addUpdateListener(a -> {
            float value = (float) a.getAnimatedValue();
            eggCard.setScaleX(value);
            eggCard.setScaleY(value);
        });

        scaleSet.play(scaleDown).before(scaleUp);
        scaleSet.play(scaleUp).before(scaleNormal);
        scaleDown.setDuration(150);
        scaleUp.setDuration(200);
        scaleNormal.setDuration(100);
        scaleSet.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleSet.start();

        // Elevate card temporarily
        ObjectAnimator elevate = ObjectAnimator.ofFloat(eggCard, "cardElevation", 12f, 24f, 12f);
        elevate.setDuration(450);
        elevate.start();

        // Subtle screen shake effect
        ObjectAnimator shakeX = ObjectAnimator.ofFloat(mainLayout, "translationX", 0f, -5f, 5f, -5f, 5f, -3f, 3f, -2f, 2f, 0f);
        shakeX.setDuration(300);
        shakeX.start();

        // Change image to cracked egg with rotation
        eggImageView.setImageResource(R.drawable.egg_cracked);
        ObjectAnimator rotation = ObjectAnimator.ofFloat(eggImageView, "rotation", 0f, -2f, 2f, 0f);
        rotation.setDuration(300);
        rotation.start();

        // Enhanced haptic feedback
        if (vibrator != null && vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(80);
            }
        }

        // Counter animation
        animateCounterChange();

        // Reset to uncracked egg after delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            eggImageView.setImageResource(R.drawable.egg_uncracked);
            isAnimating = false;
        }, 800);

        // Update counter
        eggCount++;
        preferences.edit().putInt(COUNT_KEY, eggCount).apply();
        updateUI();

        // Milestone messages
        if (eggCount % 10 == 0) {
            showMilestoneToast();
        }
    }

    private void animateCounterChange() {
        // Prepare the next number
        counterTextView.setText(String.format("%d", eggCount + 1));

        // Animate the counter
        counterTextView.animate()
                .scaleX(1.5f)
                .scaleY(1.5f)
                .setDuration(150)
                .withEndAction(() ->
                        counterTextView.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(150)
                                .start())
                .start();
    }

    private void updateUI() {
        updateTitle();
    }

    private void updateTitle() {
        int[] milestones = {100, 50, 25, 10};
        String[] titles = {
                "🥚 Master Chef",
                "🍳 Pro Cracker",
                "👩🍳 Skilled Breaker",
                "🐣 Newbie"
        };

        String oldTitle = titleTextView.getText().toString();
        String newTitle = "🥚 Beginner";

        for (int i = 0; i < milestones.length; i++) {
            if (eggCount >= milestones[i]) {
                newTitle = titles[i];
                break;
            }
        }

        // Make a final copy for use in the lambda expression.
        final String finalNewTitle = newTitle;

        // Animate title change if it's different
        if (!oldTitle.equals(finalNewTitle)) {
            titleTextView.animate()
                    .alpha(0f)
                    .setDuration(150)
                    .withEndAction(() -> {
                        titleTextView.setText(finalNewTitle);
                        titleTextView.animate()
                                .alpha(1f)
                                .setDuration(300)
                                .start();
                    })
                    .start();
        }
    }

    private void showMilestoneToast() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            View toastView = getLayoutInflater().inflate(R.layout.custom_toast, null);
            TextView toastText = toastView.findViewById(R.id.toastText);
            toastText.setText("🎉 " + eggCount + " Eggs Cracked!");

            Toast toast = new Toast(getApplicationContext());
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(toastView);
            toast.show();

            // Extra celebration animation for milestones
            animateMilestoneCelebration();
        }, 300);
    }

    private void animateMilestoneCelebration() {
        // Flash background color briefly for celebration
        ValueAnimator colorAnim = ValueAnimator.ofArgb(
                getResources().getColor(R.color.background_color, null),
                getResources().getColor(R.color.colorAccent, null),
                getResources().getColor(R.color.background_color, null)
        );

        colorAnim.setDuration(500);
        colorAnim.addUpdateListener(animator -> {
            int color = (int) animator.getAnimatedValue();
            // We'd need to set this on a view that can have its background color changed
            // mainLayout.setBackgroundColor(color);
        });
        colorAnim.start();
    }
}