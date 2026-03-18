package com.lumu99.forum.interaction.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lumu99.forum.common.enums.VoteTargetType;
import com.lumu99.forum.common.enums.VoteType;
import com.lumu99.forum.common.security.SecurityContextHelper;
import com.lumu99.forum.domain.Vote;
import com.lumu99.forum.dto.response.VoteResponse;
import com.lumu99.forum.mapper.VoteMapper;
import org.springframework.stereotype.Service;

@Service
public class VoteService {

    private final VoteMapper voteMapper;

    public VoteService(VoteMapper voteMapper) {
        this.voteMapper = voteMapper;
    }

    public VoteResponse vote(VoteTargetType targetType, Long targetId, VoteType voteType) {
        String userUuid = SecurityContextHelper.currentUserUuid();
        Vote existing = voteMapper.selectOne(
                new LambdaQueryWrapper<Vote>()
                        .eq(Vote::getTargetType, targetType)
                        .eq(Vote::getTargetId, targetId)
                        .eq(Vote::getUserUuid, userUuid)
        );

        if (existing == null) {
            Vote vote = new Vote();
            vote.setTargetType(targetType);
            vote.setTargetId(targetId);
            vote.setUserUuid(userUuid);
            vote.setVoteType(voteType);
            voteMapper.insert(vote);
            return new VoteResponse(voteType.name());
        }

        if (existing.getVoteType() == voteType) {
            voteMapper.deleteById(existing.getId());
            return new VoteResponse("NONE");
        }

        existing.setVoteType(voteType);
        voteMapper.updateById(existing);
        return new VoteResponse(voteType.name());
    }
}
