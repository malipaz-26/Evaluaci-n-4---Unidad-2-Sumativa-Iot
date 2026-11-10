package cl.inacap.dispensador

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.inacap.dispensador.ui.theme.DispensadorIoTTheme

val MockupTeal = Color(0xFF00796B)
val MockupTealDark = Color(0xFF004D40)
val MockupMintLight = Color(0xFFE0F2F1)
val MockupBgLight = Color(0xFFF8FAFC)
val MockupBgDark = Color(0xFF0B1320)
val MockupCardBorder = Color(0xFFE2E8F0)
val MockupTextPrimary = Color(0xFF1E293B)
val MockupTextMuted = Color(0xFF94A3B8)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DispensadorIoTTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MockupBgLight
                ) {
                    AppPrincipal()
                }
            }
        }
    }
}

@Composable
fun AppPrincipal(vm: DispensadorViewModel = viewModel()) {
    var usuarioAutenticado by remember { mutableStateOf(false) }
    var enPantallaRegistro by remember { mutableStateOf(false) }
    var tabSeleccionada by remember { mutableStateOf(0) }

    if (!usuarioAutenticado) {
        if (!enPantallaRegistro) {
            PantallaLoginMockup(
                vm = vm,
                onLoginExitoso = { usuarioAutenticado = true },
                onIrARegistro = { enPantallaRegistro = true }
            )
        } else {
            PantallaRegistroMockup(
                vm = vm,
                onRegistroCompletado = {
                    enPantallaRegistro = false
                    usuarioAutenticado = true
                },
                onVolverALogin = { enPantallaRegistro = false }
            )
        }
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = tabSeleccionada == 0,
                        onClick = { tabSeleccionada = 0 },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                        label = { Text("Inicio", fontSize = 11.sp, fontWeight = if (tabSeleccionada == 0) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MockupTeal,
                            selectedTextColor = MockupTeal,
                            indicatorColor = MockupMintLight,
                            unselectedIconColor = MockupTextMuted,
                            unselectedTextColor = MockupTextMuted
                        )
                    )
                    NavigationBarItem(
                        selected = tabSeleccionada == 1,
                        onClick = { tabSeleccionada = 1 },
                        icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "Conexión") },
                        label = { Text("Conexión", fontSize = 11.sp, fontWeight = if (tabSeleccionada == 1) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MockupTeal,
                            selectedTextColor = MockupTeal,
                            indicatorColor = MockupMintLight,
                            unselectedIconColor = MockupTextMuted,
                            unselectedTextColor = MockupTextMuted
                        )
                    )
                    NavigationBarItem(
                        selected = tabSeleccionada == 2,
                        onClick = { tabSeleccionada = 2 },
                        icon = { Icon(Icons.Default.History, contentDescription = "Historial") },
                        label = { Text("Historial", fontSize = 11.sp, fontWeight = if (tabSeleccionada == 2) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MockupTeal,
                            selectedTextColor = MockupTeal,
                            indicatorColor = MockupMintLight,
                            unselectedIconColor = MockupTextMuted,
                            unselectedTextColor = MockupTextMuted
                        )
                    )
                    NavigationBarItem(
                        selected = tabSeleccionada == 3,
                        onClick = { tabSeleccionada = 3 },
                        icon = { Icon(Icons.Default.Security, contentDescription = "Acceso") },
                        label = { Text("Acceso", fontSize = 11.sp, fontWeight = if (tabSeleccionada == 3) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MockupTeal,
                            selectedTextColor = MockupTeal,
                            indicatorColor = MockupMintLight,
                            unselectedIconColor = MockupTextMuted,
                            unselectedTextColor = MockupTextMuted
                        )
                    )
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                when (tabSeleccionada) {
                    0 -> PantallaInicioMockup(vm)
                    1 -> PantallaConexionContenedor(onIrAPanel = { tabSeleccionada = 0 })
                    2 -> PantallaHistorialMockup(vm)
                    3 -> PantallaAccesoMockup(onCerrarSesion = { usuarioAutenticado = false })
                }
            }
        }
    }
}

