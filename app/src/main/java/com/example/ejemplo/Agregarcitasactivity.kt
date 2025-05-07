package com.example.ejemplo

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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

    private lateinit var citasList: MutableList<String>
    private lateinit var citasIds: MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.agregarcitasactivity)

        // Inicializar los elementos
        etFechaCita = findViewById(R.id.etFechaCita)
        etHoraCita = findViewById(R.id.etHoraCita)
        etMotivoCita = findViewById(R.id.etMotivoCita)
        etLugarCita = findViewById(R.id.etLugarCita)
        buttonRegistrarCita = findViewById(R.id.buttonRegistrarCita)
        buttonVerCitas = findViewById(R.id.buttonVerCitas)
        citasListView = findViewById(R.id.citasListView)

        citasList = mutableListOf()
        citasIds = mutableListOf()

        buttonRegistrarCita.setOnClickListener {
            registrarCita()
        }

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

        // Obtener el ID del usuario
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            // Verificar si ya existe una cita con la misma fecha y hora
            FirebaseFirestore.getInstance().collection("citas")
                .document(userId)
                .collection("registros")
                .whereEqualTo("fecha", fecha)
                .whereEqualTo("hora", hora)
                .get()
                .addOnSuccessListener { queryDocumentSnapshots ->
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty) {
                        // Si ya existe una cita con la misma fecha y hora
                        Toast.makeText(this, "Ya tienes una cita registrada para esta fecha y hora", Toast.LENGTH_SHORT).show()
                    } else {
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
                                // Mostrar un mensaje de éxito
                                Toast.makeText(this, "Cita registrada exitosamente", Toast.LENGTH_SHORT).show()

                                // Limpiar los campos después de registrar la cita
                                etFechaCita.text.clear()
                                etHoraCita.text.clear()
                                etMotivoCita.text.clear()
                                etLugarCita.text.clear()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error al registrar cita", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al verificar las citas existentes", Toast.LENGTH_SHORT).show()
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
                    citasList.clear()
                    citasIds.clear()

                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty) {
                        for (document in queryDocumentSnapshots) {
                            val fecha = document.getString("fecha") ?: "No disponible"
                            val hora = document.getString("hora") ?: "No disponible"
                            val motivo = document.getString("motivo") ?: "No disponible"
                            val lugar = document.getString("lugar") ?: "No disponible"
                            val citaId = document.id

                            val cita = "Fecha: $fecha, Hora: $hora, Motivo: $motivo, Lugar: $lugar"
                            citasList.add(cita)
                            citasIds.add(citaId)
                        }

                        // Mostrar las citas en el ListView
                        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, citasList)
                        citasListView.adapter = adapter

                        // Agregar un click listener para editar/eliminar
                        citasListView.setOnItemClickListener { _, _, position, _ ->
                            val citaId = citasIds[position]
                            val cita = citasList[position]

                            // Mostrar opciones de editar o eliminar
                            mostrarOpcionesCita(citaId, cita)
                        }
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

    private fun mostrarOpcionesCita(citaId: String, cita: String) {
        val opciones = arrayOf("Editar", "Eliminar")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Opciones")
        builder.setItems(opciones) { dialog, which ->
            when (which) {
                0 -> editarCita(citaId) // Editar
                1 -> eliminarCita(citaId) // Eliminar
            }
        }
        builder.show()
    }

    private fun editarCita(citaId: String) {
        // Recuperar la cita actual desde Firestore
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("citas")
                .document(userId)
                .collection("registros")
                .document(citaId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val fecha = documentSnapshot.getString("fecha") ?: ""
                        val hora = documentSnapshot.getString("hora") ?: ""
                        val motivo = documentSnapshot.getString("motivo") ?: ""
                        val lugar = documentSnapshot.getString("lugar") ?: ""

                        // Aquí puedes mostrar los datos de la cita en los EditText para que el usuario los edite
                        etFechaCita.setText(fecha)
                        etHoraCita.setText(hora)
                        etMotivoCita.setText(motivo)
                        etLugarCita.setText(lugar)

                        // Cambiar el texto del botón para indicar que se está editando
                        buttonRegistrarCita.setText("Actualizar Cita")

                        // Actualizar la cita después de editar
                        buttonRegistrarCita.setOnClickListener {
                            val fechaActualizada = etFechaCita.text.toString().trim()
                            val horaActualizada = etHoraCita.text.toString().trim()
                            val motivoActualizado = etMotivoCita.text.toString().trim()
                            val lugarActualizado = etLugarCita.text.toString().trim()

                            val citaActualizada = hashMapOf(
                                "fecha" to fechaActualizada,
                                "hora" to horaActualizada,
                                "motivo" to motivoActualizado,
                                "lugar" to lugarActualizado
                            )

                            FirebaseFirestore.getInstance().collection("citas")
                                .document(userId)
                                .collection("registros")
                                .document(citaId)
                                .set(citaActualizada)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Cita actualizada exitosamente", Toast.LENGTH_SHORT).show()
                                    mostrarCitas()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Error al actualizar cita", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                }
        }
    }

    private fun eliminarCita(citaId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("citas")
                .document(userId)
                .collection("registros")
                .document(citaId)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Cita eliminada exitosamente", Toast.LENGTH_SHORT).show()
                    mostrarCitas()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al eliminar cita", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
