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
.\mvnw.cmd test -Dtest=SeguridadTestSuite