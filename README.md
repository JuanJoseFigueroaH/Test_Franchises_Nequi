# Franchise Management API

API RESTful para la gestión de franquicias, sucursales y productos construida con arquitectura hexagonal y programación reactiva.

## 🌐 Aplicación Desplegada

La aplicación está desplegada y disponible en:

- **URL Base**: http://13.218.100.103:8080
- **Swagger UI**: http://13.218.100.103:8080/swagger-ui.html
- **Health Check**: http://13.218.100.103:8080/actuator/health
- **Metrics**: http://13.218.100.103:8080/actuator/prometheus

### Endpoints principales (Producción)

- `POST http://13.218.100.103:8080/api/v1/franchises` - Crear franquicia
- `GET http://13.218.100.103:8080/api/v1/franchises` - Listar franquicias
- `POST http://13.218.100.103:8080/api/v1/franchises/branches` - Agregar sucursal
- `POST http://13.218.100.103:8080/api/v1/franchises/branches/products` - Agregar producto
- `DELETE http://13.218.100.103:8080/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}` - Eliminar producto
- `PATCH http://13.218.100.103:8080/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}/stock` - Actualizar stock
- `GET http://13.218.100.103:8080/api/v1/franchises/{franchiseId}/max-stock-products` - Productos con mayor stock

---

## Infraestructura como Código (Terraform)

La persistencia de datos en **AWS DynamoDB** fue aprovisionada utilizando **Terraform** como herramienta de Infrastructure as Code (IaC).

### Recursos Aprovisionados

- **Tabla DynamoDB**: `franchises`
  - Modo de facturación: **PAY_PER_REQUEST** (bajo demanda)
  - Clave de partición: `id` (String)
  - Point-in-time recovery habilitado
  - Encriptación en reposo habilitada
  - Tags para gestión y seguimiento de costos

### Desplegar la Infraestructura

```bash
# Navegar al directorio de Terraform
cd terraform

# Inicializar Terraform
terraform init

# Revisar el plan de ejecución
terraform plan

# Aplicar la configuración
terraform apply
```

### Verificar la Tabla Creada

```bash
# Ver outputs de Terraform
terraform output

# Verificar con AWS CLI
aws dynamodb describe-table --table-name franchises --region us-east-1
```

### Archivos de Terraform

- `terraform/main.tf` - Configuración principal de recursos
- `terraform/variables.tf` - Definición de variables
- `terraform/terraform.tfvars.example` - Ejemplo de valores de variables

---

## Cómo levantar el proyecto (Desarrollo Local)

### Opción 1: Con Docker (Desarrollo Local)

#### Prerequisitos
- Docker
- Docker Compose
- AWS CLI (para crear la tabla de DynamoDB)

#### Pasos

1. **Clonar el repositorio**
```bash
git clone <repository-url>
cd Test_Franchises_Nequi
```

2. **Levantar todos los servicios**
```bash
docker-compose up -d
```

Esto levantará:
- **DynamoDB Local** en el puerto `8005`
- **Redis** en el puerto `6379`
- **Franchise API** en el puerto `8080`

3. **Verificar que los servicios estén corriendo**
```bash
docker-compose ps
```

4. **Crear la tabla de DynamoDB** (IMPORTANTE - solo la primera vez)

Abre otra terminal y ejecuta:
```bash
aws dynamodb create-table --table-name franchises --attribute-definitions AttributeName=id,AttributeType=S --key-schema AttributeName=id,KeyType=HASH --billing-mode PAY_PER_REQUEST --endpoint-url http://localhost:8005 --region us-east-1
```

5. **Acceder a la aplicación**
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Health Check: http://localhost:8080/actuator/health
- Metrics: http://localhost:8080/actuator/prometheus

6. **Ver logs**
```bash
# Todos los servicios
docker-compose logs -f

# Solo la API
docker-compose logs -f franchise-api
```

7. **Detener los servicios**
```bash
docker-compose down
```

---

### Opción 2: Sin Docker (Desarrollo local)

#### Prerequisitos
- Java 21
- Maven 3.8+
- DynamoDB Local o AWS DynamoDB
- Redis

#### Pasos

1. **Instalar y levantar DynamoDB Local**
```bash
# Descargar DynamoDB Local
wget https://s3.us-west-2.amazonaws.com/dynamodb-local/dynamodb_local_latest.tar.gz
tar -xzf dynamodb_local_latest.tar.gz

# Ejecutar DynamoDB Local
java -Djava.library.path=./DynamoDBLocal_lib -jar DynamoDBLocal.jar -sharedDb -inMemory -port 8005
```

2. **Instalar y levantar Redis**
```bash
# En Windows (con Chocolatey)
choco install redis-64
redis-server

# En Linux/Mac
sudo apt-get install redis-server  # Ubuntu/Debian
brew install redis                  # macOS
redis-server
```

3. **Compilar el proyecto**
```bash
mvn clean install
```

4. **Ejecutar la aplicación**
```bash
mvn spring-boot:run
```

O ejecutar el JAR directamente:
```bash
java -jar target/franchise-1.0.0.jar
```

5. **Acceder a la aplicación**
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Health Check: http://localhost:8080/actuator/health

---

## 📋 Variables de Entorno

Si necesitas personalizar la configuración, puedes usar estas variables de entorno:

```bash
# DynamoDB
DYNAMODB_ENDPOINT=http://localhost:8005
AWS_REGION=us-east-1
AWS_ACCESS_KEY=local
AWS_SECRET_KEY=local
DYNAMODB_TABLE_NAME=franchises

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# Zipkin (opcional)
ZIPKIN_ENDPOINT=http://localhost:9411/api/v2/spans

# Ambiente
ENVIRONMENT=local
```

---

## 🧪 Ejecutar Tests

```bash
# Todos los tests
mvn test

# Con reporte de cobertura
mvn clean test jacoco:report

# Ver reporte de cobertura
open target/site/jacoco/index.html
```

---

## 📚 Documentación de la API

### Producción (AWS)
**Swagger UI**: http://13.218.100.103:8080/swagger-ui.html

### Desarrollo Local
**Swagger UI**: http://localhost:8080/swagger-ui.html

### Endpoints principales

- `POST /api/v1/franchises` - Crear franquicia
- `GET /api/v1/franchises` - Listar franquicias (con paginación)
- `POST /api/v1/franchises/branches` - Agregar sucursal
- `POST /api/v1/franchises/branches/products` - Agregar producto
- `DELETE /api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}` - Eliminar producto
- `PATCH /api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}/stock` - Actualizar stock
- `GET /api/v1/franchises/{franchiseId}/max-stock-products` - Productos con mayor stock por sucursal

---