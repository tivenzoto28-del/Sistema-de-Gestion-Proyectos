# Evidencias de pruebas — Sistema web Gestión de Investigación

Fecha de ejecución: 24 de noviembre de 2026  
Entorno: Spring Boot 4.2.0 / Java 21 / Tomcat 11 / MariaDB 11.8.6  
Base de datos: `gestion_investigacion` en `localhost:3306`  
Aplicación: `http://localhost:8080`  
Ubicación del código: `Investigacion/`

## 1. Preparación del entorno

Se ejecutó el script `el script de seguridad` con el usuario del sistema operativo `sym`, que posee `ALL PRIVILEGES ON *.*`:

```bash
mysql -usym < el script de seguridad
```

Usuarios verificados en MariaDB:

| Usuario | Contraseña | Rol en la aplicación |
|---|---|---|
| `admin_sistema` | `Admin123!` | ADMIN |
| `secretaria` | `secre1` | SECRETARIA |
| `Secretaria2` | `secre2` | SECRETARIA (mismos permisos que `secretaria`) |
| `coordinador` | `Coord123!` | COORDINADOR |
| `director_inv` | `Direct123!` | DIRECTOR |
| `profesor` | `Profe123!` | PROFESOR |
| `estudiante` | `Estudi123!` | ESTUDIANTE |

Comando de arranque:

```bash
cd Investigacion
./gradlew bootRun
```

Resultado: `Started InvestigacionApplication in 2.502 seconds` y Tomcat en el puerto 8080.

## 2. Pruebas de salud y autenticación

### 2.1 Health check

```bash
curl -sS -w '\nHTTP %{http_code}\n' http://localhost:8080/api/auth/health
```

Respuesta:

```json
{"aplicacion":"Gestion de Investigacion","estado":"OK"}
HTTP 200
```

### 2.2 Inicio de sesión correcto

```bash
curl -sS -c c_admin.txt -H 'Content-Type: application/json' \
  -d '{"usuario":"admin_sistema","password":"Admin123!"}' \
  http://localhost:8080/api/auth/login
```

Respuesta:

```json
{"usuario":"admin_sistema","rol":"ADMIN","rolEtiqueta":"Administrador del sistema","mensaje":"Sesion iniciada correctamente."}
```

Los seis usuarios iniciaron sesión correctamente y `/api/auth/me` devolvió el rol esperado en cada caso.

### 2.3 Credenciales inválidas

```bash
curl -sS -w '\nHTTP %{http_code}\n' -H 'Content-Type: application/json' \
  -d '{"usuario":"admin_sistema","password":"malo"}' \
  http://localhost:8080/api/auth/login
```

Respuesta:

```json
{"timestamp":"2026-11-24T20:03:55.876761455-05:00","status":401,"mensaje":"Credenciales invalidas. Verifique su usuario y contrasena."}
HTTP 401
```

## 3. Pruebas de control de acceso por roles

Se conservó la cookie de sesión de cada usuario y se solicitó un endpoint permitido y uno restringido.

| Usuario | Endpoint | Resultado |
|---|---|---|
| secretaria | `/api/proyectos` | **403** — "El rol Secretaria no tiene permiso..." |
| secretaria | `/api/reportes/cm1` | **200** — devuelve equipo activo |
| secretaria | `/api/reportes/cm3` | **200** — devuelve estudiantes por proyecto |
| secretaria | `/api/reportes/ca1` | **403** |
| coordinador | `/api/proyectos` | **200** |
| coordinador | `/api/productos` | **200** |
| coordinador | `/api/reportes/ca1`, `ca2`, `ca4`, `ca5`, `cm2`, `cm4` | **200** |
| coordinador | `/api/reportes/ca3`, `cm3`, `cm5` | **403** |
| coordinador | `/api/auditoria/operaciones` | **403** |
| director_inv | `/api/reportes/ca1`, `ca4` | **200** |
| director_inv | `/api/reportes/cm2`, `cm4` | **403** |
| profesor | `/api/reportes/ca3`, `cm1`, `vista-docente` | **200** |
| profesor | `/api/proyectos`, `ca1` | **403** |
| estudiante | `/api/reportes/cm5`, `vista-estudiante` | **200** |
| estudiante | `/api/proyectos`, `cm1` | **403** |

