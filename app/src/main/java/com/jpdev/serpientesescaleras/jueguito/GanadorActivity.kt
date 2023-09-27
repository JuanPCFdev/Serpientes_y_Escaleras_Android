package com.jpdev.serpientesescaleras.jueguito

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.jpdev.serpientesescaleras.R

class GanadorActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ganador)

        val btnPlayAgain = findViewById<Button>(R.id.btnPlayAgain)
        val txtWinner = findViewById<TextView>(R.id.winner)
        val mensaje = intent.extras?.getString("ganador")

        txtWinner.text = "FELICIDADES HA GANADO EL $mensaje"

        btnPlayAgain.setOnClickListener {
            val intent = Intent(this,JuegoActivity::class.java)
            startActivity(intent)
        }

    }
}