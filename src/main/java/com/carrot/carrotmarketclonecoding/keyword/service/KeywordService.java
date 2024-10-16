package com.carrot.carrotmarketclonecoding.keyword.service;

import com.carrot.carrotmarketclonecoding.keyword.dto.KeywordRequestDto.KeywordCreateRequestDto;
import com.carrot.carrotmarketclonecoding.keyword.dto.KeywordRequestDto.KeywordEditRequestDto;
import com.carrot.carrotmarketclonecoding.keyword.dto.KeywordResponseDto.KeywordDetailResponseDto;
import java.util.List;

public interface KeywordService {

    void add(Long authId, KeywordCreateRequestDto keywordCreateRequestDto);

    void edit(Long authId, Long keywordId, KeywordEditRequestDto keywordEditRequestDto);

    List<KeywordDetailResponseDto> getAllKeywords(Long authId);

    void delete(Long authId, Long keywordId);
}
