# RPGen - Generador de Combates Pokémon

Un sistema completo para crear equipos de Pokémon y realizar combates estratégicos con todas las mecánicas del juego original.

## Características Principales

### 🎮 Sistema de Combate Completo
- **Combates por turnos** con mecánicas realistas de Pokémon
- **Cálculo de daño** basado en tipos, estadísticas y efectividad
- **Animaciones** de ataque y daño
- **Sistema de velocidad** que determina el orden de ataque
- **Cambio de Pokémon** durante el combate
- **Registro de batalla** detallado

### 📊 Estadísticas Avanzadas
- **IVs (Individual Values)**: Valores de 0-31 para cada estadística
- **EVs (Effort Values)**: Puntos de esfuerzo de 0-252 por estadística (máximo 510 total)
- **Naturalezas**: 25 naturalezas diferentes que modifican estadísticas (+10%/-10%)
- **Cálculo automático** de estadísticas finales basado en nivel, IVs, EVs y naturaleza

### 🎯 Habilidades y Objetos
- **Habilidades**: Todas las habilidades disponibles para cada Pokémon (incluyendo habilidades ocultas)
- **Objetos equipados**: Más de 20 objetos diferentes con efectos únicos
- **Modificadores de estadísticas** aplicados automáticamente
- **Efectos especiales** como restauración de HP, aumento de poder, etc.

### 🏆 Gestión de Equipos
- **Crear múltiples equipos** con nombres personalizados
- **Selección de movimientos** (hasta 4 por Pokémon)
- **Configuración completa** de cada Pokémon individualmente
- **Guardado automático** en localStorage
- **Importar/exportar** equipos

### 🎨 Interfaz Moderna
- **Modo oscuro/claro** con persistencia de preferencias
- **Diseño responsivo** que funciona en diferentes dispositivos
- **Información detallada** en tooltips y modales
- **Animaciones suaves** y transiciones
- **Iconos intuitivos** para todas las acciones

## Tecnologías Utilizadas

### Backend
- **Java 17** con Spring Boot
- **PokéAPI** para datos de Pokémon
- **HTTP Client** para peticiones asíncronas
- **Gson** para parsing de JSON

### Frontend
- **HTML5** con CSS3 moderno
- **JavaScript ES6+** con async/await
- **Font Awesome** para iconos
- **Grid y Flexbox** para layouts

## Instalación y Uso

### Requisitos
- Java 17 o superior
- Maven 3.6+

### Ejecución
1. Clona el repositorio
2. Ejecuta `mvn spring-boot:run`
3. Abre `http://localhost:8080` en tu navegador

### Uso del Sistema

#### 1. Crear Equipos
- Navega a la página principal
- Busca Pokémon por nombre o tipo
- Haz clic en un Pokémon para añadirlo a tu equipo
- Configura movimientos, habilidades, objetos y estadísticas
- Guarda tu equipo con un nombre personalizado

#### 2. Configurar Pokémon
- **Habilidades**: Selecciona entre las habilidades disponibles del Pokémon
- **Objetos**: Equipa objetos que modifican estadísticas o proporcionan efectos especiales
- **Naturalezas**: Elige una naturaleza que aumente y disminuya estadísticas específicas
- **IVs/EVs**: Ajusta los valores individuales y de esfuerzo para optimizar estadísticas

#### 3. Iniciar Combate
- Ve a la página de combate
- Selecciona dos equipos diferentes
- Elige los Pokémon iniciales
- Realiza acciones por turnos (ataques o cambios)
- Observa el resultado del combate

## Mecánicas del Juego

### Cálculo de Estadísticas
```
HP = ((2 * Base + IV + EV/4) * Nivel) / 100 + Nivel + 10
Otras Stats = (((2 * Base + IV + EV/4) * Nivel) / 100 + 5) * Naturaleza * Objeto
```

### Cálculo de Daño
```
Daño = ((2 * Nivel / 5 + 2) * Poder * Ataque / Defensa) / 50 + 2
Daño Final = Daño * Efectividad * Variación * Objetos
```

### Efectividad de Tipos
- **Super efectivo**: x2 daño
- **Normal**: x1 daño
- **No muy efectivo**: x0.5 daño
- **Sin efecto**: x0 daño

## Estructura del Proyecto

```
src/main/java/com/rpgen/
├── controller/          # Controladores REST
├── core/               # Lógica del juego
│   ├── action/         # Movimientos y acciones
│   ├── battle/         # Sistema de combate
│   └── entity/         # Entidades base
├── pokemon/            # Clases específicas de Pokémon
└── web/                # Servidores web

src/main/resources/public/
├── index.html          # Página principal
├── pokemon.html        # Gestión de equipos
└── battle.html         # Combate
```

## Características Técnicas

### Optimización
- **Carga progresiva** de Pokémon desde la API
- **Caché local** de datos para mejor rendimiento
- **Peticiones asíncronas** para no bloquear la interfaz
- **Límites de concurrencia** para evitar sobrecarga de la API

### Persistencia
- **localStorage** para equipos y configuraciones
- **Hashes únicos** para identificar Pokémon en equipos
- **Sincronización automática** entre páginas

### Escalabilidad
- **Arquitectura modular** fácil de extender
- **APIs RESTful** para futuras integraciones
- **Separación clara** entre lógica de negocio y presentación

## Contribuir

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

## Agradecimientos

- **PokéAPI** por proporcionar los datos de Pokémon
- **Font Awesome** por los iconos
- **Spring Boot** por el framework de backend
