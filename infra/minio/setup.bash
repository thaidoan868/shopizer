mc alias set local http://localhost:9000 admin adminpassword
mc admin accesskey create local --access-key "shopizer-access-key" --secret-key "shopizer-secret-key"

mc mb local/avatar-public
mc anonymous set download local/avatar-public