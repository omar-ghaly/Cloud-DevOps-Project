#!/usr/bin/env groovy

def call(String manifestPath, String imageName, String imageTag) {
    echo "Updating Kubernetes manifests in ${manifestPath} with image ${imageName}:${imageTag}"

    try {
        sh """
            # تحديث الـ deployment.yaml بالـ image الجديد
            sed -i "s|image: .*${imageName}:.*|image: ${imageName}:${imageTag}|g" ${manifestPath}/deployment.yaml

            # عرض التغييرات للتحقق
            grep "image:" ${manifestPath}/deployment.yaml
        """
        echo "Successfully updated manifests"
        return true
    } catch (Exception e) {
        echo "Failed to update manifests: ${e.message}"
        throw e
    }
}
