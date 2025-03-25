# Cloud Native Web Application

This repository contains a cloud-native web application built as part of the CSYE6225 course. The application provides RESTful APIs for user management and file uploads to AWS S3 bucket.

## Architecture

- **Web Application**: RESTful API service deployed on EC2 instances
- **Database**: RDS instance for storing user data and file metadata
- **Storage**: S3 bucket for storing user profile pictures and attachments

## API Endpoints

The API specification is available at [Swagger Hub](https://app.swaggerhub.com/apis-docs/csye6225-webapp/cloud-native-webapp/2025.spring.a05).

Key functionality:
- User management (create, read, update, delete)
- File upload/download for user profile pictures
- Health check endpoint

## Infrastructure

Infrastructure components managed by Terraform:
- EC2 instances with application security group
- RDS instance in private subnet with database security group
- S3 bucket with encryption and lifecycle policies
- IAM roles for EC2 to access S3

## Development Setup

### Prerequisites

- [Your programming language and version]
- [Your package manager]
- AWS CLI
- Terraform

### Local Development

1. Clone the repository
```bash
git clone git@github.com:your-org/web-application.git
cd web-application
```

2. Install dependencies
# For Java/Maven
```bash
mvn clean install
```

3. Configure environment variables
   # Example for database configuration
```bash
export DB_HOST=localhost
export DB_USER=csye6225
export DB_PASSWORD=your_password
export DB_NAME=csye6225
```

4. Run the application locally
```bash
# For Java/Maven
mvn spring-boot:run
```

## Deployment

### Building AMI

1. Use Packer to build the AMI with the application
2. AMI includes SystemD service configuration for automatic startup

### Terraform Deployment

1. Configure Terraform variables
2. Apply Terraform configuration
```bash
terraform init
terraform plan
terraform apply
```

## Security Features

- EC2 instances in private subnet with restricted security group
- RDS instance in private subnet, not accessible from internet
- Private and encrypted S3 bucket
- IAM roles for EC2 to access S3
- Application runs as non-privileged user via SystemD

## CI/CD

- GitHub Actions for continuous integration
- Automated testing on pull requests
- Integration tests with local database
