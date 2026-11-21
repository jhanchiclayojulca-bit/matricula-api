## Pregunta 1: Pruebas automatizadas de matrícula

Se realizaron cinco escenarios de pruebas automatizadas utilizando Cucumber, Gherkin, Serenity BDD, Screenplay y Rest Assured.

### Escenario 1: Cotizar curso

Se verifica la cotización del curso, los créditos, el subtotal, el descuento y el total.

```json
{
  "codigoAlumno": "i202610003",
  "cursos": ["TS5514"],
  "creditos": 4,
  "subtotal": 480,
  "descuento": 0,
  "total": 480
}
```

### Escenario 2: Registrar matrícula

Se verifica que la matrícula se registre correctamente.

```json
{
  "id": 21,
  "codigoAlumno": "i202610003",
  "codigoCurso": "TS5514",
  "periodo": "2026-2",
  "horario": "JUEVES 08:00-12:00",
  "creditos": 4,
  "monto": 480,
  "estado": "REGISTRADA",
  "nota": null,
  "condicion": null,
  "fechaRegistro": "2026-11-21T00:43:08.5145514",
  "fechaPago": null
}
```

### Escenario 3: Pagar matrícula

Se verifica que el pago se procese correctamente y que el estado cambie a PAGADA.

```json
{
  "id": 21,
  "codigoAlumno": "i202610003",
  "codigoCurso": "TS5514",
  "periodo": "2026-2",
  "horario": "JUEVES 08:00-12:00",
  "creditos": 4,
  "monto": 480,
  "estado": "PAGADA",
  "nota": null,
  "condicion": null,
  "fechaRegistro": "2026-11-21T00:43:08.514551",
  "fechaPago": "2026-11-21T00:47:22.9929753"
}
```

### Escenario 4: Verificar pago

Se verifica que el pago esté registrado con el método YAPE y el monto correspondiente.

```json
{
  "id": 16,
  "numeroOperacion": "OP-000016",
  "idMatricula": 21,
  "codigoAlumno": "i202610003",
  "codigoCurso": "TS5514",
  "periodo": "2026-2",
  "metodo": "YAPE",
  "monto": 480,
  "fecha": "2026-11-21T00:47:22.992975"
}
```

### Escenario 5: Matrícula rechazada

Se verifica que el sistema rechace la matrícula cuando el curso pertenece a otra carrera.

```json
{
  "detail": "El curso AD5101 pertenece a la carrera AE",
  "instance": "/api/matriculas",
  "status": 422,
  "title": "Unprocessable Content",
  "codigo": "CURSO_DE_OTRA_CARRERA"
}
```

### Ejecución de pruebas

Para ejecutar los escenarios automatizados:

```powershell
.\mvnw.cmd test -Dtest=CucumberTestSuite
```

**Resultado:** Los cinco escenarios se ejecutan mediante Serenity BDD y Cucumber.

## **_Evidencias_**

Las capturas de los resultados se encuentran en la carpeta evidencias/:


01-ejecucion-todos-los-tests.png


# Pregunta 2: Seguridad de la API con Serenity Screenplay

## 1. Objetivo

Validar la seguridad de la API de matrícula mediante pruebas realizadas en Swagger UI, comprobando la autenticación, rotación de tokens, cierre de sesión, bloqueo de cuentas por intentos fallidos y control de acceso según el rol del usuario.

**Swagger UI:**

`http://localhost:8080/swagger-ui/index.html`

**Usuario de prueba:**

| Campo | Valor |
|---|---|
| Username | `i202610003` |
| Nombre | Carla Quispe |
| Rol | `ALUMNO` |
| Contraseña | `Cibertec2026` |

---

## 2. Escenario 1: Rotación del refresh token

**Objetivo:** comprobar que un refresh token solo pueda utilizarse una vez y que su reutilización cierre las sesiones del usuario.

### Paso 1. Iniciar sesión

**Endpoint:**

`POST /api/auth/login`

**Request:**

```json
{
  "username": "i202610003",
  "password": "Cibertec2026"
}
```

**Resultado:** HTTP `200 OK`.

La API devuelve `accessToken` y `refreshToken`.

### Paso 2. Renovar los tokens

**Endpoint:**

`POST /api/auth/refresh`

Se envía el `refreshToken` obtenido durante el inicio de sesión.

**Resultado:** HTTP `200 OK`.

**Response:**

