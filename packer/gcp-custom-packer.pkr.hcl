packer {
  required_plugins {
    googlecompute = {
      version = ">= 1.1.8"
      source  = "github.com/hashicorp/googlecompute"
    }
  }
}

variable "gcp_project" {
  type        = string
  description = "GCP Project ID"
}

variable "gcp_zone" {
  type        = string
  default     = "us-central1-a"
  description = "GCP Zone for the image build"
}

variable "gcp_instance_type" {
  type        = string
  description = "GCE machine type"
}

variable "gcp_ssh_username" {
  type        = string
  description = "SSH username for the instance"
}

variable "image_name_prefix" {
  type        = string
  description = "Prefix for the image name"
}

variable "gcp_jar_source" {
  type        = string
  description = "Path to the local JAR file"
}

variable "gcp_jar_destination" {
  type    = string
  default = null
}


variable "gcp_db_url" {
  type        = string
  description = "Database connection URL"
}

variable "gcp_db_username" {
  type        = string
  description = "Database username"
}

variable "gcp_db_password" {
  type        = string
  description = "Database password"
}

variable "aws_access_key_id" {
  type        = string
  description = "AWS access key ID"
}

variable "aws_secret_access_key" {
  type        = string
  description = "AWS secret access key"
}

locals {
  image_name = "${var.image_name_prefix}-{{timestamp}}"
}


# Use an official Ubuntu 24.04 LTS image from GCP
source "googlecompute" "ubuntu_base" {
  project_id    = var.gcp_project
  zone          = var.gcp_zone
  machine_type  = var.gcp_instance_type
  source_image  = "ubuntu-2404-noble-amd64-v20250214" # Verify this image exists in your region
  ssh_username  = var.gcp_ssh_username
  image_name    = local.image_name
  image_family  = "csye6225-dev"
  instance_name = "packer-${uuidv4()}"
}

build {
  sources = [
    "source.googlecompute.ubuntu_base"
  ]

  # Provisioner: Update packages and upgrade system
  provisioner "shell" {
    inline = [
      "export DEBIAN_FRONTEND=noninteractive",
      "sudo apt-get update",
      "sudo apt-get upgrade -y"
    ]
  }

  # Install required packages: MySQL server and OpenJDK 17
  provisioner "shell" {
    inline = [
      "sudo apt-get install -y mysql-server openjdk-17-jdk"
    ]
  }

  # Install Maven
  provisioner "shell" {
    inline = [
      "sudo apt-get install -y maven"
    ]
  }

  # Create application directory and set permissions
  provisioner "shell" {
    inline = [
      "sudo mkdir -p /opt/csye6225",
      "sudo chown ubuntu:ubuntu /opt/csye6225"
    ]
  }

  provisioner "shell" {
    inline = [
      "cat <<EOL | sudo tee /opt/csye6225/.env",
      "DB_URL=${var.gcp_db_url}",
      "DB_USERNAME=${var.gcp_db_username}",
      "DB_PASSWORD=${var.gcp_db_password}",
      "EOL",
      "sudo chmod 644 /opt/csye6225/.env"
    ]
  }


  # Create group and user 'csye6225' with no login shell, and change ownership of the app directory
  provisioner "shell" {
    inline = [
      "sudo groupadd -f csye6225",
      "if ! id -u csye6225 >/dev/null 2>&1; then sudo useradd -m -g csye6225 -s /usr/sbin/nologin csye6225; fi",
      "sudo chown -R csye6225:csye6225 /opt/csye6225"
    ]
  }

  ## Start MySQL service and set up the local database and user
  provisioner "shell" {
    inline = [
      "sudo systemctl start mysql",
      "sudo mysql -e \"CREATE DATABASE healthcheckdb;\"",
      "sudo mysql -e \"CREATE USER '${var.gcp_db_username}' IDENTIFIED WITH mysql_native_password BY '${var.gcp_db_password}';\"",
      "sudo mysql -e \"GRANT ALL PRIVILEGES ON healthcheckdb.* TO '${var.gcp_db_username}';\"",
      "sudo mysql -e \"FLUSH PRIVILEGES;\""
    ]
  }

  provisioner "shell" {
    inline = [
      "sudo mysql -u \"${var.gcp_db_username}\" -p\"${var.gcp_db_password}\" -e \"ALTER USER '${var.gcp_db_username}'@'localhost' IDENTIFIED WITH mysql_native_password BY '${var.gcp_db_password}'; FLUSH PRIVILEGES;\""
    ]
  }

  provisioner "file" {
    source      = var.gcp_jar_source
    destination = var.gcp_jar_destination
  }

  # Move the JAR file to /opt/csye6225 and adjust ownership
  provisioner "shell" {
    inline = [
      "sudo mv /tmp/webapp-0.0.1-SNAPSHOT.jar /opt/csye6225/webapp-0.0.1-SNAPSHOT.jar",
      "sudo chown csye6225:csye6225 /opt/csye6225/webapp-0.0.1-SNAPSHOT.jar"
    ]
  }

  # Create a systemd service to launch your application using the JAR and load environment variables from config.sh
  provisioner "shell" {
    inline = [
      "echo '[Unit]' | sudo tee /etc/systemd/system/myapp.service",
      "echo 'Description=My Application Service' | sudo tee -a /etc/systemd/system/myapp.service",
      "echo '[Service]' | sudo tee -a /etc/systemd/system/myapp.service",
      "echo 'User=csye6225' | sudo tee -a /etc/systemd/system/myapp.service",
      "echo 'EnvironmentFile=/opt/csye6225/.env' | sudo tee -a /etc/systemd/system/myapp.service",
      "echo 'WorkingDirectory=/opt/csye6225' | sudo tee -a /etc/systemd/system/myapp.service",
      "echo 'echo 'ExecStart=/usr/bin/java -Dspring.profiles.active=dev -Daws.accessKeyId=${var.aws_access_key_id} -Daws.secretAccessKey=${var.aws_secret_access_key} -jar /opt/csye6225/webapp-0.0.1-SNAPSHOT.jar' | sudo tee -a /etc/systemd/system/myapp.service",
      "echo 'Restart=on-failure' | sudo tee -a /etc/systemd/system/myapp.service",
      "echo '[Install]' | sudo tee -a /etc/systemd/system/myapp.service",
      "echo 'WantedBy=multi-user.target' | sudo tee -a /etc/systemd/system/myapp.service",
      "sudo systemctl daemon-reload",
      "sudo systemctl enable myapp.service"
    ]
  }

  # Provisioner: Install some basic packages (for testing)
  provisioner "shell" {
    inline = [
      "sudo apt-get install -y curl"
    ]
  }



}


