package com.teamfiv5.fiv5.repository;

import com.teamfiv5.fiv5.domain.Friendship;
import com.teamfiv5.fiv5.domain.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    // 1. (유지) 'findUsersByTokens'에서 사용
    @Query("SELECT f FROM Friendship f " +
            "WHERE (f.requester.id = :userId OR f.receiver.id = :userId)")
    List<Friendship> findAllFriendshipsByUserId(@Param("userId") Long userId);

    // 2. (유지) 'acceptFriend'에서 사용
    Optional<Friendship> findByRequesterIdAndReceiverIdAndStatus(
            Long requesterId,
            Long receiverId,
            FriendshipStatus status
    );

    // --- (N+1 최적화) 3. 'getReceivedFriendRequests'용 ---
    @Query("SELECT f FROM Friendship f JOIN FETCH f.requester " +
            "WHERE f.receiver.id = :receiverId AND f.status = :status")
    List<Friendship> findByReceiverIdAndStatusWithRequester(
            @Param("receiverId") Long receiverId,
            @Param("status") FriendshipStatus status
    );

    // --- (N+1 최적화) 4. 'getSentFriendRequests'용 ---
    @Query("SELECT f FROM Friendship f JOIN FETCH f.receiver " +
            "WHERE f.requester.id = :requesterId AND f.status = :status")
    List<Friendship> findByRequesterIdAndStatusWithReceiver(
            @Param("requesterId") Long requesterId,
            @Param("status") FriendshipStatus status
    );

    // --- (N+1 최적화) 5. 'getMyFriends'용 ---
    @Query("SELECT f FROM Friendship f " +
            "JOIN FETCH f.requester " +
            "JOIN FETCH f.receiver " +
            "WHERE (f.requester.id = :userId OR f.receiver.id = :userId) " +
            "AND f.status = :status")
    List<Friendship> findAllFriendsWithUsers(
            @Param("userId") Long userId,
            @Param("status") FriendshipStatus status
    );

    // --- (신규) 6. 'requestFriend' 중복 체크 최적화 ---
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END " +
            "FROM Friendship f " +
            "WHERE (f.requester.id = :userA AND f.receiver.id = :userB) " +
            "OR (f.requester.id = :userB AND f.receiver.id = :userA)")
    boolean existsFriendshipBetween(
            @Param("userA") Long userA,
            @Param("userB") Long userB
    );

}