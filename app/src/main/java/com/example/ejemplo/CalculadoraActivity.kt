package com.example.ejemplo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.ejemplo.databinding.ActivityCalculadoraBinding

class CalculadoraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalculadoraBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalculadoraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarSelectores()
    }

    private fun configurarSelectores() {
        // Definir valores mínimo y máximo
        binding.weightPicker.minValue = 30
        binding.weightPicker.maxValue = 150
        binding.weightPicker.value = 70

        binding.heightPicker.minValue = 100
        binding.heightPicker.maxValue = 250
        binding.heightPicker.value = 170

        // Establecer listeners para los cambios
        binding.weightPicker.setOnValueChangedListener { _, _, _ -> calcularIMC() }
        binding.heightPicker.setOnValueChangedListener { _, _, _ -> calcularIMC() }
    }

    private fun calcularIMC() {
        // Obtener valores
        val altura = binding.heightPicker.value.toDouble() / 100
        val peso = binding.weightPicker.value.toDouble()

        // Calcular
        val imc = peso / (altura * altura)

        // Mostrar
        binding.resultsTV.text = String.format("Tu IMC es de: %.2f", imc)
        binding.healthyTV.text = String.format("Considerado: %s", mensajeSaludable(imc))
    }

    private fun mensajeSaludable(imc: Double): String {
        return when {
            imc < 18.5 -> "Bajo peso"
            imc < 25.0 -> "Sano"
            imc < 30.0 -> "Sobrepeso"
            else -> "Obeso"
        }
    }
}
