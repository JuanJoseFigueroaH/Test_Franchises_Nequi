# Provider de AWS
terraform {
  required_version = ">= 1.0"
  
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

# Tabla de DynamoDB para Franchises
resource "aws_dynamodb_table" "franchises" {
  name           = var.table_name
  billing_mode   = "PAY_PER_REQUEST"
  hash_key       = "id"

  attribute {
    name = "id"
    type = "S"
  }

  tags = {
    Name        = "Franchises Table"
    Environment = var.environment
    Project     = "Franchise Management API"
    ManagedBy   = "Terraform"
  }

  # Habilitar point-in-time recovery para backups
  point_in_time_recovery {
    enabled = true
  }

  # Habilitar encriptación en reposo
  server_side_encryption {
    enabled = true
  }

  # Protección contra eliminación accidental
  deletion_protection_enabled = var.enable_deletion_protection
}

# Output de la tabla creada
output "dynamodb_table_name" {
  description = "Nombre de la tabla de DynamoDB"
  value       = aws_dynamodb_table.franchises.name
}

output "dynamodb_table_arn" {
  description = "ARN de la tabla de DynamoDB"
  value       = aws_dynamodb_table.franchises.arn
}

output "dynamodb_table_id" {
  description = "ID de la tabla de DynamoDB"
  value       = aws_dynamodb_table.franchises.id
}
