#!/bin/bash

# Exit script immediately if any command fails
set -e

echo "Checking for MinIO Client (mc)..."

# Check if 'mc' command exists in PATH
if command -v mc &> /dev/null; then
    echo "✓ MinIO Client (mc) is already installed."
    mc --version
else
    echo "x MinIO Client (mc) not found. Installing..."

    # Download the binary
    curl -fsSL https://dl.min.io/client/mc/release/linux-amd64/mc -o mc

    # Make executable
    chmod +x mc

    # Move to system PATH
    sudo mv mc /usr/local/bin/

    echo "✓ MinIO Client installed successfully!"
    mc --version
fi

mc alias set local http://localhost:9000 admin adminpassword
mc admin accesskey create local --access-key "shopizer-access-key" --secret-key "shopizer-secret-key"

mc mb local/avatar-public
mc anonymous set download local/avatar-public