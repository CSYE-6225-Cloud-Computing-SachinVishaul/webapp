# Automated Application Setup and API Testing

This repository contains a Spring Boot web application along with a shell script (`script.sh`) that automates the application setup on an Ubuntu 24.04 LTS server. In addition, the repository includes an API testing suite implemented using [REST Assured](https://rest-assured.io/) (or a similar framework) to validate both success and failure scenarios of the application's API.

## Table of Contents

- [Automated Application Setup and API Testing](#automated-application-setup-and-api-testing)
  - [Table of Contents](#table-of-contents)
  - [Overview](#overview)
  - [Prerequisites](#prerequisites)
  - [Setup and Installation](#setup-and-installation)
    - [What the Script Does](#what-the-script-does)
    - [Running the Setup Script](#running-the-setup-script)
  - [Continuous Integration](#continuous-integration)
    - [Workflow Details](#workflow-details)

## Overview

This project automates the complete setup of the web application on an Ubuntu server. The automated setup script performs the following tasks:

- Updates the package lists and upgrades installed packages.
- Installs the RDBMS (MySQL, PostgreSQL, or MariaDB) as per configuration.
- Creates the required database in the chosen RDBMS.
- Creates a dedicated Linux group and user for the application.
- Unzips the application files into the `/opt/csye6225` directory.
- Updates the permissions of the directory and its contents.
- Starts the Spring Boot application after setup is complete.

In addition, an API testing suite validates the application's REST endpoints under various scenarios. These tests are located in the `tests` folder.

## Prerequisites

- Ubuntu 24.04 LTS (or a compatible Ubuntu version)
- Sudo privileges on the server
- Internet access (for downloading packages and dependencies)
- For API testing:
  - Java JDK (version 11 or higher)
  - Maven (for building and running tests)
  - REST Assured (or your chosen API testing framework)

## Setup and Installation

The setup process is automated through a shell script named `script.sh` included in this repository.

### What the Script Does

1. **Update and Upgrade:**  
   Updates the package lists and upgrades the installed packages.
   
2. **Install RDBMS:**  
   Installs MYSQL and starts the service.
   
3. **Database Setup:**  
   Creates the required database in the RDBMS.
   
4. **User and Group Creation:**  
   Creates a new Linux group and a new user for running the application.
   
5. **Application Deployment:**  
   Unzips the application into the `/opt/csye6225` directory and sets proper permissions.
   
6. **Application Launch:**  
   Runs the Spring Boot application.

### Running the Setup Script

1. Ensure that you have the required privileges.  
2. Place the application ZIP file (if not already present) in the appropriate location.
3. Execute the script with:

   ```bash
   sudo ./script.sh


## Continuous Integration

This project leverages GitHub Actions to ensure code quality and application stability through continuous integration. The CI workflow is defined in the `webapp-ci.yml` file and is automatically triggered on pull requests across all branches.

### Workflow Details

**Workflow Name:**  
The CI workflow is named **Spring Boot CI**.

**Trigger:**  
The workflow runs on every pull request regardless of the branch, ensuring that all incoming changes are validated.

**Build Environment:**

- **Runner:**  
  The job executes on the `ubuntu-latest` virtual environment.
- **Java Setup:**  
  Java 17 is configured using the Temurin distribution.

**Database Service Configuration:**  
A MySQL 8.0 service is set up to mimic a production-like environment:
- **Container Image:**  
  `mysql:8.0`
- **Environment Variables:**
  - `MYSQL_DATABASE` is set to `healthcheckdb`.
  - `MYSQL_ROOT_PASSWORD` is securely provided via repository secrets.
- **Port Mapping:**  
  The container’s port 3306 is mapped to the host, making it accessible during the build.
- **Health Check:**  
  The service includes a health check command (`mysqladmin ping --silent`) with defined intervals, timeouts, and retries to ensure that the MySQL service is running properly.

**Environment Variables for the Build:**  
The workflow also defines environment variables for database connectivity:
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`  
These are all sourced from repository secrets and are used by the application to connect to the MySQL service.

**Build Steps:**

1. **Checkout Code:**  
   Retrieves the latest code from the repository.
2. **Set Up Java 17:**  
   Configures the environment with Java 17.
3. **Cache Maven Packages:**  
   Caches Maven dependencies to improve build performance.
4. **Run Maven Tests:**  
   Executes the test suite using Maven to validate the application’s functionality.

This CI configuration helps catch issues early in the development process by running a complete test suite on every pull request, ensuring that only well-tested code is merged.

