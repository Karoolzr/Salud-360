package com.example.ejemplo

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.widget.*
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
    private lateinit var contactosMap: MutableMap<String, String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_contacto)

        editTextNombre = findViewById(R.id.editTextNombre)
        editTextTelefono = findViewById(R.id.editTextTelefono)
        buttonGuardar = findViewById(R.id.buttonGuardar)
        listViewContactos = findViewById(R.id.listViewContactos)

        // 🔒 Limitar a 10 caracteres numéricos
        editTextTelefono.filters = arrayOf(InputFilter.LengthFilter(10))
        editTextTelefono.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(editable: Editable?) {
                if (editable != null && editable.length > 10) {
                    editTextTelefono.setText(editable.substring(0, 10))
                    editTextTelefono.setSelection(10)
                }
            }
        })

        contactosList = mutableListOf()
        contactosMap = mutableMapOf()
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, contactosList)
        listViewContactos.adapter = adapter

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        database = FirebaseDatabase.getInstance().getReference("usuarios/$userId/contactosEmergencia")

        cargarContactos()

        buttonGuardar.setOnClickListener {
            guardarContacto()
        }

        listViewContactos.setOnItemClickListener { _, _, position, _ ->
            val contacto = contactosList[position]
            val nombre = contacto.split(" - ")[0]
            val telefono = contacto.split(" - ")[1]
            mostrarOpciones(nombre, telefono)
        }
    }

    private fun guardarContacto() {
        val nombre = editTextNombre.text.toString().trim()
        val telefono = editTextTelefono.text.toString().trim()

        if (nombre.isEmpty() || telefono.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (telefono.length != 10) {
            Toast.makeText(this, "El número debe tener exactamente 10 dígitos", Toast.LENGTH_SHORT).show()
            return
        }

        if (contactosMap.containsKey(nombre) || contactosMap.containsValue(telefono)) {
            Toast.makeText(this, "Este contacto ya está registrado", Toast.LENGTH_SHORT).show()
            return
        }

        val contactoId = database.push().key!!
        val contacto = mapOf("nombre" to nombre, "telefono" to telefono)

        database.child(contactoId).setValue(contacto).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "Contacto guardado", Toast.LENGTH_SHORT).show()
                editTextNombre.text.clear()
                editTextTelefono.text.clear()
                cargarContactos()
            } else {
                Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cargarContactos() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                contactosList.clear()
                contactosMap.clear()
                for (contactoSnapshot in snapshot.children) {
                    val nombre = contactoSnapshot.child("nombre").getValue(String::class.java) ?: ""
                    val telefono = contactoSnapshot.child("telefono").getValue(String::class.java) ?: ""
                    contactosList.add("$nombre - $telefono")
                    contactosMap[nombre] = telefono
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AgregarContactoActivity, "Error al cargar contactos", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun mostrarOpciones(nombre: String, telefono: String) {
        val options = arrayOf("Editar", "Eliminar")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Seleccionar acción")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> editarContacto(nombre, telefono)
                    1 -> eliminarContacto(nombre)
                }
            }
            .show()
    }

    private fun editarContacto(nombre: String, telefono: String) {
        editTextNombre.setText(nombre)
        editTextTelefono.setText(telefono)
        eliminarContacto(nombre)
        buttonGuardar.setOnClickListener {
            guardarContacto()
        }
    }

    private fun eliminarContacto(nombre: String) {
        obtenerIdDeContacto(nombre) { contactoId ->
            if (contactoId != null) {
                database.child(contactoId).removeValue().addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Contacto eliminado", Toast.LENGTH_SHORT).show()
                        cargarContactos()
                    } else {
                        Toast.makeText(this, "Error al eliminar el contacto", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Contacto no encontrado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun obtenerIdDeContacto(nombre: String, callback: (String?) -> Unit) {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (contactoSnapshot in snapshot.children) {
                    val storedNombre = contactoSnapshot.child("nombre").getValue(String::class.java)
                    if (storedNombre == nombre) {
                        callback(contactoSnapshot.key)
                        return
                    }
                }
                callback(null)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AgregarContactoActivity, "Error al obtener el ID del contacto", Toast.LENGTH_SHORT).show()
                callback(null)
            }
        })
    }
}


