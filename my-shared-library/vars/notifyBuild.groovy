def call(String status) {
    def color = status == 'SUCCESS' ? 'good' : 'danger'
    def emoji = status == 'SUCCESS' ? '✅' : '❌'
    
    echo "${emoji} Build Status: ${status}"
    echo "Job: ${env.JOB_NAME}"
    echo "Build Number: ${env.BUILD_NUMBER}"
    echo "Build URL: ${env.BUILD_URL}"
}
