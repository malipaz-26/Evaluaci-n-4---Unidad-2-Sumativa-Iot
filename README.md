Definición del proyecto 
La problemática 
Los dueños de mascotas a menudo se enfrentan a la dificultad de gestionar la alimentación diaria de sus animales de manera exacta, oportuna y autónoma. Además, muchos dispensadores comerciales dependen totalmente de internet o la nube para funcionar (lo que falla ante cortes de servicio) o carecen de alertas precisas sobre la falta de alimento.  
Dispositivos y Sistemas Conectados
•	Nodo 1 (Arduino UNO R4 WiFi ): Configurado como servidor y Punto de Acceso local (Access Point). Conecta como entradas directas el Encoder Rotativo HW-040 (ajuste de porciones) y el Pulsador HW-483 (dispensación manual), e integra el LED del Pin 13 para indicar estados de transferencia.  

•	Nodo 2 (Arduino UNO R4 WiFi ): Conectado inalámbricamente como cliente en la red Wi-Fi local. Conecta como entrada el Sensor Ultrasónico HC-SR04 (nivel de tolva) y como salidas el Servomotor SG90 (apertura de compuerta), el Buzzer piezoeléctrico (alerta sonora) y un LED Rojo de alerta.  

•	Aplicación Móvil / Interfaz del Usuario: Dispositivo móvil conectado a la red Wi-Fi propia del Nodo 1 (Access Point) a través del navegador web.  
Objetivo de la Comunicación: 
Transmitir comandos de activación de raciones en tiempo real desde la aplicación (o interactores físicos) al Nodo 1 y de este hacia el Nodo 2 de forma inalámbrica. Asimismo, sincronizar los estados (pendientes de comida y monitoreo de inventario de alimento) entre ambos microcontroladores.

Funcionalidades Clave de la Aplicación y del Sistema Embebido
Teniendo en cuenta los sensores, actuadores y el comportamiento en tiempo real (millis()) definido en el proyecto, el sistema integra las siguientes funciones:  
Control de Dispensación Remota y Manual 
•	Dispensación Inmediata por App: Permitir al usuario enviar la orden de abrir la compuerta de comida directamente con un botón desde la interfaz móvil.  
•	Dispensación Física Directa: Permitir la activación de una porción instantánea mediante la pulsación del botón manual (HW-483) en la estructura.  

•	Aviso Acústico para la Mascota: Al presionar el botón (web o físico), el sistema ejecuta autónomamente en el Nodo 2 una secuencia sonora previa de 3 pitidos (a 1200 Hz) con el buzzer antes de liberar el alimento.  
Gestión Paramétrica de Porciones (Encoder) 
•	Ajuste Físico de Duración: Configuración dinámica del tiempo de apertura de la compuerta de 1 a 10 segundos mediante el encoder rotativo (HW-040), asegurando una dosificación exacta según el tamaño del animal.  
Monitoreo de Depósito (Nivel de Alimento) 
•	Detección Automática de Stock Faltante: Sensado continuo con el sensor ultrasónico HC-SR04. Si la distancia del depósito supera los 25 cm (nivel crítico/vacío):  

•	Se activa el LED Rojo indicador en el dispensador de forma autónoma.  

•	Se notifica y visualiza el estado en la interfaz de la aplicación para que el usuario sepa que debe rellenar la tolva.  
Interacción y Feedback de Estado (LEDs e Interfaz) 
•	Indicador de Encolamiento/Red: Encendido del LED del Pin 13 en el Nodo 1 mientras la orden web/física está registrada a la espera de ser procesada y leída por el Nodo 2 por Wi-Fi.   











Características de la App Móvil para Ser Efectiva
Para asegurar una operación óptima respetando las características de la red e infraestructura descritas en el proyecto:
Interfaz Simple e Intuitiva: 
•	Un botón prominente para "Dispensar Comida Ahora".  

•	Un indicador claro de estado del depósito: "Nivel de Alimento: OK" o "Nivel de Alimento: Crítico / Reabastecer".  
Manejo en Tiempo Real y Carga Liviana: 
•	La app debe realizar peticiones livianas para no saturar los recursos del Arduino UNO R4 WiFi, aprovechando la arquitectura no bloqueante (millis()) del código de la placa.  
Capas de Seguridad Recomendadas (Evolución): 
•	Para resolver las vulnerabilidades identificadas (OWASP):  
o	Acceso protegido por autenticación basada en token de usuario.  
o	Cifrado en la transmisión de datos HTTP/HTTPS.  
o	Control de tasa de peticiones (rate limit) en la app para evitar saturación del microcontrolador. 

Selección de herramientas de desarrollo 

