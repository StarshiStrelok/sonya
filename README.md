# Public transport web-service
Configurable public transport web-service for multiple locations. OSM / OSRM-based

## About
Web-service represent OSM-map for search public transport routes in any locality.
Allows to configure several locations simultaneously and easy switch between them.
Can search routes with restrictions by time, transfers or routes type. Also can be used for planning trips in future.

## Requirements
- one or several launched OSRM-servers for drawing routes on map. How deploy OSRM-server read [here](https://github.com/Project-OSRM/osrm-backend).
- Apache Tomcat 7+
- Java 8
- MySQL 5.5+ (app using JPA, can choose any database)
- Official transport data source. This is really need, if you want don't update information manually)

## Build
You can download one of last builds from this page or make fork, or build from source.

If you decide build from source, you will need installed node.js, gradle and [angular-cli](https://github.com/angular/angular-cli).

```
gradle clean build -Pprofile=production
```

## Deploy
- Put sonya.war into Tomcat deploy directory. Important, angular 2 used <base href="/"> tag for navigation, and application must be deployed under ROOT context.
- Create sonya.properties file into $CATALINA_HOME/conf directory with content:

```
# Data Source properties
ds_driver=com.mysql.cj.jdbc.Driver
ds_url=jdbc:mysql://localhost:3306/example?characterEncoding=utf-8
ds_user=user
ds_password=password

# Hibernate properties
hibernate.show_sql=false
hibernate.dialect=org.hibernate.dialect.MySQL5InnoDBDialect
hibernate.hbm2ddl.auto=update

# c3p0 properties
hibernate.c3p0.min_size=1
hibernate.c3p0.max_size=300
hibernate.c3p0.timeout=60
hibernate.c3p0.max_statements=0
```

- Run Tomcat
