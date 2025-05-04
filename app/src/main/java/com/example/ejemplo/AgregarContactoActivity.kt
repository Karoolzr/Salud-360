package com.example.ejemplo

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AgregarContactoActivity : AppCompatActivity() {

    private lateinit var editTextNombre: EditText
    private lateinit var editTextTelefono: EditText
    private lateinit var buttonGuardar: Button
    private lateinit var listViewContactos: ListView

    private lateinit var database: DatabaseReference
    private lateinit var contactosList: MutableList<String>
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_contacto)

        // Inicializar vistas
        editTextNombre = findViewById(R.id.editTextNombre)
        editTextTelefono = findViewById(R.id.editTextTelefono)
        buttonGuardar = findViewById(R.id.buttonGuardar)
        listViewContactos = findViewById(R.id.listViewContactos)

        // Inicializar lista y adaptador
        contactosList = mutableListOf()
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, contactosList)
        listViewContactos.adapter = adapter

        // Inicializar referencia de Firebase
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        database = FirebaseDatabase.getInstance().getReference("usuarios/$userId/contactosEmergencia")

        // Cargar contactos existentes
        cargarContactos()

        buttonGuardar.setOnClickListener {
            guardarContacto()
        }
    }

    private fun guardarContacto() {
        val nombre = editTextNombre.text.toString().trim()
        val telefono = editTextTelefono.text.toString().trim()

        if (nombre.isEmpty() || telefono.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val contactoId = database.push().key!!

        val contacto = mapOf(
            "nombre" to nombre,
            "telefono" to telefono
        )

        database.child(contactoId).setValue(contacto).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "Contacto guardado", Toast.LENGTH_SHORT).show()
                editTextNombre.text.clear()
                editTextTelefono.text.clear()
                cargarContactos() // Actualizar lista después de guardar
            } else {
                Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cargarContactos() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                contactosList.clear()
                for (contactoSnapshot in snapshot.children) {
                    val nombre = contactoSnapshot.child("nombre").getValue(String::class.java) ?: ""
                    val telefono = contactoSnapshot.child("telefono").getValue(String::class.java) ?: ""
                    contactosList.add("$nombre - $telefono")
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AgregarContactoActivity, "Error al cargar contactos", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
