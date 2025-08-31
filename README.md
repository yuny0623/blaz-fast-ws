# blaz-fast-ws 

NIO/kotlin/eventloop 기반의 동시성이 꽤 좋은 웹서버를 만들어봅시다.
간단한 구조로 시작해서 프로젝트 내에서 모든 설정 끝내는 방식으로 간단하게 작업해보면 될 것 같아요.

echo "# blaz-fast-ws" >> README.md
git init
git add README.md
git commit -m "first commit"
git branch -M main
git remote add origin https://github.com/yuny0623/blaz-fast-ws.git
git push -u origin main


작업 계획 
- main running시 Thread pool 설정 및 관리 동작부터 시작하구요
- VT로 사용합시다.
- 이후에 nio로 TCP 요청 받아들이는 부분부터 만들구요 
- 해당 요청을 eventloop으로 관리하는 방식으로 개발합시다. 
- 소켓서버가 아니라 WS니까 HTTP 관련된 응답 받을 수 있도록 작업합시다. 
