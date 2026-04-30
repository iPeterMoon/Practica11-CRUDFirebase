package com.example.practica11

import android.app.AlertDialog
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.core.Context

class MainActivity : AppCompatActivity() {

    private lateinit var txtid: EditText
    private lateinit var txtnom: EditText
    private lateinit var btnbus: Button
    private lateinit var btnmod: Button
    private lateinit var btnreg: Button
    private lateinit var btneli: Button
    private lateinit var lvDatos: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        txtid = findViewById(R.id.txtid)
        txtnom = findViewById(R.id.txtnom)
        btnbus = findViewById(R.id.btnbus)
        btnmod = findViewById(R.id.btnmod)
        btnreg = findViewById(R.id.btnreg)
        btneli = findViewById(R.id.btneli)
        lvDatos = findViewById(R.id.lvDatos)

        botonRegistrar()
        listarLuchadores()
        botonBuscar()
        botonModificar()
        botonEliminar()

    }

    private fun botonRegistrar() {
        btnreg.setOnClickListener {
            if (txtid.text.toString().trim().isEmpty() || txtnom.text.toString().trim().isEmpty()) {
                ocultarTeclado()
                Toast.makeText(this, "Complete Los Campos Faltantes!!", Toast.LENGTH_SHORT).show()
            } else {
                val id = txtid.text.toString().toInt()
                val nom = txtnom.text.toString().trim()

                val db = FirebaseDatabase.getInstance()
                val dbref = db.getReference(Luchador::class.java.simpleName)

                dbref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val luc = Luchador(id, nom)
                        agregarLuchador(luc).addOnSuccessListener {
                            ocultarTeclado()
                            Toast.makeText(this@MainActivity, "Luchador Agregado Correctamente!!", Toast.LENGTH_SHORT).show()
                            txtid.text.clear()
                            txtnom.text.clear()
                            txtid.requestFocus()
                        }.addOnFailureListener {
                            ocultarTeclado()
                            Toast.makeText(this@MainActivity, "Error Al Intentar Agregar Luchador!!", Toast.LENGTH_SHORT).show()
                            txtid.text.clear()
                            txtnom.text.clear()
                            txtid.requestFocus()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }
        }
    }

    private fun agregarLuchador(luc: Luchador) = FirebaseDatabase.getInstance()
        .getReference(Luchador::class.java.simpleName).push().setValue(luc)

    private fun listarLuchadores() {
        val db = FirebaseDatabase.getInstance()
        val dbref = db.getReference(Luchador::class.java.simpleName)
        val lisluc = ArrayList<Luchador>()
        val ada = ArrayAdapter(this, android.R.layout.simple_list_item_1, lisluc)
        lvDatos.adapter = ada

        dbref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                snapshot.getValue(Luchador::class.java)?.let {
                    lisluc.add(it)
                    ada.notifyDataSetChanged()
                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) { ada.notifyDataSetChanged() }
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })

        lvDatos.setOnItemClickListener { _, _, position, _ ->
            ocultarTeclado()
            val luc = lisluc[position]
            txtid.setText(luc.id.toString())
            txtnom.setText(luc.nombre)
        }

        lvDatos.setOnItemLongClickListener { _, _, position, _ ->
            val luc = lisluc[position]
            val aux = luc.id.toString()
            val ref = FirebaseDatabase.getInstance().getReference(Luchador::class.java.simpleName)

            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var res = false
                    for (x in snapshot.children) {
                        if (x.child("id").value.toString().equals(aux, ignoreCase = true)) {
                            res = true
                            AlertDialog.Builder(this@MainActivity)
                                .setTitle("Pregunta")
                                .setMessage("¿Está Seguro(a) De Querer Eliminar El Registro ($aux)?")
                                .setCancelable(false)
                                .setNegativeButton("Cancelar", null)
                                .setPositiveButton("Aceptar") { _, _ ->
                                    x.ref.removeValue()
                                    listarLuchadores()
                                    ocultarTeclado()
                                    Toast.makeText(this@MainActivity, "Registro ($aux) Eliminado!!", Toast.LENGTH_SHORT).show()
                                    txtid.text.clear()
                                    txtnom.text.clear()
                                    txtid.requestFocus()
                                }
                                .show()
                            break
                        }
                    }
                    if (!res) {
                        ocultarTeclado()
                        Toast.makeText(this@MainActivity, "Error. Id ($aux) No Encontrado!!", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
            true
        }
    }

    private fun botonBuscar() {
        btnbus.setOnClickListener {
            if (txtid.text.toString().trim().isEmpty()) {
                ocultarTeclado()
                Toast.makeText(this, "Indique El Id Para Buscar!!", Toast.LENGTH_SHORT).show()
            } else {
                val aux = txtid.text.toString()
                val dbref = FirebaseDatabase.getInstance().getReference(Luchador::class.java.simpleName)
                dbref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var res = false
                        for (x in snapshot.children) {
                            if (x.child("id").value.toString().equals(aux, ignoreCase = true)) {
                                res = true
                                txtnom.setText(x.child("nombre").value.toString())
                                break
                            }
                        }
                        if (!res) {
                            Toast.makeText(this@MainActivity, "Error. Id ($aux) No Encontrado!!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
            }
        }
    }

    private fun botonModificar() {
        btnmod.setOnClickListener {
            if (txtid.text.toString().trim().isEmpty() || txtnom.text.toString().trim().isEmpty()) {
                Toast.makeText(this, "Complete Los Campos!!", Toast.LENGTH_SHORT).show()
            } else {
                val aux = txtid.text.toString()
                val nom = txtnom.text.toString()
                val dbref = FirebaseDatabase.getInstance().getReference(Luchador::class.java.simpleName)

                dbref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var res = false
                        var res2 = false
                        var targetNode: DataSnapshot? = null

                        for (x in snapshot.children) {
                            if (x.child("id").value.toString().equals(aux, ignoreCase = true)) {
                                res = true
                                targetNode = x
                            }
                            if (x.child("nombre").value.toString().equals(nom, ignoreCase = true)) {
                                res2 = true
                            }
                        }

                        if (res && !res2) {
                            AlertDialog.Builder(this@MainActivity)
                                .setTitle("Pregunta")
                                .setMessage("¿Modificar el nombre del registro ($aux)?")
                                .setPositiveButton("Aceptar") { _, _ ->
                                    targetNode?.ref?.child("nombre")?.setValue(nom)
                                    Toast.makeText(this@MainActivity, "Modificado!", Toast.LENGTH_SHORT).show()
                                }
                                .setNegativeButton("Cancelar", null)
                                .show()
                        } else {
                            val msg = if (!res) "Id no encontrado" else "Nombre ya en uso"
                            Toast.makeText(this@MainActivity, "Error: $msg", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
            }
        }
    }

    private fun botonEliminar() {
        btneli.setOnClickListener {
            if (txtid.text.toString().trim().isEmpty()) {
                Toast.makeText(this, "Indique el ID", Toast.LENGTH_SHORT).show()
            } else {
                val aux = txtid.text.toString()
                val dbref = FirebaseDatabase.getInstance().getReference(Luchador::class.java.simpleName)
                dbref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var found = false
                        for (x in snapshot.children) {
                            if (x.child("id").value.toString() == aux) {
                                found = true
                                x.ref.removeValue()
                                Toast.makeText(this@MainActivity, "Eliminado!", Toast.LENGTH_SHORT).show()
                                break
                            }
                        }
                        if (!found) Toast.makeText(this@MainActivity, "No encontrado", Toast.LENGTH_SHORT).show()
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
            }
        }
    }

    private fun ocultarTeclado() {
        val view = this.currentFocus
        view?.let {
            val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }
}