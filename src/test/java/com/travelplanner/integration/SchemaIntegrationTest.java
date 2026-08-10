package com.travelplanner.integration;

import com.travelplanner.identity.domain.*;
import com.travelplanner.identity.repository.FriendRequestRepository;
import com.travelplanner.identity.repository.FriendshipRepository;
import com.travelplanner.identity.repository.UserPreferenceRepository;
import com.travelplanner.identity.repository.UserRepository;
import com.travelplanner.planning.domain.*;
import com.travelplanner.planning.repository.TripRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.BeforeAll;
import org.flywaydb.core.Flyway;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Integration test chạy trên PostgreSQL thật (qua Testcontainers).
 *
 * MỤC ĐÍCH: Xác nhận rằng Flyway migration V1/V2 tạo schema đúng kiểu dữ liệu
 * (VARCHAR thay vì UUID) cho các cột lưu email. Bug gốc "invalid input syntax
 * for type uuid" CHỈ xảy ra trên PostgreSQL, H2 không bắt được lỗi này.
 *
 * CHIẾN LƯỢC:
 * - Dùng Flyway (enabled: true) để chạy migration thật → tạo bảng đúng như prod.
 * - Dùng ddl-auto: validate để Hibernate kiểm tra Entity khớp với schema DB.
 * - Nếu cột vẫn là UUID mà code insert email → PostgreSQL sẽ throw exception
 *   ngay tại dòng repository.save().
 */
