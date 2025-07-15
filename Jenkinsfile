pipeline {
    agent any

    options {
        skipDefaultCheckout(true)
    }
    
    environment {
        IMAGE_NAME = 'ssafysong/inside-movie'
        TAG = 'be'
        CONTAINER_NAME = 'backend'
        DOCKER_CREDENTIALS_ID = 'movie'
    }

    stages {
        stage('Build') {
            steps {
                checkout scm
                sh './gradlew build -x test'
            }
        }

        stage('Docker Build') {
            steps {
                sh "docker build -t ${IMAGE_NAME}:${TAG} ."
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
                        docker pull ${IMAGE_NAME}:${TAG} &&
                        docker stop ${CONTAINER_NAME} || true &&
                        docker rm ${CONTAINER_NAME} || true &&
                        docker run -d -p 8080:8080 --name ${CONTAINER_NAME} ${IMAGE_NAME}:${TAG}
                    "
                    """
                }
            }
        }
    }
}
