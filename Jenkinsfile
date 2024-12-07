pipeline {
    agent any

    environment {
        IMAGE_NAME = "been980804/wibee-admin"
        DEPLOYMENT_REPO = 'https://github.com/Mi-Ss-A/wibeechat-argocd-config'
        TAG = "test-${BUILD_NUMBER}"
        DOCKER_IMAGE = "${IMAGE_NAME}:${TAG}"
    }

    stages {
        stage('Checkout Source') {
            steps {
                checkout scm
            }
        }

        stage('Build Docker image & Push') {
            steps {
                script {
                    docker.withRegistry("https://registry.hub.docker.com", 'docker-token') {
                        def app = docker.build("${DOCKER_IMAGE}", "-f Dockerfile .")
                        app.push()
                    }
                }
            }
        }
    }
}
