package com.example.ejemplo


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class MenuActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private val PERMISSION_REQUEST_CALL_PHONE = 1
    private val PERMISSION_REQUEST_SEND_SMS = 2
    private var telefonoEmergencia: String = ""  // Variable para almacenar el número de teléfono de emergencia

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menuactivity)

        // Firebase
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

        // botones
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
        buttonLlamar.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                database.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (contactoSnapshot in snapshot.children) {
                                val telefono = contactoSnapshot.child("telefono").getValue(String::class.java)
                                if (!telefono.isNullOrEmpty()) {
                                    telefonoEmergencia = telefono
                                    val mensajeAyuda = "\uD83C\uDD98 Necesito asistencia médica urgente"
                                    enviarSMS(telefonoEmergencia, mensajeAyuda)
                                    llamarContactoEmergencia(telefonoEmergencia)
                                    break
                                }
                            }
                        } else {
                            Toast.makeText(this@MenuActivity, "No hay contacto de emergencia registrado", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@MenuActivity, "Error al cargar el contacto de emergencia", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }

    }

    // Función para cargar el número de emergencia desde Firebase
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
                                Log.d("Menu", "Número de emergencia cargado: $telefonoEmergencia")
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

    // Función para enviar un SMS de emergencia
    private fun enviarSMS(numero: String, mensaje: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.SEND_SMS),
                PERMISSION_REQUEST_SEND_SMS
            )
        } else {
            try {
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(numero, null, mensaje, null, null)
                Toast.makeText(this, "Mensaje de emergencia enviado", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Error al enviar SMS", Toast.LENGTH_SHORT).show()
                Log.e("Menu", "Error al enviar SMS: ${e.message}")
            }
        }
    }

    private fun llamarContactoEmergencia(numero: String) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CALL_PHONE),
                PERMISSION_REQUEST_CALL_PHONE
            )
        } else {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$numero"))
            startActivity(intent)
        }
    }

}