Ejemplo de respuesta denegada:

```json
{"timestamp":"2026-11-24T19:41:37.933296815-05:00","status":403,"mensaje":"El rol Secretaria no tiene permiso para ejecutar esta operacion."}
```

## 4. Pruebas de reportes almacenados

Todos los procedimientos de consulta respondieron HTTP 200 con la sesión de `admin_sistema`:

| Procedimiento / endpoint | Resultado | Fragmento de salida |
|---|---|---|
| `sp_ca1` → `/api/reportes/ca1` | 200 | `[{"proyecto":"CIBERGAD","tipo_producto":"Congreso","cantidad":1}, ...]` |
| `sp_ca2` → `/api/reportes/ca2` | 200 | `[{"acronimo":"SATCLIMA","presupuesto":32000.00,"promedio_ueb":17159.09,"diferencia":14840.91}, ...]` |
| `sp_ca3` → `/api/reportes/ca3` | 200 | `[{"acronimo":"CIBERGAD","num_secuencia":2,"titulo":"Buenas prácticas...","tipo":"Congreso"}, ...]` |
| `sp_ca4` → `/api/reportes/ca4` | 200 | `[{"acronimo":"SATCLIMA","articulos":2,"alto_impacto":1,"regionales":1,"puntaje_total":1.20,"evaluacion":"Cumple 1 punto"}, ...]` |
| `sp_ca5` → `/api/reportes/ca5` | 200 | `[{"acronimo":"DATABOL","inicio":"05/01/2026","fin_vigente":"30/11/2026","prorroga":"No","meses_ejecucion":10,"dias_restantes":6,"estado":"Por finalizar"}, ...]` |
| `sp_cm1` → `/api/reportes/cm1` (sin filtro) | 200 | `[{"acronimo":"DATABOL","cargo":"Director","nombre":"María Fernanda Gavilanes Silva", ...}]` |
| `sp_cm2` → `/api/reportes/cm2` | 200 | `[{"nombre":"Ana Lucía Arellano Torres","proyectos_activos":2,"disponibilidad":"Límite alcanzado"}, ...]` |
| `sp_cm3` → `/api/reportes/cm3` | 200 | `[{"acronimo":"SATCLIMA","estudiantes":3,"nombres":"Bryan Santiago Ruiz Vargas, ...", ...}]` |
| `sp_cm4` → `/api/reportes/cm4` | 200 | `[{"nombre_linea":"Inteligencia artificial y ciencia de datos","profesores":3,"proyectos":2, ...}]` |
| `sp_cm5` → `/api/reportes/cm5` | 200 | `[{"acronimo":"CIBERGAD","sec":1,"titulo":"Diagnóstico de seguridad...","tipo":"Revista", ...}]` |

Nota: la consulta `cm1` sin parámetro confirmó el correcto manejo de `NULL` en `Sql.asignar` mediante `PreparedStatement.setNull`.

## 5. Pruebas transaccionales de extremo a extremo

Se creó el proyecto de prueba `TESTUI` (ID 12), se ejecutaron todas las operaciones y al final se eliminó para no alterar los datos de demostración.

### 5.1 Registro de proyecto — `sp_registrar_proyecto_transaccional`

```bash
curl -sS -b c_secretaria.txt -H 'Content-Type: application/json' \
  -d '{"nombreProyecto":"Proyecto de prueba integral","acronimo":"TESTUI","presupuesto":12000,"programa":"Programa de pruebas","descripcion":"Proyecto creado por la interfaz para validar las transacciones","inicioPro":"2026-12-01","finPro":"2027-06-30","resolucion":"RCU-TEST-UI"}' \
  http://localhost:8080/api/proyectos
```

Respuesta: `{"mensaje":"Proyecto registrado correctamente mediante transaccion."}`

