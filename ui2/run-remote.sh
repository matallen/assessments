export SERVER=http://`hostname -I | awk '{print $1}'`:8081
#export SERVER=http://localhost:8081
mvn quarkus:dev -Dquarkus.http.port=8080
