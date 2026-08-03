-- Create the user with a password
CREATE USER app_user WITH ENCRYPTED PASSWORD 'app_user_password';

-- Create the database owned by that user
CREATE DATABASE shopizer OWNER app_user;

-- Grant all privileges on the database to the user
GRANT ALL PRIVILEGES ON DATABASE shopizer TO app_user;