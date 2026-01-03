variable "keycloak_url" {
  description = "Keycloak base URL, e.g. http://keycloak:8080"
  type        = string
}

variable "keycloak_admin_username" {
  description = "Keycloak admin username (master realm)"
  type        = string
}

variable "keycloak_admin_password" {
  description = "Keycloak admin password (master realm)"
  type        = string
  sensitive   = true
}

variable "backend_client_secret" {
  description = "Client secret for the backend confidential client"
  type        = string
  sensitive   = true
}

variable "default_admin_username" {
  type    = string
}

variable "default_admin_password" {
  type      = string
  sensitive = true
}

variable "minio_endpoint" {
  description = "MinIO endpoint, e.g. http://minio:9000"
  type        = string
}

variable "minio_root_user" {
  type    = string
  default = "minio"
}

variable "minio_root_password" {
  type      = string
  sensitive = true
  default   = "minio12345"
}

variable "minio_bucket" {
  description = "Default bucket to create"
  type        = string
  default     = "shopizer"
}