Verificación en BD:

```text
12  TESTUI  2027-06-30  NULL  NULL
```

El dashboard inicial, antes de la prueba, devolvió `{"proyectos":11,"proyectosActivos":8,"profesores":33,"estudiantes":7,"productos":18,"lineas":7}`.

### 5.2 Prórroga — `sp_control_prorroga_transaccional`

```bash
curl -sS -b c_secretaria.txt -H 'Content-Type: application/json' \
  -d '{"nuevaFechaFin":"2027-09-15"}' \
  http://localhost:8080/api/proyectos/12/prorroga
```

Respuesta: `{"mensaje":"Prorroga registrada correctamente."}`  
Verificación: `prorroga = 2027-09-15` (3 meses después de `fin_pro = 2027-06-30`, dentro del límite permitido).

### 5.3 Registro de producto con Factory Method — `sp_registrar_producto_transaccional`

```bash
curl -sS -b c_secretaria.txt -H 'Content-Type: application/json' \
  -d '{"idProyecto":12,"tipo":1,"titulo":"Articulo de prueba integral","nombreRevista":"Revista de Pruebas","editorial":"Editorial UEB","volumen":"Vol. 1","quartil":"Q1","fechaPublicacion":"2027-03-15"}' \
  http://localhost:8080/api/productos
```

Respuesta: `{"mensaje":"Producto de tipo Revista registrado correctamente mediante Factory Method y transaccion."}`  
Verificación:

```text
id_producto=19, id_proyecto=12, num_secuencia=1, titulo="Articulo de prueba integral", id_tipo=1
publicacion_revista: nombre_revista="Revista de Pruebas", quartil="Q1"
```

### 5.4 Asignación de equipo y estudiante

Se asignaron los docentes 29 (Director), 30 (Director subrogante) y 31 (Investigador), más un estudiante:

```text
29  Director               RCU-ING-29  NULL
30  Director subrogante    RCU-ING-30  NULL
31  Investigador           RCU-ING-31  NULL
12  Estudiante Prueba UI
```

Respuestas: `Profesor asignado. El trigger valido el cupo y calculo el tamano del equipo.` y `Estudiante registrado correctamente.`

### 5.5 Cambio de director — `sp_cambio_director_transaccional`

```bash
curl -sS -b c_admin.txt -H 'Content-Type: application/json' \
  -d '{"idDocenteSaliente":29,"idDocenteEntrante":32,"resolucionSalida":"RCU-SAL-29","motivoSalida":"Cambio de direccion para prueba","resolucionIngreso":"RCU-ING-32"}' \
  http://localhost:8080/api/proyectos/12/cambio-director
```

Respuesta: `{"mensaje":"Cambio de director ejecutado correctamente."}`  
Verificación: docente 29 con `resolucion_salida = RCU-SAL-29` y docente 32 incorporado como Director.

### 5.6 Relevo de dirección — `sp_control_responsables_transaccional`

```bash
curl -sS -b c_admin.txt -H 'Content-Type: application/json' \
  -d '{"idDirectorSaliente":32,"idNuevoSubrogante":33,"resolucionSalida":"RCU-SAL-32","motivoSalida":"Relevo por prueba de interfaz","resolucionIngreso":"RCU-ING-33"}' \
  http://localhost:8080/api/proyectos/12/relevo
```

Respuesta: `{"mensaje":"Relevo de direccion ejecutado correctamente."}`  
Verificación: docente 32 con salida registrada y docente 33 como Director subrogante.

### 5.7 Cierre y desvinculación — trigger de auditoría + `sp_desvincular_cierre_transaccional`

```bash
curl -sS -b c_admin.txt -H 'Content-Type: application/json' \
  -d '{"resolucionCierre":"RCU-CIERRE-UI","desvincular":true}' \
  http://localhost:8080/api/proyectos/12/cerrar
```

Respuesta: `{"mensaje":"Proyecto cerrado. El trigger de auditoria registro el cambio."}`

Verificación:

