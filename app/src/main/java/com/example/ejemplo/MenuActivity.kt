package com.example.ejemplo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MenuActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private var telefonoEmergencia: String = ""  // Variable para almacenar el número de teléfono de emergencia

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menuactivity)  // Asegúrate de que el layout sea correcto

        // Inicializar la referencia de Firebase
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            database = FirebaseDatabase.getInstance().getReference("usuarios/$userId/contactosEmergencia")
        }

        // Enlazar los botones con sus acciones
        val buttonAgregarMedicamentos = findViewById<Button>(R.id.buttonAgregarMedicamentos)
        val buttonAgregarSignos = findViewById<Button>(R.id.buttonAgregarSignos)
        val buttonAgregarCitas = findViewById<Button>(R.id.buttonAgregarCitas)
        val buttonAgregarContacto = findViewById<Button>(R.id.buttonAgregarContacto)
        val buttonCalculadora = findViewById<Button>(R.id.buttonCalculadora)
        val buttonAsistente = findViewById<Button>(R.id.buttonAsistente)
        val buttonLlamar = findViewById<Button>(R.id.buttonLlamar)

        // Establecer listeners para los botones
        buttonAgregarMedicamentos.setOnClickListener {
            val intent = Intent(this, AgregarMedicamentosActivity::class.java)
            startActivity(intent)
        }

        buttonAgregarSignos.setOnClickListener {
            val intent = Intent(this, SignosVitalesActivity::class.java)
            startActivity(intent)
        }

        buttonAgregarCitas.setOnClickListener {
            val intent = Intent(this, Agregarcitasactivity::class.java)
            startActivity(intent)
        }

        buttonAgregarContacto.setOnClickListener {
            val intent = Intent(this, AgregarContactoActivity::class.java)
            startActivity(intent)
        }

        buttonCalculadora.setOnClickListener {
            val intent = Intent(this, CalculadoraActivity::class.java)
            startActivity(intent)
        }

        buttonAsistente.setOnClickListener {
            val intent = Intent(this, assistente::class.java)
            startActivity(intent)
        }

        // Cargar el número de teléfono de emergencia
        cargarTelefonoEmergencia()

        // Acción al hacer clic en "Llamar" y enviar mensaje de ayuda
        buttonLlamar.setOnClickListener {
            if (telefonoEmergencia.isNotEmpty()) {
                val mensajeAyuda = "¡Ayuda urgente! Necesito asistencia, por favor."

                // Intent para realizar la llamada
                val intentLlamada = Intent(Intent.ACTION_CALL, Uri.parse("tel:$telefonoEmergencia"))

                // Intent para enviar un SMS
                val intentMensaje = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$telefonoEmergencia"))
                intentMensaje.putExtra("sms_body", mensajeAyuda)

                // Verificar que la aplicación para realizar la llamada está disponible
                if (intentLlamada.resolveActivity(packageManager) != null) {
                    startActivity(intentLlamada)
                } else {
                    Toast.makeText(this, "No se puede realizar la llamada, verifica los permisos.", Toast.LENGTH_SHORT).show()
                }

                // Verificar que la aplicación para enviar SMS está disponible
                if (intentMensaje.resolveActivity(packageManager) != null) {
                    startActivity(intentMensaje)
                } else {
                    Toast.makeText(this, "No se puede enviar el mensaje, verifica los permisos.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "No se ha registrado un contacto de emergencia.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cargarTelefonoEmergencia() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            database.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Verificar si hay contactos registrados
                    if (snapshot.exists()) {
                        for (contactoSnapshot in snapshot.children) {
                            val telefono = contactoSnapshot.child("telefono").getValue(String::class.java)
                            if (telefono != null) {
                                telefonoEmergencia = telefono
                                break  // Salir del bucle una vez que encontramos el primer contacto
                            }
                        }
                    } else {
                        Toast.makeText(this@MenuActivity, "No hay contactos de emergencia registrados", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MenuActivity, "Error al cargar el contacto de emergencia", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

}


