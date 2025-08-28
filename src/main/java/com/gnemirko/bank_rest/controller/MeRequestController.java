package com.gnemirko.bank_rest.controller;

import com.gnemirko.bank_rest.dto.CreateBlockRequest;
import com.gnemirko.bank_rest.dto.RequestResponse;
import com.gnemirko.bank_rest.entity.Request;
import com.gnemirko.bank_rest.security.Auth;
import com.gnemirko.bank_rest.service.RequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User / Requests", description = "Заявки пользователя (блокировка карты)")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/me/requests")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class MeRequestController {

    private final RequestService requestService;

    @Operation(
            summary = "Создать заявку на блокировку собственной карты",
            description = "Проверяет, что карта принадлежит текущему пользователю",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = RequestResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Валидация/формат", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                    @ApiResponse(responseCode = "403", description = "Нет доступа", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                    @ApiResponse(responseCode = "404", description = "Карта не найдена", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
            }
    )
    @PostMapping("/block")
    public RequestResponse requestBlock(@Valid @RequestBody CreateBlockRequest req) {
        Long me = Auth.currentUserId();
        CreateBlockRequest fixed = new CreateBlockRequest(me, req.state(), req.cardId(), req.operation());
        Request created = requestService.createRequest(fixed);
        return RequestResponse.from(created);
    }
}