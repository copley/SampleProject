#!groovy

def version = null

node('dockerhost') {

    def DOCKER_BUILD_NODE = 'maven:3.3.9-jdk-8'

    stage name: 'Prepare Docker Build Node'
    // Here you can change to a private registry if needed
    docker.withRegistry('https://index.docker.io/v1/') {
        // Depending on the build process -- you may also want to mount a dependency cache folder to host to shorten build time?
        def buildImage = docker.image(DOCKER_BUILD_NODE)
        def PWD = pwd()
        buildImage.inside("-v ${PWD}/../../.m2/repository:/cache/.m2/repository") {
            stage name: 'Code Checkout'
            checkout scm

            if (binding.variables.get('RELEASE_TYPE') == 'release') {
                version = "${MAJOR}.${MINOR}.${PATCH}.${env.BUILD_NUMBER}"
            } else {
                branch = ("branch-${env.BUILD_NUMBER}-${env.BRANCH_NAME}" =~ /\\|\/|:|"|<|>|\||\?|\*|\-/).replaceAll("_")
                version = "0.0.0-${branch}.${env.BUILD_NUMBER}"
            }

            stage name: 'Build'
            // Not deploying anything so don't need withCredentials logic
            //withCredentials([[$class: 'UsernamePasswordBinding', credentialsId: MAVEN_DEPLOY_CREDENTIALS_ID, variable: 'mavenDeployCredentials']]) {
            // Run actual build command
            sh("mvn -Duser.home=/cache/ versions:set -DnewVersion=${version}")
            sh('mvn -Duser.home=/cache/ clean install')
            //}
        }

        stage name: 'Component Testing'
        echo 'Not needed for demo :)'

        stage name: 'Stash Artifacts'
        stash includes: 'target/*.jar', name: 'binaries'
    }

    stage name: 'Build Docker Container'
    docker.build("sampleservice:${version}", "--build-arg VERSION=${version} .")

    stage name: 'Deploy Docker Container'
    echo 'Not needed for demo :)'

    stage name: 'Integration Testing'
    echo 'Not needed for demo :)'
}

def sampleServiceImage = null

node('dockerhost') {
    stage name: 'Load Testing'
    def sampleServiceContainer = null
    def loadContainer = null
    sampleServiceImage = docker.image("sampleservice:${version}")
    def loadImage = docker.image('jordi/ab')

    try {
        // start sample service
        sampleServiceContainer = sampleServiceImage.run()

        // Start load container
        NUMBER_COUNT = 1000000
        CONCURRENT_COUNT = 20
        loadImage.inside("--link ${sampleServiceContainer.id}:sampleService") {
            sh([script: "ab -k -n ${NUMBER_COUNT} -c ${CONCURRENT_COUNT} http://sampleService:8080/helloworld > loadResults.txt"])
        }

        stage name: 'Load Test Results'
        // inspect results (in logs)
        def loadResults = readFile('loadResults.txt')
        println loadResults
    } catch (all) {
        echo "There was an error: ${all.getMessage()}"
        currentBuild.result = 'FAILURE'
    } finally {
        sampleServiceContainer.stop()
    }
}

// if build did not fail or currently unstable -- do git tags and promote docker container to latest tag
if (currentBuild.result == null || currentBuild.result == 'SUCCESS') {
    stage name: 'Verify Load Results'
    input 'Are the load results good enough?'

    node('dockerhost') {
        stage name: 'Git Tag'
        // TODO: Mark git tag here

        stage name: 'Docker Image Latest Promotion'
        sampleServiceImage.tag('latest')
        //sampleServiceImage.push('latest')

        stage name: 'Archive Binaries'
        unstash 'binaries'
        archive('target/*.jar')
    }
}