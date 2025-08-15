package com.orderapp.controller;

import com.orderapp.model.dto.AppResponse;
import com.orderapp.model.dto.OrderRequest;
import com.orderapp.model.dto.OrderResponse;
import com.orderapp.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Create a new order",
            description = "This method creates a new order and saves it to the DB.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Order was created. The created order ID is returned in data field.",
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
            description = "Request body containing the required fields to create a order.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = OrderRequest.class)
            )
    )
    @PostMapping()
    public ResponseEntity<AppResponse<Long>> createOrder(@RequestBody @Valid OrderRequest orderRequest) {
        AppResponse<Long> appResponse = orderService.createOrder(orderRequest);
        HttpStatus status = appResponse.success() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(appResponse);
    }

    @Operation(summary = "Get order by ID",
            description = "This method gets order from DB by its ID.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The order was found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/orderResponseSchema")
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
    public ResponseEntity<AppResponse<OrderResponse>> getOrderById(
            @Parameter(description = "Order ID", example = "951", required = true)
            @PathVariable Long id) {
        AppResponse<OrderResponse> appResponse = orderService.getOrderById(id);
        HttpStatus status = appResponse.success() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(appResponse);
    }

    @Operation(summary = "Get all orders",
            description = "This method gets all exists orders from DB")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "List of orders",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/orderListResponseSchema")
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
    @GetMapping()
    public ResponseEntity<AppResponse<List<OrderResponse>>> getAllOrders() {
        AppResponse<List<OrderResponse>> appResponse = orderService.getAllOrders();
        HttpStatus status = appResponse.success() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(appResponse);
    }
}
