package com.example.ejemplo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class assistente : AppCompatActivity() {

    private lateinit var etPrompt: EditText
    private lateinit var btnEnviar: Button
    private lateinit var tvRespuesta: TextView

    // Pega tu API Key aquí (obtenida de Google AI Studio)
    private val apiKey = "AIzaSyDagRfYqXP4O9KDd4seXNG0xgXHyWQJbf4"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assistente)

        etPrompt = findViewById(R.id.etPrompt)
        btnEnviar = findViewById(R.id.btnIngresar)
        tvRespuesta = findViewById(R.id.tvRespuesta)

        val generativeModel = GenerativeModel(
            modelName = "models/gemini-1.5-flash",
            apiKey = apiKey
        )

        btnEnviar.setOnClickListener {
            val userPrompt = etPrompt.text.toString()

            // Aquí modificamos el prompt para que solo responda temas de salud
            val promptModificado = """
                Eres un asistente experto en salud. 
                Solo debes responder preguntas relacionadas con medicina, bienestar, nutrición, enfermedades, síntomas, prevención, tratamientos, salud mental o hábitos saludables.
                Si te preguntan algo fuera de estos temas, responde educadamente que solo puedes hablar de salud.

                Pregunta del usuario: $userPrompt
            """.trimIndent()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = generativeModel.generateContent(promptModificado)
                    val textResponse = response.text ?: "Sin respuesta disponible."
                    runOnUiThread {
                        tvRespuesta.text = textResponse
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        tvRespuesta.text = "Error: ${e.message}"
                    }
                }
            }
        }
    }
}