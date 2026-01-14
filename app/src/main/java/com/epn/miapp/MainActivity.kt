package com.epn.miapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CarCrash
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.epn.miapp.ui.theme.MiAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MiAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ComponentesBasicos(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun ComponentesBasicos(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // Estado para los componentes interactivos
    var texto by remember { mutableStateOf("") }
    var checked by remember { mutableStateOf(false) }
    var switchActivo by remember { mutableStateOf(false) }
    var contador by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. TEXT - Texto simple y estilizado
        Text(
            text = "Componentes Básicos",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. TEXTFIELD - Campo de entrada
        TextField(
            value = texto,
            onValueChange = { texto = it },
            label = { Text("Escribe tu nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Mostrar lo que escribió el usuario
        if (texto.isNotEmpty()) {
            Text(text = "Hola, $texto!", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. BUTTON - Botón
        Button(onClick = { contador++ }) {
            Text("Presionado: $contador veces")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botón con borde (outlined)
        OutlinedButton(onClick = { contador = 0 }) {
            Text("Reiniciar contador")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. ROW - Fila horizontal con Checkbox y Switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // CHECKBOX
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = { checked = it }
                )
                Text("Acepto")
            }

            // SWITCH
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Modo oscuro")
                Switch(
                    checked = switchActivo,
                    onCheckedChange = { switchActivo = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. CARD - Tarjeta
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Esta es una Card", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Las cards son útiles para agrupar contenido relacionado.")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. BOX - Superposición
        Box(
            modifier = Modifier
                .size(100.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(containerColor = Color.LightGray)
            ) {}
            Text("Centrado", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 7. Texto con color condicional
        Text(
            text = if (switchActivo) "Switch está ON" else "Switch está OFF",
            color = if (switchActivo) Color.Green else Color.Red,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        // SEPARADOR
        Divider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            text = "Funcionalidades de la App",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // 8. Botón para Registro de Accidentes (PRINCIPAL)
        Button(
            onClick = {
                val intent = Intent(context, AccidentRegisterActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.Default.CarCrash,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "🚨 REGISTRAR ACCIDENTE",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 9. Botón para abrir la cámara
        Button(
            onClick = {
                val intent = Intent(context, CameraActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text("📸 Abrir Cámara")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 10. Botón para abrir ubicación
        Button(
            onClick = {
                val intent = Intent(context, LocationActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary
            )
        ) {
            Text("📍 Obtener Ubicación")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun ComponentesBasicosPreview() {
    MiAppTheme {
        ComponentesBasicos()
    }
}