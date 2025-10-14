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
- Pra facilitar a vida ja vai fazendo a documentação swagger conforme as task(Controller e DTOs)
- Façam suas task dentro do seu card( git checkout -b feature/Bnº) e antes de dar PR sempre dar pull na main e mergear com sua branch
- Para confirmar a PR vai no git na parte de pull request

## Funcionalidades:

- http://localhost:8081/auth/register
{
    "nome": "Nathalie Xavier",
    "email": "nat@aluno.ifsp.edu.br",
    "senha": "123456"
}
- ai vai mandar um email... soq é na minha conta, ai vamos ter uma situação (me avisa que eu mando o código )*obs: isso a gente vai mudar depois*
- http://localhost:8081/auth/login
{
    "email": "nat@aluno.ifsp.edu.br",
    "senha": "123456"
}
- http://localhost:8081/users/me
quando voce faz login gera um token, ai pra conseguir ver isso voce tem que ir em autorization no postman e mudar o auth type pra bearer token e colocar o codigo la


### Vamos trabalhar com branch pelo amor de deus
### Alguem pode fazer um diagrama de classe (seria bom) fiquei com preguiça e não fiz, mas dificulta demais pensar
### Eu não gostaria de fazer o backlog da proxima sprint entao conto com voces S2
