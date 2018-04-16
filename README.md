
# Conatiner Based Development
Log in to https://labs.play-with-docker.com/
## Simple App
```sh
alias build-machine='docker run --rm -it -v $PWD:/my -w /my maven:3-jdk-9-slim'
build-machine bash
# inside the container
mvn -q archetype:generate # filter: jdk9, item #1, group: grp, artifactId:yourname-jdk
cd yourname-jdk
mvn -q package
java -jar target/yourname-jdk-1.0-SNAPSHOT.jar
# close the container ^d
docker build -t test yourname-jdk
docker run --rm test
```

## REST Service
```sh
alias build-machine='docker run --rm -it -v $PWD:/my -w /my maven:3-jdk-9-slim'
build-machine mvn -q archetype:generate
# filter: dropwizard-app, item #1, group: grp, artifactId:yourname-dw
cd yourname-dw
build-machine mvn -q package
docker build -t test .
docker run --rm -p 8080:8080 test 
```
### Using docker compose
```sh
docker-compose up -d
# verify the services are up using
docker-compose ps
```
### Add DB
Uncomment db definition in docker-compose.yml. Repeat last step.
Shutdown the cluster using ``docker-compose down``. Launch it again and verify that the data is there.
### Scale
Try scaling using ``docker-compose scale yourname-dw=3``. Doesn't work.

Edit the docker-compose.yml, removing 8081, 3306 ports, and remove mapping for 8080.
```yml
version: "3"
services:
  yourname-dw:
    build: .
    image: yourname-dw:1.0-SNAPSHOT
    ports:
      - "8080"
    environment:
      JDBC_DRIVER: "com.mysql.cj.jdbc.Driver"
      JDBC_URL: "jdbc:mysql://db:3306/mydb?useSSL=false"

  db:
    image: mysql
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_USER: root
      MYSQL_PASSWORD: 123456
      MYSQL_DATABASE: mydb
    volumes:
      - yourname-dw-volume:/var/lib/mysql

volumes:
  yourname-dw-volume
```

run:
```sh
docker-compose down
docker-compose up -d --scale yourname-dw=3
```
## Swarm
### Single Node Swarm
```sh
docker swarm init --advertise-addr $YOUR_IP
wget https://eitan101.github.io/containers/manifests-examples/myapp/docker-compose.yml
docker deploy -c docker-compose.yml my
docker service ls
docker service scale my_myapp=3
# open http://{host_ip}:8080
docker service ls
docker service logs -f my_myapp
docker stack rm my
```

### Swarm Cluster
In the ``play with docker`` site:

*  Delete your instance
*  Press the wrench icon
*  Choose 2 Managers and 3 workers template.

From the ``manager1`` instance repeat the previous scenario, skipping the ``swarm init`` command.

### Run on single node with local images

```sh
alias build-machine='docker run --rm -it -v $PWD:/my -w /my maven:3-jdk-9-slim'
build-machine mvn -q archetype:generate
# filter: dropwizard-app, item #1, group: grp, artifactId:myapp
cd myapp
build-machine mvn -q package
docker-compose build
docker swarm init --advertise-addr $YOUR_IP
docker deploy -c docker-compose.yml my
docker service ls
docker service scale my_myapp=3
# open http://{host_ip}:8080
docker service ls
docker service logs -f my_myapp
docker stack rm my
```

## Kubernetes

### Prerequisites
[Signup to Katacoda](https://katacoda.com)

### Minikube
Scenario can be found [here](https://www.katacoda.com/eitan101/scenarios/minikube-1)

### Minikube with local images
Scenario can be found [here](https://www.katacoda.com/eitan101/scenarios/minikube-2)

### Run minikube on your device locally

Install minikube.

```sh
alias build-machine='docker run --rm -it -v $PWD:/my -w /my maven:3-jdk-9-slim'
build-machine mvn -q archetype:generate
# filter: dropwizard-app, item #1, group: grp, artifactId:myapp
# or: git clone https://github.com/eitan101/containers.git && cd containers
cd myapp
build-machine mvn -q package
minikube start
eval $(minikube docker-env)
docker-compose build
kubectl apply -f k8s.yml
watch kubectl get pods
minikube service myapp
# update app "replicas: 1" to 3
kubectl apply -f k8s.yml
# minikube stop/start - to test data persistancy
kubectl delete -f k8s.yml
```

## Kafka Clients

### Using Swarm

```sh
#docker swarm init --advertise-addr $YOUR_IP
wget https://eitan101.github.io/containers/manifests-examples/mykafka/docker-compose.yml
docker deploy -c docker-compose.yml my
watch docker service ls
docker service logs -f my_mykafka-consumer
docker service scale my_mykafka-consumer=3
docker service logs -f my_mykafka-consumer
docker stack rm mystack
```

### Using Swarm with local images

```sh
alias build-machine='docker run --rm -it -v $PWD:/my -w /my maven:3-jdk-9-slim'
build-machine mvn -q archetype:generate -Dfilter=kafka-app
cd mykafka/
build-machine mvn -q package
docker-compose build
docker deploy -c docker-compose.yml mystack
watch docker service ls
docker service logs -f mystack_mykafka-consumer
docker service scale mystack_mykafka-consumer=3
docker service logs -f mystack_mykafka-consumer
docker stack rm mystack
```

### Using minikube
Scenario can be found [here](https://www.katacoda.com/eitan101/scenarios/minikube-1)

### Minikube with local images
Scenario can be found [here](https://www.katacoda.com/eitan101/scenarios/minikube-2)

### Run minikube on your device locally

Install minikube.

```sh
alias build-machine='docker run --rm -it -v $PWD:/my -w /my maven:3-jdk-9-slim'
build-machine mvn -q archetype:generate -Dfilter=kafka-app
# filter: kafka-app, item #1, group: grp, artifactId:mykafka
# or: git clone https://github.com/eitan101/containers.git && cd containers
cd mykafka
docker run --rm -it -v $PWD:/my -w /my maven:3-jdk-9-slim mvn -q package
minikube start
eval $(minikube docker-env)
docker-compose build
kubectl apply -f k8s.yml
watch kubectl get deployment
kubectl get pod
kubectl logs -f $pod
```