```text
acronimo=TESTUI, resolucion_consejo_uni_cierre=RCU-CIERRE-UI
Docentes 30, 31 y 33 con resolucion_salida=RCU-CIERRE-UI
Estudiantes vinculados: 0
```

Registro de `auditoria_proyecto` generado por el trigger:

```json
{"id_auditoria":9,"id_proyecto":12,"operacion":"UPDATE","datos_anteriores":"acronimo=TESTUI; presupuesto=12000.00; fin_pro=2027-06-30; prorroga=2027-09-15; cierre=-","datos_nuevos":"acronimo=TESTUI; presupuesto=12000.00; fin_pro=2027-06-30; prorroga=2027-09-15; cierre=RCU-CIERRE-UI","usuario":"root@localhost","fecha_hora":"2026-11-24T20:03:10"}
```

### 5.8 Reclasificación de producto — `sp_reclasificar_producto_transaccional`

```bash
curl -sS -b c_admin.txt -H 'Content-Type: application/json' \
  -d '{"tipo":2,"titulo":"Articulo reclasificado a congreso","nombreCongreso":"Congreso Prueba UI","tipoCongreso":"Internacional","lugarCelebracion":"Quito","pais":"Ecuador","tipoParticipacion":"Ponente","fechaInicio":"2027-04-10","fechaFin":"2027-04-12"}' \
  http://localhost:8080/api/productos/19/reclasificar
```

Respuesta: `{"mensaje":"Producto reclasificado a Congreso correctamente."}`  
Verificación: `id_tipo=2` y fila en `publicacion_congreso` con `nombre_congreso="Congreso Prueba UI"`.

### 5.9 Actualización y eliminación con auditoría

```text
PUT  /api/proyectos/12 → {"mensaje":"Proyecto actualizado. El trigger de auditoria registro el cambio."}
DELETE /api/proyectos/12 → {"mensaje":"Proyecto eliminado. El trigger de auditoria registro la operacion."}
```

Verificación final: `proyecto_investigacion` y `productos` sin el registro de prueba (0 filas).

## 6. Pruebas de validación y rollback

### 6.1 Validación Bean Validation

```bash
curl -sS -w '\nHTTP %{http_code}\n' -b c_secretaria.txt -H 'Content-Type: application/json' \
  -d '{"nombreProyecto":"Invalido","acronimo":"INV1","presupuesto":-1,"programa":"P","inicioPro":"2026-12-01","finPro":"2027-06-30","resolucion":"R"}' \
  http://localhost:8080/api/proyectos
```

Respuesta: HTTP 400 — `{"mensaje":"El presupuesto no puede ser negativo."}`

### 6.2 Rollback de transacción ante error SQLSTATE 45000

```bash
curl -sS -w '\nHTTP %{http_code}\n' -b c_secretaria.txt -H 'Content-Type: application/json' \
  -d '{"nombreProyecto":"Invalido fechas","acronimo":"INV2","presupuesto":100,"programa":"P","inicioPro":"2027-12-01","finPro":"2026-06-30","resolucion":"R"}' \
  http://localhost:8080/api/proyectos
```

Respuesta: HTTP 400 — `{"mensaje":"Error critico: no se pudo registrar el proyecto. Transaccion revertida."}`  
Verificación: `SELECT COUNT(*) ... WHERE acronimo IN ('INV1','INV2')` → `0`.  
El `EXIT HANDLER` del procedimiento insertó en `auditoria_operaciones`:

```json
{"id_auditoria":9,"fecha":"2026-11-24T20:03:32","operacion":"INSERT","procedimiento_ejecutado":"sp_registrar_proyecto_transaccional","resultado":"FALLIDO - Rollback ejecutado por error SQL"}
```

## 7. Pruebas del frontend

```text
GET /            → 200, 3303 bytes  (index.html)
GET /app.js      → 200, 58623 bytes (controladores y formularios)
GET /styles.css  → 200, 16716 bytes (diseño y menú por rol)
```

`node --check app.js` → sin errores de sintaxis.

## 8. Compilación

