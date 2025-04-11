def extractNumbers(inputStr) {
    def numbers = []
    def temp = ""

    inputStr.each {
        if (Character.isDigit(it as char)) {
            temp += it
        } else if (temp) {
            numbers.add(temp.toLong())
            temp = ""
        }
    }

    if (temp) {
        numbers.add(temp.toLong())
    }

    return numbers.takeRight(12)
}

def calculateCoverage(missed, covered) {
    def total = missed + covered
    def coverage = (total != 0) ? (covered * 100 / total) as int : 100
    return coverage
}

pipeline {
    agent any

    tools {
        maven 'Maven 3.8.7' 
    }

    environment {
        MIN_COVERAGE = 70  
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    checkout scm            
                    sh 'git fetch origin main:refs/remotes/origin/main'
                    def changes = sh(
                        script: "git diff --name-only origin/main...HEAD",
                        returnStdout: true
                    ).trim().split("\n")
                    echo "Changes detected: ${changes.join(', ')}"
        
                    if (changes.any { it.startsWith('spring-petclinic-vets-service/') }) {
                        env.SERVICE = 'vets-service'
                    } else if (changes.any { it.startsWith('spring-petclinic-customers-service/') }) {
                        env.SERVICE = 'customers-service'
                    } else if (changes.any { it.startsWith('spring-petclinic-visits-service/') }) {
                        env.SERVICE = 'visits-service'
                    }
                     else if (changes.any { it == 'pom.xml' || it == 'Jenkinsfile' }) {
                        env.SERVICE = 'all-services'
                    } else {
                        env.SERVICE = ''
                    }
        
                    if (env.SERVICE == '') {
                        currentBuild.result = 'SUCCESS'
                        echo "No relevant changes detected. Skipping build and tests."
                    } else if (env.SERVICE == 'all-services') {
                        echo "Changes in root directory. Building and testing all services."
                    } else {
                        echo "🔧 Changes in ${env.SERVICE}. Proceeding with build and tests."
                    }
                }
            }
        }

        stage('Test') {
            when {
                expression { return env.SERVICE?.trim() }
            }
            steps {
                script {
                    def serviceDir = "spring-petclinic-${env.SERVICE}"
                    def jacocoXmlPath = "${serviceDir}/target/site/jacoco/jacoco.xml"
        
                    dir(serviceDir) {
                        echo "🧪 Running tests for ${env.SERVICE}..."
                        sh 'mvn verify'
                    }
        
                    echo "Removing DOCTYPE from Jacoco report..."
                    sh "sed -i 's/<!DOCTYPE[^>]*>//' ${jacocoXmlPath}"
        
                    def jacocoContent = readFile(jacocoXmlPath)
                    def number = extractNumbers(jacocoContent)
        
                    def keys = ['Instruction', 'Branch', 'Line', 'Complexity', 'Method', 'Class']
                    def total = 0
                    keys.eachWithIndex { key, i ->
                        def missed = number[i * 2], covered = number[i * 2 + 1]
                        def cov = calculateCoverage(missed, covered)
                        total += cov
                        echo "${key} coverage: ${cov}%"
                    }
        
                    def coverage = total / keys.size() as int
                    echo "Average Coverage: ${coverage}%"
        
                    echo "Publishing coverage report for ${env.SERVICE}..."
                    jacoco execPattern: "${serviceDir}/target/jacoco.exec",
                           classPattern: "${serviceDir}/target/classes",
                           sourcePattern: "${serviceDir}/src/main/java",
                           inclusionPattern: '**/*.class',
                           exclusionPattern: '**/*Test*'

                    if (coverage < env.MIN_COVERAGE.toInteger()) {
                        error "Coverage below ${env.MIN_COVERAGE}%. Failing build for ${env.SERVICE}."
                    }
                }
            }
        }

        stage('Build') {
            when {
                expression { return env.SERVICE?.trim() }
            }
            steps {
                script {
                    def serviceDir = "spring-petclinic-${env.SERVICE}"
                    dir(serviceDir) {
                        echo "Building ${env.SERVICE}..."
                        sh 'mvn package -DskipTests'
                    }
                }
            }
        }
    }

    post {
        success {
            echo '✅ Build, test, and coverage passed.'
        }
        failure {
            echo '❌ Pipeline failed.'
        }
    }
}
