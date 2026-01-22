variable "aws_region" {
  description = "Región de AWS donde se desplegará la infraestructura"
  type        = string
  default     = "us-east-1"
}

variable "table_name" {
  description = "Nombre de la tabla de DynamoDB"
  type        = string
  default     = "franchises"
}

variable "environment" {
  description = "Ambiente de despliegue (dev, staging, production)"
  type        = string
  default     = "production"
}

variable "enable_deletion_protection" {
  description = "Habilitar protección contra eliminación de la tabla"
  type        = bool
  default     = false
}