```bash
./gradlew build
```

Resultado:

```text
> Task :compileJava UP-TO-DATE
> Task :bootJar
> Task :test
> Task :build
BUILD SUCCESSFUL in 17s
```

## 9. Conclusión de las pruebas

Se verificaron los 6 roles, los 10 procedimientos de reportes, las 7 operaciones transaccionales, los disparadores de auditoría, la validación de entrada, el rollback ante errores, la correcta gestión de `NULL` y el servicio de archivos estáticos. No se presentaron errores de ejecución después de retirar la propiedad `spring.jackson.serialization.write-dates-as-timestamps`, incompatible con Jackson 3 de Spring Boot 4.

## 10. Mejora final: vinculación de cuentas a una persona y pantalla de acceso

Se agregó la tabla `usuario_persona` en `el script de seguridad`, que vincula cada cuenta con una persona concreta:

```text
profesor   -> PROFESOR   -> id_profesor=2  -> María Fernanda Gavilanes Silva
estudiante -> ESTUDIANTE -> id_estudiante=1 -> Kevin Alexander Chela Guamán
```

`AuthService` consulta esa tabla con la conexión de catálogos y guarda `idProfesor`, `idEstudiante` y `nombrePersona` en `SessionUser`, que se devuelve en `/api/auth/login` y `/api/auth/me`.

### 10.1 Docente aislado

```bash
curl -sS -b c_prof.txt http://localhost:8080/api/reportes/vista-docente
```

Solo devuelve los proyectos activos del docente 2: `DATABOL` y `SATCLIMA`.  
`/api/reportes/ca3` solo devuelve productos de esos dos proyectos.  
`/api/reportes/cm1?idProyecto=1` ignora el parámetro ajeno y devuelve únicamente el equipo de `DATABOL` y `SATCLIMA`.  
`/api/reportes/docente/13/proyectos` también ignora el id ajeno y devuelve solo los proyectos del docente 2.  
`/api/reportes/ca1` responde **403** para el rol Profesor.

### 10.2 Estudiante aislado

```bash
curl -sS -b c_est.txt http://localhost:8080/api/reportes/vista-estudiante
```

Solo devuelve `PLATINV`, el proyecto del estudiante 1.  
`/api/reportes/cm5` solo devuelve los 3 productos de `PLATINV`.  
`/api/reportes/estudiante/3/proyectos` ignora el id ajeno y devuelve solo `PLATINV`.  
`/api/reportes/ca1` y `/api/proyectos` responden **403** para el rol Estudiante.

### 10.3 Pantalla inicial

`GET /` sirve únicamente el formulario de inicio de sesión: no incluye panel de demostración, listado de usuarios de prueba ni descripciones de arquitectura. Después de autenticarse se muestra un panel limpio con menú por rol; los docentes y estudiantes solo ven "Mi espacio" con sus propios proyectos y productos.

```text
GET / -> 200, sin "demo-user", sin "feature-list", sin "login-brand"
./gradlew build -> BUILD SUCCESSFUL
node --check app.js -> sin errores
```

## 11. Creación delegada de usuarios y simulación final

Se agregó `sp_crear_usuario_app`, ejecutado con `SQL SECURITY DEFINER`. Valida el rol del creador, el formato del usuario (solo letras, números y `_`), contraseña mínima de 6 caracteres, existencia de la persona y duplicados. Construye `CREATE USER` y `GRANT` con `CONCAT` + `QUOTE` y sentencias preparadas, sin interpolación insegura. Toda creación se registra en `auditoria_operaciones`.

Permisos implementados:

- ADMIN: crea `SECRETARIA` y `COORDINADOR`.
- SECRETARIA: crea `PROFESOR` y `ESTUDIANTE` vinculados a un `id_profesor` o `id_estudiante` real.
- COORDINADOR y roles académicos: sin acceso a `/api/usuarios` (403).

### 11.1 Simulación ejecutada

