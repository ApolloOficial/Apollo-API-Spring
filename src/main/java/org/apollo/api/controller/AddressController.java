package org.apollo.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apollo.api.dto.AddressDTO;
import org.apollo.api.service.AddressService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
@Tag(name = "Address", description = "Address management operations")
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    @Operation(summary = "List addresses")
    @ApiResponse(responseCode = "200", description = "Addresses returned successfully")
    public List<AddressDTO> findAll() {
        return addressService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find address by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Address returned successfully"),
            @ApiResponse(responseCode = "404", description = "Address not found")
    })
    public AddressDTO findById(@PathVariable Long id) {
        return addressService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create address")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Address created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid address data")
    })
    public AddressDTO create(@Valid @RequestBody AddressDTO dto) {
        return addressService.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update address")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Address updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid address data"),
            @ApiResponse(responseCode = "404", description = "Address not found")
    })
    public AddressDTO update(@PathVariable Long id, @Valid @RequestBody AddressDTO dto) {
        return addressService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete address")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Address deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Address not found")
    })
    public void delete(@PathVariable Long id) {
        addressService.delete(id);
    }
}
