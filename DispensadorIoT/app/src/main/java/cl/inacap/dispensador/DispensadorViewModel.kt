package cl.inacap.dispensador

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.inacap.dispensador.model.ConfiguracionInicialDTO
import cl.inacap.dispensador.model.EstadoDispensador
import cl.inacap.dispensador.model.HorarioProgramado
import cl.inacap.dispensador.model.RegistroHistorial
import cl.inacap.dispensador.security.ValidadorSeguridad
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DispensadorViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val coleccionHistorial = firestore.collection("historial_dispensaciones")
    private val docEstado = firestore.collection("dispensadores").document("inacap_dispensador_01")

    private val _estado = MutableStateFlow(EstadoDispensador())
    val estado: StateFlow<EstadoDispensador> = _estado.asStateFlow()

    private val _historial = MutableStateFlow<List<RegistroHistorial>>(emptyList())
    val historial: StateFlow<List<RegistroHistorial>> = _historial.asStateFlow()

    private val _horarios = MutableStateFlow(
        listOf(
            HorarioProgramado(etiqueta = "Ración 1", hora = "11:00", duracionSeg = 5, activo = true),
            HorarioProgramado(etiqueta = "Ración 2", hora = "17:00", duracionSeg = 5, activo = true)
        )
    )
    val horarios: StateFlow<List<HorarioProgramado>> = _horarios.asStateFlow()

    private val _segundosManual = MutableStateFlow(5)
    val segundosManual: StateFlow<Int> = _segundosManual.asStateFlow()

    private val _mensajeNotificacion = MutableStateFlow<String?>(null)
    val mensajeNotificacion: StateFlow<String?> = _mensajeNotificacion.asStateFlow()

    init {
        // 1. Carga offline-first inmediata desde el archivo oculto en assets
        cargarFallbackAssets()
        // 2. Escucha reactiva en tiempo real en Firebase Firestore
        sincronizarConNube()
    }

    private fun sincronizarConNube() {
        // Listener del documento general del dispensador
        docEstado.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
            val nivel = snapshot.getLong("porcentajeNivel")?.toInt() ?: 87
            val dist = snapshot.getLong("distanciaCm")?.toInt() ?: 14
            val racion = snapshot.getString("ultimaRacion") ?: ""
            val online = snapshot.getBoolean("estaEnLinea") ?: true

            // Sanitización de hardware bajo norma ISO/IEC 27400
            val distanciaFiltrada = ValidadorSeguridad.sanitizarDistancia(dist) ?: 14

            _estado.value = _estado.value.copy(
                porcentajeNivel = nivel,
                distanciaCm = distanciaFiltrada,
                ultimaRacion = racion,
                estaEnLinea = online
            )
        }

        // Listener de la colección de auditoría / historial en tiempo real
        coleccionHistorial
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20)
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) return@addSnapshotListener
                val lista = snapshots.documents.mapNotNull { doc ->
                    val fecha = doc.getString("fecha") ?: ""
                    val tipo = doc.getString("tipo") ?: ""
                    val duracion = doc.getLong("duracionSeg")?.toInt() ?: 0
                    if (fecha.isNotEmpty()) RegistroHistorial(fecha, tipo, duracion) else null
                }
                if (lista.isNotEmpty()) {
                    _historial.value = lista
                }
            }
    }

    private fun cargarFallbackAssets() {
        try {
            // Lectura del asset ofuscado/ocultado con extensión .txt
            val inputStream = getApplication<Application>().assets.open("config_inicial.json.txt")
            val reader = InputStreamReader(inputStream)
            val dto = Gson().fromJson(reader, ConfiguracionInicialDTO::class.java)
            reader.close()

            val dist = ValidadorSeguridad.sanitizarDistancia(dto.distanciaCm) ?: 14
            _estado.value = EstadoDispensador(
                porcentajeNivel = dto.porcentajeNivel,
                distanciaCm = dist,
                ultimaRacion = dto.ultimaRacion,
                estaEnLinea = dto.estaEnLinea,
                ssid = dto.ssid,
                ipGateway = dto.ipGateway
            )
            if (dto.historial.isNotEmpty()) {
                _historial.value = dto.historial
            }
        } catch (_: Exception) {
            // En caso de que aún conserve el nombre original sin .txt
            try {
                val inputStream = getApplication<Application>().assets.open("config_inicial.json")
                val reader = InputStreamReader(inputStream)
                val dto = Gson().fromJson(reader, ConfiguracionInicialDTO::class.java)
                reader.close()

                val dist = ValidadorSeguridad.sanitizarDistancia(dto.distanciaCm) ?: 14
                _estado.value = EstadoDispensador(
                    porcentajeNivel = dto.porcentajeNivel,
                    distanciaCm = dist,
                    ultimaRacion = dto.ultimaRacion,
                    estaEnLinea = dto.estaEnLinea,
                    ssid = dto.ssid,
                    ipGateway = dto.ipGateway
                )
                if (dto.historial.isNotEmpty()) {
                    _historial.value = dto.historial
                }
            } catch (_: Exception) {}
        }
    }

    // --- AUTENTICACIÓN FIREBASE AUTH ---
    fun autenticarEnFirebase(correo: String, clave: String, onResultado: (Boolean, String?) -> Unit) {
        if (correo.isBlank() || clave.isBlank()) {
            onResultado(false, "Completa todos los campos.")
            return
        }
        auth.signInWithEmailAndPassword(correo.trim(), clave)
            .addOnSuccessListener { onResultado(true, null) }
            .addOnFailureListener { onResultado(false, it.localizedMessage ?: "Error de autenticación") }
    }

    fun registrarEnFirebase(nombre: String, correo: String, clave: String, onResultado: (Boolean, String?) -> Unit) {
        if (nombre.isBlank() || correo.isBlank() || clave.length < 6) {
            onResultado(false, "Contraseña mínima de 6 caracteres.")
            return
        }
        auth.createUserWithEmailAndPassword(correo.trim(), clave)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: ""
                val perfil = mapOf(
                    "uid" to uid,
                    "nombre" to nombre.trim(),
                    "email" to correo.trim(),
                    "rol" to "TUTOR_OPERADOR"
                )
                firestore.collection("usuarios").document(uid).set(perfil)
                onResultado(true, null)
            }
            .addOnFailureListener { onResultado(false, it.localizedMessage ?: "Error al registrar") }
    }

    // --- CONTROL DE DISPENSACIÓN Y HORARIOS ---
    fun actualizarSegundosManual(seg: Int) {
        _segundosManual.value = seg
    }

    fun cambiarHora(index: Int, nuevaHora: String) {
        val lista = _horarios.value.toMutableList()
        if (index in lista.indices) {
            lista[index] = lista[index].copy(hora = nuevaHora)
            _horarios.value = lista
        }
    }

    fun cambiarDuracionHorario(index: Int, incremento: Int) {
        val lista = _horarios.value.toMutableList()
        if (index in lista.indices) {
            val nuevaDuracion = (lista[index].duracionSeg + incremento).coerceIn(1, 10)
            lista[index] = lista[index].copy(duracionSeg = nuevaDuracion)
            _horarios.value = lista
        }
    }

    fun alternarHorario(index: Int) {
        val lista = _horarios.value.toMutableList()
        if (index in lista.indices) {
            lista[index] = lista[index].copy(activo = !lista[index].activo)
            _horarios.value = lista
        }
    }

    fun dispensarComida() {
        val segundos = _segundosManual.value
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val horaTexto = "${sdf.format(Date())} hrs"

        val registroFirestore = hashMapOf(
            "fecha" to horaTexto,
            "tipo" to "App Móvil (Wi-Fi)",
            "duracionSeg" to segundos,
            "timestamp" to System.currentTimeMillis()
        )

        // Subida del evento a Firestore
        coleccionHistorial.add(registroFirestore)

        val actualizacionEstado = mapOf(
            "ultimaRacion" to "$horaTexto ($segundos seg)",
            "estaEnLinea" to true
        )
        docEstado.set(actualizacionEstado, SetOptions.merge())

        _mensajeNotificacion.value = "¡Ración de $segundos seg enviada exitosamente!"
    }
}