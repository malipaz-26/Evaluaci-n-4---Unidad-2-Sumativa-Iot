package cl.inacap.dispensador.model

data class UsuarioTutor(
    val nombre: String,
    val email: String,
    val contrasena: String
)

data class RegistroHistorial(
    val fecha: String,
    val tipo: String,
    val duracionSeg: Int
)

data class EstadoDispensador(
    val porcentajeNivel: Int = 87,
    val distanciaCm: Int = 12,
    val ultimaRacion: String = "Hoy 10:30 hrs (5 seg)",
    val estaEnLinea: Boolean = true,
    val ssid: String = "Dispensador_AP",
    val ipGateway: String = "192.168.4.1"
)

data class ConfiguracionInicialDTO(
    val porcentajeNivel: Int = 85,
    val distanciaCm: Int = 14,
    val ultimaRacion: String = "",
    val estaEnLinea: Boolean = true,
    val ssid: String = "Dispensador_AP",
    val ipGateway: String = "192.168.4.1",
    val historial: List<RegistroHistorial> = emptyList()
)

data class HorarioProgramado(
    val etiqueta: String,
    val hora: String,
    val duracionSeg: Int,
    val activo: Boolean = true
)