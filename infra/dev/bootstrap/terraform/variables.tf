variable "KEYCLOAK_URL" {
  description = "Keycloak base URL, e.g. http://keycloak:8080"
  type        = string
}

variable "KEYCLOAK_ADMIN_USERNAME" {
  description = "Keycloak admin username (master realm)"
  type        = string
}

variable "KEYCLOAK_ADMIN_PASSWORD" {
  description = "Keycloak admin password (master realm)"
  type        = string
  sensitive   = true
}

variable "BACKEND_CLIENT_SECRET" {
  description = "Client secret for the backend confidential client"
  type        = string
  sensitive   = true
}

variable "DEFAULT_ADMIN_USERNAME" {
  type = string
}

variable "DEFAULT_ADMIN_PASSWORD" {
  type      = string
  sensitive = true
}

variable "MINIO_ENDPOINT" {
  description = "MinIO endpoint, e.g. http://minio:9000"
  type        = string
}

variable "MINIO_ROOT_USER" {
  type    = string
}

variable "MINIO_ROOT_PASSWORD" {
  type      = string
  sensitive = true
}

variable "MINIO_BUCKET" {
  description = "Default bucket to create"
  type        = string
  default     = "shopizer"
}