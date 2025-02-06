#!/bin/bash

# Load environment variables from the secure configuration file
source /opt/csye6225/config.sh

# Update and upgrade
echo "Updating package lists..."
sudo apt-get update

echo "Upgrading installed packages..."
sudo apt-get upgrade -y

echo "creating a new directory csye6225"
sudo mkdir -p /opt/csye6225

echo "Creating new Linux group and user for the application and assigning permissions..."
sudo groupadd csye6225
sudo useradd -m -g csye6225 appuser

sudo chown -R appuser:csye6225 /opt/csye6225
sudo chmod -R 755 /opt/csye6225

# Install unzip if not installed
echo "Installing unzip..."
sudo apt install unzip -y

echo "Unzipping webapp.zip..."
sudo unzip /tmp/webapp.zip -d /opt/csye6225

# Install MySQL
echo "Installing MySQL server..."
sudo apt install mysql-server -y

echo "Starting MySQL service..."
sudo systemctl start mysql

# Create MySQL database and user using environment variables
echo "Creating MySQL database 'healthcheckdb'..."
sudo mysql -e "CREATE DATABASE healthcheckdb;"

echo "Creating MySQL user '$DB_USERNAME' with password from environment variable..."
sudo mysql -e "CREATE USER '$DB_USERNAME' IDENTIFIED WITH mysql_native_password BY '$DB_PASSWORD';"

echo "Granting privileges to user '$DB_USERNAME' on 'healthcheckdb'..."
sudo mysql -e "GRANT ALL PRIVILEGES ON healthcheckdb.* TO '$DB_USERNAME';"

echo "Flushing MySQL privileges..."
sudo mysql -e "FLUSH PRIVILEGES;"

# Install Java and Maven
echo "Installing OpenJDK 17..."
sudo apt install openjdk-17-jdk -y

echo "Installing Maven..."
sudo apt install maven -y

# Navigate to project folder
echo "Changing directory to 'webapp'..."
cd /opt/csye6225/webapp

# Create the .env file with database configurations (if required by your application)
echo "Creating .env file with database configurations..."
cat <<EOL > .env
DB_URL=$DB_URL
DB_USERNAME=$DB_USERNAME
DB_PASSWORD=$DB_PASSWORD
EOL

echo "Altering SQL authentication for integration"
sudo mysql -u "$DB_USERNAME" -p"$DB_PASSWORD" -e "ALTER USER '$DB_USERNAME'@'localhost' IDENTIFIED WITH mysql_native_password BY '$DB_PASSWORD'; FLUSH PRIVILEGES;"

# Build the project
echo "Building the project with Maven..."
mvn clean install

# Launch the application
echo "Launching the application..."
java -jar target/webapp-0.0.1-SNAPSHOT.jar &
