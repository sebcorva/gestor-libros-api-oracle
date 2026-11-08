#!/usr/bin/env bash

# Permite pasar un nombre personalizado como argumento, ej: ./script-zip.sh MiNombre_Apellido.zip
NOMBRE_ZIP="${1:-gestor-libros-ms-entrega.zip}"

echo "=========================================="
echo "📦 Generando archivo ZIP para entrega"
echo "Archivo destino: $NOMBRE_ZIP"
echo "=========================================="

# Si ya existe un zip con ese nombre, lo eliminamos
if [ -f "$NOMBRE_ZIP" ]; then
  rm -f "$NOMBRE_ZIP"
fi

# Limpiamos los archivos compilados de Maven (target/) para reducir peso.
# Ojo: no ejecutar con la aplicacion corriendo (rompe el reinicio de DevTools).
if [ -f "./mvnw" ]; then
  echo "🧹 Limpiando binarios con ./mvnw clean..."
  ./mvnw clean -q
fi

# Creamos el ZIP excluyendo carpetas pesadas e innecesarias
echo "🗜️  Comprimiendo proyecto..."
zip -r "$NOMBRE_ZIP" . \
  -x "target/*" \
  -x "*/target/*" \
  -x ".git/*" \
  -x "*/.git/*" \
  -x ".vscode/*" \
  -x "*/.vscode/*" \
  -x ".idea/*" \
  -x "*/.idea/*" \
  -x "*.iml" \
  -x ".DS_Store" \
  -x "*/.DS_Store" \
  -x "*.zip" \
  -x "*.sh" \
  -x "docs/*"

echo ""
echo "=========================================="
echo "✅ ¡ZIP creado exitosamente!"
ls -lh "$NOMBRE_ZIP"
echo "=========================================="