```json
{
  "accessToken": "<nuevo_accessToken>",
  "refreshToken": "<nuevo_refreshToken>",
  "tipo": "Bearer",
  "expiraEn": 900,
  "username": "i202610003",
  "rol": "ALUMNO"
}
```

### Paso 3. Reutilizar el refresh token anterior

Se vuelve a enviar el refresh token original.

**Resultado:** HTTP `401 Unauthorized`.

**Response:**

```json
{
  "detail": "El refresh token ya fue usado; por seguridad se cerraron todas las sesiones del usuario",
  "instance": "/api/auth/refresh",
  "status": 401,
  "title": "Unauthorized",
  "codigo": "REFRESH_REUTILIZADO"
}
```

### Paso 4. Comprobar la invalidación del access token

**Endpoint:**

`GET /api/auth/perfil`

Se utiliza el access token generado durante la renovación anterior.

**Resultado:** HTTP `401 Unauthorized`.

**Response:**

```json
{
  "detail": "El token es inválido, venció o su sesión fue cerrada",
  "instance": "/api/auth/perfil",
  "status": 401,
  "title": "Unauthorized",
  "codigo": "TOKEN_INVALIDO"
}
```

**Conclusión:** la API detecta la reutilización del refresh token, cierra las sesiones del usuario e invalida también el access token generado durante la renovación.

---

## 3. Escenario 2: Cierre de sesión

**Objetivo:** comprobar que, después de cerrar sesión, el access token utilizado deje de ser válido.

### Paso 1. Iniciar sesión

**Endpoint:**

`POST /api/auth/login`

**Request:**

```json
{
  "username": "i202610003",
  "password": "Cibertec2026"
}
```

**Resultado:** HTTP `200 OK`.

Se obtiene un nuevo access token.

### Paso 2. Cerrar sesión

**Endpoint:**

`POST /api/auth/logout`

Se envía el access token mediante el encabezado Authorization.

```http
Authorization: Bearer <accessToken>
```

**Resultado:** HTTP `204 No Content`.

**Mensaje:** Sesión cerrada.

### Paso 3. Consultar el perfil después del logout

**Endpoint:**

`GET /api/auth/perfil`

Se utiliza el mismo access token con el que se cerró sesión.

**Resultado:** HTTP `401 Unauthorized`.

**Response:**

```json
{
  "detail": "El token es inválido, venció o su sesión fue cerrada",
  "instance": "/api/auth/perfil",
  "status": 401,
  "title": "Unauthorized",
  "codigo": "TOKEN_INVALIDO"
}
```

**Conclusión:** después del cierre de sesión, la API invalida el token e impide acceder al perfil del usuario.

---

## 4. Escenario 3: Bloqueo de cuenta por intentos fallidos

**Objetivo:** comprobar que la cuenta se bloquee después de cinco intentos fallidos y que no permita iniciar sesión incluso utilizando la contraseña correcta durante el periodo de bloqueo.

**Usuario de prueba:**

`i202610004`

### Paso 1. Realizar cinco intentos fallidos

**Endpoint:**

`POST /api/auth/login`

**Request utilizado en los primeros cuatro intentos:**

```json
{
  "username": "i202610004",
  "password": "ClaveIncorrecta123"
}
```

**Resultado de los primeros cuatro intentos:** HTTP `401 Unauthorized`.

**Response:**

```json
{
  "detail": "Usuario o contraseña incorrectos",
  "instance": "/api/auth/login",
  "status": 401,
  "title": "Unauthorized",
  "codigo": "CREDENCIALES_INVALIDAS"
}
```

### Paso 2. Quinto intento fallido

Se vuelve a enviar la misma petición.

**Request:**

```json
{
  "username": "i202610004",
  "password": "ClaveIncorrecta123"
}
```

**Resultado:** HTTP `423 Locked`.

**Response:**

```json
{
  "detail": "Cuenta bloqueada por intentos fallidos hasta las 04:41",
  "instance": "/api/auth/login",
  "status": 423,
  "title": "Locked",
  "codigo": "CUENTA_BLOQUEADA"
}
```

### Paso 3. Intentar ingresar con la contraseña correcta

Se intenta iniciar sesión nuevamente utilizando la contraseña correcta.

**Request:**

```json
{
  "username": "i202610004",
  "password": "Cibertec2026"
}
```

**Resultado:** HTTP `423 Locked`.

**Response:**

