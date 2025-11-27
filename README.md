# Trabajo de graduación - Desarrollo de una aplicación móvil para la generación de un mapa dinámico de sistemas de transporte público del Área Metropolitana de Guatemala

El proyecto consiste en una aplicación móvil y sus respectivos servicios de backend, diseñados para la recolección, procesamiento y visualización de información de sistemas de transporte público en el Área Metropolitana de Guatemala.

La aplicación permite el registro de información de forma estática, a través de formularios y dinámica a través de servicios en segundo plano para obtener datos de forma recurrente. La aplicación registra de forma periódica puntos con ubicación, velocidad y marca de tiempo, construyendo trayectos georreferenciados a partir del movimiento real de las unidades.

Estos datos son procesados en el backend mediante un pipeline de limpieza, map-matching sobre la red vial y algoritmos de simplificación de rutas, lo que habilita la comparación de trayectos y la generación de recorridos representativos. 

Sobre esta base, el sistema ofrece un mapa dinámico para consultar información de las rutas, paradas y la ubicación de las unidades activas. Por otro lado, integra mecanismos de predicción de trayectos y proporciona herramientas de gestión para operadores, como la creación y gestión de rutas y paradas.

El proyecto se divide en dos partes principales:
- [Servicios de backend](#servicios-backend)
- [Aplicación móvil](#aplicación-móvil)

## Tabla de Contenidos

- [Arquitectura del Sistema](#arquitectura-del-sistema)
- [Servicios Backend](#servicios-backend)
  - [Main API (Express/Node.js)](#1-main-api-expressnodejs)
    - [Tecnologías Utilizadas](#tecnologías-utilizadas-main-api)
    - [Requisitos Previos](#requisitos-previos-main-api)
    - [Estructura del Proyecto](#estructura-del-proyecto-main-api)
  - [Routes API (FastAPI/Python)](#2-routes-api-fastapipython)
    - [Tecnologías Utilizadas](#tecnologías-utilizadas-routes-api)
    - [Requisitos Previos](#requisitos-previos-routes-api)
    - [Estructura del Proyecto](#estructura-del-proyecto-routes-api)
  - [OSRM Service](#3-osrm-service)
    - [Tecnologías Utilizadas](#tecnologías-utilizadas-osrm-service)
    - [Requisitos Previos](#requisitos-previos-osrm-service)
- [Instrucciones de Instalación y Ejecución (Backend)](#instrucciones-de-instalación-y-ejecución-backend)
  - [Ambiente de Desarrollo](#ambiente-de-desarrollo)
  - [Ambiente de Producción](#ambiente-de-producción)
  - [Detener los Servicios](#detener-los-servicios)
- [Aplicación Móvil](#aplicación-móvil)
  - [Tecnologías Utilizadas](#tecnologías-utilizadas-app)
  - [Requisitos Previos](#requisitos-previos-app)
  - [Instrucciones de Instalación y Ejecución](#instrucciones-de-instalación-y-ejecución-1)
  - [Estructura del Proyecto](#estructura-del-proyecto)
- [Demo e Información Adicional](#demo-e-información-adicional)
  - [Video Demo](#video-demo)
  - [Informe Final](#informe-final)
- [Autor](#autor)

---

<h2 id="arquitectura-del-sistema">Arquitectura del Sistema</h2>

El sistema está diseñado como una arquitectura cliente-servidor con los siguientes componentes principales:

<h3 id="backend">Backend</h3>

Sistema backend para gestión de transporte público que proporciona servicios de gestión de rutas, paradas, agencias y seguimiento en tiempo real de unidades de transporte.

El backend está compuesto por tres microservicios independientes que trabajan en conjunto:

- **Main API**: Servicio principal de gestión y administración desarrollado en Node.js/Express
- **Routes API**: Servicio especializado en procesamiento de rutas y predicciones desarrollado en Python/FastAPI
- **OSRM Service**: Servicio de routing y cálculo de distancias geográficas basado en OpenStreetMap

<h3 id="aplicación-móvil-nativa">Aplicación Móvil Nativa</h3>

- **App Android**: Aplicación móvil desarrollada en Kotlin que consume los servicios backend y proporciona una interfaz de usuario para visualización de rutas, paradas, seguimiento en tiempo real y gestión de información del transporte público

---

<h2 id="servicios-backend">Servicios Backend</h2>

<h3 id="1-main-api-expressnodejs">1. Main API (Express/Node.js)</h3>

Servicio principal del sistema desarrollado en Node.js con Express y TypeScript. Proporciona endpoints RESTful para la gestión completa de agencias de transporte, rutas, paradas, usuarios, sesiones y seguimiento de unidades en tiempo real. Incluye autenticación basada en JWT, autorización por roles, almacenamiento de archivos en AWS S3 y validación exhaustiva de datos.

<h4 id="tecnologías-utilizadas-main-api">Tecnologías Utilizadas</h4>

- **Runtime**: Node.js 22
- **Framework**: Express.js 5.1.0
- **Lenguaje**: TypeScript 5.8.3
- **Base de Datos**: 
  - MongoDB 6.17.0 (con Mongoose 8.15.1)
- **Autenticación**: 
  - JWT (jsonwebtoken 9.0.2)
  - Argon2 0.43.0 (hashing de contraseñas)
- **Almacenamiento**: 
  - AWS S3 SDK 3.883.0
  - Multer 2.0.1 (manejo de archivos)
- **Validación**: Yup 1.6.1
- **Utilidades**:
  - H3-js 4.3.0 (indexación geográfica)
  - Dayjs 1.11.13 (manejo de fechas)
  - UUID 11.1.0

<h4 id="requisitos-previos-main-api">Requisitos Previos</h4>

- Node.js 22 o superior
- npm (gestor de paquetes)
- MongoDB (instancia local o remota)
- Docker y Docker Compose

<h4 id="estructura-del-proyecto-main-api">Estructura del Proyecto</h4>

```
main-api/
├── apiServices/              # Servicios de API organizados por dominio
│   ├── agency/               # Gestión de agencias de transporte
│   ├── route/                # Gestión de rutas
│   ├── run/                  # Gestión de recorridos y seguimiento
│   ├── session/              # Gestión de sesiones y autenticación
│   ├── stop/                 # Gestión de paradas
│   └── user/                 # Gestión de usuarios
├── app.ts                    # Configuración principal de Express
├── bin/
│   └── www.ts               # Punto de entrada de la aplicación
├── config/                   # Archivos de configuración
├── db/
│   └── mongodb/
│       ├── connection.ts    # Conexión a MongoDB
│       └── schemas/         # Esquemas de Mongoose
├── middlewares/              # Middlewares personalizados (auth, validación, etc.)
├── routes/                   # Definición de rutas principales
├── services/                 # Servicios auxiliares
│   ├── cloudStorage/        # Servicio de almacenamiento en AWS S3
│   ├── hash/                # Servicio de hash de contraseñas
│   ├── token/               # Servicio de JWT
│   └── uploadFile/          # Servicio de carga de archivos
├── types/                    # Definiciones de tipos TypeScript
├── utils/                    # Utilidades y helpers
├── Dockerfile               # Dockerfile para producción
├── Dockerfile.dev           # Dockerfile para desarrollo
├── package.json             # Dependencias y scripts
└── tsconfig.json            # Configuración de TypeScript
```

---

<h3 id="2-routes-api-fastapipython">2. Routes API (FastAPI/Python)</h3>

Servicio especializado desarrollado en Python con FastAPI que se encarga del procesamiento avanzado de rutas de transporte. Incluye funcionalidades de predicción de llegadas, simplificación de rutas mediante algoritmos como Ramer-Douglas-Peucker, cálculo de distancias geodésicas y procesamiento de secuencias temporales mediante DTW (Dynamic Time Warping). Proporciona endpoints protegidos con autenticación JWT para el procesamiento de datos geográficos y temporales.

<h4 id="tecnologías-utilizadas-routes-api">Tecnologías Utilizadas</h4>

- **Runtime**: Python 3.11
- **Framework**: FastAPI
- **Servidor ASGI**: Uvicorn
- **Base de Datos**: Motor (driver async para MongoDB)
- **Procesamiento Numérico**:
  - NumPy
  - SciPy
  - Numba (optimización JIT)
- **Geolocalización**: H3 (indexación hexagonal)
- **Autenticación**: Python-JOSE (JWT)
- **Configuración**: 
  - Pydantic Settings
  - Python-dotenv

<h4 id="requisitos-previos-routes-api">Requisitos Previos</h4>

- Python 3.11 o superior
- pip (gestor de paquetes de Python)
- MongoDB (instancia local o remota)
- Docker y Docker Compose

<h4 id="estructura-del-proyecto-routes-api">Estructura del Proyecto</h4>

```
routes-api/
├── app/
│   ├── api_services/        # Servicios de API especializados
│   │   ├── prediction/      # Servicio de predicción de llegadas
│   │   └── simplification/  # Servicio de simplificación de rutas
│   ├── config/              # Configuración de la aplicación
│   ├── db/                  # Conexión a MongoDB
│   ├── main.py              # Configuración principal de FastAPI
│   ├── middlewares/         # Middlewares personalizados
│   ├── routes/              # Definición de rutas
│   └── utils/               # Utilidades y algoritmos
├── Dockerfile               # Dockerfile para producción
├── Dockerfile.dev           # Dockerfile para desarrollo
└── requirements.txt         # Dependencias de Python
```

---

<h3 id="3-osrm-service">3. OSRM Service</h3>

Servicio de routing basado en OSRM (Open Source Routing Machine) que proporciona funcionalidades de cálculo de rutas, distancias y tiempos de viaje para el territorio de Guatemala. Utiliza datos de OpenStreetMap procesados específicamente para vehículos automotores. Este servicio es utilizado por el Main API para realizar cálculos de routing y matching de puntos de ruta.

<h4 id="tecnologías-utilizadas-osrm-service">Tecnologías Utilizadas</h4>

- **Imagen Base**: OSRM Backend (oficial)
- **Algoritmo**: Contraction Hierarchies (CH)
- **Datos**: OpenStreetMap PBF para Guatemala

<h4 id="requisitos-previos-osrm-service">Requisitos Previos</h4>

- Docker y Docker Compose
- Archivo PBF de Guatemala procesado (guatemala.osrm)

---

<h2 id="instrucciones-de-instalación-y-ejecución-backend">Instrucciones de Instalación y Ejecución (Backend)</h2>

**Importante**: El punto de entrada principal de la aplicación es el servidor Express (Main API), que debe ejecutarse en el puerto 3000. Los demás servicios (Routes API y OSRM) son servicios auxiliares que el Main API consume internamente.

<h3 id="ambiente-de-desarrollo">Ambiente de Desarrollo</h3>

**Nota**: Las dependencias se instalan automáticamente dentro de los contenedores Docker durante el proceso de construcción de las imágenes. No es necesario instalar dependencias localmente.

#### 1. Preparar Archivos OSRM

Los archivos OSRM deben prepararse una vez antes de ejecutar el servicio.

1. **Descargar datos de OpenStreetMap para Guatemala**:
   
   Descargar el archivo PBF desde:
   ```
   https://download.geofabrik.de/central-america/guatemala-latest.osm.pbf
   ```
   
   Colocar el archivo en el directorio `backend/osrm/` con el nombre `guatemala.osm.pbf`

2. **Extraer los datos OSRM**:
   
   En Windows (PowerShell):
   ```powershell
   docker run --rm -v ${PWD}/osrm:/data osrm/osrm-backend:latest osrm-extract -p /opt/car.lua /data/guatemala.osm.pbf
   ```
   
   En Linux/Mac:
   ```bash
   docker run --rm -v $(pwd)/osrm:/data osrm/osrm-backend:latest osrm-extract -p /opt/car.lua /data/guatemala.osm.pbf
   ```

3. **Contraer la gráfica (Contract)**:
   
   En Windows (PowerShell):
   ```powershell
   docker run --rm -v ${PWD}/osrm:/data osrm/osrm-backend:latest osrm-contract /data/guatemala.osrm
   ```
   
   En Linux/Mac:
   ```bash
   docker run --rm -v $(pwd)/osrm:/data osrm/osrm-backend:latest osrm-contract /data/guatemala.osrm
   ```

   Esto generará los archivos necesarios (`.osrm`) en el directorio `backend/osrm/`

#### 2. Configurar Variables de Entorno

##### Main API

Crear un archivo `.env` en el directorio `backend/main-api/` con las siguientes variables:
```env
JWT_KEY=tu_clave_secreta_jwt
JWT_KEY_ROUTE_SERVICE=tu_clave_secreta_para_routes_api
DEV_MONGO_DB_CONNECTION_URI=mongodb://localhost:27017/nombre_base_datos
DEV_AWS_BUCKET_ACCESS_KEY=tu_access_key
DEV_AWS_BUCKET_SECRET_KEY=tu_secret_key
DEV_AWS_BUCKET_NAME=nombre_bucket
```

##### Routes API

Crear un archivo `.env.dev` en el directorio `backend/routes-api/` con las siguientes variables:
```env
MONGO_DB_CONNECTION_URI=mongodb://host:puerto/nombre_base_datos
MONGO_DB_NAME=nombre_base_datos
JWT_KEY_ROUTE_SERVICE=tu_clave_secreta_jwt_produccion
ALGORITHM=HS256
```

#### 3. Ejecutar con Docker Compose

Desde la raíz del proyecto de backend (`backend/`), ejecutar:

```bash
docker compose -f docker-compose.yml -f docker-compose.override.yml up --build
```

Este comando iniciará los tres servicios en modo desarrollo con las siguientes características:
- **Hot-reload** activado en Main API y Routes API
- **Volúmenes montados** para sincronización de código en tiempo real
- **Dockerfiles de desarrollo** con herramientas de desarrollo

Una vez iniciados, los servicios estarán disponibles en:
- **Main API (Punto de entrada)**: `http://localhost:3000`
- **Routes API**: `http://localhost:8000`
- **OSRM Service**: `http://localhost:5000`

---

<h3 id="ambiente-de-producción">Ambiente de Producción</h3>

**Nota**: Las dependencias se instalan automáticamente dentro de los contenedores Docker durante el proceso de construcción de las imágenes. No es necesario instalar dependencias localmente.

#### 1. Preparar Archivos OSRM

Los archivos OSRM deben estar preparados en el servidor antes de iniciar los servicios. Sigue los mismos pasos del ambiente de desarrollo, pero ajustando las rutas según tu entorno de producción.

Para Ubuntu/Linux en producción:

1. **Extraer los datos OSRM**:
   ```bash
   docker run --rm -v ~/backend/osrm:/data osrm/osrm-backend \
       osrm-extract -p /opt/car.lua /data/guatemala.osm.pbf
   ```

2. **Contraer la gráfica (Contract)**:
   ```bash
   docker run --rm -v ~/backend/osrm:/data osrm/osrm-backend \
       osrm-contract /data/guatemala.osrm
   ```

#### 2. Pasos del Pipeline de Despliegue

A continuación se detallan los pasos para ejecutar el proyecto en un entorno de producción. El ambiente utilizado fue una instancia Ubuntu EC2 alojada en AWS. 

##### Paso 1: Subir archivos del proyecto

Asegúrate de que todos los archivos necesarios estén en el servidor (excluyendo `node_modules`, `.git`, `.github`, y `osrm/`):

```bash
# Ejemplo usando rsync desde tu máquina local
rsync -avz --delete \
  --exclude '.git' \
  --exclude 'node_modules' \
  --exclude '.github' \
  --exclude 'osrm/' \
  ./ usuario@servidor:~/backend/
```

##### Paso 2: Configurar Variables de Entorno

**Main API**: Crear o actualizar el archivo `.env` en `main-api/`:

```env
JWT_KEY=tu_clave_secreta_jwt_produccion
JWT_KEY_ROUTE_SERVICE=tu_clave_secreta_para_routes_api_produccion
DEV_MONGO_DB_CONNECTION_URI=mongodb://host:puerto/nombre_base_datos
DEV_AWS_BUCKET_ACCESS_KEY=tu_access_key_produccion
DEV_AWS_BUCKET_SECRET_KEY=tu_secret_key_produccion
DEV_AWS_BUCKET_NAME=nombre_bucket_produccion
```

**Routes API**: Crear o actualizar el archivo `.env.production` en `routes-api/`:

```env
MONGO_DB_CONNECTION_URI=mongodb://host:puerto/nombre_base_datos
MONGO_DB_NAME=nombre_base_datos
JWT_KEY_ROUTE_SERVICE=tu_clave_secreta_jwt_produccion
ALGORITHM=HS256
```

#### 3. Ejecutar con Docker Compose

Desde la raíz del proyecto de backend (`backend/`) en el servidor, ejecutar:

```bash
cd ~/backend
docker compose -f docker-compose.yml down
docker compose -f docker-compose.yml build --no-cache
docker compose -f docker-compose.yml up -d
docker image prune -f
```

Este proceso:
1. **Detiene** los contenedores existentes
2. **Reconstruye** las imágenes sin usar caché (`--no-cache`)
3. **Inicia** los servicios en modo detached (`-d`)
4. **Limpia** las imágenes sin uso

Una vez iniciados, los servicios estarán disponibles en:
- **Main API (Punto de entrada)**: `http://localhost:3000`
- **Routes API**: `http://localhost:8000`
- **OSRM Service**: `http://localhost:5000`

---

<h3 id="detener-los-servicios">Detener los Servicios</h3>

Para detener todos los servicios en cualquier ambiente:

```bash
docker compose down
```

Para detener y eliminar volúmenes:

```bash
docker compose down -v
```

---

<h2 id="aplicación-móvil">Aplicación Móvil</h2>

Aplicación móvil Android desarrollada en Kotlin que permite a los usuarios visualizar y gestionar información de sistemas de transporte público del Área Metropolitana de Guatemala. La aplicación proporciona funcionalidades de visualización de rutas, paradas, seguimiento en tiempo real de unidades de transporte, creación de rutas y paradas, así como navegación mediante mapas interactivos.

La aplicación se conecta con los servicios backend para obtener y sincronizar datos, y utiliza servicios de geolocalización para el seguimiento en tiempo real de unidades de transporte. Incluye autenticación de usuarios, almacenamiento local seguro de credenciales y una interfaz de usuario moderna basada en Material Design.

<h3 id="tecnologías-utilizadas-app">Tecnologías Utilizadas</h3>

- **Lenguaje**: Kotlin 2.0.21
- **SDK Mínimo**: Android 8.0 (API 26)
- **SDK Objetivo**: Android 15 (API 35)
- **Arquitectura**:
  - MVVM (Model-View-ViewModel)
  - Navigation Component para navegación entre pantallas
  - ViewBinding para acceso a vistas
- **Inyección de Dependencias**: 
  - Hilt 2.51 (Dagger Hilt)
- **Red y API**:
  - Retrofit 2.9.0 (cliente HTTP)
  - Gson 2.10.1 (serialización JSON)
  - OkHttp Logging Interceptor 4.12.0
- **Almacenamiento Local**:
  - DataStore Preferences 1.1.7 (almacenamiento clave-valor)
  - Jetpack Security Crypto 1.0.0 (almacenamiento encriptado)
- **UI y Componentes**:
  - Material Design Components 1.13.0
  - AndroidX AppCompat 1.7.0
  - AndroidX ConstraintLayout 2.2.1
  - AndroidX ViewPager2 1.1.0 (galería de imágenes)
- **Mapas y Ubicación**:
  - Google Maps SDK 19.2.0
  - Google Play Services Location 21.3.0
  - Secrets Gradle Plugin 2.0.1 (gestión de API keys)
- **Imágenes**:
  - Coil 2.4.0 (carga de imágenes remotas)
- **Corrutinas**:
  - Kotlinx Coroutines Android 1.7.3
- **Lifecycle**:
  - AndroidX Lifecycle ViewModel 2.8.7
  - AndroidX Lifecycle LiveData 2.8.7
- **Testing**:
  - JUnit 4.13.2
  - AndroidX Test JUnit 1.2.1
  - Espresso 3.6.1

<h3 id="requisitos-previos-app">Requisitos Previos</h3>

- **Android Studio**: Hedgehog (2023.1.1) o superior
- **JDK**: Java 11 o superior
- **Android SDK**: API 26 (Android 8.0) mínimo, API 35 (Android 15) objetivo
- **Gradle**: 8.9.0 (incluido en el proyecto)
- **Google Maps API Key**: Se requiere una clave de API de Google Maps para el funcionamiento de los mapas
- **Conexión a Internet**: Para conectarse a los servicios backend

<h3 id="instrucciones-de-instalación-y-ejecución-1">Instrucciones de Instalación y Ejecución</h3>

<h4 id="configuración-inicial">Configuración Inicial</h4>

1. **Clonar el repositorio**:
   ```bash
   git clone <url-del-repositorio>
   cd PG-2025-21762/app
   ```

2. **Configurar Google Maps API Key**:
   
   La aplicación utiliza el plugin Secrets Gradle Plugin para gestionar la API key de Google Maps de forma segura.
   
   - Obtener una API key de Google Maps desde [Google Cloud Console](https://console.cloud.google.com/)
   - Crear o editar el archivo `local.properties` en la raíz del proyecto `app/` (si no existe):
   ```properties
   MAPS_API_KEY=tu_api_key_de_google_maps
   ```
   
   **Nota**: El archivo `local.properties` está en `.gitignore` y no se sube al repositorio por seguridad.

3. **Configurar URL del Backend**:
   
   Verificar que la URL base del backend esté correctamente configurada en el código. 
   
   Para producción o desarrollo, actualizar la variable ```apiUrl``` en el archivo de constantes (ubicado en `app\src\main\java\com\example\public_transport_app\utils\consts.kt`).

<h4 id="ejecución-en-android-studio">Ejecución en Android Studio</h4>

1. **Abrir el proyecto**:
   - Abrir Android Studio
   - Seleccionar "Open" y navegar a la carpeta del proyecto
   - Android Studio detectará automáticamente el proyecto Gradle y comenzará la sincronización

2. **Sincronizar dependencias**:
   - Android Studio sincronizará automáticamente las dependencias de Gradle
   - Si no se sincroniza automáticamente, hacer clic en "Sync Now" o usar `File > Sync Project with Gradle Files`

3. **Configurar dispositivo o emulador**:
   - Conectar un dispositivo Android físico mediante USB con la depuración USB habilitada, o
   - Crear y configurar un AVD (Android Virtual Device) desde `Tools > Device Manager`

4. **Ejecutar la aplicación**:
   - Seleccionar el dispositivo/emulador desde el menú desplegable de dispositivos
   - Hacer clic en el botón "Run"
   - La aplicación se compilará, instalará y ejecutará en el dispositivo seleccionado


<h3 id="estructura-del-proyecto">Estructura del Proyecto</h3>

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/public_transport_app/
│   │   │   ├── data/
│   │   │   │   ├── entity/          # Entidades de datos locales
│   │   │   │   ├── local/           # Almacenamiento local (DataStore, encriptación)
│   │   │   │   ├── remote/          # Clientes API y DTOs
│   │   │   │   └── repository/       # Repositorios de datos
│   │   │   ├── di/                  # Módulos de inyección de dependencias (Hilt)
│   │   │   ├── ui/                  # Componentes de UI (Fragments, ViewModels)
│   │   │   │   ├── homePage/        # Pantalla principal
│   │   │   │   ├── routeView/       # Visualización de rutas
│   │   │   │   ├── routesListView/  # Lista de rutas
│   │   │   │   ├── stopsListView/   # Lista de paradas
│   │   │   │   ├── loginPage/       # Autenticación
│   │   │   │   ├── trackingService/ # Servicio de seguimiento en segundo plano
│   │   │   │   └── shared/          # Componentes compartidos
│   │   │   └── utils/               # Utilidades y helpers
│   │   ├── res/                     # Recursos (layouts, drawables, strings, etc.)
│   │   └── AndroidManifest.xml      # Configuración de la aplicación
│   ├── androidTest/                 # Pruebas instrumentadas
│   └── test/                        # Pruebas unitarias
├── build.gradle                     # Configuración del módulo app
└── proguard-rules.pro              # Reglas de ProGuard
```

---

<h2 id="demo-e-información-adicional">Demo e Información Adicional</h2>

<h3 id="video-demo">Video Demo</h3>

Puedes encontrar una demostración del sistema en funcionamiento en:

[Ver demo en video](demo/demo.mp4)

<video src="demo/demo.mp4" controls width="320"></video>

<h3 id="informe-final">Informe Final</h3>

Para obtener información detallada sobre el proyecto, metodología, resultados y conclusiones, consulta el informe final:

[Ver informe final](docs/informe_final.pdf)

---

<h2 id="autor">Autor</h2>
Diego Andrés Morales Aquino

Carnet 21762

