# Modelo Entidad-Relación - Sistema de Reservas de Citas Médicas

## Entidades y Atributos

### 1. PATIENT (Paciente)
| Atributo | Tipo | Restricciones |
|-----------|------|---------------|
| id | BIGINT | PK, AutoIncrement |
| document_number | VARCHAR(20) | NOT NULL, UNIQUE |
| full_name | VARCHAR(150) | NOT NULL |
| email | VARCHAR(255) | NOT NULL, UNIQUE |
| phone | VARCHAR(20) | Opcional |
| status | VARCHAR(255) | NOT NULL (ACTIVE/INACTIVE) |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | Opcional |

### 2. SPECIALTY (Especialidad)
| Atributo | Tipo | Restricciones |
|-----------|------|---------------|
| id | BIGINT | PK, AutoIncrement |
| name | VARCHAR(100) | NOT NULL, UNIQUE |
| description | VARCHAR(300) | Opcional |

### 3. DOCTOR (Doctor)
| Atributo | Tipo | Restricciones |
|-----------|------|---------------|
| id | BIGINT | PK, AutoIncrement |
| full_name | VARCHAR(150) | NOT NULL |
| email | VARCHAR(255) | NOT NULL, UNIQUE |
| license_number | VARCHAR(20) | Opcional |
| active | BOOLEAN | NOT NULL, DEFAULT true |
| specialty_id | BIGINT | FK → Specialty, NOT NULL |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | Opcional |

### 4. OFFICE (Consultorio)
| Atributo | Tipo | Restricciones |
|-----------|------|---------------|
| id | BIGINT | PK, AutoIncrement |
| name | VARCHAR(50) | NOT NULL, UNIQUE |
| location | VARCHAR(200) | Opcional |
| status | VARCHAR(255) | NOT NULL (AVAILABLE/UNAVAILABLE/MAINTENANCE) |

### 5. APPOINTMENT_TYPE (Tipo de Cita)
| Atributo | Tipo | Restricciones |
|-----------|------|---------------|
| id | BIGINT | PK, AutoIncrement |
| name | VARCHAR(100) | NOT NULL, UNIQUE |
| duration_minutes | INTEGER | NOT NULL |

### 6. APPOINTMENT (Cita)
| Atributo | Tipo | Restricciones |
|-----------|------|---------------|
| id | BIGINT | PK, AutoIncrement |
| patient_id | BIGINT | FK → Patient, NOT NULL |
| doctor_id | BIGINT | FK → Doctor, NOT NULL |
| office_id | BIGINT | FK → Office, NOT NULL |
| appointment_type_id | BIGINT | FK → AppointmentType, NOT NULL |
| start_at | DATETIME | NOT NULL |
| end_at | DATETIME | NOT NULL |
| status | VARCHAR(255) | NOT NULL (SCHEDULED/CONFIRMED/COMPLETED/CANCELLED/NO_SHOW) |
| cancellation_reason | VARCHAR(255) | Opcional (obligatorio al cancelar) |
| observations | VARCHAR(255) | Opcional |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | Opcional |

### 7. DOCTOR_SCHEDULE (Horario del Doctor)
| Atributo | Tipo | Restricciones |
|-----------|------|---------------|
| id | BIGINT | PK, AutoIncrement |
| doctor_id | BIGINT | FK → Doctor, NOT NULL |
| day_of_week | VARCHAR(255) | NOT NULL (MONDAY-SUNDAY) |
| start_time | TIME | NOT NULL |
| end_time | TIME | NOT NULL |

---

## Relaciones

| Relación | Tipo | Descripción |
|----------|------|-------------|
| Doctor → Specialty | N:1 | Un doctor tiene una especialidad |
| Specialty → Doctor | 1:N | Una especialidad puede tener varios doctores |
| Appointment → Patient | N:1 | Un paciente puede tener varias citas |
| Appointment → Doctor | N:1 | Un doctor puede tener varias citas |
| Appointment → Office | N:1 | Un consultorio puede tener varias citas |
| Appointment → AppointmentType | N:1 | Una cita tiene un tipo de cita |
| DoctorSchedule → Doctor | N:1 | Un doctor puede tener varios horarios |

---

## Restricciones de Negocio

1. **Sin conflictos de horario**: Un doctor no puede tener dos citas simultáneas
2. **Sin conflictos de consultorio**: Un consultorio no puede tener dos citas simultáneas
3. **Horario válido**: Las citas solo pueden crearse dentro del horario del doctor
4. **Consultorio disponible**: Las citas solo pueden crearse en consultorios disponibles
5. **Paciente activo**: Solo pacientes activos pueden agendar citas
6. **Doctor activo**: Solo doctores activos pueden recibir citas
7. **Cancelación con razón**: La cancelación de una cita requiere un motivo
8. **Duración basada en tipo**: La fecha fin se calcula automáticamente según el tipo de cita

---

## Diagrama Visual (Texto)

```
┌─────────────┐       ┌─────────────┐
│  SPECIALTY  │       │   PATIENT   │
├─────────────┤       ├─────────────┤
│ PK id       │       │ PK id       │
│ name        │       │ doc_number  │◄────┐
│ description │       │ full_name   │     │
└──────┬──────┘       │ email       │     │
       │              │ status      │     │
       │ 1           └──────────────┘     │
       ▼ N                               │
┌─────────────┐       ┌─────────────┐     │
│   DOCTOR    │       │  APPOINTMENT│     │
├─────────────┤       ├─────────────┤     │
│ PK id       │       │ PK id       │     │
│ full_name   │       │ start_at    │     │
│ email       │       │ end_at      │     │
│ active      │       │ status      │     │
│ FK specialty│◄──────│ cancellation│     │
│ created_at  │       │ observations│     │
│ updated_at  │       │ FK patient  │─────┘
└──────┬──────┘       │ FK doctor   │─────┐
       │              │ FK office   │     │
       │ 1           │ FK appt_type│     │
       ▼ N           └──────────────┘     │
┌─────────────┐                           │
│DOCTOR_SCHED │       ┌─────────────┐     │
├─────────────┤       │   OFFICE    │     │
│ PK id       │       ├─────────────┤     │
│ day_of_week │       │ PK id       │     │
│ start_time  │       │ name        │◄────┘
│ end_time    │       │ location    │
│ FK doctor   │       │ status      │
└─────────────┘       └─────────────┘

┌──────────────────┐
│ APPOINTMENT_TYPE │
├──────────────────┤
│ PK id            │
│ name             │
│ duration_minutes│
└──────────────────┘
```

---

## Notas Técnicas

- Todas las entidades usan GenerationType.IDENTITY para AUTO_INCREMENT
- Las fechas de creación y actualización se manejan con @PrePersist y @PreUpdate
- Los enumerados se almacenan como VARCHAR en la BD
- Soft delete: los pacientes y doctores tienen status para indicar si están activos