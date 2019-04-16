#!/usr/bin/env groovy

properties([
  [
    $class: 'ThrottleJobProperty',
    categories: ['pipeline'],
    throttleEnabled: true,
    throttleOption: 'category'
  ]
])
pipeline {
    agent none
    options {
        buildDiscarder(logRotator(numToKeepStr: '15'))
        disableConcurrentBuilds()
    }
    environment {
        COMPOSE_PROJECT_NAME = "referencedata${BRANCH_NAME}"
    }
    parameters {
        string(name: 'contractTestsBranch', defaultValue: 'master', description: 'The branch of contract tests to checkout')
    }
    stages {
        stage('Preparation') {
            agent any
            steps {
                withCredentials([usernamePassword(
                  credentialsId: "cad2f741-7b1e-4ddd-b5ca-2959d40f62c2",
                  usernameVariable: "USER",
                  passwordVariable: "PASS"
                )]) {
                    sh 'set +x'
                    sh 'docker login -u $USER -p $PASS'
                }
                script {
                    CURRENT_BRANCH = env.GIT_BRANCH // needed for agent-less stages
                    def properties = readProperties file: 'gradle.properties'
                    if (!properties.serviceVersion) {
                        error("serviceVersion property not found")
                    }
                    VERSION = properties.serviceVersion
                    STAGING_VERSION = properties.serviceVersion
                    if (CURRENT_BRANCH != 'master' || (CURRENT_BRANCH == 'master' && !VERSION.endsWith("SNAPSHOT"))) {
                        STAGING_VERSION += "-STAGING"
                    }
                    currentBuild.displayName += " - " + VERSION
                }
            }
            post {
                failure {
                    script {
                        notifyAfterFailure()
                    }
                }
            }
        }
        stage('Build') {
            agent any
            environment {
                PATH = "/usr/local/bin/:$PATH"
                STAGING_VERSION = "${STAGING_VERSION}"
            }
            steps {
                withCredentials([file(credentialsId: '8da5ba56-8ebb-4a6a-bdb5-43c9d0efb120', variable: 'ENV_FILE')]) {
                    sh( script: "./ci-buildImage.sh" )
                }
            }
            post {
                success {
                    archiveArtifacts artifacts: 'build/libs/*.jar,' +
                            'build/resources/main/api-definition.html,' +
                            'build/reports/dependency-check-report.html'
                }
                failure {
                    script {
                        notifyAfterFailure()
                    }
                }
                always {
                    checkstyle pattern: '**/build/reports/checkstyle/*.xml'
                    pmd pattern: '**/build/reports/pmd/*.xml'
                    junit '**/build/test-results/*/*.xml'
                }
            }
        }
        stage('Build demo-data') {
            when {
                expression {
                    return CURRENT_BRANCH == 'master'
                }
            }
            steps {
                build job: "OpenLMIS-3.x-build-demo-data-pipeline"
            }
            post {
                failure {
                    script {
                        notifyAfterFailure()
                    }
                }
            }
        }
        stage('Deploy to test') {
            when {
                expression {
                    return CURRENT_BRANCH == 'master' && VERSION.endsWith("SNAPSHOT")
                }
            }
            steps {
                build job: 'OpenLMIS-referencedata-deploy-to-test', wait: false
            }
            post {
                failure {
                    script {
                        notifyAfterFailure()
                    }
                }
            }
        }
        stage('ERD generation') {
            agent {
                node {
                    label 'master'
                }
            }
            environment {
                PATH = "/usr/local/bin/:$PATH"
            }
            steps {
                dir('erd') {
                    sh(script: "../ci-erdGeneration.sh")
                    archiveArtifacts artifacts: 'erd-referencedata.zip'
                }
            }
            post {
                failure {
                    script {
                        notifyAfterFailure()
                    }
                }
            }
        }
        stage('Push image') {
            agent any
            when {
                expression {
                    env.GIT_BRANCH =~ /rel-.+/ || (env.GIT_BRANCH == 'master' && !VERSION.endsWith("SNAPSHOT"))
                }
            }
            steps {
                sh "docker pull openlmis/referencedata:${STAGING_VERSION}"
                sh "docker tag openlmis/referencedata:${STAGING_VERSION} openlmis/referencedata:${VERSION}"
                sh "docker push openlmis/referencedata:${VERSION}"
            }
            post {
                success {
                    script {
                        if (!VERSION.endsWith("SNAPSHOT")) {
                            currentBuild.setKeepLog(true)
                        }
                    }
                }
                failure {
                    script {
                        notifyAfterFailure()
                    }
                }
            }
        }
    }
    post {
        fixed {
            script {
                BRANCH = "${env.GIT_BRANCH}".trim()
                if (BRANCH.equals("master") || BRANCH.startsWith("rel-")) {
                    slackSend color: 'good', message: "${env.JOB_NAME} - #${env.BUILD_NUMBER} Back to normal"
                }
            }
        }
    }
}

def notifyAfterFailure() {
    BRANCH = "${env.GIT_BRANCH}".trim()
    if (BRANCH.equals("master") || BRANCH.startsWith("rel-")) {
        slackSend color: 'danger', message: "${env.JOB_NAME} - #${env.BUILD_NUMBER} ${env.STAGE_NAME} FAILED (<${env.BUILD_URL}|Open>)"
    }
    emailext subject: "${env.JOB_NAME} - #${env.BUILD_NUMBER} ${env.STAGE_NAME} FAILED",
        body: """<p>${env.JOB_NAME} - #${env.BUILD_NUMBER} ${env.STAGE_NAME} FAILED</p><p>Check console <a href="${env.BUILD_URL}">output</a> to view the results.</p>""",
        recipientProviders: [[$class: 'CulpritsRecipientProvider'], [$class: 'DevelopersRecipientProvider']]
}
