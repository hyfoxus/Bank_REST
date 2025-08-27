package com.gnemirko.bank_rest.service;

import com.gnemirko.bank_rest.dto.CreateBlockRequest;
import com.gnemirko.bank_rest.entity.Request;

public interface RequestService {
    Request createRequest(CreateBlockRequest request);
    Request completeRequest(Request request);
}