@SpringBootTest
@ActiveProfiles("tc")
@Testcontainers
@Transactional
class SchemaIntegrationTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("pgvector/pgvector:pg16");

    @BeforeAll
    static void beforeAll() {
        Flyway.configure()
            .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .load()
            .migrate();
    }

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private UserPreferenceRepository userPreferenceRepository;

    // ==================== TRIP ====================

    @Test
    @DisplayName("[Postgres] Tạo Trip với ownerId = email → persist thành công, không lỗi UUID syntax")
    void createTrip_withEmailOwnerId_persistsSuccessfully() {
        String ownerEmail = "owner@example.com";

        Trip trip = Trip.builder()
                .ownerId(ownerEmail)
                .title("Chuyến đi Đà Nẵng")
                .status(TripStatus.PLANNED)
                .tripDate(LocalDate.now())
                .duration(TripDuration.HALF_DAY)
                .purpose(TripPurpose.RELAXATION)
                .groupSize(2)
                .startLat(16.0544)
                .startLon(108.2022)
                .transportation(Transportation.CAR)
                .activities(new ArrayList<>())
                .createdAt(Instant.now())
                .build();

        // Nếu cột owner_id vẫn là UUID → PostgreSQL sẽ throw
        // "ERROR: invalid input syntax for type uuid: owner@example.com"
        Trip saved = tripRepository.save(trip);
        tripRepository.flush(); // Bắt buộc flush để Postgres thực thi SQL ngay

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getOwnerId()).isEqualTo(ownerEmail);

        // Đọc lại từ DB
        Trip loaded = tripRepository.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getOwnerId()).isEqualTo(ownerEmail);
        assertThat(loaded.getVisibility()).isEqualTo(TripVisibility.PRIVATE);
    }

    // ==================== FRIENDSHIP ====================

    @Test
    @DisplayName("[Postgres] Tạo Friendship với userId/friendId = email → persist thành công")
    void createFriendship_withEmailIds_persistsSuccessfully() {
        String userEmail = "alice@example.com";
        String friendEmail = "bob@example.com";

        Friendship friendship = Friendship.builder()
                .userId(userEmail)
                .friendId(friendEmail)
                .build();

        // Nếu cột user_id/friend_id vẫn là UUID → PostgreSQL crash tại đây
        assertThatCode(() -> {
            friendshipRepository.save(friendship);
            friendshipRepository.flush();
        }).doesNotThrowAnyException();

        assertThat(friendshipRepository.findByUserIdAndFriendId(userEmail, friendEmail)).isPresent();
        assertThat(friendshipRepository.findByUserIdAndFriendId(friendEmail, userEmail)).isEmpty();
    }

    // ==================== FRIEND REQUEST ====================

    @Test
    @DisplayName("[Postgres] Tạo FriendRequest với email → persist thành công")
    void createFriendRequest_withEmail_persistsSuccessfully() {
        String sender = "alice@example.com";
        String receiver = "bob@example.com";

        FriendRequest request = FriendRequest.builder()
                .senderEmail(sender)
                .receiverEmail(receiver)
                .status(FriendRequestStatus.PENDING)
                .build();

        FriendRequest saved = friendRequestRepository.save(request);
        friendRequestRepository.flush();

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getSenderEmail()).isEqualTo(sender);
        assertThat(saved.getReceiverEmail()).isEqualTo(receiver);
    }

    // ==================== USER PREFERENCE ====================

    @Test
    @DisplayName("[Postgres] Tạo UserPreference với userId = email → persist thành công")
    void createUserPreference_withEmailUserId_persistsSuccessfully() {
        String userEmail = "prefuser@example.com";

        UserPreference pref = UserPreference.builder()
                .userId(userEmail)
                .tagWeights(Map.of("beach", 1.0, "food", 0.8))
                .travelStyle("RELAXATION")
                .groupType("COUPLE")
                .build();

        // Nếu cột user_id vẫn là UUID → PostgreSQL crash tại đây
        assertThatCode(() -> {
            userPreferenceRepository.save(pref);
            userPreferenceRepository.flush();
        }).doesNotThrowAnyException();

        assertThat(userPreferenceRepository.findByUserId(userEmail)).isPresent();
    }

    // ==================== FULL E2E FLOW ====================

    @Test
    @DisplayName("[Postgres] Luồng đầy đủ: User → FriendRequest → Friendship → Trip → UserPreference")
    void fullFlow_withEmails_worksEndToEnd() {
        // 1. Tạo 2 User
        User user1 = userRepository.save(User.builder()
                .email("e2e-user1@test.com").password("pwd").role(Role.ROLE_USER).build());
        User user2 = userRepository.save(User.builder()
                .email("e2e-user2@test.com").password("pwd").role(Role.ROLE_USER).build());
        userRepository.flush();

        // 2. User1 gửi friend request cho User2
        FriendRequest req = friendRequestRepository.save(FriendRequest.builder()
                .senderEmail(user1.getEmail())
                .receiverEmail(user2.getEmail())
                .status(FriendRequestStatus.PENDING)
                .build());
        friendRequestRepository.flush();

        // 3. User2 accept → tạo Friendship 2 chiều
        req.setStatus(FriendRequestStatus.ACCEPTED);
        friendRequestRepository.save(req);

        friendshipRepository.save(Friendship.builder()
                .userId(user1.getEmail()).friendId(user2.getEmail()).build());
        friendshipRepository.save(Friendship.builder()
                .userId(user2.getEmail()).friendId(user1.getEmail()).build());
        friendshipRepository.flush();

        // 4. Verify bạn bè 2 chiều
        assertThat(friendshipRepository.findByUserIdAndFriendId(user1.getEmail(), user2.getEmail())).isPresent();
        assertThat(friendshipRepository.findByUserIdAndFriendId(user2.getEmail(), user1.getEmail())).isPresent();

        // 5. User1 tạo Trip
        Trip trip = tripRepository.save(Trip.builder()
                .ownerId(user1.getEmail())
                .title("E2E Test Trip")
                .status(TripStatus.PLANNED)
                .tripDate(LocalDate.now())
                .duration(TripDuration.HALF_DAY)
                .purpose(TripPurpose.RELAXATION)
                .groupSize(2)
                .transportation(Transportation.CAR)
                .activities(new ArrayList<>())
                .createdAt(Instant.now())
                .build());
        tripRepository.flush();

        assertThat(trip.getOwnerId()).isEqualTo(user1.getEmail());
        assertThat(trip.getVisibility()).isEqualTo(TripVisibility.PRIVATE);

        // 6. User1 lưu UserPreference (onboarding)
        UserPreference pref = userPreferenceRepository.save(UserPreference.builder()
                .userId(user1.getEmail())
                .tagWeights(Map.of("nature", 1.0))
                .travelStyle("RELAXATION")
                .build());
        userPreferenceRepository.flush();

        assertThat(pref.getUserId()).isEqualTo(user1.getEmail());
    }
}
