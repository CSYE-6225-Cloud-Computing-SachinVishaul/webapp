# webapp
# Health Check API

## Build and Deploy in Spring Tool Suite (STS)

### Steps:
1. **Clone the Repository**:  
   Go to `File > Import > Git > Projects from Git` in STS and clone the repository URL.

2. **Configure Database**:  
   Update `application.properties` with your database credentials:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/health_db
   spring.datasource.username=<username>
   spring.datasource.password=<password>
   ```

3. **Build the Application**:  
   Right-click the project > `Run As > Maven clean` and then `Maven install`.

4. **Run the Application**:  
   Right-click the project > `Run As > Spring Boot App`.

5. **Test the API**:  
   Access the `/healthz` endpoint:  
   ```
   GET http://localhost:8080/healthz
   
