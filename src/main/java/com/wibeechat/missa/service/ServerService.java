//package com.wibeechat.missa.service;
//
//@Service
//@RequiredArgsConstructor
//public class ServerService {
//    private final ServerRepository serverRepository;
//    private final WebSocketHandler webSocketHandler;
//
//    public List<Server> getAllServers() {
//        return serverRepository.findAll();
//    }
//
//    public Server getServerById(Long id) {
//        return serverRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Server not found"));
//    }
//
//    @Transactional
//    public void restartServer(Long id) {
//        Server server = getServerById(id);
//        // 서버 재시작 로직 구현
//        server.setStatus(Server.ServerStatus.WARNING);
//        serverRepository.save(server);
//
//        // WebSocket을 통해 클라이언트에게 상태 변경 알림
//        webSocketHandler.sendServerUpdate(server);
//    }
//}