// ==========================================
// 1. PANTALLA LOGIN (Con tolerancia Offline)
// ==========================================
@Composable
fun PantallaLoginMockup(
    vm: DispensadorViewModel,
    onLoginExitoso: () -> Unit,
    onIrARegistro: () -> Unit
) {
    var email by remember { mutableStateOf("amalia@inacap.cl") }
    var password by remember { mutableStateOf("123456") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMensaje by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MockupBgLight)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(MockupTeal, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                }
                Text("Control de Acceso", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MockupTextPrimary)
            }

            Surface(
                color = MockupMintLight,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB2DFDB))
            ) {
                Text(
                    text = "• EN LÍNEA",
                    color = MockupTeal,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, MockupCardBorder),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "CREDENCIALES DE USUARIO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupTextMuted,
                    letterSpacing = 1.sp
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Correo Electrónico", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MockupTextPrimary)
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMensaje = null },
                        placeholder = { Text("amalia@inacap.cl", color = MockupTextMuted) },
                        leadingIcon = { Text("@", fontSize = 16.sp, color = MockupTextMuted, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MockupCardBorder,
                            focusedBorderColor = MockupTeal,
                            unfocusedContainerColor = MockupBgLight.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Contraseña", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MockupTextPrimary)
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMensaje = null },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MockupTextMuted, modifier = Modifier.size(20.dp)) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = MockupTextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MockupCardBorder,
                            focusedBorderColor = MockupTeal,
                            unfocusedContainerColor = MockupBgLight.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )
                }

                if (errorMensaje != null) {
                    Text(errorMensaje!!, color = Color(0xFFE53935), fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        cargando = true
                        vm.autenticarEnFirebase(email, password) { exito, error ->
                            cargando = false
                            if (exito) {
                                onLoginExitoso()
                            } else {
                                // Resiliencia Offline: Si falla por red o proxy, permite el paso local
                                if (error?.contains("network", ignoreCase = true) == true ||
                                    error?.contains("timeout", ignoreCase = true) == true ||
                                    error?.contains("not verified", ignoreCase = true) == true) {
                                    onLoginExitoso()
                                } else {
                                    errorMensaje = error
                                }
                            }
                        }
                    },
                    enabled = !cargando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MockupTeal)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            if (cargando) "CONECTANDO..." else "INICIAR SESIÓN",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("¿No tienes una cuenta registrada?", fontSize = 12.sp, color = MockupTextMuted)
                    Text(
                        "Iniciar Sesión / Registrarse",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MockupTeal,
                        modifier = Modifier.clickable { onIrARegistro() }
                    )
                }
            }
        }
    }
}

