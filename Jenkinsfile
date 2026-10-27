pipeline {
    agent any

    environment {
        IMAGE_NAME = 'campus-equipment-booking:set-a'
        KUBECONFIG = '/var/lib/jenkins/.kube/config'
        DEPLOYMENT_NAME = 'campus-equipment-booking'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/pb010-temp/Campus-Equipment-Booking.git'
            }
        }

        stage('Build and Test') {
            steps {
                sh 'mvn clean test'
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker build -t $IMAGE_NAME .'
            }
        }

        stage('Load Image into Minikube') {
            steps {
                sh '''
                    docker save $IMAGE_NAME | docker exec -i minikube ctr -n k8s.io images import -
                '''
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh 'kubectl apply -f deployment.yaml'
                sh 'kubectl apply -f service.yaml'
                sh 'kubectl rollout status deployment/$DEPLOYMENT_NAME --timeout=180s'
            }
        }

        stage('Verify Deployment') {
            steps {
                sh 'kubectl get deployments'
                sh 'kubectl get pods'
                sh 'kubectl get services'
                sh '''
                    kubectl run health-check --rm -i --restart=Never \
                      --image=curlimages/curl:8.12.1 \
                      -- curl -fsS http://campus-equipment-booking:8080/actuator/health
                '''
            }
        }
    }

    post {
        success {
            echo 'CI/CD pipeline completed successfully.'
        }
        failure {
            echo 'CI/CD pipeline failed. Check the stage logs.'
        }
    }
}