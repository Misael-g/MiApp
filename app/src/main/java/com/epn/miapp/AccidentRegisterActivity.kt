// kotlin
package com.epn.miapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.epn.miapp.ui.theme.MiAppTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AccidentRegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MiAppTheme {
                AccidentRegisterScreen()
            }
        }
    }
}

data class AccidentReport(
    val tipoAccidente: String = "",
    val fechaSiniestro: String = "",
    val matricula: String = "",
    val nombreConductor: String = "",
    val cedulaConductor: String = "",
    val observaciones: String = "",
    val fotoUri: Uri? = null,
    val latitud: Double? = null,
    val longitud: Double? = null
)

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AccidentRegisterScreen() {
    val context = LocalContext.current
    var report by remember { mutableStateOf(AccidentReport()) }
    var expandedTipo by remember { mutableStateOf(false) }
    var isLoadingLocation by remember { mutableStateOf(false) }

    val tiposAccidente = listOf("Choque", "Colisión", "Atropello")

    // Estados de permisos
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Crear archivo para la foto
    fun createImageFile(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(null)
        val imageFile = File.createTempFile(
            "ACCIDENT_${timeStamp}_",
            ".jpg",
            storageDir
        )
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    }

    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher para tomar foto
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            report = report.copy(fotoUri = tempPhotoUri)
            Toast.makeText(context, "Foto capturada exitosamente", Toast.LENGTH_SHORT).show()
        }
    }

    // Función para obtener ubicación (no-suspend, usa listeners)
    @SuppressLint("MissingPermission")
    fun obtenerUbicacion() {
        try {
            isLoadingLocation = true
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
            ).addOnSuccessListener { location: Location? ->
                location?.let {
                    report = report.copy(
                        latitud = it.latitude,
                        longitud = it.longitude
                    )
                    Toast.makeText(
                        context,
                        "Ubicación obtenida: ${it.latitude}, ${it.longitude}",
                        Toast.LENGTH_SHORT
                    ).show()
                } ?: run {
                    Toast.makeText(context, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show()
                }
                isLoadingLocation = false
            }.addOnFailureListener { e ->
                Toast.makeText(context, "Error al obtener ubicación: ${e.message}", Toast.LENGTH_SHORT).show()
                isLoadingLocation = false
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error al obtener ubicación: ${e.message}", Toast.LENGTH_SHORT).show()
            isLoadingLocation = false
        }
    }

    // Función para vibrar
    fun vibrarTelefono() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(5000, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(5000)
        }
    }

    // Función para validar y guardar
    fun guardarAccidente() {
        when {
            report.tipoAccidente.isEmpty() -> {
                Toast.makeText(context, "Seleccione el tipo de accidente", Toast.LENGTH_SHORT).show()
            }
            report.fechaSiniestro.isEmpty() -> {
                Toast.makeText(context, "Ingrese la fecha del siniestro", Toast.LENGTH_SHORT).show()
            }
            report.matricula.isEmpty() -> {
                Toast.makeText(context, "Ingrese la matrícula del vehículo", Toast.LENGTH_SHORT).show()
            }
            report.nombreConductor.isEmpty() -> {
                Toast.makeText(context, "Ingrese el nombre del conductor", Toast.LENGTH_SHORT).show()
            }
            report.cedulaConductor.isEmpty() -> {
                Toast.makeText(context, "Ingrese la cédula del conductor", Toast.LENGTH_SHORT).show()
            }
            report.fotoUri == null -> {
                Toast.makeText(context, "Capture una fotografía del accidente", Toast.LENGTH_SHORT).show()
            }
            report.latitud == null || report.longitud == null -> {
                Toast.makeText(context, "Obtenga la ubicación GPS", Toast.LENGTH_SHORT).show()
            }
            else -> {
                // Guardar el accidente (aquí puedes implementar guardado en base de datos)
                vibrarTelefono()
                Toast.makeText(context, "Accidente registrado exitosamente", Toast.LENGTH_LONG).show()

                // Limpiar formulario
                report = AccidentReport()
                tempPhotoUri = null
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro de Accidentes") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tipo de Accidente (Dropdown)
            ExposedDropdownMenuBox(
                expanded = expandedTipo,
                onExpandedChange = { expandedTipo = !expandedTipo }
            ) {
                OutlinedTextField(
                    value = report.tipoAccidente,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de Accidente") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipo) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = expandedTipo,
                    onDismissRequest = { expandedTipo = false }
                ) {
                    tiposAccidente.forEach { tipo ->
                        DropdownMenuItem(
                            text = { Text(tipo) },
                            onClick = {
                                report = report.copy(tipoAccidente = tipo)
                                expandedTipo = false
                            }
                        )
                    }
                }
            }

            // Fecha del Siniestro
            OutlinedTextField(
                value = report.fechaSiniestro,
                onValueChange = { report = report.copy(fechaSiniestro = it) },
                label = { Text("Fecha del Siniestro") },
                placeholder = { Text("dd/MM/yyyy") },
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            // Matrícula
            OutlinedTextField(
                value = report.matricula,
                onValueChange = { report = report.copy(matricula = it.uppercase()) },
                label = { Text("Matrícula del Vehículo") },
                placeholder = { Text("ABC-1234") },
                leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            // Nombre del Conductor
            OutlinedTextField(
                value = report.nombreConductor,
                onValueChange = { report = report.copy(nombreConductor = it) },
                label = { Text("Nombre del Conductor") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            // Cédula del Conductor
            OutlinedTextField(
                value = report.cedulaConductor,
                onValueChange = { report = report.copy(cedulaConductor = it) },
                label = { Text("Cédula del Conductor") },
                placeholder = { Text("1234567890") },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            // Observaciones
            OutlinedTextField(
                value = report.observaciones,
                onValueChange = { report = report.copy(observaciones = it) },
                label = { Text("Observaciones") },
                placeholder = { Text("Detalles adicionales del accidente...") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            // Botón para capturar foto
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (report.fotoUri != null)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (report.fotoUri != null) Icons.Default.CheckCircle else Icons.Default.Camera,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = if (report.fotoUri != null)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (report.fotoUri != null)
                            "Fotografía capturada ✓"
                        else
                            "Sin fotografía",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (permissionsState.allPermissionsGranted) {
                                tempPhotoUri = createImageFile()
                                takePictureLauncher.launch(tempPhotoUri)
                            } else {
                                permissionsState.launchMultiplePermissionRequest()
                            }
                        }
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Capturar Foto")
                    }
                }
            }

            // Botón para obtener ubicación GPS
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (report.latitud != null)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (report.latitud != null) Icons.Default.CheckCircle else Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = if (report.latitud != null)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (report.latitud != null && report.longitud != null) {
                        Text(
                            text = "Ubicación GPS obtenida ✓",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Lat: ${String.format("%.6f", report.latitud)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Lon: ${String.format("%.6f", report.longitud)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        Text(
                            text = "Sin ubicación GPS",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (permissionsState.allPermissionsGranted) {
                                obtenerUbicacion()
                            } else {
                                permissionsState.launchMultiplePermissionRequest()
                            }
                        },
                        enabled = !isLoadingLocation
                    ) {
                        if (isLoadingLocation) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.MyLocation, contentDescription = null)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isLoadingLocation) "Obteniendo..." else "Obtener Ubicación")
                    }
                }
            }

            // Botón Guardar
            Button(
                onClick = { guardarAccidente() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("GUARDAR ACCIDENTE", style = MaterialTheme.typography.titleMedium)
            }

            // Información de permisos
            if (!permissionsState.allPermissionsGranted) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "⚠️ Permisos requeridos",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Esta aplicación necesita permisos de cámara y ubicación para funcionar correctamente.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { permissionsState.launchMultiplePermissionRequest() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Solicitar Permisos")
                        }
                    }
                }
            }
        }
    }
}
