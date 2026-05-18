# Configurações do script
$tagPredefinida = "v0.17.1"  # Defina a tag diretamente aqui
$usarTagPredefinida = $true   # Defina como $true para usar a tag predefinida ou $false para solicitar ao usuário
$tag = ""
$version = ""
$repositorio_externo = "https://github.com/injibr/mimoto/"
$id_commit_pull = ""
$branch = git rev-parse --abbrev-ref HEAD
$versaoConfig = "v1.0.6"

# Credenciais LDAP (configurar aqui para não solicitar no console)
$ldapUser = "SEU_USER_LDAP"  # Substitua pelo seu usuário
$ldapPassword = "SUA_SENHA"  # Substitua pela sua senha

# Configuração do relatório
$pastaRelatorios = Join-Path (Get-Location) "relatorios"
$dataHora = Get-Date -Format "yyyyMMdd_HHmmss"
$nomeRelatorio = "relatorio_atualizacao_${dataHora}.log"
$caminhoRelatorio = Join-Path $pastaRelatorios $nomeRelatorio

# Criar pasta de relatórios se não existir
if (-not (Test-Path -Path $pastaRelatorios)) {
    New-Item -Path $pastaRelatorios -ItemType Directory | Out-Null
    Escrever-Mensagem "Pasta de relatórios criada: $pastaRelatorios"
}

# Nome do arquivo de script atual para excluir do commit
$nomeScriptAtual = Split-Path -Leaf $MyInvocation.MyCommand.Path

# Usar a rede dataprev-internet. Não utilizar dataprev-nac nem dataprev-coporativa

function Testar-RepositorioGithub {
    Escrever-Log "Verificando repositório remoto do GitHub..."
    if (git remote | Select-String -Pattern "github") {
        Escrever-Mensagem "Repositório remoto já existe."
    }
    else {
        git remote add github $repositorio_externo
        Escrever-Mensagem "Repositório remoto não existe localmente, criando repositório."
    }
    
    # Testar conexão com GitHub
    Escrever-Mensagem "Testando conexão com GitHub..."
    try {
        $resultadoTeste = git ls-remote --heads github 2>&1
        if ($LASTEXITCODE -eq 0) {
            Escrever-Mensagem "Conexão com GitHub bem-sucedida."
            Escrever-Log "Conexão com GitHub estabelecida com sucesso"
        } else {
            $detalhesErro = "Código de erro: $LASTEXITCODE. Detalhes: $resultadoTeste"
            Escrever-Mensagem "Falha na conexão com o GitHub. $detalhesErro"
            Escrever-Mensagem "Tentando soluções alternativas..."
            Escrever-Log "[ERRO] Falha na conexão com GitHub. $detalhesErro"
            
            # Tentar com formato de URL diferente
            Escrever-Log "Tentando URL alternativa: https://github.com/injibr/mimoto.git"
            git remote set-url github "https://github.com/injibr/mimoto.git"
            
            # Verificar se a alteração resolveu o problema
            $resultadoTeste = git ls-remote --heads github 2>&1
            if ($LASTEXITCODE -eq 0) {
                Escrever-Mensagem "Conexão estabelecida com URL alternativa."
                Escrever-Log "Conexão estabelecida com URL alternativa."
            } else {
                Escrever-Mensagem "Falha persistente na conexão. Verifique sua rede e configurações de proxy."
                Escrever-Log "[ERRO CRÍTICO] Falha persistente na conexão com GitHub após tentativas alternativas."
                exit 1
            }
        }
    }
    catch {
        $excecao = $_.Exception
        $mensagemErro = "Teste de conexão falhou. Exceção: $($excecao.GetType().Name). Mensagem: $($excecao.Message)"
        Escrever-Mensagem $mensagemErro
        Escrever-Log "[ERRO] $mensagemErro"
        Escrever-Log "[DETALHE] Stack trace: $($_.ScriptStackTrace)"
    }
}

function Ler-EntradaTag {
    if ($usarTagPredefinida) {
        $script:tag = $tagPredefinida
        Escrever-Mensagem "Tag predefinida no script: $tag"
        
        # Solicitar confirmação do usuário
        $confirmacao = Read-Host -Prompt "Confirma o uso desta tag? (S/N)"
        if ($confirmacao -ne "S" -and $confirmacao -ne "s") {
            $script:tag = Read-Host -Prompt "Digite a tag desejada para a nova versão"
            if ([string]::IsNullOrEmpty($tag)) {
                $mensagemErro = "Tag inválida, operação cancelada."
                Escrever-Mensagem $mensagemErro
                Escrever-Log "[ERRO] $mensagemErro"
                exit 1
            }
        }
    } else {
        $script:tag = Read-Host -Prompt "Digite a tag para a nova versão"
        if ([string]::IsNullOrEmpty($tag)) {
            $mensagemErro = "Tag inválida, operação cancelada."
            Escrever-Mensagem $mensagemErro
            Escrever-Log "[ERRO] $mensagemErro"
            exit 1
        }
    }
    
    # Validar formato da tag
    if (-not ($tag -match '^v\d+\.\d+\.\d+$')) {
        Escrever-Mensagem "AVISO: A tag '$tag' não segue o formato recomendado (vX.Y.Z)."
        $confirmarFormato = Read-Host -Prompt "Deseja continuar mesmo assim? (S/N)"
        if ($confirmarFormato -ne "S" -and $confirmarFormato -ne "s") {
            Escrever-Log "[AVISO] Operação cancelada pelo usuário devido ao formato da tag."
            exit 0
        }
    }
    
    $script:version = $tag -replace 'v', ''
    Escrever-Log "Tag selecionada: $tag (versão: $version)"
    Escrever-Mensagem "Versão confirmada: $tag"
}

