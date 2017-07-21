# Public transport web-service
Configurable public transport web-service for multiple locations. Based on OSM, OSRM, Angular2, Leaflet.

![image](https://github.com/StarshiStrelok/sonya/blob/master/sonya-transport-html5/src/assets/image/screenshots/0.png)

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

## Create profiles
Application concept - one locality-one transport profile.

In first you must create Administrator for future work.

After running application you will see black screen and toolbar. Entrance into admin session is hidden in language menu.
Press 'Tab' button and open 'language menu'. Then press on 'Sign In' button and register new user. Sign in after successful registration. You are in admin session now.

![image](https://github.com/StarshiStrelok/sonya/blob/master/sonya-transport-html5/src/assets/image/screenshots/1.png)

For creating new transport profile press 'Create new transport profile button' and fill form:

![image](https://github.com/StarshiStrelok/sonya/blob/master/sonya-transport-html5/src/assets/image/screenshots/2.png)

- Profile name (Locality name in English)
- Map settings. Leaflet map settings, bounds and zoom restrictions, it's very simple.
![image](https://github.com/StarshiStrelok/sonya/blob/master/sonya-transport-html5/src/assets/image/screenshots/3.png)

- Map layers. If you want using only standard OSM layer skip this section. Also has opportunity apply MapBox maps.

![image](https://github.com/StarshiStrelok/sonya/blob/master/sonya-transport-html5/src/assets/image/screenshots/4.png)

- Route types. Very important section.

  Define your route types here, for example 'Autobus', 'Metro', 'Trolleybus', 'Tram', 'Route taxi' etc.
  
  Line color - route line color, define as CSS color.
  
  OSRM service URL - see more info on OSRM project page.
  
  Average speed (in km/h) - average speed for transport, affects search algorithms and relevance of results, faster transportation rises.
  
  Bus stop marker image required! Upload own marker image, default image only for example. Available only after creating route type (save form and open again).
  
  Data parser - data parser name for this route type (Optional), about data parsing see below.

![image](https://github.com/StarshiStrelok/sonya/blob/master/sonya-transport-html5/src/assets/image/screenshots/5.png)

- Search settings

  Search using the schedule - to enable only if the schedule for all types of transport is loaded. Affects search relevance, if disable - the shortest paths will be at the top, else, if enable - the fastest in time.
 
  Access zone radius (km) - the radius inside which is looking for bus stops for transfer to another route. Recommend using 0.1-0.5 km. Affects search accuracy and search routes time, high value - slower search and high accuracy, low value - faster search, low accuracy.
 
  Bus stop limit near point (integer) - mean how many bus stops use for search near start and destination point. Recommend use 4. The higher the slower but more accurate.

## Data
Best way for import transport data - write data parser, which must be implement DataParser interface. Examples can be found here: [for Brest autobuses](https://github.com/StarshiStrelok/sonya/blob/master/sonya-transport/src/main/java/ss/sonya/transport/dataparser/brest/AutobusDP.java) and [for Minsk trams](https://github.com/StarshiStrelok/sonya/blob/master/sonya-transport/src/main/java/ss/sonya/transport/dataparser/minsk/TramDP.java).

Note that the data parser name stored in route type special field in transport profile form. One parser for one route type.

You can write own data parsers and connect it's for route types.

Do remember button 'Update data for all profiles' in main panel of admin session? All connected data parsers triggered after click on this button. Also all data parsers triggered every day in 03:00 by default and your data will be actual always.

Also data can be filled / corrected manually through admin session interface, but I advise to use data parsers way.

## Search engine
Some words about search engine.

It based on breadth-first search algorithm with restriction by depth and not optimized to end yet.

Parallel computing is critical. Calculation perform on central processor (threads count = processor cores + hyperthreading) and for big data may take a long time (several seconds for 123K graph edges on Intel Core i3 x2) if processor is weak.

## At the end

I suppose that this code may be useful for someone, but it's only experimental software.

I created it, because I live in Brest (Belarus) and here before there was no convenient application for the search for public transport routes.

For technical questions, please contact my email, starshistrelok@gmail.com
