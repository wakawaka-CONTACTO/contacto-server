 package org.kiru.user.user.service.out;


 import static org.assertj.core.api.Assertions.assertThat;
 import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
 import static org.mockito.ArgumentMatchers.any;
 import static org.mockito.ArgumentMatchers.anyLong;
 import static org.mockito.Mockito.when;

 import java.util.Optional;
 import org.junit.jupiter.api.BeforeEach;
 import org.junit.jupiter.api.DisplayName;
 import org.junit.jupiter.api.Test;
 import org.junit.jupiter.api.extension.ExtendWith;
 import org.kiru.core.exception.EntityNotFoundException;
 import org.kiru.core.user.user.domain.User;
 import org.kiru.core.user.user.entity.UserJpaEntity;
 import org.kiru.user.user.adapter.UserRepositoryAdapter;
 import org.kiru.user.user.repository.UserRepository;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.junit.jupiter.MockitoExtension;

 @ExtendWith(MockitoExtension.class)
 class UserQueryWithCacheTest {

     @InjectMocks
     private UserRepositoryAdapter userQueryWithCache;

     @Mock
     private UserRepository userRepository;

     private User existUser;
     private UserJpaEntity existUserEntity;

     @BeforeEach
     void setUp() {
         existUserEntity = UserJpaEntity.builder()
                 .id(1L)
                 .instagramId("exampleInstagram")
                 .username("username")
                 .email("email@example.com")
                 .description("description")
                 .webUrl("weburl")
                 .password("1234")
                 .build();
         existUser = UserJpaEntity.toModel(existUserEntity);
     }

     @Test
     @DisplayName("저장된 사용자 조회 - 성공")
     void getUser_Success() {
         // Given
         when(userRepository.findById(1L))
                 .thenReturn(Optional.of(UserJpaEntity.of(existUser)));
         // When
         User result = userQueryWithCache.getUser(1L);

         // Then
         assertThat(result).isNotNull();
         assertThat(result.getId()).isEqualTo(1L);
         assertThat(result.getUsername()).isEqualTo("username");
         assertThat(result.getEmail()).isEqualTo("email@example.com");
         assertThat(result.getUserPortfolio()).isNull();
         assertThat(result.getUserTalents()).isNull();
         assertThat(result.getUserPurposes()).isNull();

     }

     @Test
     @DisplayName("사용자 저장 성공")
     void saveUser_Success() {
         // Given
         when(userRepository.existsById(1L))
                 .thenReturn(true);
         when(userRepository.save(any(UserJpaEntity.class)))
                 .thenReturn(existUserEntity);
         // When
         User result = userQueryWithCache.saveExistUser(existUser);

         //Then
         assertThat(result).isNotNull();
         assertThat(result.getId()).isEqualTo(1L);
         assertThat(result.getUsername()).isEqualTo("username");
         assertThat(result.getEmail()).isEqualTo("email@example.com");
         assertThat(result.getUserPortfolio()).isNull();
         assertThat(result.getUserTalents()).isNull();
         assertThat(result.getUserPurposes()).isNull();
     }

     @Test
     @DisplayName("사용자 저장 - 실패 (존재하지 않는 사용자)")
     void getUser_NotFound() {
         // Given
         when(userRepository.existsById(anyLong())).thenReturn(false);
         // When & Then
         assertThatThrownBy(() -> userQueryWithCache.saveExistUser(existUser))
             .isInstanceOf(EntityNotFoundException.class);
     }
 }
