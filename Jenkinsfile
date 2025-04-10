pipeline {
    agent any

    tools {
        maven 'Maven 3.8.7'  // Đảm bảo Maven được cài sẵn trên Jenkins
    }

    environment {
        MIN_COVERAGE = 70  // Đặt giá trị độ phủ tối thiểu là 70%
    }

    stages {
        // Checkout mã nguồn từ Git
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        // Kiểm tra sự thay đổi trong các thư mục dịch vụ và xác định dịch vụ cần build
        stage('Check Changes') {
            steps {
                script {
                    // Fetch đầy đủ nhánh main để tạo reference local cho origin/main
                    sh 'git fetch origin main:refs/remotes/origin/main'
        
                    // Lấy danh sách file thay đổi
                    def changes = sh(
                        script: "git diff --name-only origin/main...HEAD",
                        returnStdout: true
                    ).trim().split("\n")
        
                    echo "Changes detected: ${changes.join(', ')}"
        
                    // Xác định SERVICE
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
        
                    // Xuất thông báo tương ứng
                    if (env.SERVICE == '') {
                        currentBuild.result = 'SUCCESS'
                        echo "✅ No relevant changes detected. Skipping build and tests."
                    } else if (env.SERVICE == 'all-services') {
                        echo "🔁 Changes in root directory. Building and testing all services."
                    } else {
                        echo "🔧 Changes in ${env.SERVICE}. Proceeding with build and tests."
                    }
                }
            }
        }



        // Test cho dịch vụ đã thay đổi
        stage('Test') {
            when {
                expression { return env.SERVICE?.trim() }
            }
            steps {
                script {
                    def serviceDir = "spring-petclinic-${env.SERVICE}"
                    dir(serviceDir) {
                        if (!fileExists('pom.xml')) {
                            error "❌ pom.xml not found in ${serviceDir}. Skipping tests."
                        }
                        echo "Running tests for ${env.SERVICE}..."
                        sh 'mvn verify'  // Chạy test với Maven
                    }
                }
            }
        }

        stage('Remove Jacoco DOCTYPE') {
            when {
                expression { return env.SERVICE?.trim() }
            }
            steps {
                script {
                    def jacocoXmlPath = "spring-petclinic-${env.SERVICE}/target/site/jacoco/jacoco.xml"
                    echo "🔍 Checking for Jacoco file: ${jacocoXmlPath}"
                    if (fileExists(jacocoXmlPath)) {
                        echo "🧹 Removing DOCTYPE from Jacoco report..."
                        sh "sed -i 's/<!DOCTYPE[^>]*>//' ${jacocoXmlPath}"
                    } else {
                        error "❌ Jacoco report not found at ${jacocoXmlPath}"
                    }
                }
            }
        }

        // Kiểm tra độ phủ test
        stage('Check Coverage') {
            when {
                expression { return env.SERVICE?.trim() }
            }
            steps {
                script {
                    def coverageFile = "${env.WORKSPACE}/spring-petclinic-${env.SERVICE}/target/site/jacoco/jacoco.xml"
                    def coverage = 0
        
                    if (fileExists(coverageFile)) {
                    def jacoco = new XmlSlurper().parse(new File(coverageFile))
                    def instructionCounter
                    for (c in jacoco.counter) {
                        if (c.attributes()['type'] == 'INSTRUCTION') {
                            instructionCounter = c
                            break
                        }
                    }
                    def covered = instructionCounter.covered.toInteger()
                    def missed = instructionCounter.missed.toInteger()
                    def coverage = covered * 100 / (covered + missed)

                    echo "📊 Test coverage: ${coverage}%"
                } else {
                    error "❌ Coverage file not found for ${env.SERVICE}."
                }

                    if (coverage < env.MIN_COVERAGE.toInteger()) {
                        error "❌ Coverage below ${env.MIN_COVERAGE}%. Failing build for ${env.SERVICE}."
                    }
                }
            }
        }

        // Publish báo cáo coverage (JaCoCo)
        stage('Publish Coverage Report') {
            when {
                expression { return env.SERVICE?.trim() }
            }
            steps {
                script {
                    def serviceDir = "spring-petclinic-${env.SERVICE}"
                    echo "Publishing coverage report for ${env.SERVICE}..."
                    jacoco execPattern: "${serviceDir}/target/jacoco.exec",
                           classPattern: "${serviceDir}/target/classes",
                           sourcePattern: "${serviceDir}/src/main/java",
                           inclusionPattern: '**/*.class',
                           exclusionPattern: '**/*Test*'
                }
            }
        }

        // Build dịch vụ đã thay đổi
        stage('Build') {
            when {
                expression { return env.SERVICE?.trim() }
            }
            steps {
                script {
                    def serviceDir = "spring-petclinic-${env.SERVICE}"
                    dir(serviceDir) {
                        if (!fileExists('pom.xml')) {
                            error "❌ pom.xml not found in ${serviceDir}. Skipping build."
                        }
                        echo "Building ${env.SERVICE}..."
                        sh 'mvn package -DskipTests'  // Chạy build với Maven
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
