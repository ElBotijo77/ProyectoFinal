# Decisiones de diseño de la base de datos

Aquí explicamos las decisiones más importantes que tomamos al diseñar la base de datos de la app (`V1__init.sql`). La base de datos es PostgreSQL en Supabase y las migraciones las gestionamos con Flyway.

## Convenciones

- Las tablas van en plural y en `snake_case`.
- Las claves son `INT` autogeneradas, menos en `tomas`, que usa `BIGINT` porque es la tabla que más va a crecer: una fila por cada dosis.
- Los valores cerrados (estados, roles, permisos, etc) los guardamos como texto en mayúsculas con un `CHECK` para un mejor tratado en Java.
- Las fechas con hora siempre van en `TIMESTAMPTZ` para no tener problemas con las zonas horarias.

## Usuarios sin columna de rol

Al principio pensamos en poner una columna `rol` en `usuarios`, pero lo descartamos. Una misma persona puede ser paciente y cuidador a la vez: alguien que tome su medicación y además cuida de un familiar. Por eso el rol se saca de las relaciones.

- Es cuidador si aparece en `paciente_cuidador`.
- Es paciente si tiene un perfil en `pacientes`.

El email lo guardamos siempre en minúsculas (hay un `CHECK` que lo obliga) para que no se puedan registrar dos cuentas como `Ana@mail.com` y `ana@mail.com`. La contraseña nunca se guarda, solo el hash de BCrypt que se genera en Spring Security.

## Pacientes que no tienen cuenta

Muchas personas mayores no van a usar la app ellas mismas, sino que la gestiona su cuidador. Por eso en `pacientes` la columna `id_usuario` es opcional, y el paciente tiene su propio nombre y apellidos.

También guardamos las horas a las que suele desayunar, comer y cenar cada paciente, y su zona horaria. Las horas se usan en las pautas y la zona horaria hace falta para pasar esas horas a instantes reales. Por ejemplo, en Canarias es una hora menos que en la península.

## Rol y permisos en la relación con el cuidador

El rol `PRINCIPAL` y `SECUNDARIO` y los permisos (`VER` o `EDITAR`) los pusimos en la tabla `paciente_cuidador` y no en el usuario. El motivo es que dependen de a quién cuidas: poder ser el cuidador principal de un paciente y solo ver los datos de otro.

Además, la propia base de datos controla dos reglas:

- El cuidador principal siempre tiene permiso para editar.
- Cada paciente tiene como mucho un cuidador principal (lo hacemos con un índice único parcial).

```sql
CREATE UNIQUE INDEX ux_paciente_cuidador_principal
    ON paciente_cuidador (id_paciente)
    WHERE rol = 'PRINCIPAL';
```

## Medicamentos

La tabla `medicamentos` es un catálogo común para todos, pensado para importar los datos de CIMA (la base de datos oficial de la AEMPS). Guardamos el número de registro de CIMA, y si un medicamento se añade a mano ese campo se queda vacío.

La concentración y la forma (comprimido, jarabe, etc) son del medicamento, pero la cantidad que toma cada persona va en la pauta.

## Tratamientos

Un tratamiento es un medicamento que toma un paciente concreto, y en la app se ve como una tarjeta.

- La foto personalizada se guarda en un bucket privado de Supabase Storage. En la tabla solo guardamos la ruta del archivo, no una URL pública, porque es un dato personal. Si no hay foto, se usa la del catálogo.
- Se estudia la posibilidad de guardar la imagen personalizada en el dispositio del usuario, ya que el peso de las imagenes pueden ser
considerables
- Si `fecha_fin` está vacía, el tratamiento es continuo.
- No se puede borrar un medicamento del catálogo si alguien lo está tomando (`ON DELETE RESTRICT`).

## Pautas

Las pautas indican cuándo y cuánto se toma. Si un medicamento se toma 3 veces al día, son 3 filas.

Como las personas mayores suelen organizar las pastillas según las comidas y no por horas exactas, cada pauta tiene un `momento`: desayuno, comida, cena o personalizado. Si la hora está vacía, se usa la hora de esa comida del paciente. Así, si alguien empieza a cenar más tarde, solo hay que cambiar su hora de cena y todas sus pautas se ajustan solas. Si el momento es personalizado, la hora es obligatoria.

Otras cosas que tuvimos en cuenta:

- Los días de la semana se guardan en un array (1 = lunes, 7 = domingo). Si está vacío, se toma todos los días.
- La cantidad es decimal para poder poner medio comprimido o un cuarto, siendo algo habitual.

## Tomas

Esta es la decisión más importante del diseño. En la tabla `tomas` solo guardamos las tomas que alguien ha registrado. Se mostrarán como  `TOMADA` u `OMITIDA`. Las pendientes y las olvidadas no se guardan: se calculan comparando las pautas con las tomas que ya hay.

Elegimos hacerlo así porque:

- No hay que crear filas por adelantado para todas las dosis futuras.
- Las alarmas pueden funcionar en el móvil aunque no haya conexión, porque el móvil ya sabe las pautas.
- Si se cambia una pauta, no se quedan filas pendientes que haya que borrar.

De cada toma guardamos cuándo tocaba `programada_para`, cuándo se tomó de verdad `hora_real` y quién la marcó `registrada_por`, que puede ser el paciente o un cuidador.

Para que el móvil pueda sincronizar sin crear duplicados, no puede haber dos tomas de la misma pauta a la misma hora:

```sql
CONSTRAINT uq_tomas_pauta_instante UNIQUE (id_pauta, programada_para)
```

Así el móvil puede enviar las tomas con un upsert (`ON CONFLICT`), y aunque las mande varias veces solo se guarda una.

## Borrar sin perder el historial

Los tratamientos y las pautas no se borran si ya tienen tomas, sino que se desactivan con las columnas `activo` y `activa`. Si borrásemos una pauta, se borrarían también todas sus tomas y perderíamos el historial, eliminando la posibilidad de exportar informes para que lo revisen los profesionales de sanidad.

## Seguridad en Supabase

Supabase publica las tablas a través de su API REST. Para que nadie pueda acceder a los datos con las claves públicas de la app, activamos Row Level Security en todas las tablas sin añadir ninguna política. Así nadie puede leer ni escribir desde fuera, y el backend sigue funcionando normal porque se conecta como propietario de las tablas. Todo el acceso pasa por el backend en Spring, que es donde se comprueban los permisos.