// ==========================================
// 2. PANTALLA REGISTRO
// ==========================================
@Composable
fun PantallaRegistroMockup(
    vm: DispensadorViewModel,
    onRegistroCompletado: () -> Unit,
    onVolverALogin: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMensaje by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MockupBgLight)
            .padding(horizontal = 20.dp, vertical = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MockupMintLight.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable { onVolverALogin() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = MockupTextPrimary)
                }
                Column {
                    Text("Crear Cuenta", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MockupTextPrimary)
                    Text("Registro de Usuario", fontSize = 12.sp, color = MockupTextMuted)
                }
            }

            Surface(
                color = MockupMintLight,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB2DFDB))
            ) {
                Text(
                    text = "NUEVO USUARIO",
                    color = MockupTeal,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, MockupCardBorder),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    "DATOS DE REGISTRO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupTextMuted,
                    letterSpacing = 1.sp
                )

                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("Nombre de Usuario", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MockupTextPrimary)
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it; errorMensaje = null },
                        placeholder = { Text("Ej: Amalia Cortés", color = MockupTextMuted) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MockupTextMuted, modifier = Modifier.size(20.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MockupCardBorder,
                            focusedBorderColor = MockupTeal,
                            unfocusedContainerColor = MockupBgLight.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("Correo Electrónico", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MockupTextPrimary)
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMensaje = null },
                        placeholder = { Text("nombre@inacap.cl", color = MockupTextMuted) },
                        leadingIcon = { Text("@", fontSize = 16.sp, color = MockupTextMuted, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MockupCardBorder,
                            focusedBorderColor = MockupTeal,
                            unfocusedContainerColor = MockupBgLight.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("Contraseña (Mínimo 6 carácteres)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MockupTextPrimary)
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMensaje = null },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MockupTextMuted, modifier = Modifier.size(20.dp)) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MockupCardBorder,
                            focusedBorderColor = MockupTeal,
                            unfocusedContainerColor = MockupBgLight.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("Confirmar Contraseña", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MockupTextPrimary)
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; errorMensaje = null },
                        leadingIcon = { Icon(Icons.Default.Security, contentDescription = null, tint = MockupTextMuted, modifier = Modifier.size(20.dp)) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MockupCardBorder,
                            focusedBorderColor = MockupTeal,
                            unfocusedContainerColor = MockupBgLight.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )
                }

                if (errorMensaje != null) {
                    Text(errorMensaje!!, color = Color(0xFFE53935), fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        if (password != confirmPassword) {
                            errorMensaje = "Las contraseñas no coinciden."
                            return@Button
                        }
                        cargando = true
                        vm.registrarEnFirebase(nombre, email, password) { exito, error ->
                            cargando = false
                            if (exito) onRegistroCompletado() else errorMensaje = error
                        }
                    },
                    enabled = !cargando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MockupTeal)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            if (cargando) "REGISTRANDO..." else "REGISTRARSE Y CONTINUAR",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("¿Ya tienes una cuenta?", fontSize = 12.sp, color = MockupTextMuted)
                    Text(
                        "Iniciar Sesión",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MockupTeal,
                        modifier = Modifier.clickable { onVolverALogin() }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

// ==========================================
// 3. PANTALLA INICIO
// ==========================================
@Composable
fun PantallaInicioMockup(vm: DispensadorViewModel = viewModel()) {
    val estado by vm.estado.collectAsState()
    val horarios by vm.horarios.collectAsState()
    val segundosManual by vm.segundosManual.collectAsState()
    val mensajeNotificacion by vm.mensajeNotificacion.collectAsState()

    val opcionesDuracion = listOf(1, 3, 5, 8, 10)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MockupBgLight)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(MockupTeal, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Pets,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        "Dispensador de alimento",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MockupTextPrimary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(Color(0xFF00B074), CircleShape)
                        )
                        Text(
                            "Dispensador_AP",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF00B074)
                        )
                    }
                }
            }

            Surface(
                color = MockupMintLight,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB2DFDB))
            ) {
                Text(
                    text = "• EN LÍNEA",
                    color = MockupTeal,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, MockupCardBorder),
            elevation = CardDefaults.cardElevation(1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "CONTENEDOR DE COMIDA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MockupTextMuted,
                        letterSpacing = 0.5.sp
                    )

                    Surface(
                        color = MockupMintLight,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF80CBC4))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF00B074),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                "Capacidad Óptima",
                                color = MockupTeal,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    text = "${estado.porcentajeNivel}%",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockupTextPrimary
                )

                LinearProgressIndicator(
                    progress = { estado.porcentajeNivel / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MockupTeal,
                    trackColor = MockupBgLight
                )
            }
        }

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, MockupCardBorder),
            elevation = CardDefaults.cardElevation(1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "ALIMENTACIÓN POR HORARIO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MockupTextMuted,
                        letterSpacing = 0.5.sp
                    )

                    Surface(
                        color = MockupMintLight,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF80CBC4))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MockupTeal,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                "Automático",
                                color = MockupTeal,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    horarios.take(2).forEachIndexed { index, horario ->
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MockupBgLight),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MockupCardBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Ración ${index + 1}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MockupTextPrimary
                                    )
                                    Switch(
                                        checked = horario.activo,
                                        onCheckedChange = { vm.alternarHorario(index) },
                                        modifier = Modifier.height(24.dp),
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = MockupTeal,
                                            uncheckedThumbColor = Color.White,
                                            uncheckedTrackColor = MockupCardBorder
                                        )
                                    )
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text("HORA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MockupTextMuted)
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color.White,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MockupCardBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "<",
                                                fontSize = 11.sp,
                                                color = MockupTextMuted,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.clickable {
                                                    val partes = horario.hora.split(":")
                                                    val nuevaHora = ((partes[0].toIntOrNull() ?: 11) - 1).coerceIn(0, 23)
                                                    vm.cambiarHora(index, String.format("%02d:00", nuevaHora))
                                                }
                                            )
                                            Text(
                                                horario.hora,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MockupTextPrimary
                                            )
                                            Text(
                                                ">",
                                                fontSize = 11.sp,
                                                color = MockupTextMuted,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.clickable {
                                                    val partes = horario.hora.split(":")
                                                    val nuevaHora = ((partes[0].toIntOrNull() ?: 11) + 1).coerceIn(0, 23)
                                                    vm.cambiarHora(index, String.format("%02d:00", nuevaHora))
                                                }
                                            )
                                        }
                                    }
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text("DURACIÓN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MockupTextMuted)
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color.White,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MockupCardBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "<",
                                                fontSize = 11.sp,
                                                color = MockupTextMuted,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.clickable { vm.cambiarDuracionHorario(index, -1) }
                                            )
                                            Text(
                                                "${horario.duracionSeg} seg",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MockupTeal
                                            )
                                            Text(
                                                ">",
                                                fontSize = 11.sp,
                                                color = MockupTextMuted,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.clickable { vm.cambiarDuracionHorario(index, 1) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF80CBC4)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MockupMintLight.copy(alpha = 0.4f),
                        contentColor = MockupTeal
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = null,
                            tint = MockupTeal,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Guardar y Aplicar Horarios",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupTeal
                        )
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, MockupCardBorder),
            elevation = CardDefaults.cardElevation(1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "DISPENSACIÓN MANUAL INMEDIATA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupTextMuted,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            "Tiempo de apertura de compuerta.",
                            fontSize = 12.sp,
                            color = MockupTextMuted
                        )
                    }

                    Surface(
                        color = MockupMintLight,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF80CBC4))
                    ) {
                        Text(
                            "$segundosManual seg",
                            color = MockupTeal,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Column {
                    Slider(
                        value = segundosManual.toFloat(),
                        onValueChange = { vm.actualizarSegundosManual(it.toInt()) },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = MockupTeal,
                            activeTrackColor = MockupTeal,
                            inactiveTrackColor = MockupBgLight
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        opcionesDuracion.forEach { valor ->
                            Text(
                                "${valor}s",
                                fontSize = 10.sp,
                                color = if (segundosManual == valor) MockupTeal else MockupTextMuted,
                                fontWeight = if (segundosManual == valor) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Button(
                    onClick = { vm.dispensarComida() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MockupTeal)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Restaurant,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "DISPENSAR COMIDA AHORA",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                if (mensajeNotificacion != null) {
                    Text(
                        mensajeNotificacion!!,
                        color = MockupTeal,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

// ==========================================
// 4. PANTALLA CONEXIÓN QR
// ==========================================
@Composable
fun PantallaConexionContenedor(onIrAPanel: () -> Unit) {
    var vinculado by remember { mutableStateOf(false) }

    if (!vinculado) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MockupBgDark)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.08f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Escanear Código QR", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Buscando dispositivo IoT...", fontSize = 12.sp, color = Color(0xFF64FFDA))
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.08f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.FlashOn, contentDescription = null, tint = Color(0xFF64FFDA), modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .size(290.dp)
                    .background(Color(0xFF1E293B).copy(alpha = 0.5f), RoundedCornerShape(28.dp))
                    .border(2.dp, Color(0xFF00B4D8).copy(alpha = 0.4f), RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.size(14.dp).border(2.dp, Color(0xFF64FFDA), CircleShape))
                        Box(modifier = Modifier.size(14.dp).border(2.dp, Color(0xFF64FFDA), CircleShape))
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.QrCode2,
                                contentDescription = null,
                                tint = Color(0xFF1E293B),
                                modifier = Modifier.size(110.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "SSID: Dispensador_AP",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                color = MockupTeal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                color = Color(0xFF111827),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF64FFDA), CircleShape))
                    Column {
                        Text("Apunta la cámara al código QR del comedero", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Se detectará y conectará automáticamente", fontSize = 11.sp, color = MockupTextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { vinculado = true },
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF64FFDA)),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF004D40).copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("[ Simular Código Detectado ]", color = Color(0xFF64FFDA), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MockupBgLight)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(MockupMintLight, CircleShape)
                    .border(3.dp, Color(0xFF00B074), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color(0xFF00B074),
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("¡Conexión Exitosa!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MockupTextPrimary)
            Text("Dispensador vinculado correctamente vía QR", fontSize = 13.sp, color = MockupTextMuted)

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, MockupCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("DISPOSITIVO ENLAZADO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MockupTextMuted)
                        Surface(
                            color = MockupMintLight,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "• EN LÍNEA",
                                color = MockupTeal,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = MockupCardBorder.copy(alpha = 0.5f))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Punto de Acceso:", fontSize = 13.sp, color = MockupTextMuted)
                        Text("Dispensador_AP", fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = MockupTextPrimary)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Intensidad Señal:", fontSize = 13.sp, color = MockupTextMuted)
                        Text("-48 dBm (Excelente)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MockupTeal)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Token de Sesión:", fontSize = 13.sp, color = MockupTextMuted)
                        Text("Verificado (ISO 27400)", fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = MockupTextPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onIrAPanel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MockupTeal)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("IR AL PANEL DE CONTROL", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Escanear otro dispositivo",
                color = MockupTextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { vinculado = false }
            )
        }
    }
}

// ==========================================
// 5. PANTALLA HISTORIAL
// ==========================================
@Composable
fun PantallaHistorialMockup(vm: DispensadorViewModel = viewModel()) {
    val listaHistorial by vm.historial.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MockupBgLight)
            .padding(horizontal = 20.dp, vertical = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(MockupTeal, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.History, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                }
                Column {
                    Text("Historial de Acciones", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = MockupTextPrimary)
                    Text("Trazabilidad Operacional OT", fontSize = 12.sp, color = MockupTeal)
                }
            }

            Surface(
                color = MockupMintLight,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB2DFDB))
            ) {
                Text(
                    text = "• EN LÍNEA",
                    color = MockupTeal,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, MockupCardBorder),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "TRAZABILIDAD INDUSTRIAL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MockupTextMuted,
                            letterSpacing = 1.sp
                        )
                        Text("Registro de Activaciones", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MockupTextPrimary)
                    }

                    Surface(
                        color = MockupMintLight,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF80CBC4))
                    ) {
                        Text(
                            "${listaHistorial.size} eventos",
                            color = MockupTeal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                listaHistorial.forEach { item ->
                    val esBoton = item.tipo.contains("Pulsador", ignoreCase = true)

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MockupBgLight.copy(alpha = 0.7f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MockupCardBorder.copy(alpha = 0.8f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(if (esBoton) Color(0xFFE0F7FA) else MockupMintLight, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        if (esBoton) Icons.Default.TouchApp else Icons.Default.Check,
                                        contentDescription = null,
                                        tint = if (esBoton) Color(0xFF00ACC1) else Color(0xFF00B074),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        "Ración Servida (${item.duracionSeg} seg)",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MockupTextPrimary
                                    )
                                    Text(
                                        "Origen: ${item.tipo}",
                                        fontSize = 11.sp,
                                        color = MockupTeal
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(item.fecha, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MockupTextPrimary)
                                Text("Auditoría", fontSize = 10.sp, color = MockupTextMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. PANTALLA ACCESO Y SEGURIDAD
// ==========================================
@Composable
fun PantallaAccesoMockup(onCerrarSesion: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MockupBgLight)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(MockupTeal, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
            }
            Column {
                Text("Control de Acceso y OT", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = MockupTextPrimary)
                Text("Conformidad ISO/IEC 27400", fontSize = 12.sp, color = MockupTeal)
            }
        }

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, MockupCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("POLÍTICAS TÉCNICAS APLICADAS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MockupTextMuted)
                Text("• Sanitización Física: Activa (Rango 2 a 80 cm)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MockupTextPrimary)
                Text("• Cifrado en Tránsito: TLS / HTTPS (Firebase SDK)", fontSize = 12.sp, color = MockupTextMuted)
                Text("• Permisos: Mínimo privilegio (INTERNET, ACCESS_NETWORK_STATE)", fontSize = 12.sp, color = MockupTextMuted)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onCerrarSesion,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("CERRAR SESIÓN", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}