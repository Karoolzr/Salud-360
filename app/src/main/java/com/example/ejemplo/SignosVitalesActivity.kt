package com.example.ejemplo

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SignosVitalesActivity : AppCompatActivity() {

    private lateinit var spinnerVitalSigns: Spinner
    private lateinit var editText: EditText
    private lateinit var buttonIngresar: Button
    private lateinit var listViewSignos: ListView

    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private lateinit var signosAdapter: ArrayAdapter<String>
    private var signosList = mutableListOf<String>()
    private var signosMap = mutableMapOf<String, SignoVital>() // Para mapear los ID de Firebase a los signos vitales

    private var signoIdSeleccionado: String? = null

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

        // Cargar los signos vitales al iniciar la actividad
        cargarSignosDesdeFirebase()

        // Cuando el usuario haga clic en "Ingresar"
        buttonIngresar.setOnClickListener {
            if (signoIdSeleccionado == null) {
                ingresarSignoVital()
            } else {
                editarSignoVital()
            }
        }

        // Configurar el evento para seleccionar un signo vital de la lista
        listViewSignos.setOnItemClickListener { _, _, position, _ ->
            val selectedSigno = signosList[position]
            val signoId = signosMap.filterValues { it.toDisplayString() == selectedSigno }.keys.firstOrNull()

            if (signoId != null) {
                signoIdSeleccionado = signoId
                val signoVital = signosMap[signoId]
                if (signoVital != null) {
                    editText.setText(signoVital.dato)
                    spinnerVitalSigns.setSelection(getSpinnerPosition(signoVital.signoVital))
                    buttonIngresar.text = "Editar"
                }
            }
        }

        // Configurar el evento para eliminar un signo vital
        listViewSignos.setOnItemLongClickListener { _, _, position, _ ->
            val selectedSigno = signosList[position]
            val signoId = signosMap.filterValues { it.toDisplayString() == selectedSigno }.keys.firstOrNull()

            if (signoId != null) {
                mostrarDialogoEliminar(signoId)
            }

            true
        }
    }

    private fun cargarSignosDesdeFirebase() {
        val userId = mAuth.currentUser?.uid ?: return

        database.reference.child("signosVitales").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                signosList.clear()
                signosMap.clear()

                for (signoSnapshot in snapshot.children) {
                    val signo = signoSnapshot.getValue(SignoVital::class.java)
                    val signoId = signoSnapshot.key

                    if (signo != null && signoId != null) {
                        val display = signo.toDisplayString()
                        signosList.add(display)
                        signosMap[signoId] = signo
                    }
                }

                signosAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SignosVitalesActivity, "Error al cargar datos", Toast.LENGTH_SHORT).show()
            }
        })
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
                            val signoInfo = signo.toDisplayString()
                            signosList.add(signoInfo)
                            signosMap[signoId] = signo
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

    private fun editarSignoVital() {
        val signoVital = spinnerVitalSigns.selectedItem.toString()
        val dato = editText.text.toString().trim()

        if (dato.isEmpty()) {
            // Si el campo de dato está vacío
            Toast.makeText(this, "Por favor ingrese un dato", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = mAuth.currentUser?.uid
        if (userId != null && signoIdSeleccionado != null) {
            val signo = SignoVital(signoVital, dato)

            // Actualizamos el signo vital en Firebase
            database.reference.child("signosVitales").child(userId).child(signoIdSeleccionado!!).setValue(signo)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Signo vital actualizado exitosamente", Toast.LENGTH_SHORT).show()
                        // Limpiar el campo de dato después de actualizar
                        editText.text.clear()

                        // Actualizar la lista local
                        val signoInfo = signo.toDisplayString()
                        signosList[signosList.indexOfFirst { it == signo.toDisplayString() }] = signoInfo
                        signosAdapter.notifyDataSetChanged()

                        // Limpiar selección
                        limpiarFormulario()
                    } else {
                        Toast.makeText(this, "Error al actualizar signo vital", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this, "No se pudo obtener el usuario autenticado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun limpiarFormulario() {
        editText.text.clear()
        spinnerVitalSigns.setSelection(0)
        signoIdSeleccionado = null
        buttonIngresar.text = "Ingresar"
    }

    private fun getSpinnerPosition(signoVital: String): Int {
        val spinnerAdapter = spinnerVitalSigns.adapter as ArrayAdapter<String>
        return spinnerAdapter.getPosition(signoVital)
    }

    private fun mostrarDialogoEliminar(signoId: String) {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Eliminar Signo Vital")
        dialog.setMessage("¿Estás seguro de que quieres eliminar este signo vital?")
        dialog.setPositiveButton("Eliminar") { _, _ ->
            eliminarSignoVital(signoId)
        }
        dialog.setNegativeButton("Cancelar", null)
        dialog.show()
    }

    private fun eliminarSignoVital(signoId: String) {
        val userId = mAuth.currentUser?.uid
        if (userId != null) {
            // Eliminamos el signo vital de Firebase
            database.reference.child("signosVitales").child(userId).child(signoId).removeValue()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Signo vital eliminado exitosamente", Toast.LENGTH_SHORT).show()
                        // Actualizamos la lista local
                        signosList.removeAll { it == signosMap[signoId]?.toDisplayString() }
                        signosMap.remove(signoId)
                        signosAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this, "Error al eliminar signo vital", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this, "No se pudo obtener el usuario autenticado", Toast.LENGTH_SHORT).show()
        }
    }

    data class SignoVital(val signoVital: String = "", val dato: String = "") {
        fun toDisplayString(): String = "Signo: $signoVital, Dato: $dato"
    }
}
