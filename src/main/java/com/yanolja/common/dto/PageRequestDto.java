package com.yanolja.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "페이지 요청 정보 DTO")
public class PageRequestDto {
    
    @Schema(description = "페이지 번호 (0부터 시작)")
    private Integer page;
    
    @Schema(description = "페이지 크기")
    private Integer size;
    
    @Schema(description = "정렬 컬럼명")
    private String sortColumn;
    
    @Schema(description = "정렬 방향 (asc, desc)")
    private String sortDirection;
    
    /**
     * Spring Data의 PageRequest로 변환
     */
    public PageRequest toPageable() {
        Sort sort = null;
        
        if (sortColumn != null && sortDirection != null) {
            Sort.Direction direction = 
                "desc".equalsIgnoreCase(sortDirection) 
                    ? Sort.Direction.DESC 
                    : Sort.Direction.ASC;
            
            sort = Sort.by(direction, sortColumn);
        }
        
        return sort != null 
            ? PageRequest.of(page != null ? page : 0, size != null ? size : 10, sort)
            : PageRequest.of(page != null ? page : 0, size != null ? size : 10);
    }
    
    /**
     * 특정 엔티티의 DB 컬럼명으로 변환하여 Pageable 생성
     * @param columnMappingFunction 컬럼명 변환 함수
     * @return Spring Data PageRequest
     */
    public PageRequest toPageable(java.util.function.Function<String, String> columnMappingFunction) {
        Sort sort = null;
        
        if (sortColumn != null && sortDirection != null) {
            Sort.Direction direction = 
                "desc".equalsIgnoreCase(sortDirection) 
                    ? Sort.Direction.DESC 
                    : Sort.Direction.ASC;
            
            String dbColumn = columnMappingFunction.apply(sortColumn);
            sort = Sort.by(direction, dbColumn);
        }
        
        return sort != null 
            ? PageRequest.of(page != null ? page : 0, size != null ? size : 10, sort)
            : PageRequest.of(page != null ? page : 0, size != null ? size : 10);
    }
} 