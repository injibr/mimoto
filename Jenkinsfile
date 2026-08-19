automata {
 
    
    build.agent.image = 'library/maven:3.9-eclipse-temurin-21'

    gitOps.provider = 'GIT_INFRA'
    gitOps.engine = 'HELM'
    gitOps.repos = [
        dev: 'gitops-np/credenciais-verificaveis',
        hom: 'gitops-np/credenciais-verificaveis',
        prd: 'gitops-p/credenciais-verificaveis',
    ]

    containers.add descriptor: 'Dockerfile', imageName: 'inji/mimoto', tagKey:'injiWeb.mimoto.tag'
 
    artifacts.add file: 'target/mimoto-${version}.jar'

    build.opts = "-Dgpg.skip=true -Dmaven.javadoc.skip=true"

}
