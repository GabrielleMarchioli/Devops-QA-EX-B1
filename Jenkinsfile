// Pipeline do Core Java (ADR-006: um pipeline por componente).
// Job multibranch com Script Path = Jenkinsfile. Servicos Python tem Jenkinsfile proprio em servicos/*.
pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '15'))
    }

    environment {
        IMAGEM = "educacao/core-gamificacao:${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Testes: unidade + arquitetura') {
            // Bloqueia deploy: regra de dominio quebrada ou dominio acoplado ao Spring (ArchUnit)
            steps {
                sh 'chmod +x mvnw && ./mvnw -B clean verify'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    archiveArtifacts artifacts: 'target/site/jacoco/**', allowEmptyArchive: true
                }
            }
        }

        stage('Quality gate (JaCoCo)') {
            // O goal jacoco:check ja roda no verify; este estagio deixa a regra visivel na esteira
            steps {
                echo 'Cobertura minima de 80% em domain/application validada no mvn verify'
            }
        }

        stage('Imagem Docker') {
            steps {
                sh 'docker build -t $IMAGEM -t educacao/core-gamificacao:latest .'
            }
        }

        stage('Deploy (homologacao)') {
            when { branch 'main' }
            steps {
                // deploy independente: apenas o container do Core e recriado
                sh 'docker compose up -d --no-deps --build app'
            }
        }

        stage('Smoke test') {
            when { branch 'main' }
            steps {
                // Bloqueia promocao se o Core nao subir saudavel ou nao expor metricas
                sh '''
                  for i in $(seq 1 30); do
                    curl -fs http://localhost:8080/actuator/health && break
                    sleep 2
                  done
                  curl -fs http://localhost:8080/actuator/health | grep -q '"status":"UP"'
                  curl -fs http://localhost:8080/actuator/prometheus | grep -q 'outbox_eventos_pendentes'
                '''
            }
        }
    }

    post {
        failure {
            echo 'Pipeline do Core falhou: deploy bloqueado.'
        }
    }
}
