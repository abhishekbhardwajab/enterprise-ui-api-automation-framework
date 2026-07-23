pipeline {
    agent any

    tools {
        maven 'Maven-3.8.1'
        jdk 'JDK-11'
    }

    parameters {
        choice(name: 'ENVIRONMENT', choices: ['qa', 'dev', 'staging', 'prod'], description: 'Target environment identifier')
        choice(name: 'EXECUTION', choices: ['remote', 'local'], description: 'remote = BrowserStack, local = headless Chrome on the agent')
    }

    environment {
        BROWSERSTACK_CREDS = credentials('browserstack-credentials') // -> BROWSERSTACK_CREDS_USR / BROWSERSTACK_CREDS_PSW
        SLACK_CHANNEL = '#qa-automation'
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '30'))
        timeout(time: 45, unit: 'MINUTES')
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Start BrowserStackLocal tunnel') {
            when { expression { params.EXECUTION == 'remote' } }
            steps {
                sh '''
                    curl -sSL -o BrowserStackLocal.zip \
                        "https://www.browserstack.com/browserstack-local/BrowserStackLocal-linux-x64.zip"
                    unzip -o BrowserStackLocal.zip
                    chmod +x BrowserStackLocal
                    ./BrowserStackLocal --key "$BROWSERSTACK_CREDS_PSW" --daemon start
                '''
            }
        }

        stage('Initial run') {
            steps {
                withEnv(["BROWSERSTACK_USERNAME=${BROWSERSTACK_CREDS_USR}", "BROWSERSTACK_ACCESS_KEY=${BROWSERSTACK_CREDS_PSW}"]) {
                    sh """
                        mvn -B clean test \
                            -Denv=${params.ENVIRONMENT} \
                            -Dexecution=${params.EXECUTION}
                    """
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Failed-item rerun') {
            when {
                expression { fileExists('target/rerun.txt') && readFile('target/rerun.txt').trim() }
            }
            steps {
                withEnv(["BROWSERSTACK_USERNAME=${BROWSERSTACK_CREDS_USR}", "BROWSERSTACK_ACCESS_KEY=${BROWSERSTACK_CREDS_PSW}"]) {
                    sh """
                        mvn -B test -PfailedTests \
                            -Denv=${params.ENVIRONMENT} \
                            -Dexecution=${params.EXECUTION}
                    """
                }
            }
        }

        stage('Retrieve BrowserStack build link') {
            when { expression { params.EXECUTION == 'remote' } }
            steps {
                script {
                    def buildName = sh(
                        script: "grep '^browserstack.build.name=' src/test/resources/application.properties | cut -d= -f2",
                        returnStdout: true
                    ).trim()
                    def response = sh(
                        script: """
                            curl -s -u "$BROWSERSTACK_CREDS_USR:$BROWSERSTACK_CREDS_PSW" \
                                "https://api.browserstack.com/automate/builds.json"
                        """,
                        returnStdout: true
                    ).trim()
                    def builds = readJSON text: response
                    def match = builds.find { it.automation_build.name == buildName }
                    env.BROWSERSTACK_BUILD_URL = match ? "https://automate.browserstack.com/builds/${match.automation_build.hashed_id}" : 'unavailable'
                }
            }
        }

        stage('Stop BrowserStackLocal tunnel') {
            when { expression { params.EXECUTION == 'remote' } }
            steps {
                sh './BrowserStackLocal --key "$BROWSERSTACK_CREDS_PSW" --daemon stop || true'
            }
        }

        stage('Calculate final build state') {
            steps {
                script {
                    // Falls back to a warning + "unstable" if a summary is missing or unparsable,
                    // rather than failing the whole pipeline on a reporting glitch.
                    def initial = readCucumberSummary('target/cucumber-reports/cucumber.json')
                    def rerun = fileExists('target/cucumber-reports/rerun.json') ? readCucumberSummary('target/cucumber-reports/rerun.json') : null

                    if (initial == null) {
                        env.FINAL_STATE = 'unstable'
                        echo 'WARNING: could not parse target/cucumber-reports/cucumber.json - marking build unstable.'
                    } else if (initial.failed == 0) {
                        env.FINAL_STATE = 'success'
                    } else if (rerun != null && rerun.failed == 0) {
                        env.FINAL_STATE = 'passed-on-rerun'
                    } else if (rerun != null && rerun.failed < initial.failed) {
                        env.FINAL_STATE = 'unstable'
                    } else if (initial.total == 0) {
                        env.FINAL_STATE = 'skipped'
                    } else {
                        env.FINAL_STATE = 'failed'
                    }
                    echo "Final build state: ${env.FINAL_STATE}"
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'target/cucumber-reports/**, target/rerun.txt, reports/screenshots/**', allowEmptyArchive: true
            script {
                def color = [success: 'good', 'passed-on-rerun': 'good', unstable: 'warning', failed: 'danger', skipped: 'warning']
                        .getOrDefault(env.FINAL_STATE ?: 'failed', 'danger')
                def link = env.BROWSERSTACK_BUILD_URL ? "\nBrowserStack build: ${env.BROWSERSTACK_BUILD_URL}" : ''

                def threadTs = slackSend(
                        channel: env.SLACK_CHANNEL,
                        color: color,
                        message: "*${env.JOB_NAME}* #${env.BUILD_NUMBER} - ${env.FINAL_STATE ?: 'unknown'} (env=${params.ENVIRONMENT}, execution=${params.EXECUTION})"
                )
                slackSend(
                        channel: threadTs.threadId,
                        color: color,
                        message: "Build details: ${env.BUILD_URL}${link}"
                )
            }
        }
    }
}

/**
 * Reads a Cucumber JSON report and returns [total, failed] scenario counts.
 * Returns null if the file is missing or fails to parse, so callers can
 * fall back to an "unstable" state with a warning instead of crashing the
 * whole pipeline on a reporting glitch.
 */
def readCucumberSummary(String path) {
    if (!fileExists(path)) {
        return null
    }
    try {
        def features = readJSON file: path
        int total = 0
        int failed = 0
        features.each { feature ->
            feature.elements?.each { scenario ->
                total++
                boolean scenarioFailed = scenario.steps?.any { it.result?.status == 'failed' } ?: false
                if (scenarioFailed) {
                    failed++
                }
            }
        }
        return [total: total, failed: failed]
    } catch (Exception e) {
        echo "WARNING: failed to parse ${path}: ${e.message}"
        return null
    }
}
