# MedicApp

Aplicación de control de medicación para personas mayores. Tiene dos vistas:

- **Paciente**: consulta su medicación del día y registra las tomas.
- **Cuidador**: gestiona la medicación de uno o varios pacientes y hace seguimiento de sus tomas.

Proyecto final de 2º de DAM.

## Estructura del monorepo

```
medicapp/
├── backend/   API REST · Java 21 + Spring Boot 3 (Maven) + PostgreSQL (Supabase)
├── android/   App móvil · Kotlin + Jetpack Compose (pendiente de crear)
├── web/       Cliente web · React (opcional, más adelante)
├── docs/
│   ├── diagramas/    Diagramas (E/R, casos de uso, arquitectura...)
│   └── decisiones/   Registro de decisiones técnicas
└── .github/   CI (GitHub Actions) y plantilla de pull request
```

El backend está organizado **por funcionalidad** (`medicacion`, `paciente`, `cuidador`,
`toma`, `auth`), y cada paquete contiene su controller, service, repository, dto y entity.
Lo transversal (configuración, excepciones globales, seguridad) va en `common`.

> **Importante:** cada parte se abre por separado en su IDE, **nunca la raíz del repositorio**:
>
> - `backend/` → VS Code
> - `android/` → Android Studio

## Requisitos

- **Java 21** (JDK). Comprueba con `java -version`.
- No hace falta instalar Maven: se usa el Maven Wrapper incluido (`mvnw` / `mvnw.cmd`).
- Acceso a la base de datos PostgreSQL del proyecto en Supabase.

## Variables de entorno

El backend no contiene credenciales. Lee la conexión de estas variables:

| Variable      | Descripción                                                   |
|---------------|---------------------------------------------------------------|
| `DB_URL`      | URL JDBC, p. ej. `jdbc:postgresql://<host>:5432/postgres?sslmode=require` |
| `DB_USER`     | Usuario de la base de datos                                   |
| `DB_PASSWORD` | Contraseña de la base de datos                                |

Lo más cómodo es crear un archivo `.env` a partir de la plantilla:

```bash
cp .env.example .env    # y rellena los valores
```

El backend carga automáticamente `.env` si está en la raíz del repositorio o en `backend/`.
También puedes definir las variables en el sistema o en la configuración de ejecución
del IDE; las variables del sistema tienen prioridad sobre el `.env`.
El archivo `.env` está en `.gitignore`: **no lo subas nunca**.

## Arrancar el backend

Desde la carpeta `backend/`:

```bash
# Linux / macOS / Git Bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Windows (PowerShell / CMD)
mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

El perfil `dev` muestra en el log las sentencias SQL. Sin perfil, arranca con la
configuración base.

Comprueba que está levantado en <http://localhost:8080/actuator/health>
(debe responder `{"status":"UP"}`).

### Tests

```bash
./mvnw verify
```

Los tests que necesitan base de datos solo se ejecutan si `DB_URL` está definida, así que
`verify` funciona sin conexión (también en la CI).

### Base de datos

El esquema se gestiona **solo con migraciones de Flyway** en
`backend/src/main/resources/db/migration/` (`V1__descripcion.sql`, `V2__...`).
Hibernate únicamente valida (`ddl-auto: validate`) que las entidades coinciden con las tablas.
Una migración ya aplicada no se modifica nunca: los cambios van en una migración nueva.
