package com.orderapp.controller;

import com.orderapp.model.dto.AppResponse;
import com.orderapp.model.dto.ClientRequest;
import com.orderapp.model.dto.ClientResponse;
import com.orderapp.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

import static com.orderapp.utils.Constants.*;

@RestController
@RequestMapping("/client")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @Operation(summary = "Create a new client",
            description = "This method creates a new client and saves it to the DB.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Client was created. The created client ID is returned in data field.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/longResponseSchema")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request or validation error.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/errorResponseSchema")
                    )
            )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Request body containing the required fields to create a client.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(ref = "#/components/schemas/clientRequestSchema")
            )
    )
    @PostMapping()
    public ResponseEntity<AppResponse<Long>> createClient(@RequestBody @Valid ClientRequest clientRequest) {
        AppResponse<Long> appResponse = clientService.createClient(clientRequest);
        HttpStatus status = appResponse.success() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(appResponse);
    }

    @Operation(summary = "Get all clients",
            description = "This method gets all exists pageable clients from DB")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "List of clients",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/pageClientResponseSchema")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request or validation error.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/errorResponseSchema")
                    )
            )
    })
    @Parameters(value = {
            @Parameter(name = "page", description = "Page number", example = "0",
                    schema = @Schema(implementation = Integer.class)),
            @Parameter(name = "size", description = "Count of units per page", example = "10",
                    schema = @Schema(implementation = Integer.class)),
            @Parameter(name = "sortBy", description = "Sort by specific field in Client model - name, email, phoneNumber, createdAt", example = "createdAt",
                    schema = @Schema(implementation = String.class)),
            @Parameter(name = "sortDir", description = "Sort direction, in which order to sort - ASC or DESC", example = "DESC",
                    schema = @Schema(implementation = String.class))
    })
    @GetMapping()
    public ResponseEntity<AppResponse<Page<ClientResponse>>> getAllClients(@RequestParam(defaultValue = DEFAULT_OFFSET) int page,
                                                                           @RequestParam(defaultValue = DEFAULT_LIMIT_PER_PAGE) int size,
                                                                           @RequestParam(required = false, defaultValue = DEFAULT_SORT_FIELD) String sortBy,
                                                                           @RequestParam(required = false, defaultValue = SORT_ORDER_DESC) String sortDir) {
        AppResponse<Page<ClientResponse>> appResponse = clientService.getAllClients(page, size, sortBy, sortDir);
        HttpStatus status = appResponse.success() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(appResponse);
    }

    @Operation(summary = "Get client by ID",
            description = "This method gets client from DB by its ID.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The client was found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/clientResponseSchema")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request or validation error.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/errorResponseSchema")
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<AppResponse<ClientResponse>> getClientById(
            @Parameter(description = "Client ID", example = "159", required = true)
            @PathVariable Long id) {
        AppResponse<ClientResponse> appResponse = clientService.getClientById(id);
        HttpStatus status = appResponse.success() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(appResponse);
    }

    @Operation(summary = "Update client by ID",
            description = "This method updates client from DB by its ID.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The client was updated. Updated client ID is returned in data field.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/longResponseSchema")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request or validation error.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/errorResponseSchema")
                    )
            )
    })
    @Parameters(value = {
            @Parameter(
                    name = "id",
                    description = "Client ID",
                    example = "753",
                    required = true,
                    schema = @Schema(implementation = Long.class))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Request body containing the required fields to update a client.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ClientRequest.class)))
    @PutMapping("/{id}")
    public ResponseEntity<AppResponse<Long>> updateClientById(@RequestBody @Valid ClientRequest clientRequest,
                                                              @PathVariable Long id) {
        AppResponse<Long> appResponse = clientService.updateClientById(id, clientRequest);
        HttpStatus status = appResponse.success() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(appResponse);
    }

    @Operation(summary = "Deactivate client by ID",
            description = "This method deactivates client from DB by its ID.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The client was deactivated. Deactivated client ID is returned in data field.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/longResponseSchema")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request or validation error.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/errorResponseSchema")
                    )
            )
    })
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<AppResponse<Long>> deactivateClientById(
            @Parameter(description = "Client ID", example = "357", required = true)
            @PathVariable Long id) {
        AppResponse<Long> appResponse = clientService.deactivateClientById(id);
        HttpStatus status = appResponse.success() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(appResponse);
    }

    @Operation(summary = "Get clients by filter",
            description = "This method gets clients by filter from the DB.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Page of ClientResponse matching filter",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/pageClientResponseSchema")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request or validation error.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/errorResponseSchema")
                    )
            )
    })
    @Parameters(value = {
            @Parameter(name = "minProfit", description = "Client minimum profit", example = "50.00",
                    schema = @Schema(implementation = BigDecimal.class)),
            @Parameter(name = "maxProfit", description = "Client maximum profit", example = "200.00",
                    schema = @Schema(implementation = BigDecimal.class)),
            @Parameter(name = "name", description = "Filter by client name", example = "Bob",
                    schema = @Schema(implementation = Integer.class)),
            @Parameter(name = "email", description = "Filter by client email", example = "bob@mail.com",
                    schema = @Schema(implementation = String.class)),
            @Parameter(name = "phoneNumber", description = "Filter by client phoneNumber", example = "333-555-7777",
                    schema = @Schema(implementation = Integer.class)),
            @Parameter(name = "page", description = "Page number", example = "0",
                    schema = @Schema(implementation = Integer.class)),
            @Parameter(name = "size", description = "Count of units per page", example = "10",
                    schema = @Schema(implementation = Integer.class)),
            @Parameter(name = "sortBy", description = "Sort by specific field in Client model - name, email, phoneNumber, createdAt", example = "createdAt",
                    schema = @Schema(implementation = String.class)),
            @Parameter(name = "sortDir", description = "Sort direction, in which order to sort - ASC or DESC", example = "DESC",
                    schema = @Schema(implementation = String.class))
    })
    @GetMapping("/search")
    public ResponseEntity<Page<ClientResponse>> searchClients(@RequestParam(required = false) BigDecimal minProfit,
                                                              @RequestParam(required = false) BigDecimal maxProfit,
                                                              @RequestParam(required = false) String name,
                                                              @RequestParam(required = false) String email,
                                                              @RequestParam(required = false) String phoneNumber,
                                                              @RequestParam(defaultValue = DEFAULT_OFFSET) int page,
                                                              @RequestParam(defaultValue = DEFAULT_LIMIT_PER_PAGE) int size,
                                                              @RequestParam(defaultValue = DEFAULT_SORT_FIELD) String sortBy,
                                                              @RequestParam(defaultValue = SORT_ORDER_DESC) String sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        Page<ClientResponse> responsePage = clientService.searchClients(minProfit, maxProfit, name, email, phoneNumber, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(responsePage);
    }

    @GetMapping("/reset-profits")
    public ResponseEntity<String> resetAllClientProfits() {
        clientService.resetProfits();
        return ResponseEntity.ok("All client profits have been reset to 0.");
    }

    @PatchMapping("/{id}/test-update")
    public ResponseEntity<AppResponse<Long>> updateTestClientById(@PathVariable Long id) {
        AppResponse<Long> appResponse = clientService.updateTestClientById(id);
        HttpStatus status = appResponse.success() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(appResponse);
    }
}
