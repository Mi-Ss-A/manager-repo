pipeline {
    agent any

    environment {
        IMAGE_NAME = "been980804/wibee-admin"
        DEPLOYMENT_REPO = 'https://github.com/Mi-Ss-A/wibeechat-argocd-config'
        GIT_CREDENTIALS = credentials('git-token')
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

        stage('Delete Older Images') {
            steps {
                script {
                    sh '''
                    # 현재 빌드된 이미지와 직전 버전 태그 계산
                    IMAGE_NAME="${IMAGE_NAME}"
                    CURRENT_TAG="${TAG}"
                    PREVIOUS_TAG="test-$(( ${BUILD_NUMBER} - 1 ))"

                    echo "현재 유지할 이미지 태그: ${CURRENT_TAG}, ${PREVIOUS_TAG}"

                    # 모든 이미지 목록 가져오기
                    docker images --format '{{.Repository}}:{{.Tag}}' | grep "$IMAGE_NAME" | while read -r IMAGE; do
                        # 유지할 태그인지 확인
                        if [[ "$IMAGE" == *:$CURRENT_TAG || "$IMAGE" == *:$PREVIOUS_TAG ]]; then
                            echo "유지할 이미지: $IMAGE"
                        else
                            echo "삭제할 이미지: $IMAGE"
                            docker rmi -f "$IMAGE" || echo "이미지 삭제 실패: $IMAGE"
                        fi
                    done
                    '''
                }
            }
        }

        stage('Update K8S Manifest') {
            steps {
                dir('k8s-manifest') {
                    git url: DEPLOYMENT_REPO, branch: 'test', credentialsId: 'git-token'
                    sh '''
                    sed -i "s|image: .*$|image: ${DOCKER_IMAGE}|" admin/deployment.yaml
                    git config user.name "been980804"
                    git config user.email "dlgusqls980804@naver.com"
                    git commit -am "Update usersvc image to ${DOCKER_IMAGE}"
                    git push https://${GIT_CREDENTIALS_USR}:${GIT_CREDENTIALS_PSW}@github.com/Mi-Ss-A/wibeechat-argocd-config test
                    '''
                }
            }
        }
    }
}
