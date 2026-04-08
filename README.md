# Sistema de Reservas de Citas Médicas

## 1. Descripción del Proyecto

Sistema web RESTful para la gestión de citas médicas que permite:
- Registro y gestión de pacientes, doctores, especialidades y consultorios
- Agendamiento de citas con validación de disponibilidad
- Seguimiento del estado de las citas (programada, confirmada, completada, cancelada, no presentada)
- Reportes de productividad y ocupación

## 2. Reglas de Negocio

### 2.1 Citas Médicas
- Una cita debe tener: paciente, doctor, consultorio, tipo de cita y fecha/hora de inicio
- La fecha fin se calcula automáticamente según la duración del tipo de cita
- No puede existir dos citas para el mismo doctor en horario overlapped
- No puede existir dos citas para el mismo consultorio en horario overlapped
- Solo pacientes con status ACTIVE pueden agendar citas
- Solo doctores con active=true pueden recibir citas
- Solo consultorios con status AVAILABLE pueden tener citas

### 2.2 Estados de Cita
| Estado | Descripción | Transiciones válidas |
|--------|-------------|---------------------|
| SCHEDULED | Cita creada, sin confirmar | → CONFIRMED, → CANCELLED |
| CONFIRMED | Cita confirmada por el paciente | → COMPLETED, → CANCELLED, → NO_SHOW |
| COMPLETED | Cita atendida exitosamente | (final) |
| CANCELLED | Cita cancelada (requiere motivo) | (final) |
| NO_SHOW | Paciente no se presentó | (final) |

### 2.3 Horarios de Doctores
- Un doctor puede tener múltiples horarios por día de la semana
- Las citas solo pueden crearse dentro del horario configured del doctor
- No puede existir dos horarios para el mismo doctor en el mismo día

### 2.4 Pacientes y Doctores
- Soft delete: no se eliminan registros, se cambia el status
- Document number y email deben ser únicos

## 3. Decisiones de Diseño

### 3.1 Arquitectura
- **Patrón MVC** con separación clara en capas:
  - **API Layer**: Controllers REST con DTOs de request/response
  - **Service Layer**: Lógica de negocio con interfaces e implementaciones
  - **Domain Layer**: Entidades JPA y repositorios

### 3.2 Tecnologías
- Spring Boot 3.5.x
- Spring Data JPA (Hibernate 6.x)
- PostgreSQL
- Lombok para reducir boilerplate
- Jakarta Validation para validación de entrada
- Testcontainers para tests de integración

### 3.3 Patrones Utilizados
- **Repository Pattern**: Interfaces Spring Data para acceso a datos
- **Service Layer**: Separación de lógica de negocio
- **DTO Pattern**: Objetos de transferencia para API
- **Mapper Pattern**: Conversión entre Entity y DTO
- **Builder Pattern**: Construcción de objetos complejos

### 3.4 Diseño de API
- RESTful con códigos de estado HTTP apropiados
- Paginación para endpoints de listados
- Validación con Jakarta Bean Validation
- Excepciones personalizadas con @ControllerAdvice

## 4. Estructura del Proyecto

```
src/main/java/edu/unimagdalena/reservas/
├── api/                          # Controllers REST
│   ├── AppointmentController.java
│   ├── AvailabilityController.java
│   ├── DoctorController.java
│   ├── PatientController.java
│   ├── OfficeController.java
│   ├── SpecialtyController.java
│   ├── DoctorScheduleController.java
│   ├── AppointmentTypeController.java
│   ├── ReportController.java
│   ├── dto/request/              # DTOs de entrada
│   └── dto/response/             # DTOs de salida
│
├── services/                     # Capa de servicios
│   ├── AppointmentService.java   # Interfaces
│   ├── impl/                     # Implementaciones
│   └── mapper/                   # Mappers
│
├── domine/                       # Capa de dominio
│   ├── entities/                 # Entidades JPA
│   ├── repositories/             # Repositorios
│   └── enums/                    # Enumeraciones
│
├── exception/                    # Excepciones personalizadas
└── ReservasApplication.java
```

## 5. Forma de Ejecución

### 5.1 Requisitos Previos
- Java 21
- Maven 3.9+
- Docker Desktop (para ejecutar PostgreSQL o tests de integración)

### 5.2 Configuración de Base de Datos

#### Opción A: Docker Compose (Recomendado)
```bash
docker-compose up -d
```

#### Opción B: PostgreSQL local
Crear base de datos `reservas2025` con credenciales:
- Usuario: `postgres`
- Contraseña: `Colombia2020*`

### 5.3 Compilación y Ejecución

```bash
# Compilar
mvn clean package

# Ejecutar
mvn spring-boot:run
```

La aplicación estará disponible en: `http://localhost:8080`

### 5.4 Tests Automáticos

```bash
# Ejecutar todos los tests
mvn test

# Tests de integración (requiere Docker)
mvn test -Ddocker.available=true
```

### 5.5 Variables de Configuración (application.properties)

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/reservas2025
spring.datasource.username=postgres
spring.datasource.password=Colombia2020*
spring.jpa.hibernate.ddl-auto=update
```

## 6. Endpoints Principales

| Recurso | Métodos |
|---------|---------|
| `/api/patients` | GET, POST |
| `/api/patients/{id}` | GET, PUT |
| `/api/doctors` | GET, POST |
| `/api/doctors/{id}` | GET, PUT |
| `/api/specialties` | GET, POST |
| `/api/offices` | GET, POST, PUT |
| `/api/appointment-types` | GET, POST |
| `/api/appointments` | GET, POST |
| `/api/appointments/{id}` | GET |
| `/api/appointments/{id}/confirm` | PUT |
| `/api/appointments/{id}/cancel` | PUT |
| `/api/appointments/{id}/complete` | PUT |
| `/api/appointments/{id}/no-show` | PUT |
| `/api/doctor-schedules` | GET, POST, DELETE |
| `/api/availability/doctor` | GET |
| `/api/availability/office` | GET |
| `/api/availability/slots` | GET |
| `/api/reports/no-show-patients` | GET |
| `/api/reports/office-occupancy` | GET |
| `/api/reports/doctor-productivity` | GET |

## 7. Colección Postman

Se incluye `postman_collection.json` con todos los endpoints preconfigurados para pruebas.

## 8. Tests

- **Unit Tests**: Controllers y Services con Mockito
- **Integration Tests**: Repositorios con Testcontainers (requiere Docker)
- **Total**: 62 tests pasando

## 9. Docker

```bash
# Construir imagen
docker build -t reservas-app .

# Ejecutar contenedor
docker run -p 8080:8080 --link reservas_postgres:reservas_postgres reservas-app
```

El proyecto incluye:
- `Dockerfile`: Imagen de la aplicación
- `docker-compose.yml`: PostgreSQL + Aplicación
- `.dockerignore`: Archivos ignorados en build