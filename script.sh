#!/bin/bash

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

# Create MySQL database and user
echo "Creating MySQL database 'healthcheckdb'..."
sudo mysql -e "CREATE DATABASE healthcheckdb;"

echo "Creating MySQL user 'root'@'localhost' with password 'root'..."
sudo mysql -e "CREATE USER 'root' IDENTIFIED WITH mysql_native_password BY 'root';"

echo "Granting privileges to user 'root' on 'healthcheckdb'..."
sudo mysql -e "GRANT ALL PRIVILEGES ON healthcheckdb.* TO 'root';"

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

# Create the .env file with the necessary configurations
echo "Creating .env file with database configurations..."
cat <<EOL > .env
DB_URL=jdbc:mysql://localhost:3306/healthcheckdb
DB_USERNAME=root
DB_PASSWORD=root
EOL


echo "altering sql authentication for integration"
sudo mysql -u root -p'root' -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'root'; FLUSH PRIVILEGES;"


# Build the project
echo "Building the project with Maven..."
mvn clean install

# Launch the application with the .env file
echo "Launching the application..."
java -jar target/webapp-0.0.1-SNAPSHOT.jar
