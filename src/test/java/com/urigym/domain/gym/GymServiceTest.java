package com.urigym.domain.gym;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Guards against /api/owner/gyms regressing to an unpaged, full-table response
 * (see OwnerController#getMyGyms).
 */
class GymServiceTest {

    private final GymRepository gymRepository = mock(GymRepository.class);
    private final GymService gymService = new GymService(gymRepository);

    @Test
    void getGymsByOwner_delegatesToPagedRepositoryQuery() {
        UUID ownerId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);
        Gym gym = Gym.builder().id(UUID.randomUUID()).name("우리짐").build();
        Page<Gym> repoPage = new PageImpl<>(List.of(gym), pageable, 1);
        when(gymRepository.findByOwnerIdOrderByCreatedAtDescIdAsc(ownerId, pageable)).thenReturn(repoPage);

        Page<Gym> result = gymService.getGymsByOwner(ownerId, pageable);

        assertThat(result.getContent()).containsExactly(gym);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(gymRepository).findByOwnerIdOrderByCreatedAtDescIdAsc(ownerId, pageable);
    }
}
