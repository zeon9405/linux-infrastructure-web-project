# Linux 기반 웹 서비스 구축 및 운영 프로젝트

## 1. 프로젝트 개요

Rocky Linux 환경에서 웹 서비스를 직접 구축하고, 웹 요청부터 데이터베이스 처리까지 전체 흐름을 구현하고 검증하는 프로젝트입니다.

단순한 설치를 넘어 서비스 구성, 통신 흐름, 서비스 관리 및 향후 운영·장애 대응 역량을 확보하는 것을 목표로 합니다.

## 2. 시스템 아키텍처

```text
Windows Browser
      |
      | HTTP :8080
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
Java Application Server (:8081)
      |
      | JDBC
      v
MariaDB
```

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

## 4. 주요 구현 내용

### Web Server
- Nginx 설치 및 서비스 실행
- 정적 HTML 웹 페이지 구성
- `/api/` 요청을 Java 애플리케이션으로 전달하는 리버스 프록시 구성

### Java Application
- HTTP 요청 처리
- JDBC를 통한 MariaDB 연결
- 메시지 등록 및 조회 기능 구현
- 입력값 검증 및 HTTP 상태 코드 처리

### Database
- MariaDB 데이터베이스 및 테이블 구성
- 애플리케이션 전용 계정 및 권한 설정
- PreparedStatement를 이용한 데이터 등록
- 등록 데이터 조회

### Service Management
- systemd를 이용한 Java 애플리케이션 서비스 등록
- 서비스 자동 시작 및 재시작 정책 설정
- firewalld 및 SELinux 설정 확인

## 5. 검증 결과

실제 가상 서버 환경에서 다음 항목을 확인했습니다.

- Nginx 서비스 실행 및 설정 문법 검사
- HTTP 요청에 대한 정상 응답 확인
- Windows 브라우저에서 웹 페이지 접속
- 브라우저에서 메시지 등록 요청 전송
- Nginx를 통한 Java 애플리케이션 요청 전달
- Java 애플리케이션을 통한 MariaDB 데이터 등록
- 등록 데이터 조회 결과 확인
- Nginx 및 Java 애플리케이션의 서비스 활성화와 자동 시작 설정 확인

## 6. 저장소 구조

```text
linux-infrastructure-web-project/
├── app/
│   └── AppServer.java
├── web/
│   └── index.html
├── config-examples/
│   ├── infra-app.service.example
│   └── nginx.conf.example
├── .gitignore
└── README.md
```

## 7. 보안 및 설정 관리

- 데이터베이스 비밀번호는 소스 코드와 분리해 환경 설정 파일로 관리합니다.
- 실제 비밀번호와 비밀 설정 파일은 공개 저장소에 포함하지 않습니다.
- 공개용 설정 예제에는 실제 서버의 민감한 정보를 노출하지 않도록 구성합니다.

## 8. 향후 진행 계획

- 서비스 및 시스템 로그 분석
- CPU, 메모리, 디스크 등 운영 지표 기록
- 장애 상황 재현 및 원인 분석
- 데이터베이스 백업 및 복구 검증
- 서버 재부팅 후 서비스 자동 복구 확인
- 운영 자동화 스크립트 작성
- Docker 및 AWS 환경으로 확장
