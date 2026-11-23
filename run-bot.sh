#!/bin/bash
# -----------------------------------------------------------------------------
# Script de inicio rapido para ejecutar la aplicacion con el Bot de Telegram
# -----------------------------------------------------------------------------

if [ -f .env ]; then
  echo ">>> Cargando credenciales locales desde .env..."
  source .env
else
  echo ">>> AVISO: No se encontro el archivo .env. Puedes crearlo copiando .env.example"
fi

echo ">>> Iniciando Archivo Digital..."
./mvnw spring-boot:run
