// Folders
import pluggable.scm.*;
import adop.cartridge.properties.*;

SCMProvider scmProvider = SCMProviderHandler.getScmProvider("${SCM_PROVIDER_ID}", binding.variables)
CartridgeProperties cartridgeProperties = new CartridgeProperties("${CARTRIDGE_CUSTOM_PROPERTIES}");

def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"
def projectScmNamespace = "${SCM_NAMESPACE}"

// Jobs
def publishCartridgeJob = freeStyleJob(projectFolderName + "/PublishCartridgeRepo")

def newCartridgeGitRepo = cartridgeProperties.getProperty("scm.code.repo.name", "my-new-cartridge");
 
 // Setup Job 
 publishCartridgeJob.with{
    parameters{
            stringParam("CARTRIDGE_REPO",'${newCartridgeGitRepo}',"Git URL of the cartridge you want to publish.")
            stringParam("TARGET_CARTRIDGE_REPO","","Git URL of the target repository where you want to push your cartridge to. Ensure you have added the Jenkins SSH key to the repository manager.")
    }
    environmentVariables {
        env('WORKSPACE_NAME',workspaceFolderName)
        env('PROJECT_NAME',projectFolderName)
    }
    scm scmProvider.get(projectScmNamespace,'${TARGET_CARTRIDGE_REPO}', "*/master", "adop-jenkins-master", null)
    wrappers {
        preBuildCleanup()
        injectPasswords()
        maskPasswords()
        sshAgent("adop-jenkins-master")
    }
    steps {
        shell('''#!/bin/bash -ex

echo
echo

# Fetch all branches from local Gerrit repository for cartridge
git remote add gerrit $CARTRIDGE_REPO
git fetch gerrit

# Push all branches to remote repository
git push origin +refs/remotes/gerrit/*:refs/heads/*

set +x
echo
echo ALL FINISHED!
echo Your local cartridge has been pushed to the specified target: ${TARGET_CARTRIDGE_REPO}
echo
''')
    }
    
 }