function Configurar-CredenciaisLdap {
    Escrever-Log "Configurando credenciais LDAP..."
    if ($ldapUser -eq "SEU_USUARIO_AQUI" -or $ldapPassword -eq "SUA_SENHA_AQUI") {
        $mensagemErro = "ATENÇÃO: Configure as credenciais LDAP no início do script antes de executar!"
        Escrever-Mensagem $mensagemErro
        Escrever-Log $mensagemErro
        exit 1
    }
    
    Escrever-Mensagem "Usando credenciais LDAP configuradas no script."
    Escrever-Log "Credenciais LDAP carregadas (usuário: $ldapUser)"
    
    # Configurar proxy Git se necessário
    Configurar-ProxyGit
}

function Configurar-ProxyGit {
    # $urlProxy = "http://$($ldapUser):$($ldapPassword)@proxy.prevnet:8080"
    # git config --global http.proxy $urlProxy
    # git config --global https.proxy $urlProxy
    # git config --global http.sslVerify false
    # Escrever-Mensagem "Proxy Git configurado."
    # Escrever-Log "Proxy Git configurado com sucesso"
}

function Baixar-Codigo {
    Testar-RepositorioGithub
    Escrever-Mensagem "Atualizando código para nova versão $tag..."
    Escrever-Log "Iniciando download do código versão $tag"
    
    # Capturar saída completa do comando git pull
    $resultadoPull = git pull --no-edit github $tag --allow-unrelated-histories 2>&1
    if ($LASTEXITCODE -ne 0) {
        $mensagemErro = "Falha ao baixar o código da versão $tag."
        $detalhesErro = "Código de erro: $LASTEXITCODE. Detalhes: $resultadoPull"
        
        # Analisar o tipo de erro para fornecer mensagens mais específicas
        if ($resultadoPull -match "couldn't find remote ref $tag") {
            $mensagemErro = "A tag '$tag' não foi encontrada no repositório remoto."
            $detalhesErro += ". Verifique se a tag existe no GitHub."
        } elseif ($resultadoPull -match "Authentication failed") {
            $mensagemErro = "Falha de autenticação ao acessar o repositório."
            $detalhesErro += ". Verifique suas credenciais LDAP e configurações de proxy."
        } elseif ($resultadoPull -match "Connection timed out") {
            $mensagemErro = "Tempo de conexão esgotado ao tentar acessar o repositório."
            $detalhesErro += ". Verifique sua conexão de rede e configurações de firewall."
        }
        
        Escrever-Mensagem "$mensagemErro $detalhesErro"
        Escrever-Log "[ERRO] $mensagemErro"
        Escrever-Log "[DETALHE] $detalhesErro"
        exit 1
    }
    else {
        $script:id_commit_pull = git rev-parse HEAD
        $mensagemSucesso = "Atualização do código bem-sucedida."
        Escrever-Mensagem $mensagemSucesso
        Escrever-Log "$mensagemSucesso (Commit: $id_commit_pull)"
    }
}

function Enviar-VersaoParaIC {
    # Atualizar-ArquivosPropriedades
    Baixar-Codigo
    Enviar-Codigo
    Enviar-SCM
}

function Nova-TagGit {
    # Renomear tags para o padrão quando ok 
    if ($id_commit_pull) {
        Escrever-Mensagem "Criando nova tag $tag ..."
        Escrever-Log "Criando tag anotada: $tag"
        $mensagemCommit = git show --pretty=format:%B -s $id_commit_pull
        git tag -a "${tag}" -m $mensagemCommit
    }
    else {
        Escrever-Mensagem "Criando nova tag $tag ..."
        Escrever-Log "Criando tag simples: $tag"
        git tag "${tag}"
    }
}

function Enviar-Codigo {
    Escrever-Mensagem "Fazendo commit do código ..."
    Escrever-Log "Iniciando commit dos arquivos"
    
    # Adicionar todos os arquivos ao stage
    git add *
    
    # Remover o arquivo de script atual do stage para não incluir no commit
    git reset HEAD $nomeScriptAtual
    Escrever-Log "Arquivo de script excluído do commit: $nomeScriptAtual"
    
    # Ignorar a pasta de relatórios
    git reset HEAD "relatorios/*"
    Escrever-Log "Pasta de relatórios excluída do commit"
    
    # Realizar o commit
    git commit -m "Atualizando arquivos para versão $tag"
    Nova-TagGit
}

