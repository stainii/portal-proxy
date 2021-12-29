pipeline {
  options {
    buildDiscarder(logRotator(numToKeepStr: '10', artifactNumToKeepStr: '10'))
  }
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
    always {
      cleanWs(cleanWhenAborted: true,
              cleanWhenFailure: true,
              cleanWhenNotBuilt: false,
              cleanWhenSuccess: true,
              deleteDirs: true,
              disableDeferredWipeout: true,
              notFailBuild: true)
    }
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
