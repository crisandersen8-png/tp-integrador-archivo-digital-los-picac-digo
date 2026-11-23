# 📱 Guía Rápida para Demostración del Bot de Telegram

Esta guía contiene los pasos exactos para levantar el sistema desde cero y mostrarle el funcionamiento del bot al equipo o a los docentes en menos de 3 minutos.

---

## Paso 1: Levantar el Servidor con el Bot Habilitado

Abrí una terminal en la carpeta del proyecto (`tp-integrador-archivo-digital-los-picac-digo`).

### Método Rápido (Recomendado):
El proyecto ya cuenta con el archivo `.env` configurado localmente. Solo ejecutá:

* **En Git Bash:**
  ```bash
  ./run-bot.sh
  ```
* **En PowerShell:**
  ```powershell
  .\run-bot.ps1
  ```

*(El script lee automáticamente el `.env` y levanta Spring Boot sin que tengas que tipear ninguna credencial).*

---

### Método Manual Alternativo:
Si preferís exportar manualmente las variables en tu terminal:

#### En Git Bash:
```bash
export TELEGRAM_BOT_ENABLED="true"
export TELEGRAM_BOT_TOKEN="9249490649:AAE_YdJztUIiEbcB3jKsFeeK3S0kAMPSNNc"
export TELEGRAM_BOT_USERNAME="archivo_digital_demo_bot"

./mvnw spring-boot:run
```

#### En PowerShell:
```powershell
$env:TELEGRAM_BOT_ENABLED="true"
$env:TELEGRAM_BOT_TOKEN="9249490649:AAE_YdJztUIiEbcB3jKsFeeK3S0kAMPSNNc"
$env:TELEGRAM_BOT_USERNAME="archivo_digital_demo_bot"

.\mvnw.cmd spring-boot:run
```

> **¿Cómo sé que ya arrancó?**  
> Esperá a que en la consola aparezca el mensaje:  
> `Started ArchivoDigitalApplication in ... seconds`

---

## Paso 2: Cargar los Datos de Prueba (Script de 1 paso)

Como la base de datos H2 en memoria inicia limpia, abrí una **segunda terminal de Git Bash** y pegá este bloque completo. Cargará 6 piezas, 1 digitalización y el tag `HISTORICO`:

```bash
# 1. Cargar 6 piezas fisicas (para probar busqueda y paginacion)
curl -s -X POST "http://localhost:8080/api/v1/piezas-fisicas" -H "Content-Type: application/json" -d '{
  "codigoInventario": "ACTA-001", "nombre": "Acta Fundacional de San Martin",
  "descripcion": "Documento historico original manuscrito", "fechaIngreso": "2026-03-15",
  "tipoMaterial": "Papel", "estado": "EN_DEPOSITO"
}' > /dev/null

for i in {2..6}; do
  curl -s -X POST "http://localhost:8080/api/v1/piezas-fisicas" -H "Content-Type: application/json" -d "{
    \"codigoInventario\": \"ACTA-00$i\", \"nombre\": \"Acta Documental Nro $i\",
    \"descripcion\": \"Registro historico administrativo\", \"fechaIngreso\": \"2026-03-1$i\",
    \"tipoMaterial\": \"Papel\", \"estado\": \"EN_DEPOSITO\"
  }" > /dev/null
done

# 2. Cargar el Tag HISTORICO
curl -s -X POST "http://localhost:8080/api/v1/tags" -H "Content-Type: application/json" \
  -d '{"nombre": "HISTORICO"}' > /dev/null

# 3. Asociar digitalizacion a la pieza 1 y vincularle el tag
curl -s -X POST "http://localhost:8080/api/v1/piezas-fisicas/1/digitalizaciones" -H "Content-Type: application/json" \
  -d '{"urlRecurso": "https://archivo.unsam.edu.ar/digital/acta1.pdf", "descripcion": "Escaneo HD"}' > /dev/null

curl -s -X POST "http://localhost:8080/api/v1/digitalizaciones/1/tags" -H "Content-Type: application/json" \
  -d '{"tagId": 1}' > /dev/null

echo "✅ ¡Datos de prueba cargados con exito!"
```

---

## Paso 3: Guión de Demostración en Telegram

Abrí Telegram, entrá al bot (**`@archivo_digital_demo_bot`**) y enviá los siguientes comandos en orden:

### 1. Comandos Informativos
* `/start` ➡️ Muestra la bienvenida institucional del Archivo Digital.
* `/ayuda` ➡️ Muestra la guía de comandos y sintaxis de filtros.

### 2. Búsqueda y Paginación (5 resultados por página)
* `/buscar acta` ➡️ Muestra las primeras 5 piezas y el enlace: `➡️ Pagina siguiente: /buscar acta p:2`.
* `/buscar acta p:2` ➡️ Muestra la 6ta pieza y el enlace para retroceder: `⬅️ Pagina anterior: /buscar acta p:1`.

### 3. Búsqueda por Etiquetas (Tags) y Filtros Combinados
* `/buscar tag:HISTORICO` ➡️ Encuentra la pieza `ACTA-001` y muestra `Digitalizaciones: 1`.
* `/buscar fundacional tag:HISTORICO` ➡️ Combina término y tag, encontrando el Acta Fundacional.

### 4. Validaciones y Manejo de Errores (Robustez)
* `/buscar` *(sin texto)* ➡️ Advierte que se debe ingresar término o etiqueta.
* `/buscar acta p:0` ➡️ Advierte que la página debe ser mayor o igual a 1.
* `/buscar acta p:10` ➡️ Advierte que la página solicitada no existe (informa cuántas hay).
* `/comando_invalido` ➡️ Avisa que el comando no existe y sugiere escribir `/ayuda`.
