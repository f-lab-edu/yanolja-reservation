package com.yanolja.areas.room.service;

import com.yanolja.areas.room.dto.RoomOptionDto;
import com.yanolja.areas.room.entity.RoomOption;
import com.yanolja.areas.room.repository.RoomOptionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomOptionServiceTest {

    @Mock
    private RoomOptionRepository roomOptionRepository;

    @InjectMocks
    private RoomOptionService roomOptionService;

    private RoomOptionDto.Request roomOptionRequest;
    private RoomOption roomOption;
    private RoomOption roomOption2;

    @BeforeEach
    void setUp() {
        // 테스트용 객실 옵션 요청 DTO 생성
        roomOptionRequest = new RoomOptionDto.Request();
        roomOptionRequest.setName("조식 서비스");
        roomOptionRequest.setPrice(new BigDecimal("25000"));

        // 테스트용 엔티티 생성
        roomOption = RoomOption.createRoomOption("조식 서비스", new BigDecimal("25000"));
        ReflectionTestUtils.setField(roomOption, "id", 1L);
        ReflectionTestUtils.setField(roomOption, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(roomOption, "updatedAt", LocalDateTime.now());

        roomOption2 = RoomOption.createRoomOption("늦은 체크아웃", new BigDecimal("30000"));
        ReflectionTestUtils.setField(roomOption2, "id", 2L);
        ReflectionTestUtils.setField(roomOption2, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(roomOption2, "updatedAt", LocalDateTime.now());
    }

    @Test
    @DisplayName("객실 옵션 등록 성공 테스트")
    void createRoomOption_Success() {
        // Given
        when(roomOptionRepository.existsByName("조식 서비스")).thenReturn(false);
        when(roomOptionRepository.save(any(RoomOption.class))).thenReturn(roomOption);

        // When
        RoomOptionDto.Response response = roomOptionService.createRoomOption(roomOptionRequest);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("조식 서비스", response.getName());
        assertEquals(new BigDecimal("25000"), response.getPrice());
        assertNotNull(response.getCreatedAt());
        assertNotNull(response.getUpdatedAt());

        verify(roomOptionRepository, times(1)).existsByName("조식 서비스");
        verify(roomOptionRepository, times(1)).save(any(RoomOption.class));
    }

    @Test
    @DisplayName("객실 옵션 등록 실패 테스트 - 중복 이름")
    void createRoomOption_Fail_DuplicateName() {
        // Given
        when(roomOptionRepository.existsByName("조식 서비스")).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            roomOptionService.createRoomOption(roomOptionRequest);
        });

        assertEquals("이미 존재하는 옵션 이름입니다: 조식 서비스", exception.getMessage());
        verify(roomOptionRepository, times(1)).existsByName("조식 서비스");
        verify(roomOptionRepository, never()).save(any(RoomOption.class));
    }

    @Test
    @DisplayName("모든 객실 옵션 목록 조회 성공 테스트")
    void getAllRoomOptions_Success() {
        // Given
        List<RoomOption> roomOptions = Arrays.asList(roomOption, roomOption2);
        when(roomOptionRepository.findAllByOrderByCreatedAtDesc()).thenReturn(roomOptions);

        // When
        List<RoomOptionDto.ListResponse> responses = roomOptionService.getAllRoomOptions();

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("조식 서비스", responses.get(0).getName());
        assertEquals(new BigDecimal("25000"), responses.get(0).getPrice());
        assertEquals("늦은 체크아웃", responses.get(1).getName());
        assertEquals(new BigDecimal("30000"), responses.get(1).getPrice());

        verify(roomOptionRepository, times(1)).findAllByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("객실 옵션 상세 조회 성공 테스트")
    void getRoomOptionById_Success() {
        // Given
        when(roomOptionRepository.findById(1L)).thenReturn(Optional.of(roomOption));

        // When
        RoomOptionDto.Response response = roomOptionService.getRoomOptionById(1L);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("조식 서비스", response.getName());
        assertEquals(new BigDecimal("25000"), response.getPrice());
        assertNotNull(response.getCreatedAt());
        assertNotNull(response.getUpdatedAt());

        verify(roomOptionRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("객실 옵션 상세 조회 실패 테스트 - 존재하지 않는 ID")
    void getRoomOptionById_Fail_NotFound() {
        // Given
        when(roomOptionRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            roomOptionService.getRoomOptionById(999L);
        });

        assertEquals("객실 옵션을 찾을 수 없습니다. ID: 999", exception.getMessage());
        verify(roomOptionRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("객실 옵션 정보 수정 성공 테스트")
    void updateRoomOption_Success() {
        // Given
        when(roomOptionRepository.findById(1L)).thenReturn(Optional.of(roomOption));
        when(roomOptionRepository.existsByNameAndIdNot("수정된 조식 서비스", 1L)).thenReturn(false);

        RoomOptionDto.Request updateRequest = new RoomOptionDto.Request();
        updateRequest.setName("수정된 조식 서비스");
        updateRequest.setPrice(new BigDecimal("30000"));

        // When
        RoomOptionDto.Response response = roomOptionService.updateRoomOption(1L, updateRequest);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("수정된 조식 서비스", response.getName());
        assertEquals(new BigDecimal("30000"), response.getPrice());

        verify(roomOptionRepository, times(1)).findById(1L);
        verify(roomOptionRepository, times(1)).existsByNameAndIdNot("수정된 조식 서비스", 1L);
    }

    @Test
    @DisplayName("객실 옵션 정보 수정 실패 테스트 - 존재하지 않는 ID")
    void updateRoomOption_Fail_NotFound() {
        // Given
        when(roomOptionRepository.findById(999L)).thenReturn(Optional.empty());

        RoomOptionDto.Request updateRequest = new RoomOptionDto.Request();
        updateRequest.setName("수정된 조식 서비스");
        updateRequest.setPrice(new BigDecimal("30000"));

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            roomOptionService.updateRoomOption(999L, updateRequest);
        });

        assertEquals("객실 옵션을 찾을 수 없습니다. ID: 999", exception.getMessage());
        verify(roomOptionRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("객실 옵션 정보 수정 실패 테스트 - 중복 이름")
    void updateRoomOption_Fail_DuplicateName() {
        // Given
        when(roomOptionRepository.findById(1L)).thenReturn(Optional.of(roomOption));
        when(roomOptionRepository.existsByNameAndIdNot("늦은 체크아웃", 1L)).thenReturn(true);

        RoomOptionDto.Request updateRequest = new RoomOptionDto.Request();
        updateRequest.setName("늦은 체크아웃");
        updateRequest.setPrice(new BigDecimal("30000"));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            roomOptionService.updateRoomOption(1L, updateRequest);
        });

        assertEquals("이미 존재하는 옵션 이름입니다: 늦은 체크아웃", exception.getMessage());
        verify(roomOptionRepository, times(1)).findById(1L);
        verify(roomOptionRepository, times(1)).existsByNameAndIdNot("늦은 체크아웃", 1L);
    }

    @Test
    @DisplayName("객실 옵션 삭제 성공 테스트")
    void deleteRoomOption_Success() {
        // Given
        when(roomOptionRepository.findById(1L)).thenReturn(Optional.of(roomOption));

        // When
        roomOptionService.deleteRoomOption(1L);

        // Then
        verify(roomOptionRepository, times(1)).findById(1L);
        verify(roomOptionRepository, times(1)).delete(roomOption);
    }

    @Test
    @DisplayName("객실 옵션 삭제 실패 테스트 - 존재하지 않는 ID")
    void deleteRoomOption_Fail_NotFound() {
        // Given
        when(roomOptionRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            roomOptionService.deleteRoomOption(999L);
        });

        assertEquals("객실 옵션을 찾을 수 없습니다. ID: 999", exception.getMessage());
        verify(roomOptionRepository, times(1)).findById(999L);
        verify(roomOptionRepository, never()).delete(any(RoomOption.class));
    }

    @Test
    @DisplayName("ID 목록으로 객실 옵션 조회 성공 테스트")
    void getRoomOptionsByIds_Success() {
        // Given
        List<Long> ids = Arrays.asList(1L, 2L);
        List<RoomOption> roomOptions = Arrays.asList(roomOption, roomOption2);
        when(roomOptionRepository.findAllById(ids)).thenReturn(roomOptions);

        // When
        List<RoomOptionDto.ListResponse> responses = roomOptionService.getRoomOptionsByIds(ids);

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("조식 서비스", responses.get(0).getName());
        assertEquals("늦은 체크아웃", responses.get(1).getName());

        verify(roomOptionRepository, times(1)).findAllById(ids);
    }

    @Test
    @DisplayName("빈 ID 목록으로 객실 옵션 조회 테스트")
    void getRoomOptionsByIds_EmptyList() {
        // Given
        List<Long> emptyIds = Arrays.asList();
        when(roomOptionRepository.findAllById(emptyIds)).thenReturn(Arrays.asList());

        // When
        List<RoomOptionDto.ListResponse> responses = roomOptionService.getRoomOptionsByIds(emptyIds);

        // Then
        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(roomOptionRepository, times(1)).findAllById(emptyIds);
    }
} 