Para el desarrollo de la aplicación móvil se adoptan los estándares oficiales de Modern Android Development (MAD), seleccionando herramientas y bibliotecas optimizadas para arquitecturas reactivas, baja latencia en entornos ciberfísicos (OT) y cumplimiento de las directrices de ciberseguridad industrial (ISO/IEC 27400). 
1. Entorno de Desarrollo, Lenguaje y Framework de UI
•	Android Studio + Kotlin DSL (build.gradle.kts): Entorno de desarrollo oficial. Se emplea la configuración con Kotlin DSL para la gestión declarativa, tipada y modular de bibliotecas y dependencias de compilación. 
•	Lenguaje Kotlin: Se utiliza como lenguaje base aprovechando su tipado estático con Null Safety (?, ?:), previniendo caídas imprevistas (NullPointerException) ante anomalías o demoras de respuesta de los dispositivos en red. Asimismo, el uso de estructuras inmutables (val), data class y sealed interface permite modelar de forma precisa y finita los estados de conexión y las lecturas de los sensores. 
•	Jetpack Compose (Material Design 3): Toolkit declarativo moderno para la interfaz gráfica que prescinde completamente de layouts antiguos en XML. Permite que las pantallas se redibujen automáticamente conforme cambia el estado de los datos (StateFlow + collectAsStateWithLifecycle()). Garantiza además el cumplimiento de accesibilidad con componentes y botones táctiles con dimensiones de al menos $48\,\text{dp}$. 
•	Arquitectura MVVM (Model-View-ViewModel): Separa estrictamente la interfaz gráfica de la lógica de negocio y de red (ui, data, model, security). El ViewModel preserva el estado operativo ante la recreación de la Activity (por ejemplo, durante la rotación de pantalla mediante by viewModels()), evitando reiniciar las conexiones o perder la telemetría adquirida. 
•	Kotlin Coroutines + Flow: Gestión asíncrona no bloqueante ejecutada sobre el despachador de entrada/salida (Dispatchers.IO). Garantiza que los sondeos periódicos y peticiones a los dispositivos IoT no congelen el hilo principal de la interfaz (Main Thread). 
2. Conectividad con Dispositivos IoT y Hardware Móvil
•	Cliente de Red: OkHttp 4 (com.squareup.okhttp3:okhttp:4.12.0) 
o	Justificación técnica: Proporciona control granular sobre los tiempos de espera de red (Connect Timeout: 3000 ms, Read Timeout: 3000 ms), asegurando que ante una interferencia de radiofrecuencia o apagado del dispensador, la app libere los recursos sin colapsar. 
o	Seguridad: Facilita la inyección automática de cabeceras de autorización (Authorization: Bearer <TOKEN>) en cada petición HTTP/HTTPS, cumpliendo el control de acceso exigido por la norma ISO/IEC 27400. 
•	Librería de Visión y Captura: CameraX + Google ML Kit Barcode Scanning
o	Justificación técnica: Se integra para resolver el aprovisionamiento amigable y seguro del dispensador mediante la lectura de códigos QR adheridos al hardware. Procesa la imagen directamente en el procesador del dispositivo móvil sin requerir conexión a internet, extrayendo instantáneamente los parámetros de red (SSID y token de sesión). 
3. Autenticación de Usuarios: Firebase Authentication
•	Biblioteca: com.google.firebase:firebase-auth (gestionada mediante Firebase BoM) 
•	Justificación frente a las alternativas evaluadas:
o	Frente a SQLite (Local): Gestionar credenciales en una base de datos local SQLite resulta vulnerable ante ataques de extracción física de memoria o ingeniería inversa del APK en teléfonos rooteados. Tampoco permite validar identidades en un ecosistema distribuido si el usuario supervisa el dispensador desde múltiples terminales. 
o	Frente a MySQL con API externa: Requeriría desplegar, mantener y asegurar un servidor propio intermedio, asumiendo riesgos de inyecciones SQL y forzando la implementación manual de mecanismos complejos de cifrado de contraseñas (salting + hashing), ampliando la superficie de ataque. 
o	Ventajas de Firebase Authentication: Provee un servicio de identidad en la nube con altos estándares de ciberseguridad. Resuelve el inicio de sesión mediante correo y contraseña cifrados y mantiene la sesión persistente en el contenedor seguro del hardware (Android Keystore), evitando almacenar contraseñas en texto plano dentro de la aplicación. 
4. Almacenamiento, Gestión y Persistencia de Datos: Cloud Firestore
•	Biblioteca: com.google.firebase:firebase-firestore 
•	Justificación de Elección e Interoperabilidad de Datos:
o	Aislamiento y Reglas de Seguridad Granulares: La información se organiza en una jerarquía centrada en el usuario (users/{uid}/dispensaciones y users/{uid}/dispositivos), permitiendo que reglas de seguridad del lado del servidor restrinjan el acceso de forma estricta: solo el usuario autenticado puede leer o escribir sus propios registros. 
o	Trazabilidad Inmutable (Lineamiento de la Industria OT): Cada ración dispensada (sea por botón manual, por app o por horario programado) se persiste con marcas temporales del servidor (FieldValue.serverTimestamp()), garantizando la integridad de las bitácoras y evitando adulteraciones cronológicas desde el dispositivo móvil. 
o	Caché Local Offline (Resiliencia Operativa): Firestore cuenta con soporte nativo de persistencia fuera de línea en el almacenamiento interno de Android. Si se corta la conexión a internet en el hogar, la aplicación continúa registrando y consultando las dispensaciones localmente, sincronizando los datos de forma atómica y transparente en cuanto se restablece la conectividad. Esto cumple el principio de alta disponibilidad de la tecnología operacional. 


