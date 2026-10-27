# Campus Equipment Booking

A Spring Boot application for booking campus equipment. This repository includes booking-validation fixes, automated tests, Docker packaging, Kubernetes deployment manifests, and a Jenkins CI/CD pipeline.

## 1. Prerequisites

Install or configure:

- Java 17
- Apache Maven 3.8.7 or compatible
- Docker
- `kubectl`
- Minikube (for local Kubernetes deployment)
- Git

The Jenkins pipeline also requires Jenkins with access to Docker and the Kubernetes cluster.

## 2. Setup and execution

Run these commands from the project root.

### Build and test

```bash
mvn clean test
```

This cleans previous build output, compiles the application and tests, and runs the automated tests.

### Package the application

```bash
mvn package -DskipTests
```

The executable Spring Boot JAR is generated at:

```text
target/campus-equipment-booking-1.0.0.jar
```

To build and test in one command, use:

```bash
mvn clean package
```

### Run the application locally

```bash
java -jar target/campus-equipment-booking-1.0.0.jar
```

The application listens on port `8080` by default. In another terminal, verify health and the equipment API:

```bash
curl -i http://localhost:8080/actuator/health
curl -i http://localhost:8080/api/equipment
```

The health endpoint should return HTTP 200 with a status of `UP`.

## 3. Git evidence

### Feature branch

- Feature branch: `feature/booking-validation`
- Integration branch: `main`
- Remote repository: `https://github.com/pb010-temp/Campus-Equipment-Booking`

### Relevant commit history

The following relevant commits were recorded during the work:

| Commit | Summary |
|---|---|
| `5d6958d` | Initialise CIA-2 project |
| `3126278` | Fix booking capacity defect (Task 3) |
| `bff6453` | Add three booking-validation tests |
| `95f2a4a` | Complete Task 4: restrict booking quantity to two units |
| `c8599f3` | Complete Task 6: Kubernetes deployment |
| Latest pipeline-fix commits | Jenkins pipeline and Minikube image-import adjustments; see `git log` for current hashes and messages |

To inspect the current branch and history:

```bash
git branch -a
git log --oneline --decorate -10
git status
```

The booking-validation feature branch was pushed and merged into `main`. The current `main` branch is used by the Jenkins pipeline.

## 4. Maven build and test evidence

Commands used by the CI pipeline:

```bash
mvn clean test
mvn package -DskipTests
```

Test evidence from the completed test run:

- Active automated tests: **11**
- Failures: **0**
- Errors: **0**
- Test result: **passed**
- Package result: **BUILD SUCCESS**
- Executable artifact: `target/campus-equipment-booking-1.0.0.jar`

`-DskipTests` is used for the packaging stage because the pipeline runs tests in the preceding `Build and Test` stage. To run tests and package in a single local command, use `mvn clean package`.

To see the exact test counts on the current checkout, run:

```bash
mvn clean test
```

Maven writes detailed test reports under `target/surefire-reports/`.

## 5. Docker evidence

### Dockerfile

The root `Dockerfile` uses `eclipse-temurin:17-jre`, sets `/app` as the working directory, copies the packaged JAR to `app.jar`, exposes port `8080`, and starts the application with `java -jar app.jar`.

```dockerfile
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/campus-equipment-booking-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Build and run

Package the JAR first, then build the image:

```bash
mvn package -DskipTests
docker build -t campus-equipment-booking:set-a .
```

Run the container on host port `8081` (port `8080` is used by Jenkins on the host in this setup):

```bash
docker run -d --name campus-equipment-booking \
  -p 8081:8080 campus-equipment-booking:set-a
