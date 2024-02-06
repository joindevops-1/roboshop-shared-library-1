def call(Map configMap){
    // mapName.get("key-name")
    def component = configMap.get("component")
    echo "component is : $component"
    pipeline {
        agent { node { label 'AGENT-1' } }
        environment{
            //here if you create any variable you will have global access, since it is environment no need of def
            packageVersion = ''
        }
        parameters {
            booleanParam(name: 'Deploy', defaultValue: false, description: 'Toggle this value')
            
            booleanParam(name: 'Destroy', defaultValue: false, description: 'Toggle this value')
        }
        
        stages {
            stage('Get version'){
                steps{
                    script{
                        def pom = readMavenPom file: 'pom.xml'
                        packageVersion = pom.version
                        echo "version: ${packageVersion}"
                    }
                }
            }
            stage('Install depdencies') {
                steps {
                    sh 'mvn package'
                }
            }
            stage('Unit test') {
                steps {
                    echo "unit testing is done here"
                }
            }
            //sonar-scanner command expect sonar-project.properties should be available
            stage('Sonar Scan') {
                steps {
                    echo "Sonar scan done"
                }
            }
            stage('Build') {
                steps {
                    sh 'ls -ltr '
                    sh 'ls -ltr target'
                    
                }
            }
            stage('SAST') {
                steps {
                    echo "SAST Done"
                    echo "package version: $packageVersion"
                }
            }
            //install pipeline utility steps plugin, if not installed
            stage('Publish Artifact') {
                steps {
                    nexusArtifactUploader(
                        nexusVersion: 'nexus3',
                        protocol: 'http',
                        nexusUrl: pipelineGlobals.nexusURL(),
                        groupId: 'com.roboshop',
                        version: "$packageVersion",
                        repository: "${component}",
                        credentialsId: 'nexus-auth',
                        artifacts: [
                            [artifactId: "${component}",
                            classifier: '',
                            file: "target/${component}-${packageVersion}.jar",
                            type: 'jar']
                        ]
                    )
                }
            }

            // here I need to configure downstram job. I have to pass package version for deployment
            // This job will wait until downstrem job is over
            // by default when a non-master branch CI is done, we can go for DEV development
            stage('Deploy') {
                when{
                    expression{
                        params.Deploy
                    }
                }
                steps {
                    script{
                        echo "Deployment"
                        def params = [
                            string(name: 'version', value: "$packageVersion"),
                            string(name: 'environment', value: "dev"),
                            booleanParam(name: 'Destroy', value: "${params.Destroy}"),
                            booleanParam(name: 'Deploy', value: "${params.Deploy}")
                        ]
                        build job: "../${component}-deploy", wait: true, parameters: params
                    }
                }
            }
        }

        post{
            always{
                echo 'cleaning up workspace'
                //deleteDir()
            }
        }
    }
}