| Paso | Acción | Resultado |
|---|---|---|
| 1 | Admin crea `sec_nueva` (SECRETARIA) | 200 |
| 2 | Admin crea `coord_nuevo` (COORDINADOR) | 200 |
| 3 | `sec_nueva` inicia sesión | 200, rol SECRETARIA |
| 4 | `sec_nueva` crea `prof_nuevo` ligado al profesor 13 | 200, nombre "Fausto Rodrigo Escobar Núñez" |
| 5 | `sec_nueva` crea `est_nuevo` ligado al estudiante 6 | 200, nombre "Erika Fernanda Gaibor Arias" |
| 6 | `sec_nueva` intenta crear COORDINADOR | 403 |
| 7 | `coord_nuevo` consulta `/api/usuarios` | 403 |
| 8 | `coord_nuevo` consulta `/api/reportes/ca1` | 200 |
| 9 | `prof_nuevo` consulta sus proyectos | DATABOL y PLATINV solamente |
| 10 | `est_nuevo` consulta sus proyectos | IOTAGRO solamente |
| 11 | Contraseña corta, persona inexistente, duplicado y usuario con guion | 400 en los cuatro casos |
| 12 | Admin lista usuarios / secretaria lista usuarios | Admin ve 10 / secretaria ve solo 4 prof. y est. |

### 11.2 Ciclo operativo completo con el nuevo usuario

Con `sec_nueva` se creó el proyecto `SIMTOT`, se registró prórroga y producto; con `admin_sistema` se asignó docente, estudiante, cierre con desvinculación y eliminación. Quedaron 0 filas residuales y la auditoría registró los cinco procedimientos:

```text
sp_registrar_proyecto_transaccional | EXITOSO - Proyecto creado: SIMTOT
sp_control_prorroga_transaccional   | EXITOSO - Prorroga registrada en proyecto ID: 13
sp_registrar_producto_transaccional | EXITOSO - Producto registrado para proyecto ID: 13
sp_desvincular_cierre_transaccional | EXITOSO - Desvinculacion por cierre del proyecto ID: 13
```

### 11.3 Ajuste de manejo de errores

`DatabaseService.conCatalogo` ahora distingue fallo de conexión (HTTP 500) de error SQL devuelto por un procedimiento (HTTP 400 con el mensaje de negocio). Por eso las validaciones del SP se muestran correctamente en el frontend.

## 12. Ampliación de permisos y limpieza de la interfaz

Se revisó la matriz de procedimientos por rol y se amplió de forma coherente:

| Rol | Procedimientos disponibles |
|---|---|
| ADMIN | Todos, además de `sp_crear_usuario_app`. |
| SECRETARIA | Registro de proyecto/producto/prórroga, CM1, CM3, CA3, CM5 y `sp_crear_usuario_app` para cuentas académicas. |
| COORDINADOR | CA1–CA5 y CM1–CM5 (visión analítica completa). |
| DIRECTOR | CA1–CA5 y CM1–CM5 (visión directiva completa). |
| PROFESOR | CA3 y CM1, filtrados a sus proyectos por la fachada. |
| ESTUDIANTE | CM5, filtrado a los productos de sus proyectos. |

La simulación confirmó que un coordinador recién creado responde 200 en los diez reportes, que una secretaría recién creada responde 200 en CA3/CM5 y puede crear docentes/estudiantes, y que docente/estudiante/coordinador reciben 403 en `/api/usuarios`. Los docentes y estudiantes siguen viendo únicamente `DATABOL`/`PLATINV` e `IOTAGRO` respectivamente.

Se eliminaron las referencias al archivo de seguridad de los mensajes de la API y de la interfaz. El texto visible habla solo de "credenciales", "roles" y "procedimientos autorizados", sin mencionar scripts técnicos.

## 13. Revisión final y comentarios por bloques

Se eliminó código sin uso (`Role.desdeUsuario` y `mostrarResultado`) y cualquier referencia al archivo de seguridad en mensajes de la API, configuración e interfaz.

