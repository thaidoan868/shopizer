#!/usr/bin/env bash
set -euo pipefail

cd /work/terraform

terraform init -input=false
terraform apply -auto-approve -input=false

echo "[bootstrap] done."
