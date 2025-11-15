#!/bin/bash

# ============================================================================
# Script para generar secretos seguros para producción
# ============================================================================
#
# DESCRIPCIÓN:
# Genera valores aleatorios y seguros para credenciales y secretos.
# Útil para configurar entornos de producción.
#
# USO:
#   ./scripts/generate-secrets.sh
#
# SALIDA:
# Muestra los secretos generados que puedes copiar a tu archivo .env
#
# ============================================================================

# Colores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║     🔐 GENERADOR DE SECRETOS - TASK MANAGEMENT API        ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

# Función para generar password aleatorio
generate_password() {
    local length=${1:-32}
    openssl rand -base64 "$length" | tr -d "=+/" | cut -c1-"$length"
}

# Función para generar JWT secret
generate_jwt_secret() {
    openssl rand -hex 64
}

echo -e "${CYAN}📝 Generando secretos seguros...${NC}"
echo ""

# ============================================================================
# CREDENCIALES DE BASE DE DATOS
# ============================================================================
echo -e "${YELLOW}# ============================================================================${NC}"
echo -e "${YELLOW}# CREDENCIALES DE BASE DE DATOS (PostgreSQL)${NC}"
echo -e "${YELLOW}# ============================================================================${NC}"
echo ""

DB_PASSWORD=$(generate_password 32)
echo -e "${GREEN}DATABASE_USERNAME=${NC}taskmanager_user"
echo -e "${GREEN}DATABASE_PASSWORD=${NC}$DB_PASSWORD"
echo -e "${GREEN}POSTGRES_USER=${NC}taskmanager_user"
echo -e "${GREEN}POSTGRES_PASSWORD=${NC}$DB_PASSWORD"
echo ""

# ============================================================================
# JWT SECRET
# ============================================================================
echo -e "${YELLOW}# ============================================================================${NC}"
echo -e "${YELLOW}# JWT SECRET (Autenticación)${NC}"
echo -e "${YELLOW}# ============================================================================${NC}"
echo ""

JWT_SECRET=$(generate_jwt_secret)
echo -e "${GREEN}JWT_SECRET=${NC}$JWT_SECRET"
echo -e "${GREEN}JWT_EXPIRATION=${NC}3600000  ${CYAN}# 1 hora (recomendado para producción)${NC}"
echo ""

# ============================================================================
# PGADMIN
# ============================================================================
echo -e "${YELLOW}# ============================================================================${NC}"
echo -e "${YELLOW}# PGADMIN (Interfaz de administración de PostgreSQL)${NC}"
echo -e "${YELLOW}# ============================================================================${NC}"
echo ""

PGADMIN_PASSWORD=$(generate_password 24)
echo -e "${GREEN}PGADMIN_EMAIL=${NC}admin@tuempresa.com  ${CYAN}# Cambiar por email real${NC}"
echo -e "${GREEN}PGADMIN_PASSWORD=${NC}$PGADMIN_PASSWORD"
echo ""

# ============================================================================
# INFORMACIÓN ADICIONAL
# ============================================================================
echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                    📋 INSTRUCCIONES                        ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${CYAN}1. Copia los valores generados arriba${NC}"
echo -e "${CYAN}2. Pégalos en tu archivo .env (reemplazando los valores existentes)${NC}"
echo -e "${CYAN}3. Guarda el archivo .env en un lugar seguro${NC}"
echo -e "${CYAN}4. NUNCA commitees estos valores en Git${NC}"
echo -e "${CYAN}5. En producción, usa un gestor de secretos (Vault, AWS Secrets Manager)${NC}"
echo ""

echo -e "${YELLOW}⚠️  IMPORTANTE:${NC}"
echo -e "${YELLOW}   - Estos secretos son únicos y no se volverán a generar${NC}"
echo -e "${YELLOW}   - Guárdalos en un gestor de contraseñas${NC}"
echo -e "${YELLOW}   - Rota los secretos cada 90 días en producción${NC}"
echo -e "${YELLOW}   - Nunca compartas secretos por email o chat${NC}"
echo ""

echo -e "${GREEN}✅ Secretos generados exitosamente${NC}"
echo ""

# ============================================================================
# EJEMPLO DE COMANDO PARA GENERAR MANUALMENTE
# ============================================================================
echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║              🛠️  COMANDOS ÚTILES                           ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${CYAN}Para generar secretos manualmente:${NC}"
echo ""
echo -e "${GREEN}# Password seguro (32 caracteres):${NC}"
echo -e "  openssl rand -base64 32 | tr -d \"=+/\" | cut -c1-32"
echo ""
echo -e "${GREEN}# JWT Secret (128 caracteres hex):${NC}"
echo -e "  openssl rand -hex 64"
echo ""
echo -e "${GREEN}# UUID (útil para API keys):${NC}"
echo -e "  uuidgen"
echo ""