```json
{
  "detail": "Cuenta bloqueada por intentos fallidos hasta las 04:41",
  "instance": "/api/auth/login",
  "status": 423,
  "title": "Locked",
  "codigo": "CUENTA_BLOQUEADA"
}
```

### Resumen de resultados

| Intento | HTTP | Código |
|---|---:|---|
| 1 | 401 | `CREDENCIALES_INVALIDAS` |
| 2 | 401 | `CREDENCIALES_INVALIDAS` |
| 3 | 401 | `CREDENCIALES_INVALIDAS` |
| 4 | 401 | `CREDENCIALES_INVALIDAS` |
| 5 | 423 | `CUENTA_BLOQUEADA` |
| Contraseña correcta durante el bloqueo | 423 | `CUENTA_BLOQUEADA` |

**Conclusión:** la API bloquea la cuenta después de cinco intentos fallidos por un periodo de 15 minutos. Durante ese periodo, incluso la contraseña correcta es rechazada.

**Nota:** para repetir este escenario según la consigna, se debe reiniciar la API antes de ejecutarlo nuevamente.

---

## 5. Escenario 4: Alumno sin permiso de administrador

**Objetivo:** comprobar que un usuario con rol ALUMNO no pueda acceder a un recurso exclusivo para ADMIN.

### Paso 1. Iniciar sesión como alumno

**Endpoint:**

`POST /api/auth/login`

**Request:**

```json
{
  "username": "i202610003",
  "password": "Cibertec2026"
}
```

**Resultado:** HTTP `200 OK`.

### Paso 2. Consultar la lista de alumnos

**Endpoint:**

`GET /api/alumnos`

Se envía el access token del usuario mediante Authorization.

```http
Authorization: Bearer <accessToken>
```

**Resultado:** HTTP `403 Forbidden`.

**Response:**

```json
{
  "detail": "Su rol no tiene permiso para este recurso",
  "instance": "/api/alumnos",
  "status": 403,
  "title": "Forbidden",
  "codigo": "ACCESO_DENEGADO"
}
```

**Conclusión:** la API valida los permisos según el rol del usuario y rechaza el acceso cuando un alumno intenta utilizar un recurso exclusivo para administradores.

---

## 6. Resumen general de resultados

| Escenario | Validación | HTTP | Código |
|---|---|---:|---|
| 1 | Renovación inicial del token | 200 | — |
| 1 | Reutilización del refresh token | 401 | `REFRESH_REUTILIZADO` |
| 1 | Access token invalidado | 401 | `TOKEN_INVALIDO` |
| 2 | Cierre de sesión | 204 | — |
| 2 | Consultar perfil después del logout | 401 | `TOKEN_INVALIDO` |
| 3 | Cuatro intentos incorrectos | 401 | `CREDENCIALES_INVALIDAS` |
| 3 | Quinto intento | 423 | `CUENTA_BLOQUEADA` |
| 3 | Contraseña correcta durante el bloqueo | 423 | `CUENTA_BLOQUEADA` |
| 4 | Alumno consulta lista de alumnos | 403 | `ACCESO_DENEGADO` |

**Resultado:** los cuatro escenarios fueron ejecutados manualmente en Swagger UI y sus respuestas coincidieron con las reglas de seguridad verificadas.

---

## 7. Consideraciones para la automatización

La implementación automatizada se realizará utilizando Serenity BDD, Cucumber, Screenplay y Rest Assured.

Se utilizarán los siguientes componentes:

- `seguridad.feature`: definición de los cuatro escenarios en Gherkin español.
- `SeguridadStepDefinitions.java`: implementación de los pasos de Cucumber.
- `IniciarSesion.java`: tarea para autenticar usuarios.
- `RenovarToken.java`: tarea para renovar los tokens.
- `CerrarSesion.java`: tarea para cerrar sesiones.
- `ElCodigoDeError.java`: Question para consultar el código de error.
- `Hooks.java`: inicialización de los actores e impresión de los datos del estudiante y fecha de ejecución.

Los tokens se almacenarán en memoria del Actor utilizando `remember()` y se recuperarán con `recall()`, evitando el uso de variables estáticas.

**Importante:** las respuestas documentadas corresponden a las pruebas manuales en Swagger. La ejecución automatizada de los cuatro escenarios deberá comprobarse posteriormente con Serenity Screenplay.

## **_Evidencias_**

Las capturas de los resultados se encuentran en la carpeta evidencias/:


02-ejecucion-todos-los-tests.png


### Ejecución de pruebas

