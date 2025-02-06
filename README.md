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
