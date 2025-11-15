#!/bin/bash

# ============================================================================
# Script para cargar variables de entorno desde archivo .env
# ============================================================================
#
# DESCRIPCIÓN:
# Este script carga las variables de entorno definidas en el archivo .env
# para que estén disponibles en la sesión actual de bash.
#
# USO:
#   source scripts/load-env.sh
#   . scripts/load-env.sh
#
# IMPORTANTE:
# - Usar 'source' o '.' para que las variables se carguen en la sesión actual
# - No ejecutar directamente con ./load-env.sh (no funcionará)
# - El archivo .env debe existir en la raíz del proyecto
#
# ============================================================================

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Función para imprimir mensajes con color
print_message() {
    local color=$1
    local message=$2
    echo -e "${color}${message}${NC}"
}

# Verificar si el archivo .env existe
ENV_FILE=".env"

if [ ! -f "$ENV_FILE" ]; then
    print_message "$RED" "❌ ERROR: Archivo .env no encontrado"
    print_message "$YELLOW" "📝 Instrucciones:"
    print_message "$YELLOW" "   1. Copiar el archivo de ejemplo: cp .env.example .env"
    print_message "$YELLOW" "   2. Editar .env con tus configuraciones"
    print_message "$YELLOW" "   3. Ejecutar nuevamente: source scripts/load-env.sh"
    return 1
fi

print_message "$BLUE" "📂 Cargando variables de entorno desde $ENV_FILE..."

# Contador de variables cargadas
count=0

# Leer el archivo .env línea por línea
while IFS= read -r line || [ -n "$line" ]; do
    # Ignorar líneas vacías y comentarios
    if [[ -z "$line" ]] || [[ "$line" =~ ^[[:space:]]*# ]]; then
        continue
    fi

    # Exportar la variable
    # Eliminar espacios alrededor del signo =
    clean_line=$(echo "$line" | sed 's/[[:space:]]*=[[:space:]]*/=/g')

    # Validar formato variable=valor
    if [[ "$clean_line" =~ ^[A-Z_][A-Z0-9_]*=.* ]]; then
        export "$clean_line"
        ((count++))

        # Obtener nombre de la variable (antes del =)
        var_name="${clean_line%%=*}"

        # Si es una variable sensible, no mostrar el valor
        if [[ "$var_name" == *"PASSWORD"* ]] || \
           [[ "$var_name" == *"SECRET"* ]] || \
           [[ "$var_name" == *"KEY"* ]] || \
           [[ "$var_name" == "JWT_SECRET" ]]; then
            print_message "$GREEN" "   ✓ $var_name=****** (sensible)"
        else
            print_message "$GREEN" "   ✓ $clean_line"
        fi
    fi
done < "$ENV_FILE"

print_message "$GREEN" "✅ $count variables de entorno cargadas exitosamente"

# Mostrar algunas variables importantes (sin mostrar valores sensibles)
print_message "$BLUE" "
📊 Resumen de Configuración:
   🗄️  Base de Datos: ${DATABASE_URL:-No configurada}
   🚀 Servidor: Puerto ${SERVER_PORT:-8080}
   🔒 Rate Limiting: ${RATE_LIMIT_ENABLED:-No configurado}
   📝 Perfil Spring: ${SPRING_PROFILES_ACTIVE:-default}
"

print_message "$YELLOW" "💡 Tip: Para verificar una variable: echo \$NOMBRE_VARIABLE"
print_message "$YELLOW" "💡 Ejemplo: echo \$DATABASE_URL"
