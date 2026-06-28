# LogiSquare Docker 사용법

## 현재 구성

- 로컬: Spring Boot는 IntelliJ에서 실행하고, MySQL과 Redis는 Docker에서 실행합니다.
- 서버: Nginx, Spring Boot, MySQL, Redis를 모두 Docker에서 실행합니다.
- MySQL 데이터는 Docker volume에 저장합니다.
- 나중에 RDS로 이전할 때는 MySQL 컨테이너를 제거하고 `DB_HOST`를 RDS 엔드포인트로 바꾸면 됩니다.

## 로컬 개발

처음 한 번 예제 환경 파일을 복사합니다. `.env`는 Git에 커밋하지 않습니다.

```powershell
Copy-Item .env.example .env
notepad .env
```

MySQL과 Redis를 실행합니다.

```powershell
docker compose -f docker-compose.local.yml up -d
docker compose -f docker-compose.local.yml ps
```

IntelliJ Spring Boot 실행 구성은 다음처럼 설정합니다.

```text
Active profiles: local
Working directory: C:\LogiSquare-server
```

Spring Boot는 프로젝트 루트의 `.env`를 자동으로 읽습니다.

종료하거나 로그를 확인할 때는 다음 명령어를 사용합니다.

```powershell
docker compose -f docker-compose.local.yml logs -f
docker compose -f docker-compose.local.yml down
```

데이터까지 완전히 초기화할 때만 `down -v`를 사용합니다.

```powershell
docker compose -f docker-compose.local.yml down -v
```

## 서버 배포

서버에서 프로젝트를 받은 뒤 배포 환경 파일을 만듭니다.

```bash
cp .env.prod.example .env.prod
nano .env.prod
```

`DB_PASSWORD`는 애플리케이션용 MySQL 계정 비밀번호이고, `DB_ROOT_PASSWORD`는 MySQL 관리자 비밀번호입니다. 서로 다른 강한 비밀번호를 사용하고 `.env.prod`는 커밋하지 않습니다.

전체 서비스를 빌드하고 실행합니다.

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yml up -d --build
docker compose --env-file .env.prod -f docker-compose.prod.yml ps
```

로그 확인:

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yml logs -f app
docker compose --env-file .env.prod -f docker-compose.prod.yml logs -f mysql
```

외부에는 Nginx의 `80` 포트만 공개합니다. Spring Boot, MySQL, Redis는 Docker 내부 네트워크에서만 통신합니다.

배포 종료:

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yml down
```

위 명령은 DB 데이터를 보존합니다. 아래 명령은 서버의 MySQL과 Redis 데이터를 모두 삭제하므로 신중하게 사용합니다.

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yml down -v
```

## 나중에 RDS로 이전

1. Docker MySQL 데이터를 RDS로 덤프/복원합니다.
2. Compose에서 `mysql` 서비스를 제거합니다.
3. 앱의 `DB_HOST`를 RDS 엔드포인트로 변경합니다.
4. `DB_SSL_MODE`를 RDS 정책에 맞게 `REQUIRED` 또는 `VERIFY_IDENTITY`로 설정합니다.
5. RDS 연결 확인 후 기존 MySQL volume을 정리합니다.