Todas las clases Java llevan un bloque `/* BLOQUE: ... */` con su responsabilidad, y los archivos más grandes tienen bloques adicionales antes de cada grupo de métodos (autenticación, conexiones, fachada, factory, interceptor, reportes y espacios personales). El JavaScript incluye bloques para estado, navegación, autenticación, operaciones, productos, reportes, usuarios y utilidades; el CSS y el script SQL también están segmentados por bloques.

Verificación final: `./gradlew build` BUILD SUCCESSFUL, `node --check app.js` sin errores, health y login 200, reportes 200, creación de usuario 200, inicio 200. Las cuentas de prueba de la simulación se eliminan al ejecutar el script de seguridad, quedando solo las cuentas base.

## 14. Guía práctica de capturas

El archivo `docs/guias-capturas.txt` contiene el paso a paso para tomar las 15 imágenes sugeridas: usuario con el que entrar, menú que debe abrirse, datos de ejemplo y contenido que debe aparecer en cada captura.

## 15. Corrección del campo de fecha en productos de tipo Revista

Se detectó que el formulario de producto no mostraba fecha para el tipo Revista, aunque la guía de capturas la solicitaba. Se agregó `fecha_publicacion` a `publicacion_revista`, se actualizaron los procedimientos `sp_registrar_producto_transaccional` y `sp_reclasificar_producto_transaccional`, y `PublicacionRevista` ahora envía y exige esa fecha.

Prueba realizada: registro del producto «Revista con fecha» con fecha `2027-03-15`; verificación en base de datos:

```text
id_producto=22, titulo="Revista con fecha", fecha_publicacion=2027-03-15
```

El proyecto temporal usado en la prueba se eliminó al finalizar.

## 16. Corrección de responsabilidades: secretaría operativa

Se ajustó la lógica de roles: el administrador conserva el control total y las tareas de mantenimiento, pero la secretaría es la responsable de atender a docentes y estudiantes y de ejecutar todo el ciclo operativo de proyectos.

La secretaría puede ahora: listar y ver detalle de proyectos, registrar proyecto, prórroga y producto, actualizar y eliminar proyecto, asignar profesores, registrar estudiantes, cambio de director, relevo, cierre con desvinculación, crear cuentas académicas y consultar los reportes autorizados. El administrador mantiene acceso para control y mantenimiento, y la auditoría sigue siendo exclusiva de él.

Se otorgaron en MariaDB los permisos reales: SELECT/INSERT/UPDATE/DELETE sobre las tablas del ciclo de proyectos y EXECUTE sobre los procedimientos de cambio de director, relevo y desvinculación. El procedimiento de creación de usuarios concede los mismos permisos a las secretarías nuevas.

Simulación realizada con `secretaria/secre1`: alta, consulta, actualización, asignación de docente, registro de estudiante, cambio de director, relevo, cierre con desvinculación y eliminación de un proyecto temporal. Todas las operaciones respondieron 200; la regla de negocio del relevo se verificó al exigir un director subrogante activo.

## 17. Líneas de investigación: alta y asignación

Se agregaron tres operaciones para admin y secretaría: registrar una línea nueva, asignarla a un proyecto y asignarla a un docente. Los endpoints son `POST /api/lineas`, `POST /api/proyectos/{id}/lineas` y `POST /api/docentes/{id}/lineas`; la consulta por docente está en `GET /api/docentes/{id}/lineas`.

Simulación con `secretaria/secre1`:

```text
POST /api/lineas -> "Linea de investigacion registrada correctamente." (id_linea=8)
POST /api/proyectos/21/lineas -> "Linea asignada al proyecto correctamente."
POST /api/docentes/29/lineas  -> "Linea asignada al docente correctamente."
proyecto_linea: 1 fila | linea_profesor: 1 fila
GET /api/proyectos/21 -> lineas: [{"id_linea":8,"nombre_linea":"Linea de prueba integral"}]
```

Un segundo intento de asignación devolvió 400 con "La linea ya estaba asignada a este docente." El administrador también ejecutó el alta y la asignación con 200. El proyecto y las líneas temporales se eliminaron al finalizar.
