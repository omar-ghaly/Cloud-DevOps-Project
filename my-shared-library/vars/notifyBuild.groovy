#!/usr/bin/env groovy

def call(String buildStatus = 'SUCCESS') {
    def emoji = buildStatus == 'SUCCESS' ? '✅' : '❌'
    
    echo "${emoji} Build ${buildStatus}: ${env.JOB_NAME} #${env.BUILD_NUMBER}"
    echo "Build URL: ${env.BUILD_URL}"
    echo "Duration: ${currentBuild.durationString}"
}
