# Linux 기반 웹 서비스 구축 및 운영 프로젝트

## 1. 프로젝트 개요

Rocky Linux 환경에서 웹 서비스를 직접 구축하고, 웹 요청부터 데이터베이스 처리까지 전체 흐름을 구현하고 검증한 프로젝트입니다.

단순한 설치를 넘어 서비스 구성, 통신 흐름, 서비스 관리, 데이터 백업·복구 및 운영·장애 대응 역량을 확보하는 것을 목표로 합니다.

## 2. 시스템 아키텍처

```text
Windows Browser
      |
      | HTTP :8080 (VirtualBox host port)
      v
VirtualBox NAT Port Forwarding
      |
      | Guest :80
      v
Rocky Linux 9.8
      |
      v
Nginx Web Server
      |
      | Reverse Proxy (/api/)
      v
Java Application Server (127.0.0.1:8081)
      |
      | JDBC
      v
MariaDB (:3306)
```

Windows 브라우저에서 요청을 보내면 VirtualBox NAT 포트 포워딩을 통해 Rocky Linux의 Nginx로 전달됩니다. Nginx는 `/api/` 요청을 Java 애플리케이션으로 전달하고, Java 애플리케이션은 JDBC를 통해 MariaDB에 접근합니다.

## 3. 기술 스택

- OS: Rocky Linux 9.8
- Virtualization: Oracle VirtualBox
- Web Server: Nginx
- Application: Java 17
- Database: MariaDB
- Database Connectivity: MariaDB JDBC Driver
- Service Management: systemd
- Network Security: firewalld, SELinux
- Version Control: Git, GitHub
- Scripting: Bash

## 4. 주요 구현 내용

### Web Server

- Nginx 설치 및 서비스 실행
- 정적 HTML 웹 페이지 구성
- `/api/` 요청을 Java 애플리케이션으로 전달하는 리버스 프록시 구성
- SELinux 정책으로 인해 프록시 연결이 차단되는 문제를 확인하고, 필요한 네트워크 연결 설정을 적용해 해결

### Java Application

- Java 17 기반 HTTP 요청 처리
- JDBC를 통한 MariaDB 연결
- 메시지 등록 및 조회 기능 구현
- 입력값 검증 및 HTTP 상태 코드 처리
- systemd 서비스로 애플리케이션 실행

### Database

- MariaDB 데이터베이스 및 테이블 구성
- 애플리케이션 전용 계정과 최소 필요 권한 설정
- `PreparedStatement`를 이용한 데이터 등록
- 등록 데이터 조회
- DB 비밀번호를 애플리케이션 소스 코드와 분리하고 별도 환경 설정 파일로 관리

### Service Management

- systemd를 이용한 Java 애플리케이션 서비스 등록
- 서비스 자동 시작 및 재시작 정책 설정
- `firewalld` 및 SELinux 설정 확인

### Backup and Restore

- `mariadb-dump --single-transaction`을 이용한 데이터베이스 백업
- 백업 파일을 별도의 테스트 데이터베이스에 복원
- 원본 DB와 복원 DB의 행 수 및 `id`/`message` 데이터를 비교해 복원 결과 검증
- `/usr/local/sbin/backup-infra-db.sh` Bash 스크립트 작성 및 실행
- 스크립트 문법 검사, 임시 파일 사용, 빈 백업 파일 검사 및 백업 파일 권한 확인
- 7일이 지난 자동 백업 파일을 정리하도록 보존 정책 코드 추가
- 공개용 백업 스크립트 예제 `scripts/backup-infra-db.sh.example` 작성 및 GitHub 저장소에 추가
- 공개용 예제에서 자동 삭제가 기본적으로 비활성화되도록 설정

## 5. 검증 결과

실제 가상 서버 환경에서 다음 항목을 확인했습니다.

