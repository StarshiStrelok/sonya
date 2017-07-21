# Public transport web-service
Configurable public transport web-service for multiple locations. OSM / OSRM-based

## About
Web-service represent OSM-map for search public transport routes in any locality.
Allows to configure several locations simultaneously and easy switch between them.
Can search routes with restrictions by time, transfers or routes type. Also can be used for planning trips in future.

## Requirements
- one or several launched OSRM-servers for drawing routes on map. How deploy OSRM-server read [here](https://github.com/Project-OSRM/osrm-backend).
- Apache Tomcat 7+.
- Java 8
- Official transport data source. This is really need, if you want don't update information manually)

## Build
You can download one of last builds from this page or make fork, or build from source.

If you decide build from source, you will need installed node.js, gradle and [angular-cli](https://github.com/angular/angular-cli).

```
gradle clean build -Pprofile=production
```