```

If a container with that name already exists, stop and remove it before running the command again:

```bash
docker rm -f campus-equipment-booking
```

### Health and API verification

```bash
curl -i http://localhost:8081/actuator/health
curl -i http://localhost:8081/api/equipment
```

Observed verification: both endpoints returned HTTP 200; the health endpoint returned a JSON status of `UP`.

## 6. Kubernetes evidence

### Deployment manifest

`deployment.yaml` defines a Deployment named `campus-equipment-booking` with two replicas, image `campus-equipment-booking:set-a`, container port `8080`, and readiness/liveness probes against `/actuator/health`. The image pull policy is `IfNotPresent`, appropriate for the locally loaded Minikube image.

### Service manifest

`service.yaml` defines a NodePort Service named `campus-equipment-booking`:

- Service port: `8080`
- Target port: `8080`
- NodePort: `30081`

### Deploy

Start Minikube and make the locally built image available to its container runtime:

```bash
minikube start --driver=docker
mvn package -DskipTests
docker build -t campus-equipment-booking:set-a .
minikube image load campus-equipment-booking:set-a
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml
kubectl rollout status deployment/campus-equipment-booking --timeout=180s
```

If using the Jenkins pipeline, its image-import stage transfers the image into the Minikube container runtime before applying the manifests.

### Inspect and verify

```bash
kubectl get deployments
kubectl get pods
kubectl get services
kubectl describe deployment campus-equipment-booking
```

The expected application Deployment state is `2/2` ready replicas. The Service should show `8080:30081/TCP`.

The Minikube service URL can be obtained with:

```bash
minikube service campus-equipment-booking --url
```

Use the returned URL to check the application:

```bash
curl -i <MINIKUBE_SERVICE_URL>/actuator/health
curl -i <MINIKUBE_SERVICE_URL>/api/equipment
```

Observed evidence: the health endpoint and `/api/equipment` returned HTTP 200 when accessed through the Minikube NodePort. The Jenkins pipeline also ran a Kubernetes in-cluster health check and received `{"status":"UP","groups":["liveness","readiness"]}`.

### Scaling and self-healing

Scaling was tested manually by increasing the Deployment to three replicas, deleting a pod, and observing Kubernetes create a replacement pod.

```bash
kubectl scale deployment/campus-equipment-booking --replicas=3
kubectl get pods -w
```

In a separate terminal, delete one pod belonging to the application Deployment (identify its current name with `kubectl get pods`):

```bash
kubectl delete pod <APPLICATION_POD_NAME>
kubectl get pods -w
```

Kubernetes' Deployment/ReplicaSet controller creates a replacement to restore the desired replica count. The submitted `deployment.yaml` specifies two replicas, so applying it again restores the declared desired count to two.

## 7. Jenkins CI/CD and GitHub webhook

The Jenkins job is named `Campus-Equipment-Booking-CI-CD` and uses the `main` branch and root `Jenkinsfile`.

Pipeline stages:

1. Checkout
2. Build and Test
3. Package
4. Build Docker Image
5. Load Image into Minikube
6. Deploy to Kubernetes
7. Verify Deployment

A completed pipeline run reported `Finished: SUCCESS`. The deployment rollout succeeded, and the in-cluster health check returned `UP`.

A GitHub push webhook was configured to notify Jenkins at its `/github-webhook/` endpoint. The GitHub webhook's last delivery was reported as successful. The ngrok tunnel must remain running for GitHub to reach a locally hosted Jenkins instance; its public URL can change after restarting ngrok, in which case the webhook URL must be updated.

## 8. Change summary and limitations

### Defect correction and change request

- Corrected the booking-capacity validation defect (Task 3).
- Added three booking-validation tests.
- Enforced the Task 4 limit of no more than two units per booking.
- Added Docker packaging and Kubernetes Deployment/Service configuration.
- Added a Jenkins pipeline for build, test, image creation, deployment, and health verification.

### Modified files

Files associated with the implementation include:

- `BookingService.java` — booking validation/capacity logic.
- `BookingServiceTest.java` — booking-validation tests.
- `Dockerfile` — container image definition.
- `deployment.yaml` — Kubernetes Deployment, replicas, image, and health probes.
- `service.yaml` — Kubernetes NodePort Service.
- `Jenkinsfile` — CI/CD pipeline.
- `README.md` — setup, execution, and evidence documentation.

Check the actual changed-file list for the submitted branch with:

```bash
git diff --name-only 5d6958d..HEAD
```

### Known limitations

- The local Kubernetes workflow depends on Minikube and a locally available Docker image; it is not a production registry-based deployment.
- The GitHub webhook relies on the ngrok tunnel while Jenkins runs locally. The tunnel must remain active, and a changed public URL must be reflected in GitHub webhook settings.
- The Kubernetes Service uses a NodePort for local testing rather than a production ingress/load-balancer setup.
- The two-replica count is the manifest's declared default; manual scaling to three replicas is a separate runtime test and is not persisted in `deployment.yaml`.
- The evidence above describes the observed local run. Re-run the commands on the final submitted commit if fresh evidence is required.

## 9. Final verification checklist

Before submission, run:

```bash
git status
git log --oneline --decorate -10
mvn clean test
mvn package -DskipTests
kubectl get deployments
kubectl get pods
kubectl get services
```

Confirm that the latest Jenkins build is successful and that the working tree contains only intended changes.
