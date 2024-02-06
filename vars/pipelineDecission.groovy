#!groovy

def decidePipleine(Map configMap){
    application = configMap.get("application")
    //# here we are getting nodeJSVM
    switch(application) {
        case 'nodejsVM':
            echo "application is Node JS and VM based"
            nodejsVM(configMap)
            break
        case 'staticVM':
            echo "application is Node JS and VM based"
            staticVM(configMap)
            break
        case 'nodejsEKS':
            echo "application is Node JS and VM based"
            nodeJSEKS(configMap)
            break
        case 'javaVM':
            javaVM(configMap)
            break
        case 'javaEKS':
            javaEKS(configMap)
            break
            
        default:
            error "Un recognised application"
            break
    }
}