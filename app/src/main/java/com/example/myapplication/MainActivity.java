package com.example.myapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // ViewModel to survive configuration changes
    public static class EggViewModel extends ViewModel {
        public int eggCount;
    }


    private EggViewModel viewModel;
    private CardView eggCard;
    private ImageView eggImageView;
    private TextView counterTextView;
    private TextView titleTextView;
    private TextView instructionText;
    private ConstraintLayout mainLayout;
    private SharedPreferences preferences;
    private boolean isAnimating = false;
    private Vibrator vibrator;
    private Handler handler;
    private Runnable pulseRunnable;
    private final List<Animator> activeAnimators = new ArrayList<>();

    private static final String PREF_NAME = "EggCounterPrefs";
    private static final String COUNT_KEY = "egg_count";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(EggViewModel.class);

        // Initialize views
        eggCard = findViewById(R.id.eggCard);
        eggImageView = findViewById(R.id.eggImageView);
        counterTextView = findViewById(R.id.counterTextView);
        titleTextView = findViewById(R.id.titleTextView);
        instructionText = findViewById(R.id.instructionText);
        mainLayout = findViewById(R.id.mainConstraintLayout);

        // Initialize services
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Restore count (ViewModel has priority)
        if (viewModel.eggCount == 0) {
            viewModel.eggCount = preferences.getInt(COUNT_KEY, 0);
        }

        setupViews();
        updateUI();
        animateInstructionText();
    }
    private void showMilestoneToast() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            View toastView = getLayoutInflater().inflate(R.layout.custom_toast, null);
            TextView toastText = toastView.findViewById(R.id.toastText);
            toastText.setText("🎉 " + viewModel.eggCount + " Eggs Cracked!");

            Toast toast = new Toast(getApplicationContext());
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(toastView);
            toast.show();

            animateMilestoneCelebration();
        }, 300);
    }

    private void animateMilestoneCelebration() {
        ValueAnimator colorAnim = ValueAnimator.ofArgb(
                getResources().getColor(R.color.background_color, null),
                getResources().getColor(R.color.colorAccent, null),
                getResources().getColor(R.color.background_color, null)
        );

        colorAnim.setDuration(500);
        colorAnim.addUpdateListener(animator ->
                mainLayout.setBackgroundColor((int) animator.getAnimatedValue())
        );
        startAnimation(colorAnim);
    }

    private void setupViews() {
        eggImageView.setImageResource(R.drawable.egg_uncracked);
        eggCard.setOnClickListener(v -> {
            if (!isAnimating) crackEgg();
        });
    }

    private void animateInstructionText() {
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(instructionText, "alpha", 0f, 1f);
        fadeIn.setDuration(1000);
        startAnimation(fadeIn);

        handler = new Handler(Looper.getMainLooper());
        pulseRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isFinishing() && !isDestroyed() && viewModel.eggCount == 0) {
                    ObjectAnimator pulse = ObjectAnimator.ofFloat(instructionText, "alpha", 1f, 0.5f, 1f);
                    pulse.setDuration(1500);
                    startAnimation(pulse);
                    handler.postDelayed(this, 3000);
                }
            }
        };
        handler.postDelayed(pulseRunnable, 3000);
    }
    private void animateCounterChange() {
        counterTextView.animate()
                .scaleX(1.5f)
                .scaleY(1.5f)
                .setDuration(150)
                .withEndAction(() -> counterTextView.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150)
                        .start())
                .start();
    }

    private void crackEgg() {
        isAnimating = true;

        if (viewModel.eggCount == 0) {
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(instructionText, "alpha", 1f, 0f);
            fadeOut.setDuration(500);
            startAnimation(fadeOut);
        }

        // Card scale animation
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

        scaleSet.play(scaleDown).before(scaleUp).before(scaleNormal);
        scaleSet.setDuration(450);
        scaleSet.setInterpolator(new AccelerateDecelerateInterpolator());
        startAnimation(scaleSet);

        // Other animations
        ObjectAnimator elevate = ObjectAnimator.ofFloat(eggCard, "cardElevation", 12f, 24f, 12f);
        elevate.setDuration(450);
        startAnimation(elevate);

        ObjectAnimator shakeX = ObjectAnimator.ofFloat(mainLayout, "translationX", 0f, -5f, 5f, -5f, 5f, -3f, 3f, -2f, 2f, 0f);
        shakeX.setDuration(300);
        startAnimation(shakeX);

        eggImageView.setImageResource(R.drawable.egg_cracked);
        ObjectAnimator rotation = ObjectAnimator.ofFloat(eggImageView, "rotation", 0f, -2f, 2f, 0f);
        rotation.setDuration(300);
        startAnimation(rotation);

        // Vibration
        if (vibrator != null && vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(80);
            }
        }

        animateCounterChange();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            eggImageView.setImageResource(R.drawable.egg_uncracked);
            isAnimating = false;
        }, 800);

        // Update counts
        viewModel.eggCount++;
        preferences.edit().putInt(COUNT_KEY, viewModel.eggCount).apply();
        updateUI();

        if (viewModel.eggCount % 10 == 0) {
            showMilestoneToast();
        }
    }

    private void startAnimation(Animator animator) {
        activeAnimators.add(animator);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                activeAnimators.remove(animation);
            }
        });
        animator.start();
    }
    private void updateUI() {
        updateTitle();
        counterTextView.setText(String.valueOf(viewModel.eggCount));
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
            if (viewModel.eggCount >= milestones[i]) {
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



    // ... Keep all other methods (updateUI, updateTitle, animateCounterChange) the same ...

    @Override
    protected void onPause() {
        super.onPause();
        if (vibrator != null) vibrator.cancel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && pulseRunnable != null) {
            handler.removeCallbacks(pulseRunnable);
        }
        for (Animator animator : activeAnimators) {
            animator.cancel();
        }
    }
}