package com.chat.ChatApplication.repository;

import com.chat.ChatApplication.entity.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    Optional<Invitation> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Invitation> findByInviteToken(String inviteToken);

}