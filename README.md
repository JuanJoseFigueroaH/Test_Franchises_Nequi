# Franchise Management API

API REST para gestionar franquicias, sucursales y productos utilizando arquitectura hexagonal, programación reactiva y tecnologías modernas.

## Tecnologías

- Java 21
- Spring Boot 3.2.1
- Spring WebFlux (Programación Reactiva)
- DynamoDB (Base de datos principal)
- Redis (Cache)
- MapStruct (Mapeo de objetos)
- Lombok
- Swagger/OpenAPI
- JUnit 5, Mockito, Jacoco
- Docker & Docker Compose

## Arquitectura

El proyecto sigue los principios de **Arquitectura Hexagonal** (Ports & Adapters):

- **Domain**: Modelos de negocio y excepciones
- **Application**: Casos de uso y DTOs
- **Infrastructure**: Adaptadores de entrada (REST) y salida (DynamoDB, Redis)

## Principios Aplicados

- SOLID
- CQRS
- KISS (Keep It Simple, Stupid)
- YAGNI (You Aren't Gonna Need It)
- DRY (Don't Repeat Yourself)

## Requisitos Previos

- Docker y Docker Compose
- Java 21 (para desarrollo local)
- Maven 3.8+ (para desarrollo local)

## Configuración y Ejecución

### Con Docker Compose

1. Iniciar todos los servicios:
```bash
docker-compose up -d
```

2. Crear la tabla en DynamoDB:
```bash
bash init-dynamodb.sh
```

3. La API estará disponible en: http://localhost:8080

### Desarrollo Local

1. Iniciar DynamoDB y Redis:
```bash
docker-compose up -d dynamodb-local redis
```

2. Crear la tabla en DynamoDB:
```bash
bash init-dynamodb.sh
```

3. Ejecutar la aplicación:
```bash
mvn spring-boot:run
```

## Documentación API

Swagger UI: http://localhost:8080/swagger-ui.html

OpenAPI Docs: http://localhost:8080/api-docs

## Endpoints Implementados

### 1. Crear Franquicia
- **POST** `/api/v1/franchises`
- Body: `{"name": "Nombre de la franquicia"}`

## Pruebas

Ejecutar pruebas unitarias:
```bash
mvn test
```

Ver reporte de cobertura (Jacoco):
```bash
mvn test jacoco:report
```

El reporte estará en: `target/site/jacoco/index.html`

## Estructura del Proyecto

```
src/
├── main/
│   ├── java/com/nequi/franchise/
│   │   ├── domain/              # Capa de dominio
│   │   │   ├── model/           # Modelos de negocio
│   │   │   ├── exception/       # Excepciones de dominio
│   │   │   └── port/            # Puertos (interfaces)
│   │   ├── application/         # Capa de aplicación
│   │   │   ├── service/         # Casos de uso
│   │   │   ├── dto/             # DTOs
│   │   │   └── mapper/          # Mappers de DTOs
│   │   └── infrastructure/      # Capa de infraestructura
│   │       ├── adapter/
│   │       │   ├── input/       # Controladores REST
│   │       │   └── output/      # Adaptadores DynamoDB/Redis
│   │       └── config/          # Configuraciones
│   └── resources/
│       └── application.yml      # Configuración de la aplicación
└── test/                        # Pruebas unitarias
```
