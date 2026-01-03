#!/usr/bin/env bash
set -euo pipefail

cd /work/terraform

echo "[bootstrap] terraform init/apply..."
terraform init -input=false
terraform apply -auto-approve -input=false

# Optional MinIO bootstrap example (bucket)
# echo "[bootstrap] configuring minio..."
# mc alias set local "${MINIO_ENDPOINT}" "${MINIO_ROOT_USER}" "${MINIO_ROOT_PASSWORD}"
# mc mb -p local/shopizer || true

echo "[bootstrap] done."
