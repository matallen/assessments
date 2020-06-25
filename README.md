# Assessment Platform


## How to install & run (in Dev Mode)

Get the source code:
```
git clone https://github.com/matallen/assessments
cd assessments
```

Start the server-side:
```
cd server
mvn quarkus:dev -Dquarkus.http.port=8081
```

Start the UI:
```
cd ui
export SERVER=http://localhost:8081
mvn quarkus:dev -Dquarkus.http.port=8080
```
note: *SERVER* tells the UI application where to talk to the back-end, so if you're deploying on another platform, this url will need to be configured.



## Dependencies

* Maven v3.6.2
* Java JDK (11+ preferably, but not essential yet)

## App Dependencies

* Resteasy
* Resteasy-jackson
* Swagger
* Jwt security

