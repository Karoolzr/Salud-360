package com.example.ejemplo

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Agregarcitasactivity : AppCompatActivity() {

    private lateinit var etFechaCita: EditText
    private lateinit var etHoraCita: EditText
    private lateinit var etMotivoCita: EditText
    private lateinit var etLugarCita: EditText
    private lateinit var buttonRegistrarCita: Button
    private lateinit var buttonVerCitas: Button
    private lateinit var citasListView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.agregarcitasactivity)

        // Inicializamos los elementos
        etFechaCita = findViewById(R.id.etFechaCita)
        etHoraCita = findViewById(R.id.etHoraCita)
        etMotivoCita = findViewById(R.id.etMotivoCita)
        etLugarCita = findViewById(R.id.etLugarCita)
        buttonRegistrarCita = findViewById(R.id.buttonRegistrarCita)
        buttonVerCitas = findViewById(R.id.buttonVerCitas)
        citasListView = findViewById(R.id.citasListView)

        // Cuando el usuario hace clic en "Registrar Cita"
        buttonRegistrarCita.setOnClickListener {
            registrarCita()
        }

        // Cuando el usuario hace clic en "Ver Citas"
        buttonVerCitas.setOnClickListener {
            mostrarCitas()
        }
    }

    private fun registrarCita() {
        val fecha = etFechaCita.text.toString().trim()
        val hora = etHoraCita.text.toString().trim()
        val motivo = etMotivoCita.text.toString().trim()
        val lugar = etLugarCita.text.toString().trim()

        if (fecha.isEmpty() || hora.isEmpty() || motivo.isEmpty() || lugar.isEmpty()) {
            // Mostrar un mensaje si falta algún campo
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener el UID del usuario autenticado
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            // Crear un objeto Cita con los datos
            val cita = hashMapOf(
                "fecha" to fecha,
                "hora" to hora,
                "motivo" to motivo,
                "lugar" to lugar
            )

            // Guardar la cita en la base de datos de Firebase
            FirebaseFirestore.getInstance().collection("citas")
                .document(userId)
                .collection("registros")
                .add(cita)
                .addOnSuccessListener {
                    Toast.makeText(this, "Cita registrada exitosamente", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al registrar cita", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "No se pudo obtener el usuario autenticado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarCitas() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("citas")
                .document(userId)
                .collection("registros")
                .get()
                .addOnSuccessListener { queryDocumentSnapshots ->
                    val citasList = mutableListOf<String>()

                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty) {
                        for (document in queryDocumentSnapshots) {
                            val fecha = document.getString("fecha") ?: "No disponible"
                            val hora = document.getString("hora") ?: "No disponible"
                            val motivo = document.getString("motivo") ?: "No disponible"
                            val lugar = document.getString("lugar") ?: "No disponible"

                            val cita = "Fecha: $fecha, Hora: $hora, Motivo: $motivo, Lugar: $lugar"
                            citasList.add(cita)
                        }

                        // Mostrar las citas en el ListView
                        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, citasList)
                        citasListView.adapter = adapter
                    } else {
                        Toast.makeText(this, "No tienes citas guardadas", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al cargar las citas: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "No se pudo obtener el usuario autenticado", Toast.LENGTH_SHORT).show()
        }
    }
}
