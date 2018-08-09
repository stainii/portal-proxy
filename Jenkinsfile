pipeline {
  agent {
    docker {
      image 'stainii/portal-web-app-base'
      args '-v /root/.m2:/root/.m2'
    }
  }
  stages {
    stage('Build') {
      steps {
        sh 'mvn -B clean install'
      }
    }
  }
  post {
      changed {
          script {
              emailext subject: '$DEFAULT_SUBJECT',
                  body: '$DEFAULT_CONTENT',
                  recipientProviders: [
                      [$class: 'CulpritsRecipientProvider'],
                      [$class: 'DevelopersRecipientProvider'],
                      [$class: 'RequesterRecipientProvider']
                  ],
                  replyTo: '$DEFAULT_REPLYTO',
                  to: '$DEFAULT_RECIPIENTS'
      }
    }
  }
}
