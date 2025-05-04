package com.example.ejemplo

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.ejemplo.databinding.ActivityAgregarMedicamentosBinding

class AgregarMedicamentosActivity : AppCompatActivity() {

    private lateinit var spTipoMedicamento: Spinner
    private lateinit var spPorDiasTomar: Spinner
    private lateinit var spCadaQueHoras: Spinner
    private lateinit var etFechaInicio: EditText
    private lateinit var nombreMedicamento: EditText
    private lateinit var buttonRegistrarMedicamentos: Button
    private lateinit var listViewMedicamentos: ListView

    private val medicamentosList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAgregarMedicamentosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener las vistas del XML
        spTipoMedicamento = findViewById(R.id.sp_tipo_medicamento)
        spPorDiasTomar = findViewById(R.id.sp_porDiasTomar)
        spCadaQueHoras = findViewById(R.id.sp_cadaQueHoras)
        etFechaInicio = findViewById(R.id.etFechaInicio)
        nombreMedicamento = findViewById(R.id.nombre_medicamento)
        buttonRegistrarMedicamentos = findViewById(R.id.buttonRegistrarMedicamentos)
        listViewMedicamentos = findViewById(R.id.listViewMedicamentos)

        // Configurar Spinners con adaptadores
        val tiposMedicamentos = arrayOf("Pastilla", "Jarabe", "Inyección")
        val frecuencia = arrayOf("Cada 4 horas", "Cada 6 horas", "Cada 8 horas")
        val diasTomar = arrayOf("1 día", "3 días", "7 días")

        val adapterTipoMedicamento = ArrayAdapter(this, android.R.layout.simple_spinner_item, tiposMedicamentos)
        val adapterFrecuencia = ArrayAdapter(this, android.R.layout.simple_spinner_item, frecuencia)
        val adapterDiasTomar = ArrayAdapter(this, android.R.layout.simple_spinner_item, diasTomar)

        adapterTipoMedicamento.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapterFrecuencia.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapterDiasTomar.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spTipoMedicamento.adapter = adapterTipoMedicamento
        spCadaQueHoras.adapter = adapterFrecuencia
        spPorDiasTomar.adapter = adapterDiasTomar

        // Configurar el botón para registrar el medicamento
        buttonRegistrarMedicamentos.setOnClickListener {
            registrarMedicamento()
        }

        // Configurar ListView para mostrar los medicamentos registrados
        val adapterMedicamentos = ArrayAdapter(this, android.R.layout.simple_list_item_1, medicamentosList)
        listViewMedicamentos.adapter = adapterMedicamentos
    }

    private fun registrarMedicamento() {
        val medicamento = nombreMedicamento.text.toString()
        val tipoMedicamento = spTipoMedicamento.selectedItem.toString()
        val frecuencia = spCadaQueHoras.selectedItem.toString()
        val diasTomar = spPorDiasTomar.selectedItem.toString()
        val fechaInicio = etFechaInicio.text.toString()

        if (medicamento.isNotBlank() && fechaInicio.isNotBlank()) {
            val medicamentoInfo = "Medicamento: $medicamento\nTipo: $tipoMedicamento\n" +
                    "Frecuencia: $frecuencia\nTomar por: $diasTomar\nFecha de Inicio: $fechaInicio"

            medicamentosList.add(medicamentoInfo)
            (listViewMedicamentos.adapter as ArrayAdapter<*>).notifyDataSetChanged()

            // Limpiar los campos después de agregar el medicamento
            nombreMedicamento.text.clear()
            etFechaInicio.text.clear()
            spTipoMedicamento.setSelection(0)
            spCadaQueHoras.setSelection(0)
            spPorDiasTomar.setSelection(0)

            // Mostrar un mensaje o realizar otra acción después de agregar el medicamento si es necesario.
        }
    }
}
