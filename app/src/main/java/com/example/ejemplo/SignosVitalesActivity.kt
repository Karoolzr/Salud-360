package com.example.ejemplo

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignosVitalesActivity : AppCompatActivity() {

    private lateinit var spinnerVitalSigns: Spinner
    private lateinit var editText: EditText
    private lateinit var buttonIngresar: Button
    private lateinit var listViewSignos: ListView

    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private lateinit var signosAdapter: ArrayAdapter<String>
    private var signosList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signos_vitales)

        // Inicializamos Firebase
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Vinculamos los elementos del XML con las variables
        spinnerVitalSigns = findViewById(R.id.spinnerVitalSigns)
        editText = findViewById(R.id.editText)
        buttonIngresar = findViewById(R.id.buttonIngresar)
        listViewSignos = findViewById(R.id.listViewSignos)

        // Configuramos el adapter para el ListView
        signosAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, signosList)
        listViewSignos.adapter = signosAdapter

        // Cuando el usuario haga clic en "Ingresar"
        buttonIngresar.setOnClickListener {
            ingresarSignoVital()
        }
    }

    private fun ingresarSignoVital() {
        val signoVital = spinnerVitalSigns.selectedItem.toString()
        val dato = editText.text.toString().trim()

        if (dato.isEmpty()) {
            // Si el campo de dato está vacío
            Toast.makeText(this, "Por favor ingrese un dato", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener UID del usuario autenticado
        val userId = mAuth.currentUser?.uid
        if (userId != null) {
            val signoId = database.reference.child("signosVitales").child(userId).push().key
            val signo = SignoVital(signoVital, dato)

            if (signoId != null) {
                // Guardamos el signo vital en Firebase Realtime Database
                database.reference.child("signosVitales").child(userId).child(signoId).setValue(signo)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Signo vital registrado exitosamente", Toast.LENGTH_SHORT).show()
                            // Limpiar el campo de dato después de registrar
                            editText.text.clear()

                            // Agregar el nuevo signo vital a la lista
                            val signoInfo = "Signo: $signoVital, Dato: $dato"
                            signosList.add(signoInfo)
                            signosAdapter.notifyDataSetChanged()
                        } else {
                            Toast.makeText(this, "Error al registrar signo vital", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        } else {
            Toast.makeText(this, "No se pudo obtener el usuario autenticado", Toast.LENGTH_SHORT).show()
        }
    }

    // Clase de datos para el signo vital
    data class SignoVital(val signoVital: String, val dato: String)
}

