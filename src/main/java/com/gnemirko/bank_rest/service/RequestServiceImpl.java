package com.gnemirko.bank_rest.service;

import com.gnemirko.bank_rest.dto.CreateBlockRequest;
import com.gnemirko.bank_rest.entity.Card;
import com.gnemirko.bank_rest.entity.CardStatus;
import com.gnemirko.bank_rest.entity.Request;
import com.gnemirko.bank_rest.entity.RequestState;
import com.gnemirko.bank_rest.entity.User;
import com.gnemirko.bank_rest.exception.ResourceNotFoundException;
import com.gnemirko.bank_rest.repository.CardRepository;
import com.gnemirko.bank_rest.repository.RequestRepository;
import com.gnemirko.bank_rest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final CardRepository cardRepository;

    @Override
    @Transactional
    public Request createRequest(CreateBlockRequest req) {
        User requestor = userRepository.findById(req.requestorId())
                .orElseThrow(() -> new ResourceNotFoundException("User " + req.requestorId() + " not found"));

        Card card = cardRepository.findById(req.cardId())
                .orElseThrow(() -> new ResourceNotFoundException("Card" + req.cardId() + " not found"));

        RequestState state;
        try {
            state = RequestState.valueOf(req.state().toUpperCase());
        } catch (Exception e) {
            state = RequestState.PENDING;
        }

        Request request = new Request();
        request.setRequestor(requestor);
        request.setObject(card);
        request.setState(state);
        request.setOperation(req.operation());

        return requestRepository.save(request);
    }

    @Override
    @Transactional
    public Request completeRequest(Request input) {
        Request request = requestRepository.findById(input.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Request " + input.getId().toString() + " not found"));

        applyOperationIfNeeded(request);

        request.setState(RequestState.COMPLETE);

        return requestRepository.save(request);
    }

    /** Apply supported operations. For now only card blocking is supported. */
    private void applyOperationIfNeeded(Request r) {
        if (r.getObject() == null || r.getOperation() == null) return;

        String op = r.getOperation().trim().toUpperCase();
        if (op.contains("BLOCK")) {
            Card card = cardRepository.findById(r.getObject().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Card " + r.getObject().getId() + " not found"));
            if (card.getStatus() != CardStatus.BLOCKED) {
                card.setStatus(CardStatus.BLOCKED);
                cardRepository.save(card);
            }
        }
        // TODO: add UNBLOCK, REISSUE, LIMIT_CHANGE, etc.
    }
}