function Enviar-SCM {
    Escrever-Mensagem "Enviando alterações para SCM ..."
    Escrever-Log "Iniciando push para SCM"
    # Alterar nome da branch para a correta
    $resultadoPush = git push "https://scm.prevnet/inji/mimoto" entrega-prov --tags 2>&1
    if ($LASTEXITCODE -ne 0) {
        $mensagemErro = "Falha ao enviar alterações para o SCM."
        $detalhesErro = "Código de erro: $LASTEXITCODE. Detalhes: $resultadoPush"
        
        # Analisar o tipo de erro para fornecer mensagens mais específicas
        if ($resultadoPush -match "Authentication failed") {
            $mensagemErro = "Falha de autenticação ao acessar o SCM."
            $detalhesErro += ". Verifique suas credenciais LDAP."
        } elseif ($resultadoPush -match "Connection timed out") {
            $mensagemErro = "Tempo de conexão esgotado ao tentar acessar o SCM."
            $detalhesErro += ". Verifique sua conexão de rede e configurações de firewall."
        } elseif ($resultadoPush -match "rejected") {
            $mensagemErro = "Push rejeitado pelo servidor SCM."
            $detalhesErro += ". Pode haver conflitos ou permissões insuficientes."
        }
        
        Escrever-Mensagem "$mensagemErro $detalhesErro"
        Escrever-Log "[ERRO] $mensagemErro"
        Escrever-Log "[DETALHE] $detalhesErro"
        exit 1
    }
    else {
        $mensagemSucesso = "Código atualizado com sucesso no SCM!"
        Escrever-Mensagem $mensagemSucesso
        Escrever-Log "[SUCESSO] $mensagemSucesso"
    }
}

function Atualizar-ArquivosPropriedades {
    #Tomar cuidado com inscrições no Dockerfile.
    Escrever-Log "Função de atualização de arquivos de propriedades chamada"
}

function Escrever-Mensagem {
    param([string]$mensagem)
    Write-Host "**************************************************************"
    Write-Host $mensagem
    Write-Host ""
}

function Escrever-Log {
    param([string]$mensagem)
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $entradaLog = "[$timestamp] $mensagem"
    
    # Garantir que a pasta de relatórios exista
    if (-not (Test-Path -Path $pastaRelatorios)) {
        New-Item -Path $pastaRelatorios -ItemType Directory | Out-Null
    }
    
    Add-Content -Path $caminhoRelatorio -Value $entradaLog
    Write-Verbose $entradaLog
}

function Inicializar-Relatorio {
    # Garantir que a pasta de relatórios exista
    if (-not (Test-Path -Path $pastaRelatorios)) {
        New-Item -Path $pastaRelatorios -ItemType Directory | Out-Null
    }
    
    $cabecalho = @"
=================================================================
RELATÓRIO DE ATUALIZAÇÃO DO MIMOTO
=================================================================
Versão: $tag
Data/Hora de Execução: $(Get-Date -Format 'dd/MM/yyyy HH:mm:ss')
Usuário: $env:USERNAME
Diretório: $(Get-Location)
Arquivo de Script: $nomeScriptAtual
=================================================================

"@
    Set-Content -Path $caminhoRelatorio -Value $cabecalho
    Escrever-Mensagem "Relatório iniciado: $caminhoRelatorio"
}

function Finalizar-Relatorio {
    $rodape = @"

=================================================================
EXECUÇÃO FINALIZADA EM: $(Get-Date -Format 'dd/MM/yyyy HH:mm:ss')
=================================================================
"@
    Add-Content -Path $caminhoRelatorio -Value $rodape
    Escrever-Mensagem "Relatório finalizado: $nomeRelatorio"
}

function Principal {
    try {        
        Inicializar-Relatorio
        Escrever-Log "Iniciando execução do script de atualização"
        
        Configurar-CredenciaisLdap
        Ler-EntradaTag
        Enviar-VersaoParaIC
        
        Escrever-Log "[SUCESSO] Script executado com sucesso"
        Escrever-Mensagem "Atualização concluída com sucesso!"
        Escrever-Mensagem "Relatório salvo em: $caminhoRelatorio"
    }
    catch {
        $excecao = $_.Exception
        $mensagemErro = "Erro durante a execução: $($excecao.Message)"
        $detalhesErro = "Tipo de exceção: $($excecao.GetType().Name)"
        $stackTrace = $_.ScriptStackTrace
        
        Escrever-Mensagem "$mensagemErro"
        Escrever-Log "[ERRO FATAL] $mensagemErro"
        Escrever-Log "[DETALHE] $detalhesErro"
        Escrever-Log "[STACK TRACE] $stackTrace"
        exit 1
    }
    finally {
        Finalizar-Relatorio
    }
}

# Iniciar execução do script
Principal
