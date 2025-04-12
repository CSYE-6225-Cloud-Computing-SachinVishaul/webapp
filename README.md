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

---

## Additional Deployment Steps for CI/CD Workflow

### Pull Request Merged Workflow
The GitHub Actions workflow for pull request merges includes the following steps:

1. **Run Unit Tests**
   Ensures the application code passes all unit tests.

2. **Validate Packer Template**
   Validates the Packer template for building AMIs.

3. **Build Application Artifact**
   Builds the application JAR file using Maven.

4. **Build AMI in DEV AWS Account**
   Creates an AMI in the DEV AWS account with the following steps:
   - Upgrade OS packages.
   - Install dependencies (e.g., Python, Node.js).
   - Install application dependencies.
   - Copy application artifacts and configuration files.
   - Configure the application to start automatically when the VM is launched.

5. **Share AMI with DEMO AWS Account**
   Shares the newly created AMI with the DEMO AWS account.

6. **Reconfigure AWS CLI for DEMO Account**
   Updates the GitHub Runner's AWS CLI to use credentials for the DEMO AWS account.

7. **Create New Launch Template Version**
   Creates a new version of the Launch Template in the DEMO account with the latest AMI ID.

8. **Trigger Instance Refresh**
   Issues a command to the Auto Scaling Group to perform an "instance refresh" using the AWS CLI.

9. **Wait for Instance Refresh Completion**
   The workflow waits for the instance refresh to complete before exiting. The status of the workflow matches the status of the instance refresh.

---

### SSL Certificate Configuration

#### DEV Environment
- Use **AWS Certificate Manager** to generate SSL certificates for the DEV environment.

#### DEMO Environment
- Obtain an SSL certificate from a third-party vendor (e.g., Namecheap).
- Import the certificate into AWS Certificate Manager using the following command:
  ```bash
  aws acm import-certificate \
    --certificate fileb://path/to/certificate.pem \
    --private-key fileb://path/to/private-key.pem \
    --certificate-chain fileb://path/to/certificate-chain.pem
  ```
- Configure the load balancer to use the imported certificate.

---

### Security Enhancements

1. **AWS Key Management Service (KMS)**
   - Create separate KMS keys for:
     - EC2
     - RDS
     - S3 Buckets
     - Secrets Manager (for database passwords and email service credentials)
   - Set a 90-day rotation period for all KMS keys.
   - Use these keys when creating resources in Terraform.

2. **Database Password Management**
   - Store the auto-generated database password in AWS Secrets Manager using a custom KMS key.
   - Retrieve the password in the user-data script to configure the web application.

3. **Endpoint Security**
   - Secure all web application endpoints with valid SSL certificates.
   - Only support HTTPS traffic.
   - Redirect HTTP traffic to HTTPS is not required.
   - Traffic between the load balancer and EC2 instances can use HTTP.
   - Direct access to EC2 instances is not allowed.

---

### Testing the `/cicd` Endpoint
- A copy of the `/healthz` endpoint is available at `/cicd`.
- After merging a pull request, the `/cicd` endpoint should be tested to ensure it is functional and deployed correctly.
