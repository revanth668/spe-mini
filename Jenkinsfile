pipeline {
    agent any

    environment {
        DOCKER_USER = 'revanth668hehe'            // TODO: change to your Docker Hub username
        IMAGE       = "${DOCKER_USER}/calculator"
        TAG         = "${BUILD_NUMBER}"
        DOCKER_CREDS = credentials('dockerhub-creds') // Jenkins credentials ID (username + token)
        NOTIFY_EMAIL = 'saivenkatsreekantam@gmail.com'
    }

    triggers {
        githubPush()                  // GitHub webhook; use pollSCM('H/2 * * * *') if no webhook
    }

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn -B clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn -B test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t $IMAGE:$TAG -t $IMAGE:latest .'
            }
        }

        stage('Push') {
            steps {
                sh 'echo "$DOCKER_CREDS_PSW" | docker login -u "$DOCKER_CREDS_USR" --password-stdin'
                sh 'docker push $IMAGE:$TAG'
                sh 'docker push $IMAGE:latest'
            }
        }

        stage('Deploy') {
            steps {
                sh 'DOCKER_USER=$DOCKER_USER TAG=$TAG docker compose up -d --remove-orphans'
                sh '''
                    for i in $(seq 1 20); do
                        if curl -fs http://localhost:8083/actuator/health; then
                            echo
                            echo "Deployment healthy"
                            exit 0
                        fi
                        sleep 3
                    done
                    echo "Health check failed"
                    docker compose logs --tail=50
                    exit 1
                '''
            }
        }
    }

    post {
        always {
            sh 'docker logout || true'
        }
        success {
            emailext subject: "SUCCESS: ${JOB_NAME} #${BUILD_NUMBER}",
                     body: "Build #${BUILD_NUMBER} succeeded.\n${BUILD_URL}",
                     to: "${NOTIFY_EMAIL}"
        }
        failure {
            emailext subject: "FAILED: ${JOB_NAME} #${BUILD_NUMBER}",
                     body: "Build #${BUILD_NUMBER} failed.\nConsole: ${BUILD_URL}console",
                     to: "${NOTIFY_EMAIL}"
        }
    }
}