Para ejecutar los escenarios automatizados:

```powershell
.\mvnw.cmd test -Dtest= SeguridadTestSuite
```


# Pregunta 3: Calificaciones e historial académico con Serenity Screenplay

## 1. Objetivo

Validar el registro de calificaciones de los alumnos mediante pruebas automatizadas con Cucumber, Gherkin, Serenity BDD, Screenplay y Rest Assured.

Se comprueba que un docente pueda registrar notas en matrículas pagadas de los cursos que tiene asignados, que el sistema determine la condición del alumno y que el historial académico actualice correctamente los créditos aprobados y el promedio ponderado.

**Alumno de prueba:**

| Campo | Valor |
|---|---|
| Código | `i202610005` |
| Nombre | Rosa Medina |
| Carrera | CI |

**Docentes de prueba:**

| Código | Nombre | Curso PI5620 |
|---|---|---|
| D0001 | Marco Salazar | Sí |
| D0002 | Lucía Paredes | No |

**Curso evaluado:**

| Campo | Valor |
|---|---|
| Código | `PI5620` |
| Nombre | Proyecto Integrador |
| Créditos | 6 |
| Nota mínima aprobatoria | 13 |

---

## 2. Escenario 1: Docente registra nota e historial se actualiza

**Objetivo:** comprobar que el docente autorizado pueda registrar una calificación y que el historial académico refleje los resultados actualizados.

**Endpoint para registrar la nota:**

`PATCH /api/matriculas/{id}/nota`

**Request para la nota 14:**

```json
{
  "nota": 14
}
```

**Resultado:** HTTP `200 OK`.

La API devuelve la matrícula actualizada con la siguiente información:

```json
{
  "id": 2,
  "codigoAlumno": "i202610005",
  "codigoCurso": "PI5620",
  "periodo": "2026-2",
  "creditos": 6,
  "estado": "PAGADA",
  "nota": 14,
  "condicion": "APROBADO"
}
```

### Consultar historial académico

**Endpoint:**

`GET /api/alumnos/i202610005/historial`

**Resultado esperado:**

| Indicador | Resultado |
|---|---:|
| Créditos aprobados | 13 |
| Promedio ponderado | 14.77 |
| Condición de PI5620 | APROBADO |

**Cálculo del promedio ponderado:**

Se consideran las calificaciones de los tres cursos con nota registrada:

- Análisis y Diseño de Sistemas: 16, con 3 créditos.
- Lenguaje de Programación II: 15, con 4 créditos.
- Proyecto Integrador: 14, con 6 créditos.

```text
Promedio = (16 × 3 + 15 × 4 + 14 × 6) / 13

Promedio = (48 + 60 + 84) / 13

Promedio = 192 / 13

Promedio = 14.77
```

Los 13 créditos corresponden a cursos aprobados.

### Segunda ejecución: nota 12,5

Se evalúa una segunda calificación para comprobar que el sistema identifique una nota desaprobatoria.

**Request:**

```json
{
  "nota": 12.5
}
```

**Resultado esperado según la regla de negocio:** HTTP `200 OK`, con condición `DESAPROBADO`.

| Indicador | Resultado esperado |
|---|---:|
| Créditos aprobados | 7 |
| Promedio ponderado | 14.08 |
| Condición de PI5620 | DESAPROBADO |

**Cálculo del promedio ponderado:**

```text
Promedio = (16 × 3 + 15 × 4 + 12.5 × 6) / 13

Promedio = (48 + 60 + 75) / 13

Promedio = 183 / 13

Promedio = 14.08
```

El promedio ponderado considera los créditos de todos los cursos que tienen calificación registrada, mientras que los créditos aprobados únicamente consideran los cursos con nota aprobatoria.

---

## 3. Escenario 2: Docente que no dicta el curso intenta registrar una nota

**Objetivo:** comprobar que un docente no pueda calificar una matrícula de un curso que no tiene asignado.

El docente `D0002` intenta registrar una nota en el curso `PI5620`, que pertenece al docente `D0001`.

**Endpoint:**

`PATCH /api/matriculas/2/nota`

**Request:**

```json
{
  "nota": 16
}
```

**Resultado:** HTTP `403 Forbidden`.

**Response:**

```json
{
  "detail": "No tiene permisos sobre este recurso",
  "instance": "/api/matriculas/2/nota",
  "status": 403,
  "title": "Forbidden",
  "codigo": "ACCESO_DENEGADO"
}
```

