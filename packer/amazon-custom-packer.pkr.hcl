packer {
  required_plugins {
    amazon = {
      version = ">= 1.0.0, < 2.0.0"
      source  = "github.com/hashicorp/amazon"
    }
  }
}


variable "spring_profiles_active" {
  type        = string
  description = "Spring active profile"
  default     = "dev"
}

variable "aws_access_key_id" {
  type        = string
  description = "AWS Access Key ID for the application"
}

variable "aws_secret_access_key" {
  type        = string
  description = "AWS Secret Access Key for the application"
}

variable "aws_region" {
  type    = string
  default = "us-east-1"
}

variable "source_ami" {
  type        = string
  description = "Source AMI to use for creating the new AMI"
}

variable "instance_type" {
  type        = string
  description = "EC2 instance type"
}

variable "ssh_username" {
  type        = string
  description = "SSH username for the instance"
}

variable "ami_name_prefix" {
  type        = string
  description = "Prefix for the AMI name"
}

variable "jar_source" {
  type        = string
  description = "Path to the local JAR file"
}

variable "jar_destination" {
  type    = string
  default = null
}


variable "db_url" {
  type        = string
  description = "Database connection URL"
}

variable "db_username" {
  type        = string
  description = "Database username"
}

variable "db_password" {
  type        = string
  description = "Database password"
}


variable "volume_type" {
  type        = string
  description = "volume type"
  default     = "gp2"
}
variable "volume_size" {
  type        = number
  description = "volume size"
  default     = 25
}
variable "termination" {
  type        = bool
  description = "delete on termination"
  default     = true
}



source "amazon-ebs" "ubuntu" {
  region        = var.aws_region
  source_ami    = var.source_ami # Ensure this is Ubuntu 24.04 LTS or update accordingly
  instance_type = var.instance_type
  ssh_username  = var.ssh_username
  ami_name      = "${var.ami_name_prefix}-{{timestamp}}"

  launch_block_device_mappings {
    device_name           = "/dev/sda1"
    volume_type           = var.volume_type
    volume_size           = var.volume_size
    delete_on_termination = var.termination
  }
}

build {
  sources = [
    "source.amazon-ebs.ubuntu"
  ]

  ## Update system packages and upgrade
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

  # Create configuration file with database environment variables
  # provisioner "shell" {
  #   inline = [
  #     "echo '#!/bin/bash' | sudo tee /opt/csye6225/config.sh",
  #     "echo 'DB_URL=\"jdbc:mysql://localhost:3306/healthcheckdb\"' | sudo tee -a /opt/csye6225/config.sh",
  #     "echo 'DB_USERNAME=\"root\"' | sudo tee -a /opt/csye6225/config.sh",
  #     "echo 'DB_PASSWORD=\"root\"' | sudo tee -a /opt/csye6225/config.sh",
  #     "sudo chmod 644 /opt/csye6225/config.sh"
  #   ]
  # }

  # provisioner "shell" {
  #   inline = [
  #     "cat <<EOL | sudo tee /opt/csye6225/.env",
  #     "DB_URL=${var.db_url}",
  #     "DB_USERNAME=${var.db_username}",
  #     "DB_PASSWORD=${var.db_password}",
  #     "RDS_DB_ENDPOINT=${var.rds_db_endpoint}",
  #     "S3_BUCKET_NAME=${var.s3_bucket_name}",
  #     "AWS_REGION=${var.aws_region}",
  #     "RDS_DB_PASSWORD=${var.rds_db_password}",
  #     "EOL",
  #     "sudo chmod 644 /opt/csye6225/.env"
  #   ]
  # }


  # Create group and user 'csye6225' with no login shell, and change ownership of the app directory
  provisioner "shell" {
    inline = [
      "sudo groupadd -f csye6225",
      "if ! id -u csye6225 >/dev/null 2>&1; then sudo useradd -m -g csye6225 -s /usr/sbin/nologin csye6225; fi",
      "sudo chown -R csye6225:csye6225 /opt/csye6225"
    ]
  }

  # ## Start MySQL service and set up the local database and user
  # provisioner "shell" {
  #   inline = [
  #     "sudo systemctl start mysql",
  #     "sudo mysql -e \"CREATE DATABASE healthcheckdb;\"",
  #     "sudo mysql -e \"CREATE USER '${var.db_username}' IDENTIFIED WITH mysql_native_password BY '${var.db_password}';\"",
  #     "sudo mysql -e \"GRANT ALL PRIVILEGES ON healthcheckdb.* TO '${var.db_username}';\"",
  #     "sudo mysql -e \"FLUSH PRIVILEGES;\""
  #   ]
  # }

  # provisioner "shell" {
  ##   inline = [
  #     "sudo mysql -u \"${var.db_username}\" -p\"${var.db_password}\" -e \"ALTER USER '${var.db_username}'@'localhost' IDENTIFIED WITH mysql_native_password BY '${var.db_password}'; FLUSH PRIVILEGES;\""
  #   ]
  # }


  # Copy the pre-built JAR file to the instance's /tmp directory
  provisioner "file" {
    source      = var.jar_source
    destination = var.jar_destination
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
      "echo 'ExecStart=/usr/bin/java -Dspring.profiles.active=${var.spring_profiles_active} -jar /opt/csye6225/webapp-0.0.1-SNAPSHOT.jar' | sudo tee -a /etc/systemd/system/myapp.service",
      "echo 'Restart=on-failure' | sudo tee -a /etc/systemd/system/myapp.service",
      "echo '[Install]' | sudo tee -a /etc/systemd/system/myapp.service",
      "echo 'WantedBy=multi-user.target' | sudo tee -a /etc/systemd/system/myapp.service",
      "sudo systemctl daemon-reload",
      "sudo systemctl enable myapp.service"
    ]
  }
}

# -Daws.accessKeyId=${var.aws_access_key_id} -Daws.secretAccessKey=${var.aws_secret_access_key}