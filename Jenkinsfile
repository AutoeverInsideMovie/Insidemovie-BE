pipeline {
    agent any

    environment {
        IMAGE_NAME = 'ssafysong/inside-movie'
        TAG = 'be'
        CONTAINER_NAME = 'backend'
        DOCKER_CREDENTIALS_ID = 'movie'
        GRADLE_IMAGE = 'gradle:8.5-jdk17'
    }

    stages {
        stage('Clean Workspace') {
            steps {
                cleanWs()
            }
        }

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                script {
                    docker.image("${GRADLE_IMAGE}").inside {
                        sh './gradlew clean build -x test --no-build-cache'
                    }
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh "docker build --no-cache -t ${IMAGE_NAME}:${TAG} ."
            }
        }

        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: "${DOCKER_CREDENTIALS_ID}",
                    usernameVariable: 'DOCKER_USERNAME',
                    passwordVariable: 'DOCKER_PASSWORD'
                )]) {
                    sh """
                    echo \$DOCKER_PASSWORD | docker login -u \$DOCKER_USERNAME --password-stdin
                    docker push ${IMAGE_NAME}:${TAG}
                    """
                }
            }
        }

        stage('Deploy to EC2') {
            steps {
                sshagent(['movie_SSH']) {
                    sh """
                    ssh -o StrictHostKeyChecking=no ubuntu@52.79.175.149 "
                        docker-compose pull
                        docker-compose down &&
                        docker-compose up -d
                    "
                    """
                }
            }
        }

        stage('Cleanup') {
            steps {
                sh '''
                docker container prune -f
                docker image prune -f
                '''
            }
        }
    }
}
