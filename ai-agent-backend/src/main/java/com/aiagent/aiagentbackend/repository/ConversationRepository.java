package com.aiagent.aiagentbackend.repository;

import com.aiagent.aiagentbackend.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    // Récupère tout l'historique trié par date croissante
    List<Conversation> findAllByOrderByTimestampAsc();
}