**Conclusión:** la API valida que el docente tenga asignado el curso antes de permitir el registro de una calificación.

---

## 4. Escenario 3: No se puede calificar una matrícula no pagada

**Objetivo:** comprobar que el sistema impida registrar notas en matrículas que todavía no han sido pagadas.

La alumna Rosa Medina tiene una matrícula registrada, pero no pagada, en el curso `PS5511`.

**Endpoint:**

`PATCH /api/matriculas/3/nota`

**Request:**

```json
{
  "nota": 16
}
```

**Resultado:** HTTP `409 Conflict`.

**Response:**

```json
{
  "detail": "Solo se califican matrículas pagadas",
  "instance": "/api/matriculas/3/nota",
  "status": 409,
  "title": "Conflict",
  "codigo": "MATRICULA_NO_PAGADA"
}
```

**Conclusión:** la API impide registrar calificaciones cuando el estado de la matrícula no es `PAGADA`.

---

## 5. Implementación con Serenity Screenplay

Para automatizar los escenarios se implementaron los siguientes componentes:

| Archivo | Responsabilidad |
|---|---|
| `calificaciones.feature` | Define los escenarios en Gherkin español. |
| `CalificacionStepDefinitions.java` | Implementa los pasos de Cucumber. |
| `ObtenerMatricula.java` | Consulta las matrículas del alumno y obtiene dinámicamente el identificador. |
| `CalificarMatricula.java` | Registra la nota mediante PATCH. |
| `ConsultarHistorial.java` | Consulta el historial académico del alumno. |
| `ElHistorial.java` | Question para consultar créditos aprobados, promedio y condición. |
| `CalificacionesTestSuite.java` | Ejecuta la suite de pruebas de calificaciones. |

Los tokens de autenticación se almacenan en la memoria de cada Actor mediante `remember()` y se recuperan con `recall()`.

El identificador de la matrícula no se encuentra fijado en el código: se obtiene dinámicamente consultando las matrículas del alumno y filtrando por curso y estado.

---

## 6. Ejecución de pruebas

Para ejecutar los escenarios automatizados:

```powershell
.\mvnw.cmd clean test -Dtest=CalificacionesTestSuite
```

### Resultados de la ejecución

En la ejecución realizada se obtuvieron los siguientes resultados:

| Escenario | Resultado |
|---|---|
| Registrar nota 14 y actualizar historial | Correcto |
| Registrar nota 12.5 y actualizar historial | Fallido: HTTP 400 |
| Docente sin asignación intenta calificar | Correcto: HTTP 403 |
| Calificar matrícula no pagada | Correcto: HTTP 409 |

**Resultado de Maven:**

```text
Tests run: 4
Failures: 1
Errors: 0
Skipped: 0

BUILD FAILURE
```

### Análisis del caso de la nota 12.5

Durante la ejecución se detectó que el valor `12.5` fue interpretado incorrectamente por la configuración regional española de Cucumber.

La API recibió un valor superior al máximo permitido de 20 y respondió:

```json
{
  "detail": "Datos de entrada inválidos",
  "instance": "/api/matriculas/2/nota",
  "status": 400,
  "title": "Bad Request",
  "codigo": "DATOS_INVALIDOS",
  "errores": {
    "nota": "debe ser menor que o igual a 20.0"
  }
}
```

Por ello, se debe utilizar la notación decimal española `12,5` en la tabla de ejemplos de Gherkin para que Cucumber interprete correctamente el valor decimal.

La ejecución con `12.5` se conserva como evidencia del problema detectado. La corrección debe verificarse mediante una nueva ejecución.

---

## 7. Evidencias

Las capturas de los resultados se encuentran en la carpeta `evidencias/`:

- `03-ejecucion-calificaciones.png`: ejecución de los escenarios de calificaciones.
- `03-error-nota-decimal.png`: evidencia de la respuesta HTTP 400 al enviar la nota `12.5`.
- `03-reporte-serenity.png`: reporte generado por Serenity BDD.

El reporte HTML se genera en:

`target/site/serenity/index.html`

Para abrirlo:

```powershell
start .\target\site\serenity\index.html
```

**Conclusión:** se implementaron las pruebas automatizadas para validar el registro de calificaciones, los permisos de los docentes, la restricción de matrículas no pagadas y la actualización del historial académico. La ejecución permitió identificar un problema de interpretación decimal que debe corregirse y verificarse nuevamente.