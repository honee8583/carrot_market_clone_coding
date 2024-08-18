package com.carrot.carrotmarketclonecoding.board.service.impl;

import com.carrot.carrotmarketclonecoding.board.domain.Board;
import com.carrot.carrotmarketclonecoding.board.domain.BoardPicture;
import com.carrot.carrotmarketclonecoding.board.domain.Category;
import com.carrot.carrotmarketclonecoding.board.dto.BoardRequestDto.BoardRegisterRequestDto;
import com.carrot.carrotmarketclonecoding.board.dto.BoardRequestDto.BoardUpdateRequestDto;
import com.carrot.carrotmarketclonecoding.board.dto.BoardResponseDto.BoardDetailResponseDto;
import com.carrot.carrotmarketclonecoding.board.repository.BoardLikeRepository;
import com.carrot.carrotmarketclonecoding.board.repository.BoardPictureRepository;
import com.carrot.carrotmarketclonecoding.board.repository.BoardRepository;
import com.carrot.carrotmarketclonecoding.board.repository.CategoryRepository;
import com.carrot.carrotmarketclonecoding.board.service.BoardService;
import com.carrot.carrotmarketclonecoding.board.service.VisitService;
import com.carrot.carrotmarketclonecoding.common.exception.BoardNotFoundException;
import com.carrot.carrotmarketclonecoding.common.exception.CategoryNotFoundException;
import com.carrot.carrotmarketclonecoding.common.exception.MemberNotFoundException;
import com.carrot.carrotmarketclonecoding.common.exception.UnauthorizedAccessException;
import com.carrot.carrotmarketclonecoding.member.domain.Member;
import com.carrot.carrotmarketclonecoding.member.repository.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;
    private final BoardPictureRepository boardPictureRepository;
    private final BoardLikeRepository boardLikeRepository;
    private final VisitService visitService;
    private final BoardPictureService boardPictureService;

    @Override
    public Long register(BoardRegisterRequestDto registerRequestDto, Long memberId, boolean tmp) {
        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        Category category = findCategoryIfCategoryIdNotNull(registerRequestDto.getCategoryId());
        Board board = Board.createBoard(registerRequestDto, member, category, tmp);
        board.setPriceZeroIfMethodIsShare();
        boardRepository.save(board);
        boardPictureService.uploadPicturesIfExistAndUnderLimit(registerRequestDto.getPictures(), board);

        return board.getId();
    }

    @Override
    public BoardDetailResponseDto detail(Long boardId, String sessionId) {
        Board board = boardRepository.findById(boardId).orElseThrow(BoardNotFoundException::new);
        List<BoardPicture> pictures = boardPictureRepository.findByBoard(board);
        int like = boardLikeRepository.countByBoard(board);

        if (visitService.increaseVisit(board.getId().toString(), sessionId)) {
            board.increaseVisit();
        }

        // TODO count chats

        return BoardDetailResponseDto.createBoardDetail(board, pictures, like);
    }

    @Override
    public void update(BoardUpdateRequestDto updateRequestDto, Long boardId, Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        Board board = boardRepository.findById(boardId).orElseThrow(BoardNotFoundException::new);
        isWriterOfBoard(board, member);
        if (board.getTmp()) {
            boardRepository.deleteAllByMemberAndTmpIsTrueAndIdIsNot(member, boardId);
        }

        Category category = categoryRepository.findById(updateRequestDto.getCategoryId()).orElseThrow(CategoryNotFoundException::new);
        board.update(updateRequestDto, category);

        boardPictureService.deletePicturesIfExist(updateRequestDto.getRemovePictures());
        boardPictureService.uploadPicturesIfExistAndUnderLimit(updateRequestDto.getNewPictures(), board);
    }

    private Category findCategoryIfCategoryIdNotNull(Long categoryId) {
        if (categoryId != null && categoryId > 0) {
            return categoryRepository.findById(categoryId).orElseThrow(CategoryNotFoundException::new);
        }
        return null;

    }

    private void isWriterOfBoard(Board board, Member member) {
        if (board.getMember() != member) {
            throw new UnauthorizedAccessException();
        }
    }
}
