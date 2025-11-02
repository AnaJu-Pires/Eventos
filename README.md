
## GALERA COLOQUEI application.properties E O docker-compose.yml NO GITIGNORE QUANDO VCS DEREM PULL ELES VAO SUMIR CRIEM OUTRO DE ACORDO COM O EX OU CLONEM O DE VCS ANTES(AGORA AO ALTERAR ELES NAO SOBE MAIS COMO MUDANÇA)

# Atenção

- Docker Desktop for Windows: Para gerenciar os contêineres. Importante: Durante a instalação, mantenha a opção "Use WSL 2" marcada. Link: https://docs.docker.com/desktop/setup/install/windows-install/
- clona o repo
- roda isso : docker compose up --build(comando padrao de inciar(se quiser liberar o terminal add um -d))

*obs: comando para ver erros -->docker compose logs -f app*

### Links:

- API: http://localhost:8081
- PHPMyAdmin: http://localhost:8082 (user|password)
- Swagger: http://localhost:8081/swagger-ui.html

## Documentação e versionamento
- Para facilitar a vida ja vai fazendo a documentação swagger conforme as task(Controller e DTOs)
- Façam suas task dentro do seu card( git checkout -b feature/Bnº) e antes de dar PR sempre dar pull na main e mergear com sua branch
- Para confirmar a PR vai no git na parte de pull request
