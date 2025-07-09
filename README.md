# RPGen-Chrono: Generador de Combates de Chrono Trigger

Extensión del sistema RPGen que recrea fielmente el sistema de combate del clásico JRPG **Chrono Trigger**. Permite a los usuarios seleccionar personajes, equiparlos con armas y accesorios del juego original, y enfrentarlos a enemigos icónicos de todas las eras en un sistema de batalla ATB (Active Time Battle).

## Características Principales

### 🎮 Sistema de Combate ATB (Active Time Battle)

  - **Batallas por turnos en tiempo real** donde cada personaje y enemigo actúa al llenarse su barra de ATB.
  - La **velocidad** de cada personaje determina la rapidez con la que pueden actuar.
  - **Mecánicas de efectos** complejas, incluyendo resistencias y absorción elemental (fuego, agua, rayo, sombra), protecciones contra estados alterados y efectos especiales como contraataques o auto-reanimación.
  - Soporte para **Técnicas Individuales, Dobles y Triples**, permitiendo combinar los poderes de varios personajes en un solo ataque devastador.

### 📊 Gestión Completa de Personajes y Equipos

  - **Selección de equipos** de hasta 3 aliados y 5 enemigos para simular cualquier combate.
  - **Equipamiento completo**: Elige entre un arsenal de armas, armaduras, cascos y accesorios extraídos directamente del juego.
  - **Modificadores de estadísticas** que se aplican en tiempo real al equipar objetos, afectando atributos como el poder, la defensa o la velocidad.
  - **Sistema de niveles** que ajusta las estadísticas base de los personajes según el nivel seleccionado, desde el 1 hasta el 99.

### ⚔️ Datos Fieles al Juego Original

  - **Bases de datos completas** en formato JSON que incluyen:
      - Todos los **personajes jugables** con sus estadísticas por nivel.
      - Cientos de **enemigos** de todas las eras del juego, desde 65,000,000 A.C. hasta el Black Omen.
      - Todas las **técnicas** disponibles, con sus costes de MP y descripciones.
      - El catálogo completo de **objetos**, **armas**, **armaduras**, **cascos** y **accesorios** con sus efectos originales.
      - Todos los **estados alterados** como Lento, Paro, Veneno o Caos.

### 🎨 Interfaz Web Interactiva

  - **Selección visual de equipos** a través de tarjetas de personaje y enemigo.
  - **Panel de equipamiento dinámico** que permite cambiar el equipo de cada personaje y ver cómo afectan sus estadísticas.
  - **Simulador de combate** con registro de acciones, animaciones de ataque y un sistema de mensajes que narra el desarrollo de la batalla.
  - **Modo oscuro/claro** para adaptar la interfaz a las preferencias del usuario.

## Tecnologías Utilizadas

### Backend

  - **Java**: El núcleo de la lógica de negocio.
  - **SparkJava**: Framework ligero para crear el servidor web y las APIs REST.
  - **Gson**: Biblioteca de Google para manejar la serialización y deserialización de datos JSON.

### Frontend

  - **HTML5** y **CSS3**: Para la estructura y el diseño de la interfaz de usuario.
  - **JavaScript (Vanilla)**: Para la interactividad, la lógica del cliente y la comunicación con el backend.

## Estructura del Proyecto

```
RPGen-ChronoTrigger/
├── src/main/java/com/rpgen/chrono/
│   ├── battle/         # Lógica del motor de batalla (ChronoBattleEngine)
│   ├── data/           # Cargadores de datos desde JSON (ChronoDataLoader)
│   ├── entity/         # Clases de entidad (ChronoEntity, ChronoMove, etc.)
│   └── web/            # Servidor web y endpoints de la API (ChronoBattleServer)
│
├── src/main/resources/
│   ├── public/
│   │   ├── chrono-battle.html      # Interfaz de combate
│   │   ├── chrono-team.html        # Interfaz de selección de equipos
│   │   └── chrono-json/            # Todos los datos del juego en JSON
│   │       ├── chrono-characters.json
│   │       ├── chrono-techs.json
│   │       ├── chrono-items.json
│   │       ├── chrono-armor.json
│   │       └── enemies/
│   │           ├── 1000AD.json
│   │           ├── 600AD.json
│   │           └── ...
│   └── logback.xml                 # Configuración de logging
│
└── pom.xml                         # Dependencias y configuración de Maven
```

## Instalación y Uso

### Requisitos

  - **Java 16** o superior.
  - **Maven** para gestionar las dependencias y la compilación.

### Ejecución

1.  Clona el repositorio en tu máquina local.
2.  Descarga el núcleo desde `https://github.com/M3D1S4NT4/RPGen`
3.  Abre una terminal en el directorio raíz del núcleo
4.  Instala el núcleo usando
    ```bash
    mvn clean install
    ```
5.  Abre una terminal en el directorio raíz del proyecto.
6.  Ejecuta el comando `mvn spring-boot:run` para compilar el proyecto y descargar las dependencias.
7.  Abre tu navegador y ve a `http://localhost:4567`.

### Uso del Sistema

1.  **Selección de Equipos**: Abre 1 en tu navegador.
2.  **Elige a tus Aliados**: Haz clic en hasta 3 personajes para añadirlos a tu equipo. Puedes ajustar su nivel y ver cómo cambian sus estadísticas.
3.  **Equipa a tus Personajes**: Al seleccionar un aliado, el panel de equipamiento se activará. Elige su arma, armadura, casco y accesorio de las listas desplegables.
4.  **Selecciona a los Enemigos**: Usa el menú desplegable para elegir una era y haz clic en hasta 5 enemigos para formar el equipo contrario.
5.  **Iniciar Combate**: Pulsa el botón "Iniciar Batalla". Serás redirigido a `chrono-battle.html`.
6.  **Lucha**: Cuando la barra de ATB de un personaje se llene, podrás realizar una acción (Atacar, Técnica u Objeto). ¡Derrota a todos los enemigos para ganar\!.
