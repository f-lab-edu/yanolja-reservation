package com.yanolja.areas.room.service;

import com.yanolja.areas.room.dto.OptionUsageStatisticsDto;
import com.yanolja.areas.room.dto.RoomDto;
import com.yanolja.areas.room.dto.RoomImageDto;
import com.yanolja.areas.room.dto.RoomOptionCountStatisticsDto;
import com.yanolja.areas.room.dto.RoomOptionDto;
import com.yanolja.areas.room.entity.Room;
import com.yanolja.areas.room.entity.RoomOption;
import com.yanolja.areas.room.entity.RoomOptionMapping;
import com.yanolja.areas.room.repository.RoomRepository;
import com.yanolja.areas.room.repository.RoomOptionMappingRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceWithOptionsTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomImageService roomImageService;

    @Mock
    private RoomOptionMappingRepository roomOptionMappingRepository;

    @Mock
    private RoomOptionRepository roomOptionRepository;

    @InjectMocks
    private RoomService roomService;

    private Room room;
    private RoomOption option1;
    private RoomOption option2;
    private RoomOptionMapping mapping1;
    private RoomOptionMapping mapping2;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 설정
        room = Room.createRoom(1L, "디럭스 더블룸", "편안한 객실", 2, new BigDecimal("120000"));
        ReflectionTestUtils.setField(room, "id", 1L);

        option1 = RoomOption.createRoomOption("조식 서비스", new BigDecimal("25000"));
        ReflectionTestUtils.setField(option1, "id", 1L);

        option2 = RoomOption.createRoomOption("늦은 체크아웃", new BigDecimal("30000"));
        ReflectionTestUtils.setField(option2, "id", 2L);

        mapping1 = RoomOptionMapping.createMapping(room, option1);
        ReflectionTestUtils.setField(mapping1, "id", 1L);

        mapping2 = RoomOptionMapping.createMapping(room, option2);
        ReflectionTestUtils.setField(mapping2, "id", 2L);

        // 기본 모킹 설정
        lenient().when(roomRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(room));
        lenient().when(roomRepository.findByIdAndNotDeleted(999L)).thenReturn(Optional.empty());
        lenient().when(roomImageService.getMainImageUrl(any())).thenReturn("/api/rooms/images/main.jpg");
    }

    @Test
    @DisplayName("객실에 옵션 추가 - 성공")
    void addRoomOptions_Success() {
        // Given
        List<Long> optionIds = Arrays.asList(1L, 2L);
        when(roomOptionMappingRepository.existsByRoomIdAndRoomOptionId(1L, 1L)).thenReturn(false);
        when(roomOptionMappingRepository.existsByRoomIdAndRoomOptionId(1L, 2L)).thenReturn(false);
        when(roomOptionRepository.findById(1L)).thenReturn(Optional.of(option1));
        when(roomOptionRepository.findById(2L)).thenReturn(Optional.of(option2));

        // When
        roomService.addRoomOptions(1L, optionIds);

        // Then
        verify(roomOptionRepository, times(2)).findById(any());
        verify(roomOptionMappingRepository, times(2)).existsByRoomIdAndRoomOptionId(eq(1L), any());
    }

    @Test
    @DisplayName("객실에 옵션 추가 - 중복 옵션은 무시")
    void addRoomOptions_SkipDuplicateOptions() {
        // Given
        List<Long> optionIds = Arrays.asList(1L, 2L);
        when(roomOptionMappingRepository.existsByRoomIdAndRoomOptionId(1L, 1L)).thenReturn(true); // 이미 존재
        when(roomOptionMappingRepository.existsByRoomIdAndRoomOptionId(1L, 2L)).thenReturn(false);
        when(roomOptionRepository.findById(2L)).thenReturn(Optional.of(option2));

        // When
        roomService.addRoomOptions(1L, optionIds);

        // Then
        verify(roomOptionRepository, times(1)).findById(2L); // option2만 추가
        verify(roomOptionRepository, never()).findById(1L); // option1은 이미 존재하므로 호출 안됨
    }

    @Test
    @DisplayName("객실에 옵션 추가 - 존재하지 않는 객실")
    void addRoomOptions_RoomNotFound() {
        // Given
        List<Long> optionIds = Arrays.asList(1L);

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            roomService.addRoomOptions(999L, optionIds);
        });

        verify(roomRepository).findByIdAndNotDeleted(999L);
        verify(roomOptionRepository, never()).findById(any());
    }

    @Test
    @DisplayName("객실에서 특정 옵션 제거 - 성공")
    void removeRoomOption_Success() {
        // When
        roomService.removeRoomOption(1L, 1L);

        // Then
        verify(roomOptionMappingRepository).deleteByRoomIdAndRoomOptionId(1L, 1L);
    }

    @Test
    @DisplayName("객실의 모든 옵션 제거 - 성공")
    void removeAllRoomOptions_Success() {
        // When
        roomService.removeAllRoomOptions(1L);

        // Then
        verify(roomOptionMappingRepository).deleteByRoomId(1L);
    }

    @Test
    @DisplayName("객실의 옵션 목록 조회 - 성공")
    void getRoomOptions_Success() {
        // Given
        List<RoomOptionMapping> mappings = Arrays.asList(mapping1, mapping2);
        when(roomOptionMappingRepository.findByRoomIdAndRoomNotDeleted(1L)).thenReturn(mappings);

        // When
        List<RoomOptionDto.ListResponse> options = roomService.getRoomOptions(1L);

        // Then
        assertThat(options).hasSize(2);
        assertThat(options.get(0).getName()).isEqualTo("조식 서비스");
        assertThat(options.get(1).getName()).isEqualTo("늦은 체크아웃");
        verify(roomOptionMappingRepository).findByRoomIdAndRoomNotDeleted(1L);
    }

    @Test
    @DisplayName("특정 옵션을 사용하는 객실 목록 조회 - 성공")
    void getRoomsByOptionId_Success() {
        // Given
        List<RoomOptionMapping> mappings = Arrays.asList(mapping1);
        when(roomOptionMappingRepository.findByRoomOptionIdAndRoomNotDeleted(1L)).thenReturn(mappings);

        // When
        List<RoomDto.ListResponse> rooms = roomService.getRoomsByOptionId(1L);

        // Then
        assertThat(rooms).hasSize(1);
        assertThat(rooms.get(0).getName()).isEqualTo("디럭스 더블룸");
        verify(roomOptionMappingRepository).findByRoomOptionIdAndRoomNotDeleted(1L);
        verify(roomImageService).getMainImageUrl(1L);
    }

    @Test
    @DisplayName("모든 옵션을 가진 객실 목록 조회 - 성공")
    void getRoomsWithAllOptions_Success() {
        // Given
        List<Long> optionIds = Arrays.asList(1L, 2L);
        List<RoomOptionMapping> mappings = Arrays.asList(mapping1, mapping2);
        when(roomOptionMappingRepository.findRoomsWithAllOptions(optionIds)).thenReturn(mappings);

        // When
        List<RoomDto.ListResponse> rooms = roomService.getRoomsWithAllOptions(optionIds);

        // Then
        assertThat(rooms).hasSize(1); // distinct로 인해 하나의 객실만 반환
        assertThat(rooms.get(0).getName()).isEqualTo("디럭스 더블룸");
        verify(roomOptionMappingRepository).findRoomsWithAllOptions(optionIds);
    }

    @Test
    @DisplayName("옵션 중 하나 이상을 가진 객실 목록 조회 - 성공")
    void getRoomsWithAnyOptions_Success() {
        // Given
        List<Long> optionIds = Arrays.asList(1L, 2L);
        List<RoomOptionMapping> mappings = Arrays.asList(mapping1, mapping2);
        when(roomOptionMappingRepository.findRoomsWithAnyOptions(optionIds)).thenReturn(mappings);

        // When
        List<RoomDto.ListResponse> rooms = roomService.getRoomsWithAnyOptions(optionIds);

        // Then
        assertThat(rooms).hasSize(1); // distinct로 인해 하나의 객실만 반환
        assertThat(rooms.get(0).getName()).isEqualTo("디럭스 더블룸");
        verify(roomOptionMappingRepository).findRoomsWithAnyOptions(optionIds);
    }

    @Test
    @DisplayName("숙소별 객실-옵션 매핑 조회 - 성공")
    void getRoomsByAccommodationIdWithOptions_Success() {
        // Given
        List<RoomOptionMapping> mappings = Arrays.asList(mapping1, mapping2);
        when(roomOptionMappingRepository.findByAccommodationId(1L)).thenReturn(mappings);

        // When
        List<RoomDto.ListResponse> rooms = roomService.getRoomsByAccommodationIdWithOptions(1L);

        // Then
        assertThat(rooms).hasSize(1); // distinct로 인해 하나의 객실만 반환
        assertThat(rooms.get(0).getName()).isEqualTo("디럭스 더블룸");
        verify(roomOptionMappingRepository).findByAccommodationId(1L);
    }

    @Test
    @DisplayName("옵션별 사용 통계 조회 - 성공")
    void getOptionUsageStatistics_Success() {
        // Given
        List<OptionUsageStatisticsDto> mockStatistics = Arrays.asList(
                OptionUsageStatisticsDto.builder()
                        .optionId(1L)
                        .optionName("조식 서비스")
                        .usageCount(5L)
                        .build(),
                OptionUsageStatisticsDto.builder()
                        .optionId(2L)
                        .optionName("늦은 체크아웃")
                        .usageCount(3L)
                        .build()
        );
        when(roomOptionMappingRepository.getOptionUsageStatistics()).thenReturn(mockStatistics);

        // When
        List<OptionUsageStatisticsDto> statistics = roomService.getOptionUsageStatistics();

        // Then
        assertThat(statistics).hasSize(2);
        assertThat(statistics.get(0).getOptionName()).isEqualTo("조식 서비스");
        assertThat(statistics.get(0).getUsageCount()).isEqualTo(5L);
        verify(roomOptionMappingRepository).getOptionUsageStatistics();
    }

    @Test
    @DisplayName("객실별 옵션 개수 통계 조회 - 성공")
    void getRoomOptionCountStatistics_Success() {
        // Given
        List<RoomOptionCountStatisticsDto> mockStatistics = Arrays.asList(
                RoomOptionCountStatisticsDto.builder()
                        .roomId(1L)
                        .roomName("디럭스 더블룸")
                        .optionCount(2L)
                        .build(),
                RoomOptionCountStatisticsDto.builder()
                        .roomId(2L)
                        .roomName("스위트 룸")
                        .optionCount(1L)
                        .build()
        );
        when(roomOptionMappingRepository.getRoomOptionCountStatistics()).thenReturn(mockStatistics);

        // When
        List<RoomOptionCountStatisticsDto> statistics = roomService.getRoomOptionCountStatistics();

        // Then
        assertThat(statistics).hasSize(2);
        assertThat(statistics.get(0).getRoomName()).isEqualTo("디럭스 더블룸");
        assertThat(statistics.get(0).getOptionCount()).isEqualTo(2L);
        verify(roomOptionMappingRepository).getRoomOptionCountStatistics();
    }

    @Test
    @DisplayName("객실 생성 시 옵션 자동 추가 - 성공")
    void createRoom_WithOptions_Success() {
        // Given
        RoomDto.Request request = new RoomDto.Request();
        request.setAccommodationId(1L);
        request.setName("새 객실");
        request.setDescription("새로운 객실입니다.");
        request.setCapacity(2);
        request.setPricePerNight(new BigDecimal("100000"));
        request.setOptionIds(Arrays.asList(1L, 2L));

        when(roomRepository.save(any(Room.class))).thenReturn(room);
        when(roomOptionMappingRepository.existsByRoomIdAndRoomOptionId(eq(1L), any())).thenReturn(false);
        when(roomOptionRepository.findById(1L)).thenReturn(Optional.of(option1));
        when(roomOptionRepository.findById(2L)).thenReturn(Optional.of(option2));

        // 객실 상세 조회를 위한 모킹
        RoomImageDto.ListResponse imageList = RoomImageDto.ListResponse.builder().images(Arrays.asList()).build();
        when(roomImageService.getImagesByRoomId(1L)).thenReturn(imageList);
        when(roomOptionMappingRepository.findByRoomIdAndRoomNotDeleted(1L)).thenReturn(Arrays.asList(mapping1, mapping2));

        // When
        RoomDto.Response response = roomService.createRoom(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getOptions()).hasSize(2);
        verify(roomRepository).save(any(Room.class));
        verify(roomOptionRepository, times(2)).findById(any());
    }

    @Test
    @DisplayName("객실 수정 시 옵션 업데이트 - 성공")
    void updateRoom_WithOptionsUpdate_Success() {
        // Given
        RoomDto.Request request = new RoomDto.Request();
        request.setAccommodationId(1L);
        request.setName("수정된 객실");
        request.setDescription("수정된 설명");
        request.setCapacity(2);
        request.setPricePerNight(new BigDecimal("150000"));
        request.setOptionIds(Arrays.asList(2L)); // 기존 option1, option2에서 option2만 남김

        when(roomOptionMappingRepository.existsByRoomIdAndRoomOptionId(1L, 2L)).thenReturn(false);
        when(roomOptionRepository.findById(2L)).thenReturn(Optional.of(option2));

        // 객실 상세 조회를 위한 모킹
        RoomImageDto.ListResponse imageList = RoomImageDto.ListResponse.builder().images(Arrays.asList()).build();
        when(roomImageService.getImagesByRoomId(1L)).thenReturn(imageList);
        when(roomOptionMappingRepository.findByRoomIdAndRoomNotDeleted(1L)).thenReturn(Arrays.asList(mapping2));

        // When
        RoomDto.Response response = roomService.updateRoom(1L, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getOptions()).hasSize(1);
        verify(roomOptionMappingRepository).deleteByRoomId(1L); // 기존 옵션 모두 제거
        verify(roomOptionRepository).findById(2L); // 새 옵션 추가
    }
} 