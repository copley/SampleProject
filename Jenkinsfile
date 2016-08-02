node('dockerhost') {

    def DOCKER_BUILD_NODE = 'maven:3.3.9-jdk-8'

    GIT_BRANCH = 'master'
    GIT_URL = 'git@github.com:Irdeto-Jenkins2/SampleProject.git'

    def version = "${MAJOR}.${MINOR}.${PATCH}.${env.BUILD_NUMBER}"

    stage name: 'Prepare Docker Build Node', concurrency: MAX_CURRENCY
    // Here you can change to a private registry if needed
    docker.withRegistry('https://index.docker.io/v1/') {
        // Depending on the build process -- you may also want to mount a dependency cache folder to host to shorten build time?
        def buildImage = docker.image(DOCKER_BUILD_NODE)
        buildImage.inside() {
            stage name: 'Code Checkout'
            checkout([$class                           : 'GitSCM',
                      branches                         : [[name: GIT_BRANCH]],
                      doGenerateSubmoduleConfigurations: false,
                      extensions                       : [],
                      submoduleCfg                     : [],
                      userRemoteConfigs                : [[url: GIT_URL]]])

            stage name: 'Build'
            // Not deploying anything so don't need withCredentials logic
            //withCredentials([[$class: 'UsernamePasswordBinding', credentialsId: MAVEN_DEPLOY_CREDENTIALS_ID, variable: 'mavenDeployCredentials']]) {
            // Run actual build command
            sh("mvn versions:set -DnewVersion=${version}")
            sh('mvn clean install')
            //}
        }

        stage name: 'Component Testing'
        echo 'Not needed for demo :)'
    }

    stage name: 'Build Docker Container'
    docker.build("sampleservice:${version}", "--build-arg VERSION=${version}")

    stage name: 'Deploy Docker Container'
    echo 'Not needed for demo :)'

    stage name: 'Integration Testing'

    def sampleServiceContainer = null
    def loadContainer = null
    def sampleServiceImage = docker.image("sampleservice:${version}")
    def loadImage = docker.image('jordi/ab')

    try {
        // start sample service
        sampleServiceContainer = sampleServiceImage.run()

        // Start load container
        NUMBER_COUNT = 100
        CONCURRENT_COUNT = 5
        loadContainer = loadImage.run([args: "${sampleServiceContainer.id}:sampleService", command: "ab -k -n ${NUMBER_COUNT} -c ${CONCURRENT_COUNT} http://sampleService/helloworld"])

        // inspect results


    } catch(all) {
        echo "There was an error: ${all.getMessage()}"
        currentBuild.result = 'FAILURE'
    } finally {
        sampleServiceContainer.stop()
        loadContainer.stop()
    }

    // if build did not fail or currently unstable -- do git tags and promote docker container to latest tag
    if (currentBuild.result == null || currentBuild.result == 'SUCCESS') {
        stage name: 'Git Tag'
        // TODO: Mark git tag here

        stage name: 'Docker Image Latest Promotion'
        sampleServiceImage.tag('latest')
        //sampleServiceImage.push('latest')
    }
}