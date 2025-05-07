package com.example.ejemplo

import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.ejemplo.databinding.ActivityAgregarMedicamentosBinding

data class Medicamento(
    val nombre: String = "",
    val tipo: String = "",
    val frecuencia: String = "",
    val diasTomar: String = "",
    val fechaInicio: String = ""
)

class AgregarMedicamentosActivity : AppCompatActivity() {

    private lateinit var spTipoMedicamento: Spinner
    private lateinit var spPorDiasTomar: Spinner
    private lateinit var spCadaQueHoras: Spinner
    private lateinit var etFechaInicio: EditText
    private lateinit var nombreMedicamento: EditText
    private lateinit var buttonRegistrarMedicamentos: Button
    private lateinit var listViewMedicamentos: ListView

    private lateinit var database: DatabaseReference
    private lateinit var medicamentosList: MutableList<String>
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var snapshotMap: MutableMap<Int, String> // Para guardar IDs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAgregarMedicamentosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener vistas
        spTipoMedicamento = findViewById(R.id.sp_tipo_medicamento)
        spPorDiasTomar = findViewById(R.id.sp_porDiasTomar)
        spCadaQueHoras = findViewById(R.id.sp_cadaQueHoras)
        etFechaInicio = findViewById(R.id.etFechaInicio)
        nombreMedicamento = findViewById(R.id.nombre_medicamento)
        buttonRegistrarMedicamentos = findViewById(R.id.buttonRegistrarMedicamentos)
        listViewMedicamentos = findViewById(R.id.listViewMedicamentos)

        // Configurar spinners
        val tiposMedicamentos = arrayOf("Pastilla", "Jarabe", "Inyección")
        val frecuencia = arrayOf("Cada 4 horas", "Cada 6 horas", "Cada 8 horas")
        val diasTomar = arrayOf("1 día", "3 días", "7 días")

        spTipoMedicamento.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tiposMedicamentos)
        spCadaQueHoras.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, frecuencia)
        spPorDiasTomar.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, diasTomar)

        // Inicializar Firebase
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        database = FirebaseDatabase.getInstance().getReference("usuarios/$userId/medicamentos")

        // Inicializar lista
        medicamentosList = mutableListOf()
        snapshotMap = mutableMapOf()
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, medicamentosList)
        listViewMedicamentos.adapter = adapter

        // Eventos
        buttonRegistrarMedicamentos.setOnClickListener {
            guardarMedicamento()
        }

        listViewMedicamentos.setOnItemLongClickListener { _, _, position, _ ->
            mostrarOpciones(position)
            true
        }

        cargarMedicamentos()
    }

    private fun guardarMedicamento() {
        val nombre = nombreMedicamento.text.toString().trim()
        val tipo = spTipoMedicamento.selectedItem.toString()
        val frecuencia = spCadaQueHoras.selectedItem.toString()
        val diasTomar = spPorDiasTomar.selectedItem.toString()
        val fechaInicio = etFechaInicio.text.toString().trim()

        if (nombre.isEmpty() || fechaInicio.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val medicamentoId = database.push().key!!
        val medicamento = Medicamento(nombre, tipo, frecuencia, diasTomar, fechaInicio)

        database.child(medicamentoId).setValue(medicamento).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "Medicamento guardado", Toast.LENGTH_SHORT).show()
                limpiarCampos()
                cargarMedicamentos()
            } else {
                Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cargarMedicamentos() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                medicamentosList.clear()
                snapshotMap.clear()
                var index = 0
                for (medicamentoSnapshot in snapshot.children) {
                    val medicamento = medicamentoSnapshot.getValue(Medicamento::class.java)
                    medicamento?.let {
                        val info = "Medicamento: ${it.nombre}\nTipo: ${it.tipo}\nFrecuencia: ${it.frecuencia}\n" +
                                "Tomar por: ${it.diasTomar}\nFecha de Inicio: ${it.fechaInicio}"
                        medicamentosList.add(info)
                        snapshotMap[index] = medicamentoSnapshot.key!! // guardar el ID en la posición
                        index++
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AgregarMedicamentosActivity, "Error al cargar medicamentos", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun limpiarCampos() {
        nombreMedicamento.text.clear()
        etFechaInicio.text.clear()
        spTipoMedicamento.setSelection(0)
        spCadaQueHoras.setSelection(0)
        spPorDiasTomar.setSelection(0)
    }

    private fun mostrarOpciones(position: Int) {
        val opciones = arrayOf("Editar", "Eliminar")
        val idFirebase = snapshotMap[position] ?: return

        AlertDialog.Builder(this)
            .setTitle("Selecciona una opción")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> editarMedicamento(idFirebase) // Editar
                    1 -> eliminarMedicamento(idFirebase) // Eliminar
                }
            }.show()
    }

    private fun eliminarMedicamento(id: String) {
        database.child(id).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "Medicamento eliminado", Toast.LENGTH_SHORT).show()
                cargarMedicamentos()
            } else {
                Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun editarMedicamento(id: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Editar Medicamento")

        val layout = layoutInflater.inflate(R.layout.dialog_editar_medicamento, null)

        val nombreInput = layout.findViewById<EditText>(R.id.editNombre)
        val fechaInput = layout.findViewById<EditText>(R.id.editFecha)

        builder.setView(layout)

        builder.setPositiveButton("Guardar") { _, _ ->
            val nombreNuevo = nombreInput.text.toString().trim()
            val fechaNueva = fechaInput.text.toString().trim()

            if (nombreNuevo.isEmpty() || fechaNueva.isEmpty()) {
                Toast.makeText(this, "No puedes dejar campos vacíos", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            database.child(id).child("nombre").setValue(nombreNuevo)
            database.child(id).child("fechaInicio").setValue(fechaNueva)

            Toast.makeText(this, "Medicamento actualizado", Toast.LENGTH_SHORT).show()
            cargarMedicamentos()
        }

        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }
}

