// package org.kiru.user.user.service.out;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.assertj.core.api.Assertions.assertThatThrownBy;
// import static org.mockito.Mockito.when;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.kiru.core.exception.EntityNotFoundException;
// import org.kiru.core.user.user.entity.UserJpaEntity;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// @ExtendWith(MockitoExtension.class)
// class UserQueryWithCacheTest {

//     @Mock
//     private UserQueryWithCache userQueryWithCache;

//     private UserJpaEntity testUser;

//     @BeforeEach
//     void setUp() {
//         testUser = new UserJpaEntity();
//         testUser.setId(1L);
//         testUser.setUsername("testUser");
//         testUser.setEmail("test@example.com");
//     }

//     @Test
//     @DisplayName("캐시된 사용자 조회 - 성공")
//     void getUser_Success() {
//         // Given
//         when(userQueryWithCache.getUser(1L)).thenReturn(testUser);

//         // When
//         UserJpaEntity result = userQueryWithCache.getUser(1L);

//         // Then
//         assertThat(result).isNotNull();
//         assertThat(result.getId()).isEqualTo(1L);
//         assertThat(result.getUsername()).isEqualTo("testUser");
//         assertThat(result.getEmail()).isEqualTo("test@example.com");
//     }

//     @Test
//     @DisplayName("캐시된 사용자 조회 - 실패 (존재하지 않는 사용자)")
//     void getUser_NotFound() {
//         // Given
//         when(userQueryWithCache.getUser(999L)).thenThrow(new EntityNotFoundException());

//         // When & Then
//         assertThatThrownBy(() -> userQueryWithCache.getUser(999L))
//             .isInstanceOf(EntityNotFoundException.class);
//     }

//     @Test
//     @DisplayName("사용자 저장 및 캐시 업데이트 - 성공")
//     void saveUser_Success() {
//         // Given
//         UserJpaEntity newUser = new UserJpaEntity();
//         newUser.setUsername("newUser");
//         newUser.setEmail("new@example.com");
//         when(userQueryWithCache.saveUser(newUser)).thenReturn(testUser);

//         // When
//         UserJpaEntity result = userQueryWithCache.saveUser(newUser);

//         // Then
//         assertThat(result).isNotNull();
//         assertThat(result.getId()).isEqualTo(1L);
//         assertThat(result.getUsername()).isEqualTo("testUser");
//     }
// }
