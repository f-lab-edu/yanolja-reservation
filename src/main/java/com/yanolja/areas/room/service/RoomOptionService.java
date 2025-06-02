package com.yanolja.areas.room.service;

import com.yanolja.areas.room.dto.RoomOptionDto;
import com.yanolja.areas.room.entity.RoomOption;
import com.yanolja.areas.room.repository.RoomOptionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomOptionService {

    private final RoomOptionRepository roomOptionRepository;

    /**
     * 객실 옵션 등록
     * @param request 객실 옵션 등록 요청
     * @return 등록된 객실 옵션 정보
     */
    @Transactional
    public RoomOptionDto.Response createRoomOption(RoomOptionDto.Request request) {
        // 중복 이름 체크
        if (roomOptionRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("이미 존재하는 옵션 이름입니다: " + request.getName());
        }

        RoomOption roomOption = RoomOption.createRoomOption(
                request.getName(),
                request.getPrice()
        );

        RoomOption savedRoomOption = roomOptionRepository.save(roomOption);
        return RoomOptionDto.Response.fromEntity(savedRoomOption);
    }

    /**
     * 모든 객실 옵션 목록 조회
     * @return 객실 옵션 목록
     */
    public List<RoomOptionDto.ListResponse> getAllRoomOptions() {
        return roomOptionRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(RoomOptionDto.ListResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 객실 옵션 상세 조회
     * @param id 객실 옵션 ID
     * @return 객실 옵션 상세 정보
     */
    public RoomOptionDto.Response getRoomOptionById(Long id) {
        RoomOption roomOption = roomOptionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("객실 옵션을 찾을 수 없습니다. ID: " + id));
        
        return RoomOptionDto.Response.fromEntity(roomOption);
    }

    /**
     * 객실 옵션 정보 수정
     * @param id 객실 옵션 ID
     * @param request 수정 요청
     * @return 수정된 객실 옵션 정보
     */
    @Transactional
    public RoomOptionDto.Response updateRoomOption(Long id, RoomOptionDto.Request request) {
        RoomOption roomOption = roomOptionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("객실 옵션을 찾을 수 없습니다. ID: " + id));

        // 중복 이름 체크 (자신 제외)
        if (roomOptionRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new IllegalArgumentException("이미 존재하는 옵션 이름입니다: " + request.getName());
        }

        roomOption.update(request.getName(), request.getPrice());
        return RoomOptionDto.Response.fromEntity(roomOption);
    }

    /**
     * 객실 옵션 삭제
     * @param id 객실 옵션 ID
     */
    @Transactional
    public void deleteRoomOption(Long id) {
        RoomOption roomOption = roomOptionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("객실 옵션을 찾을 수 없습니다. ID: " + id));
        
        roomOptionRepository.delete(roomOption);
    }

    /**
     * 특정 ID 목록으로 객실 옵션 조회 (포털용)
     * @param ids 옵션 ID 목록
     * @return 객실 옵션 목록
     */
    public List<RoomOptionDto.ListResponse> getRoomOptionsByIds(List<Long> ids) {
        return roomOptionRepository.findAllById(ids).stream()
                .map(RoomOptionDto.ListResponse::fromEntity)
                .collect(Collectors.toList());
    }
} 