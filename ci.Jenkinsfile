pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Run CI') {
            steps {
                script {
                    echo "Compiling and running tests for sigeca-api:"
                    sh "chmod +x ./compile-then-tests.sh"
                    sh "sh ./compile-then-tests.sh"
                }
            }
        }
    }
    post {
        always {
            cleanWs()
        }
        success {
            echo "SUCCESS: Compilation finished and tests passed."
        }
        failure {
            echo "FAILED: Check logs above for details."
        }
    }
}