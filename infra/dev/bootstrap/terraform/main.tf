terraform {
  required_version = ">= 1.5.0"

  required_providers {
    keycloak = {
      source  = "keycloak/keycloak"
      version = ">= 5.6.0"
    }
  }
}

# keycloak configuration
provider "keycloak" {
  url       = var.KEYCLOAK_URL
  realm     = "master"
  client_id = "admin-cli"
  username  = var.KEYCLOAK_ADMIN_USERNAME
  password  = var.KEYCLOAK_ADMIN_PASSWORD
}

resource "keycloak_realm" "shopizer" {
  realm   = "shopizer"
  enabled = true
}

# user roles
resource "keycloak_role" "customer" {
  realm_id = keycloak_realm.shopizer.id
  name     = "customer"
}

resource "keycloak_role" "merchantstore" {
  realm_id = keycloak_realm.shopizer.id
  name     = "merchantstore"
}

resource "keycloak_role" "admin" {
  realm_id = keycloak_realm.shopizer.id
  name     = "admin"
}

# clients
resource "keycloak_openid_client" "frontend" {
  realm_id  = keycloak_realm.shopizer.id
  client_id = "frontend"
  name      = "Frontend"

  enabled                      = true
  access_type                  = "PUBLIC"
  standard_flow_enabled        = false
  direct_access_grants_enabled = true
}

resource "keycloak_openid_client" "backend" {
  realm_id  = keycloak_realm.shopizer.id
  client_id = "backend"
  name      = "Backend"

  enabled                  = true
  access_type              = "CONFIDENTIAL"
  client_secret            = var.BACKEND_CLIENT_SECRET
  service_accounts_enabled = true

  standard_flow_enabled        = false
  direct_access_grants_enabled = false
}

# default user
resource "keycloak_user" "default_admin" {
  realm_id   = keycloak_realm.shopizer.id
  username   = var.DEFAULT_ADMIN_USERNAME
  first_name = "Alice"
  last_name  = "Queen"
  enabled    = true

  initial_password {
    value     = var.DEFAULT_ADMIN_USERNAME
    temporary = false
  }
}

resource "keycloak_user_roles" "default_admin_roles" {
  realm_id = keycloak_realm.shopizer.id
  user_id  = keycloak_user.default_admin.id

  role_ids = [
    keycloak_role.admin.id
  ]
}

locals {
  backend_user_mgmt_roles = ["view-users", "query-users", "manage-users"]
}

# realm-management client exists inside each realm
data "keycloak_openid_client" "realm_management" {
  realm_id  = keycloak_realm.shopizer.id
  client_id = "realm-management"
}

resource "keycloak_openid_client_service_account_role" "backend_service_account_roles" {
  count = length(local.backend_user_mgmt_roles)

  realm_id                = keycloak_realm.shopizer.id
  client_id               = data.keycloak_openid_client.realm_management.id
  service_account_user_id = keycloak_openid_client.backend.service_account_user_id
  role                    = local.backend_user_mgmt_roles[count.index]
}
#
# # MinIO bootstrap via `mc`
resource "null_resource" "minio_bootstrap" {
  triggers = {
    endpoint = var.MINIO_ENDPOINT
    bucket   = var.MINIO_BUCKET
  }

  provisioner "local-exec" {
    interpreter = ["/bin/sh", "-lc"]
    command     = <<-EOT
      set -e

      # Expect `mc` to be available in the runtime (recommended: run terraform inside a bootstrap container that includes mc)
      mc alias set local "${var.MINIO_ENDPOINT}" "${var.MINIO_ROOT_USER}" "${var.MINIO_ROOT_PASSWORD}"

      # Create bucket if not exists
      mc mb -p "local/${var.MINIO_BUCKET}" || true
    EOT
  }
}
