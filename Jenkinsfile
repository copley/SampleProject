#!groovy

// Declare version on global scope so it's accessible across different logic in the Jenkins Job
def version = null
// Declare sample service image handle on global scope so it's accessible across different logic in the Jenkins Job
def sampleServiceImage = null

// Specify node which has access to dockerhost
node('dockerhost') {

    // Create stage for configuring the Build environment
    stage name: 'Prepare Docker Build Node'
    // Have variable to store the version of Maven Container to use for building.
    def DOCKER_BUILD_NODE = 'maven:3.3.9-jdk-8'

    // Here you can change to a private registry if needed
    docker.withRegistry('https://index.docker.io/v1/') {
        // Depending on the build process -- you may also want to mount a dependency cache folder to host to shorten build time
        def buildImage = docker.image(DOCKER_BUILD_NODE)
        // Store current directory (workspace) for later use
        def PWD = pwd()
        // Only need maven container for build process, so let's do it all inside so we don't need to clean ourselves up.
        // Also mount a cache folder
        buildImage.inside("-v ${PWD}/../../.m2/repository:/cache/.m2/repository") {
            // Let's checkout the code
            stage name: 'Code Checkout'
            // Use the 'scm' keyword as we are doing Jenkinsfile from same SCM source
            checkout scm

            // Use global libraries for this to get version value
            version = versionUtils.generate(binding.variables.get('MAJOR'), binding.variables.get('MINOR'), binding.variables.get('PATCH'),
                    env.BUILD_NUMBER, env.BRANCH_NAME, binding.variables.get('RELEASE_TYPE'))

            // Let's create a build stage.
            stage name: 'Build'
            // Not deploying anything so don't need withCredentials logic to pass credentials to maven deploy step
            //withCredentials([[$class: 'UsernamePasswordBinding', credentialsId: MAVEN_DEPLOY_CREDENTIALS_ID, variable: 'mavenDeployCredentials']]) {
            // Update version of project for versioning
            sh("mvn -Duser.home=/cache/ versions:set -DnewVersion=${version}")
            // Run actual build command (omit the deploy step)
            sh('mvn -Duser.home=/cache/ clean install')
            //}
        }

        // Add stage to run any component testing
        stage name: 'Component Testing'
        // May need to setup a docker environment or run a different job
        echo 'Not needed for demo :)'

        // Save the binaries for later if needed later in Jenkins Job process.
        stage name: 'Stash Artifacts'
        stash includes: 'target/*.jar', name: 'binaries'
    }

    // Build a versioned docker container for testing/deployment purposes
    stage name: 'Build Docker Container'
    docker.build("sampleservice:${version}", "--build-arg VERSION=${version} .")

    // Deploy this image somewhere
    stage name: 'Deploy Docker Container'
    echo 'Not needed for demo :)'

    // Run integration tests
    stage name: 'Integration Testing'
    // May need to setup additional (docker) environment or use a seperate job for this.
    echo 'Not needed for demo :)'
}

// Run load test on a node that has access to Docker Daemon and marked as a Load Test server
node('dockerhost && loadtest') {
    // Define a stage for this load testing.
    stage name: 'Load Testing'
    // Create variable for container handle so we can clean it up later
    def sampleServiceContainer = null
    // init a sample service image handle from newly created container
    sampleServiceImage = docker.image("sampleservice:${version}")
    // init a 3rd party container for load testing
    def loadImage = docker.image('jordi/ab')

    // throw logic in a try catch so we can handle tearing down container environment correctly
    try {
        // start sample service
        sampleServiceContainer = sampleServiceImage.run()

        // Start load container
        NUMBER_COUNT = 1000000
        CONCURRENT_COUNT = 20
        loadImage.inside("--link ${sampleServiceContainer.id}:sampleService") {
            sh([script: "ab -k -n ${NUMBER_COUNT} -c ${CONCURRENT_COUNT} http://sampleService:8080/helloworld > loadResults.txt"])
        }

        // Dummy stage for reading results
        stage name: 'Load Test Results'
        // inspect results (in logs)
        def loadResults = readFile('loadResults.txt')
        println loadResults
    } catch (all) {
        // In case there were any errors catch and log.
        echo "There was an error: ${all.getMessage()}"
        // Force build as a failure.
        currentBuild.result = 'FAILURE'
    } finally {
        // Make sure at the end we clean up our running containers.
        sampleServiceContainer.stop()
    }
}

// if build did not fail or currently unstable -- do git tags and promote docker container to latest tag
// Depending on build logic, some plugins might change status to 'SUCCESS' otherwise if no result is set then things are
// in a passing state.
if (currentBuild.result == null || currentBuild.result == 'SUCCESS') {
    // Add stage for manual verification of results. This is an example to include some manual step.
    stage name: 'Verify Load Results'
    // Keep input outside of a node to prevent using 2 executors
    input 'Are the load results good enough?'

    // Specify node where we will finish the rest of the logic
    node('dockerhost') {
        // Create stage for Tag step
        stage name: 'Git Tag'
        // There is no git publisher/tag plugin compatibility yet - so let's do it manually.
        // Let's use SSH Agent to load our Git Credentials for this commit
        sshagent(['GitHubSSHCredentialsId']) {
            // Temp change the remote origin url to ssh credentials so we can use SSHAgent properly.
            sh("git config remote.origin.url git@github.com:Irdeto-Jenkins2/SampleProject.git")
            // Create tag locally
            sh("git tag -a -f -m 'Release version of Sample Service - version ${version}' ${version}")
            // Push tag to origin
            sh("git -c core.askpass=true push origin ${version}")
        }

        // Create Stage for docker image promotion
        stage name: 'Docker Image Latest Promotion'
        // Since we are on a (possibly) different node make sure the image is local - skip for now as we never published for demo
        // sampleServiceImage.pull()
        // Create new tag for image - latest
        sampleServiceImage.tag('latest')
        // Push latest tag to docker registry (skip for demo reasons)
        //sampleServiceImage.push('latest')

        // Let's archive the binaries since everything was successful.
        stage name: 'Archive Binaries'
        // Unstash binaries to local node
        unstash 'binaries'
        // archive binary files.
        archive('target/*.jar')
    }
}