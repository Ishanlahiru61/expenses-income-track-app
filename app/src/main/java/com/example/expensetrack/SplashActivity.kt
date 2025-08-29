package com.example.expensetrack

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable

class SplashActivity : AppCompatActivity() {

    private lateinit var pigAnimation: LottieAnimationView
    private lateinit var loadingAnimation: LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        enableEdgeToEdge()


        setContentView(R.layout.activity_splash)


        pigAnimation = findViewById(R.id.pig_animation)
        loadingAnimation = findViewById(R.id.loading_animation)


        pigAnimation.setAnimation(R.raw.pig)
        loadingAnimation.setAnimation(R.raw.loading)


        pigAnimation.playAnimation()
        loadingAnimation.playAnimation()


        pigAnimation.repeatCount = LottieDrawable.INFINITE
        loadingAnimation.repeatCount = LottieDrawable.INFINITE


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        Handler().postDelayed({
            val intent = Intent(this, nav_bar::class.java)
            startActivity(intent)
            finish()
        }, 3500)
    }
}
