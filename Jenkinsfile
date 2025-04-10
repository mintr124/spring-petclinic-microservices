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
                    sh 'git fetch origin main'
                    def changes = sh(script: "git diff --name-only origin/main...HEAD", returnStdout: true).trim()
                    echo "Changes detected: ${changes}"
        
                    // Kiểm tra thay đổi trong thư mục dịch vụ
                    if (changes.contains('vets-service/')) {
                        env.SERVICE = 'vets-service'
                    } else if (changes.contains('customer-service/')) {
                        env.SERVICE = 'customer-service'
                    } else if (changes.contains('visit-service/')) {
                        env.SERVICE = 'visit-service'
                    } else if (changes.any { it.startsWith('pom.xml') || it.startsWith('Jenkinsfile') }) {
                        // Nếu thay đổi ở thư mục root (như pom.xml hoặc Jenkinsfile), build tất cả các dịch vụ
                        env.SERVICE = 'all-services'
                    } else {
                        env.SERVICE = ''
                    }
        
                    if (env.SERVICE == null) {
                        currentBuild.result = 'SUCCESS'
                        echo "No relevant changes detected. Skipping build and tests."
                    } else if (env.SERVICE == 'all-services') {
                        echo "Changes detected in root directory. Building and testing all services."
                    } else {
                        echo "Changes detected in ${env.SERVICE}. Proceeding with build and tests."
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
                        def missed = jacoco.counter.find { it.@type == 'INSTRUCTION' }.@missed.toInteger()
                        def covered = jacoco.counter.find { it.@type == 'INSTRUCTION' }.@covered.toInteger()
                        coverage = (covered * 100) / (missed + covered)
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