Resumen de Selección Tecnológica

Componente del Sistema	Herramienta / Biblioteca Seleccionada	Rol en la Aplicación	Estándar / Beneficio Técnico Demostrado
Lenguaje de Programación	Kotlin	Lógica de negocio, validaciones y modelos de datos. 	Estándar oficial Google; tipado estricto con Null Safety e inmutabilidad (val). 
Entorno de Compilación	Android Studio + Kotlin DSL	IDE y scripts de compilación (build.gradle.kts). 	Gestión tipada de dependencias y reproducibilidad del entorno. 
Interfaz de Usuario (UI)	Jetpack Compose (Material Design 3)	Construcción visual y diseño reactivo. 	Renderizado moderno declarativo; accesibilidad con componentes $\ge 48\,\text{dp}$. 
Patrón Arquitectónico	MVVM (ViewModel + StateFlow)	Desacoplamiento por capas (ui, data, model, security). 	Retención del estado ante giros de pantalla y aislamiento de la lógica de red. 
Conectividad IoT Local	OkHttp 4 + Coroutines	Peticiones HTTP/HTTPS asíncronas hacia el microcontrolador. 	Timeouts estrictos de $3\text{ s}$ en Dispatchers.IO y cabeceras de autorización seguras. 
Aprovisionamiento QR	CameraX + ML Kit Barcode	Escaneo de código QR para emparejamiento automático.	Configuración amigable sin requerir que el usuario ingrese direcciones IP manuales. 
Autenticación	Firebase Authentication	Pantalla de inicio de sesión inicial (Login First). 	Credenciales cifradas en la nube y sesión protegida en Android Keystore. 
Persistencia e Historial	Cloud Firestore	Almacenamiento de raciones, horarios y telemetría. 	Trazabilidad inmutable OT, reglas por UID y soporte de caché fuera de línea (offline). 
Seguridad de Datos	Módulo ValidadorSeguridad.kt	Sanitización y filtrado de telemetría física. 	Norma ISO/IEC 27400: descarte estricto de lecturas ultrasónicas fuera de $2\text{ a }80\,\text{cm}$




















Método de comunicación inalámbrica:
•	Tecnología Elegida (Wi-Fi Local con HTTP/JSON): Se utiliza la red Wi-Fi local generada por el microcontrolador (192.168.4.1) mediante mensajes directos en formato JSON. Esto permite que el teléfono se comunique de forma inmediata con el dispensador sin depender de internet ni de servidores externos. 
•	Distancia y Cobertura: A diferencia de Bluetooth (que pierde señal al pasar muros o superar los 10 metros), el Wi-Fi cubre todas las habitaciones de la casa. Esto permite accionar o revisar el nivel de alimento desde cualquier lugar del hogar. 
•	Consumo de Energía: Dado que el dispensador cuenta con un servomotor para mover la compuerta física y se conecta directamente a la toma de corriente de pared, no depende de una batería pequeña. Por esta razón, se prioriza una conexión rápida y con buen ancho de banda por sobre el ahorro extremo de energía. 
•	Estabilidad y Disponibilidad Continua: Al funcionar de manera autónoma en la red local, el comedero sigue operando con total normalidad aunque la casa se quede sin internet. Además, las órdenes se ejecutan al instante (en menos de 20 milisegundos) y permite conectar dos celulares a la vez sin problemas de enlace.

Pantalla de login y sistema de autenticación:
Para la gestión del acceso se seleccionó Firebase Authentication mediante credenciales de correo electrónico y contraseña. Esta decisión se fundamenta en su alto nivel de seguridad, ya que delega la gestión de identidades a un servicio administrado en la nube que cifra las credenciales y almacena la sesión de forma protegida en el contenedor seguro del hardware móvil (Android Keystore), evitando guardar contraseñas en texto plano o exponer la app a riesgos de ingeniería inversa comunes en bases de datos locales como SQLite. En términos de escalabilidad, la infraestructura en la nube permite registrar y autenticar usuarios ilimitados asignándoles un identificador único (UID) global para enlazar sus propios dispensadores sin necesidad de gestionar servidores propios. Finalmente, destaca por su facilidad de implementación, ya que ofrece un SDK oficial optimizado para Kotlin y Jetpack Compose que resuelve en pocas líneas de código el flujo completo de validación, registro y persistencia de sesión sin la complejidad técnica que exigiría programar y asegurar una API externa con MySQL.

El módulo de autenticación se estructura en dos vistas complementarias para gestionar el acceso de los usuarios. La primera corresponde a la pantalla de Iniciar Sesión, orientada a usuarios recurrentes que ingresan mediante su correo electrónico y contraseña validados a través de Firebase Authentication. La segunda es la pantalla de Registro de Usuario, accesible mediante un botón secundario desde la vista principal, la cual despliega un formulario para dar de alta nuevas cuentas solicitando nombre, correo institucional y confirmación de contraseña, asegurando un control de acceso centralizado antes de habilitar las funciones operativas del dispensador.
