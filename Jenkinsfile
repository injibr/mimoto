automata {
 
    //def version = '1.0.0'

    //descriptor = "groupId=inji,artifactId=mimoto,version=${version}"
    skipHom = true

    build.agent.image = 'library/maven:3.9-eclipse-temurin-21'

     //kustomization not ready
    //gitOps.provider = 'GIT_INFRA'     
    //gitOps.namespace = 'inji'     
    //gitOps.repos = [dev: 'gitops-np/inji']

    containers.add descriptor: 'Dockerfile', imageName: 'inji/mimoto'

    artifacts.add file: 'target/mimoto-${version}.jar'

    build.opts = "-Dgpg.skip=true -Dmaven.javadoc.skip=true"




    //qa.sonarOpts = "-Dsonar.projectKey=br.gov.dataprev.inji:mimoto -Dsonar.projectVersion=${version} -Dsonar.sources=."
    //qa.encoding = 'UTF-8'
}