- Nginx 서비스 실행 및 설정 문법 검사
- Windows 브라우저에서 웹 페이지 접속
- 브라우저의 메시지 등록 요청 전송
- Nginx를 통한 Java 애플리케이션 요청 전달
- Java 애플리케이션을 통한 MariaDB 데이터 등록 및 조회
- Nginx 및 Java 애플리케이션 서비스의 활성화 설정 확인
- 수동 백업 및 자동화 스크립트를 통한 백업 파일 생성
- 자동 백업 파일을 별도의 테스트 데이터베이스에 복원
- 원본 DB와 복원 DB의 데이터가 일치하는지 확인
- 실제 서버 백업 스크립트의 Bash 문법 검사 통과
- 7일 초과 백업 파일 정리 조건을 시험 실행(dry-run)했으며, 당시 삭제 대상 파일은 없었음

> **검증 범위:** 실제 서버에서 백업 파일 생성과 별도 테스트 데이터베이스로의 복원을 확인했습니다. 다만 오래된 백업 파일의 실제 삭제 동작과 정기 실행 예약(cron/systemd timer)은 아직 검증·구성하지 않았습니다. 공개용 예제 스크립트 역시 실제 서버에서 실행 검증한 운영용 스크립트와 구분됩니다.

## 6. 저장소 구조

```text
linux-infrastructure-web-project/
├── app/
│   └── AppServer.java
├── config-examples/
│   ├── infra-app.service.example
│   ├── infra-app.service.original
│   ├── nginx.conf.example
│   └── nginx.conf.original
├── scripts/
│   └── backup-infra-db.sh.example
├── web/
│   └── index.html
├── .gitignore
└── README.md
```

### 저장소 구성 원칙

- `app/`: Java 애플리케이션 소스 코드
- `web/`: 정적 웹 페이지
- `config-examples/`: systemd 및 Nginx 설정 예제와 참고용 원본
- `scripts/`: 운영 자동화 스크립트의 공개용 예제
- `README.md`: 프로젝트 아키텍처, 구현 내용, 검증 결과 및 진행 계획

이 저장소는 실제 Linux 서버의 디렉터리 구조를 그대로 복제한 것이 아니라, 구축 결과와 설정 예제 및 검증 내용을 보여주기 위한 포트폴리오 구조입니다.

실제 서버에서는 애플리케이션 소스와 실행 파일, systemd 서비스 설정, Nginx 설정, 비밀 설정 파일 및 백업 스크립트가 각각 별도의 시스템 경로에서 관리됩니다. 따라서 저장소의 모든 파일을 그대로 실행하는 것만으로 전체 서비스가 구성되는 것은 아니며, 실제 환경에 적용하려면 필요한 설정과 실행 조건을 확인해야 합니다.

## 7. 보안 및 설정 관리

- 데이터베이스 비밀번호는 소스 코드와 분리해 환경 설정 파일로 관리합니다.
- 실제 비밀번호와 비밀 설정 파일은 공개 저장소에 포함하지 않습니다.
- 공개용 설정 예제에는 실제 서버의 민감한 정보를 노출하지 않도록 구성합니다.
- 서버의 환경 설정 파일은 제한된 소유자와 권한으로 관리합니다.
- 공개용 백업 스크립트 예제는 실제 서버에서 사용한 스크립트와 구분합니다.
- 백업 경로, 인증 방식, 로그 파일 권한 및 파일 삭제 정책은 실제 환경에 적용하기 전에 검토해야 합니다.

## 8. 다음 진행 계획

- 백업 성공·실패 로그 기록 및 실패 상황 처리 보강
- 백업 스크립트의 정기 실행을 위한 systemd timer 또는 cron 구성 및 검증
- 테스트 파일을 이용해 오래된 백업 파일 삭제 동작 검증
- CPU, 메모리, swap, 디스크 등 운영 지표 기록
- Nginx 및 Java 서비스 로그 분석
- 장애 상황 재현, 원인 분석 및 복구 절차 기록
- 서버 재부팅 후 서비스 자동 복구 확인
- Docker 및 AWS 환경으로 확장
