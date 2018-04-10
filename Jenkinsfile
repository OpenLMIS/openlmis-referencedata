pipeline {
    agent any
    environment {
      PATH = "/usr/local/bin/:$PATH"
    }
    stages {
        stage('Preparation') {
            steps {
                checkout scm
                
                withCredentials([usernamePassword(
                  credentialsId: "cad2f741-7b1e-4ddd-b5ca-2959d40f62c2",
                  usernameVariable: "USER",
                  passwordVariable: "PASS"
                )]) {
                    sh 'set +x'
                    sh 'docker login -u $USER -p $PASS'
                }
                script {
                    def properties = readProperties file: 'gradle.properties'
                    if (!properties.serviceVersion) {
                        error("serviceVersion property not found")
                    }
                    VERSION = properties.serviceVersion
                    VERSION_WITH_BUILD_NUMBER = properties.serviceVersion + "-build" + env.BUILD_NUMBER
                    currentBuild.displayName += " - " + VERSION
                }
            }
        }
        stage('Build') {
            steps {
                withCredentials([file(credentialsId: '8da5ba56-8ebb-4a6a-bdb5-43c9d0efb120', variable: 'ENV_FILE')]) {
                    sh 'set +x'
                    sh 'sudo rm -f .env'
                    sh 'cp $ENV_FILE .env'

                    sh 'docker-compose -f docker-compose.builder.yml run -e BUILD_NUMBER=$BUILD_NUMBER -e GIT_BRANCH=$GIT_BRANCH builder'
                    sh 'docker-compose -f docker-compose.builder.yml build image'
                    sh 'docker-compose -f docker-compose.builder.yml down --volumes'
                    sh "docker tag openlmis/referencedata:latest openlmis/referencedata:${VERSION_WITH_BUILD_NUMBER}"
                }
            }
            post {
                success {
                    archive 'build/libs/*.jar,build/resources/main/api-definition.html, build/resources/main/  version.properties'
                }
                always {
                    checkstyle pattern: '**/build/reports/checkstyle/*.xml'
                    pmd pattern: '**/build/reports/pmd/*.xml'
                    junit '**/build/test-results/*/*.xml'
                }
            }
        }
        stage('Sonar Analysis') {
            steps {
                withSonarQubeEnv('Sonar OpenLMIS') {
                    withCredentials([string(credentialsId: 'SONAR_LOGIN', variable: 'SONAR_LOGIN'), string(credentialsId: 'SONAR_PASSWORD', variable: 'SONAR_PASSWORD')]) {
                        sh '''
                            set +x
                            sudo rm -f .env
                            
                            curl -o .env -L https://raw.githubusercontent.com/OpenLMIS/openlmis-ref-distro/master/settings-sample.env
                            sed -i '' -e "s#spring_profiles_active=.*#spring_profiles_active=#" .env  2>/dev/null || true
                            sed -i '' -e "s#^BASE_URL=.*#BASE_URL=http://localhost#" .env  2>/dev/null || true
                            sed -i '' -e "s#^VIRTUAL_HOST=.*#VIRTUAL_HOST=localhost#" .env  2>/dev/null || true
                            
                            docker-compose -f docker-compose.builder.yml run sonar
                            docker-compose -f docker-compose.builder.yml down --volumes
                        '''
                        // workaround because sonar plugin retrieve the path directly from the output
                        sh 'echo "Working dir: ${WORKSPACE}/build/sonar"'
                    }
                }
                timeout(time: 1, unit: 'HOURS') {
                    script {
                        def gate = waitForQualityGate()
                        def passed = gate.status == 'OK'
                        if (!passed) {
                            error 'Quality Gate FAILED'
                        }
                    }
                }
            }
        }
        stage('Contract tests - referencedata') {
            steps {
                dir('contract-tests') {
                    git url: 'https://github.com/OpenLMIS/openlmis-contract-tests.git'
                    dir('openlmis-config') {
                        git branch: 'master',
                            credentialsId: 'OpenLMISConfigKey',
                            url: 'git@github.com:villagereach/openlmis-config.git'
                    }
                    sh "sed -i '' -e 's#^OL_REFERENCEDATA_VERSION=.*#OL_REFERENCEDATA_VERSION=${VERSION_WITH_BUILD_NUMBER}#' .env  2>/dev/null || true"
                    sh '''
                        set +x
                        
                        cp ./openlmis-config/contract_tests.env ./settings.env
                        
                        ./run_contract_tests.sh docker-compose.referencedata.yml -v
                    '''
                }
            }
            post {
                always {
                    junit healthScaleFactor: 1.0, testResults: 'contract-tests/build/cucumber/junit/**.xml'
                }
            }
        }
        stage('Contract tests - fulfillment') {
            steps {
                dir('contract-tests') {
                    sh '''
                        ./run_contract_tests.sh docker-compose.fulfillment.yml -v
                    '''
                }
            }
            post {
                always {
                    junit healthScaleFactor: 1.0, testResults: 'contract-tests/build/cucumber/junit/**.xml'
                }
            }
        }
        stage('Push image') {
            steps {
                sh "docker tag openlmis/referencedata:${VERSION_WITH_BUILD_NUMBER} openlmis/referencedata:${VERSION}"
                sh "docker push openlmis/referencedata:${VERSION}"
            }
        }
    }
    post {
        always {
            sh '''
                rm -Rf ./contract-tests/openlmis-config
                rm -f ./contract-tests/settings.env
            '''
        }
    }
}
