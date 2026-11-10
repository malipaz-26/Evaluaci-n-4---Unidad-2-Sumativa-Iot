package cl.inacap.dispensador.security
/**
 * Filtro de Sanitización de Datos Físicos - Norma ISO/IEC 27400
 * Descarte estricto de tramas fuera del contenedor (2 cm a 80 cm).
 */
object ValidadorSeguridad {
    private const val DISTANCIA_MIN_CM = 2
    private const val DISTANCIA_MAX_CM = 80
    const val TOKEN_AUTORIZACION = "Bearer TokenSeguroDispensador2026"

    fun sanitizarDistancia(distanciaCruda: Int): Int? {
        return if (distanciaCruda in DISTANCIA_MIN_CM..DISTANCIA_MAX_CM) {
            distanciaCruda
        } else {
            null
        }
    }
}